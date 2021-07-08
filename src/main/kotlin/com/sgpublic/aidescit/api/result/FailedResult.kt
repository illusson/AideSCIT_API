package com.sgpublic.aidescit.api.result

import com.sgpublic.aidescit.api.core.util.AdvanceMap

/**
 * 处理失败结果封装
 * @param code 错误码
 * @param message 错误说明
 */
class FailedResult(code: Int, message: String) : AdvanceMap(
    "code" to code,
    "message" to message
) {
    companion object {
        @JvmStatic
        val INVALID_SIGN = FailedResult(-400, "服务签名错误")
        @JvmStatic
        val UNSUPPORTED_REQUEST = FailedResult(-400, "不支持的请求方式")
        @JvmStatic
        val INTERNAL_SERVER_ERROR = FailedResult(-500, "服务器内部错误")
        @JvmStatic
        val SERVER_PROCESSING_ERROR = FailedResult(-500, "请求处理出错")
    }
}