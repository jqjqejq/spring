package jp.co.pmacmobile.common.handler;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jp.co.pmacmobile.common.annotation.ResponseResult;
import jp.co.pmacmobile.common.constant.ResultCode;
import jp.co.pmacmobile.common.interceptor.ResponseResultInterceptor;
import jp.co.pmacmobile.common.result.DefaultErrorResult;
import jp.co.pmacmobile.common.result.PlatformResult;
import jp.co.pmacmobile.common.result.Result;
import jp.co.pmacmobile.common.util.RequestContextHolderUtil;

/**
 * @author 71432393
 *
 */
@ControllerAdvice
public class ResponseResultHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        ResponseResult responseResultAnn = (ResponseResult) RequestContextHolderUtil.getRequest()
                        .getAttribute(ResponseResultInterceptor.RESPONSE_RESULT);
        return responseResultAnn == null ? false : true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                    Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                    ServerHttpResponse response) {
        ResponseResult responseResultAnn = (ResponseResult) RequestContextHolderUtil.getRequest()
                        .getAttribute(ResponseResultInterceptor.RESPONSE_RESULT);

        Class<? extends Result> resultClazz = responseResultAnn.value();

        if (resultClazz.isAssignableFrom(PlatformResult.class)) {
            if (body instanceof DefaultErrorResult) {
                DefaultErrorResult defaultErrorResult = (DefaultErrorResult) body;
                if (String.valueOf(ResultCode.SYSTEM_INNER_ERROR.code()).equals(defaultErrorResult.getCode())) {
                    return PlatformResult.failure(ResultCode.SYSTEM_INNER_ERROR, defaultErrorResult);
                }
                return PlatformResult.failure(ResultCode.BUSINESS_ERROR, defaultErrorResult);
            }

            return PlatformResult.success(body);
        }

        return body;
    }

}
