package jp.co.pmacmobile.common.handler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jp.co.pmacmobile.common.exception.MobileException;
import jp.co.pmacmobile.common.result.DefaultErrorResult;

@RestController
@ControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(MobileException.class)
    public ResponseEntity<DefaultErrorResult> handleBusinessException(MobileException e, HttpServletRequest request) {
        return super.handleBusinessException(e, request);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public DefaultErrorResult handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        return super.handleRuntimeException(e, request);
    }
}
