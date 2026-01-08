import request from '@/utils/request'

// 查询跨平台商品映射列表
export function listMapping(query) {
  return request({
    url: '/system/mapping/list',
    method: 'get',
    params: query
  })
}

// 查询跨平台商品映射详细
export function getMapping(id) {
  return request({
    url: '/system/mapping/' + id,
    method: 'get'
  })
}

// 新增跨平台商品映射
export function addMapping(data) {
  return request({
    url: '/system/mapping',
    method: 'post',
    data: data
  })
}

// 修改跨平台商品映射
export function updateMapping(data) {
  return request({
    url: '/system/mapping',
    method: 'put',
    data: data
  })
}

// 删除跨平台商品映射
export function delMapping(id) {
  return request({
    url: '/system/mapping/' + id,
    method: 'delete'
  })
}
