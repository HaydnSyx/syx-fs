package cn.syxx.fs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@Slf4j
@SpringBootApplication
public class SyxFsApplication {

    @Value("${syxfs.path}")
    private String uploadPath;

    public static void main(String[] args) {
        SpringApplication.run(SyxFsApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new HeaderEncodingInterceptor());
        return restTemplate;
    }

    @Bean
    public FilterRegistrationBean<HeaderDecodingFilter> headerDecodingFilter() {
        FilterRegistrationBean<HeaderDecodingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HeaderDecodingFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public ApplicationRunner runner() {
        return args -> {
            FileUtils.init(uploadPath);
            log.info("syx-fs upload dir init finish ...");
        };
    }
}
