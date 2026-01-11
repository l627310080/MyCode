import request from '@/utils/request'

// 查询动态合规校验规则配置列表
export function listRuleConfig(query) {
  return request({
    url: '/system/rule-config/list',
    method: 'get',
    params: query
  })
}

// 查询动态合规校验规则配置详细
export function getRuleConfig(ruleId) {
  return request({
    url: '/system/rule-config/' + ruleId,
    method: 'get'
  })
}

// 新增动态合规校验规则配置
export function addRuleConfig(data) {
  return request({
    url: '/system/rule-config',
    method: 'post',
    data: data
  })
}

// 修改动态合规校验规则配置
export function updateRuleConfig(data) {
  return request({
    url: '/system/rule-config',
    method: 'put',
    data: data
  })
}

// 删除动态合规校验规则配置
export function delRuleConfig(ruleId) {
  return request({
    url: '/system/rule-config/' + ruleId,
    method: 'delete'
  })
}
