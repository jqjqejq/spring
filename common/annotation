package jp.co.pmacmobile.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jp.co.pmacmobile.common.result.PlatformResult;
import jp.co.pmacmobile.common.result.Result;

/**
 * @author 71432393
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseResult {
    Class<? extends Result> value() default PlatformResult.class;
}
