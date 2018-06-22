package jp.co.pmacmobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * アプリ起動
 *
 * @author 71432393
 *
 */
@SpringBootApplication
public class PmacMobileApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(PmacMobileApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PmacMobileApplication.class);
    }
}
