import requests
import time
import random
import threading
from concurrent.futures import ThreadPoolExecutor
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

# =================================================================
# 配置参数：模拟百万级高并发场景
# =================================================================
TARGET_URL = "http://localhost:8080/system/sku/deduct"
TOTAL_REQUESTS = 1000000  # 总请求量：100万次
CONCURRENT_THREADS = 1000  # 并发线程池大小
SKU_ID = 43               # 测试目标 SKU
DEDUCT_QTY = 1            # 每次扣减数量

# =================================================================
# HTTP 全局会话配置 (带自动重试机制)
# 作用：解决压测机端口耗尽、网络抖动导致的非业务性失败
# =================================================================
session = requests.Session()

# 定义重试策略：针对 500/502/503/504 等错误自动重试 3 次
retries = Retry(
    total=3,
    backoff_factor=0.1,
    status_forcelist=[500, 502, 503, 504]
)

# 配置连接池：连接池大小应与线程数保持一致
adapter = HTTPAdapter(
    pool_connections=CONCURRENT_THREADS,
    pool_maxsize=CONCURRENT_THREADS,
    max_retries=retries
)
session.mount('http://', adapter)

# =================================================================
# 统计监控变量
# =================================================================
success_count = 0        # 业务扣减成功计数
fail_count = 0           # 业务扣减失败计数 (如库存不足)
exception_count = 0      # 网络/物理层面异常计数
total_processed = 0      # 已完成响应的总请求数
counter_lock = threading.Lock()
start_time = 0

def send_request():
    """
    核心任务函数：发起库存扣减请求并统计结果
    """
    global success_count, fail_count, exception_count, total_processed

    # 模拟真实环境下极其微小的网络抖动
    time.sleep(random.uniform(0.005, 0.02))

    try:
        params = {"skuId": SKU_ID, "quantity": DEDUCT_QTY}
        # 设置 5 秒超时，防止死链接卡死线程池
        resp = session.post(TARGET_URL, params=params, timeout=5)

        with counter_lock:
            total_processed += 1
            # 根据接口返回内容判断业务是否成功
            if resp.status_code == 200 and "成功" in resp.text:
                success_count += 1
            else:
                fail_count += 1
    except Exception:
        # 捕获物理网络异常：如端口耗尽、超时、连接被拒
        with counter_lock:
            total_processed += 1
            exception_count += 1

def print_dashboard(label="实时状态"):
    """
    打印美化后的监控面板
    """
    with counter_lock:
        curr_total = total_processed
        curr_success = success_count
        curr_fail = fail_count
        curr_exc = exception_count

    elapsed = time.time() - start_time
    qps = curr_total / elapsed if elapsed > 0 else 0
    success_rate = (curr_success / curr_total * 100) if curr_total > 0 else 0

    print(f"\n[{label}] " + "="*40)
    print(f"| 处理进度: {curr_total}/{TOTAL_REQUESTS} ({curr_total/TOTAL_REQUESTS*100:.2f}%)")
    print(f"| 成功响应: {curr_success} | 业务失败: {curr_fail} | 网络异常: {curr_exc}")
    print(f"| 实时 QPS: {qps:.2f} req/s | 成功率: {success_rate:.2f}%")
    print("="*50)

def monitor_dashboard():
    """
    每分钟定时报告后台线程
    """
    minute = 0
    while total_processed < TOTAL_REQUESTS:
        time.sleep(60)
        minute += 1
        print_dashboard(f"监控时报 - {minute}min")

def run_stress_test():
    """
    压测主控制逻辑
    """
    global start_time
    print(f"🚀 [引擎启动] 目标: {TOTAL_REQUESTS} 任务 | 并发: {CONCURRENT_THREADS} 线程")
    start_time = time.time()

    # 1. 开启监控线程
    monitor_thread = threading.Thread(target=monitor_dashboard, daemon=True)
    monitor_thread.start()

    # 2. 提交任务到线程池
    with ThreadPoolExecutor(max_workers=CONCURRENT_THREADS) as executor:
        for i in range(1, TOTAL_REQUESTS + 1):
            executor.submit(send_request)

            # 调度进度打印
            if i % 10000 == 0:
                print(f"\r[分发进度] {i/TOTAL_REQUESTS*100:.1f}% 任务已进入就绪队列...", end="")

                # 内存保护机制：防止主线程分发过快撑爆内存
                while executor._work_queue.qsize() > 5000:
                    time.sleep(0.1)

    # 3. 阻塞等待所有异步请求返回
    print("\n\n✅ [任务分发完毕] 正在等待最后几笔请求落库...")
    executor.shutdown(wait=True)

    # 4. 打印最终汇总结果
    print("\n" + "#"*50)
    print("🚩 [压测结束] 最终对账报表")
    print_dashboard("Final Summary")
    print(f"| 总耗时: {(time.time() - start_time)/60:.2f} 分钟")
    print("#"*50 + "\n")

if __name__ == "__main__":
    run_stress_test()