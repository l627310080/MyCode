import request from '@/utils/request'

// 查询跨境商品标准信息(SPU)列表
export function listSpu(query) {
  return request({
    url: '/system/spu/list',
    method: 'get',
    params: query
  })
}

// 查询跨境商品标准信息(SPU)详细
export function getSpu(id) {
  return request({
    url: '/system/spu/' + id,
    method: 'get'
  })
}

// 新增跨境商品标准信息(SPU)
export function addSpu(data) {
  return request({
    url: '/system/spu',
    method: 'post',
    data: data
  })
}

// 修改跨境商品标准信息(SPU)
export function updateSpu(data) {
  return request({
    url: '/system/spu',
    method: 'put',
    data: data
  })
}

// 删除跨境商品标准信息(SPU)
export function delSpu(id) {
  return request({
    url: '/system/spu/' + id,
    method: 'delete'
  })
}
