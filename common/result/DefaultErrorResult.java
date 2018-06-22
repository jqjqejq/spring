package jp.co.pmacmobile.common.result;

import org.springframework.http.HttpStatus;

import jp.co.pmacmobile.common.exception.MobileException;
import jp.co.pmacmobile.common.util.RequestContextHolderUtil;

/**
 * @author 71432393
 *
 */
public class DefaultErrorResult implements Result {

	private static final long serialVersionUID = -2605473769382736384L;

	private Integer status;

	private String message;

	private String code;

	private String path;

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public static DefaultErrorResult failure(String resultCode, Throwable e, HttpStatus httpStatus, Object errors) {
		DefaultErrorResult result = DefaultErrorResult.failure(resultCode, e, httpStatus);
		return result;
	}

	public static DefaultErrorResult failure(String resultCode, Throwable e, HttpStatus httpStatus) {
		DefaultErrorResult result = new DefaultErrorResult();
		result.setCode(resultCode);
		result.setMessage(e.getMessage());
		result.setStatus(httpStatus.value());
		result.setPath(RequestContextHolderUtil.getRequest().getRequestURI());
		return result;
	}

	public static DefaultErrorResult failure(MobileException e) {
		DefaultErrorResult defaultErrorResult = DefaultErrorResult.failure(e.getCode(), e, HttpStatus.OK,
				e.getMessage());
		if (null == e.getMessage()) {
			defaultErrorResult.setMessage(e.getMessage());
		}
		return defaultErrorResult;
	}

}
