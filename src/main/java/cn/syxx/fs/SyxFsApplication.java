package cn.syxx.fs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SyxFsApplication {

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
}
