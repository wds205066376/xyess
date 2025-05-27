package com.rabbiter.fm.config;

import com.rabbiter.fm.common.interceptor.LogCostInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 允许跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOriginPatterns("http://localhost:9321", "http://127.0.0.1:9323", "http://localhost:9323",
                        "http://127.0.0.1:9321")
                .allowCredentials(true)
                .exposedHeaders(HttpHeaders.SET_COOKIE)
                .maxAge(3600L); // 预检请求的缓存时间
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogCostInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/file/**"); // 排除文件上传路径
    }
}
