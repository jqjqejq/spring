package jp.co.pmacmobile.common.handler;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jp.co.pmacmobile.common.constant.MessageConsts;
import jp.co.pmacmobile.common.exception.MobileException;
import jp.co.pmacmobile.common.result.DefaultErrorResult;

/**
 * @author 71432393
 *
 */
public class BaseGlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseGlobalExceptionHandler.class);

    protected ResponseEntity<DefaultErrorResult> handleBusinessException(MobileException e,
                    HttpServletRequest request) {
        LOGGER.info("handleBusinessException start, uri:{}, exception:{}, caused by: {}", request.getRequestURI(), e.getClass(), e.getMessage());
        DefaultErrorResult defaultErrorResult = DefaultErrorResult.failure(e);
        return ResponseEntity
                        .status(HttpStatus.valueOf(defaultErrorResult.getStatus()))
                        .body(defaultErrorResult);
    }

    protected DefaultErrorResult handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        LOGGER.error("handleRuntimeException start, uri:{}, caused by: ", request.getRequestURI(), e);
        return DefaultErrorResult.failure(MessageConsts.SYSTEM_ERROR.code(), e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
