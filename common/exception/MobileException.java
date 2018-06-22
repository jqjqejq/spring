package jp.co.pmacmobile.common.exception;

/**
 * <PRE>
 *
 * クラス名：
 *   携帯ＡＰ例外クラス
 *
 * 機能説明：
 *   携帯ＡＰで発生した例外情報を保持する。
 *
 * </PRE>
 */
public class MobileException extends Exception {
    /**
     * UID
     */
    private static final long serialVersionUID = 1L;

    private String code = null;

    private String message = null;

    /**
     * 携帯ＡＰ例外クラスの初期処理を行う。
     *
     * @param code
     *            エラーコード
     */
    public MobileException(String code) {
        this.code = code;
    }

    /**
     * 携帯ＡＰ例外クラスの初期処理を行う。
     *
     * @param code
     *            エラーコード
     *
     * @param cause
     *            例外クラス
     */
    public MobileException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    /**
     * エラーコードを返す。
     *
     * @return the code エラーコード
     */
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public MobileException(String code, String message) {
        super();
        this.code = code;
        this.message = message;
    }
}
