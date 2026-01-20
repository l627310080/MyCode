import request from '@/utils/request'

// 查询商品单品规格(SKU)列表
export function listSku(query) {
  return request({
    url: '/system/sku/list',
    method: 'get',
    params: query
  })
}

// 查询商品单品规格(SKU)详细
export function getSku(id) {
  return request({
    url: '/system/sku/' + id,
    method: 'get'
  })
}

// 新增商品单品规格(SKU)
export function addSku(data) {
  return request({
    url: '/system/sku',
    method: 'post',
    data: data
  })
}

// 修改商品单品规格(SKU)
export function updateSku(data) {
  return request({
    url: '/system/sku',
    method: 'put',
    data: data
  })
}

// 删除商品单品规格(SKU)
export function delSku(id) {
  return request({
    url: '/system/sku/' + id,
    method: 'delete'
  })
}

// 模拟库存扣减 (展示 Kafka 削峰)
export function deductSku(skuId, quantity) {
  return request({
    url: '/system/sku/deduct',
    method: 'post',
    params: {
      skuId: skuId,
      quantity: quantity
    }
  })
}
