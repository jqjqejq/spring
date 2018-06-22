package jp.co.pmacmobile.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jp.co.pmacmobile.common.interceptor.ResponseResultInterceptor;

/**
 * @author 71432393
 *
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ResponseResultInterceptor responseResultInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
        registry.addInterceptor(this.responseResultInterceptor).addPathPatterns("/**");
    }
}
