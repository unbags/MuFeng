export const PAGE_SIZE = 9
export const DELIVERY_FEE = 4
export const TOAST_DURATION_MS = 2400
export const NOTE_MAX_LENGTH = 60

export const HTTP_ERROR_MESSAGES = {
  400: '请求参数有误，请检查输入',
  401: '未授权，请先登录',
  403: '没有操作权限',
  404: '请求的资源不存在',
  409: '操作冲突，请刷新后重试',
  422: '数据校验失败，请检查输入',
  500: '服务器繁忙，请稍后重试',
  502: '网关异常，请稍后重试',
  503: '服务暂不可用，请稍后重试',
}
