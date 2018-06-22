package jp.co.pmacmobile.common.result;

import jp.co.pmacmobile.common.constant.ResultCode;

/**
 * @author 71432393
 *
 */
public class PlatformResult implements Result {

	private static final long serialVersionUID = -5795368405048932374L;

	private Integer code;

	private String msg;

	private Object result;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public static PlatformResult success() {
		PlatformResult result = new PlatformResult();
		result.setResultCode(ResultCode.SUCCESS);
		return result;
	}

	public static PlatformResult success(Object data) {
		PlatformResult result = new PlatformResult();
		result.setResultCode(ResultCode.SUCCESS);
		result.setResult(data);
		return result;
	}

	public static PlatformResult failure(ResultCode resultCode) {
		PlatformResult result = new PlatformResult();
		result.setResultCode(resultCode);
		return result;
	}

	public static PlatformResult failure(ResultCode resultCode, Object data) {
		PlatformResult result = new PlatformResult();
		result.setResultCode(resultCode);
		result.setResult(data);
		return result;
	}

	public static PlatformResult failure(String message) {
		PlatformResult result = new PlatformResult();
		result.setCode(ResultCode.SYSTEM_INNER_ERROR.code());
		result.setMsg(message);
		return result;
	}

	private void setResultCode(ResultCode code) {
		this.code = code.code();
		this.msg = code.message();
	}

}
