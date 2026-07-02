package com.plands.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class LogConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeQueryString(true); // ?startDate=... 같은 주소창 데이터 보기
        loggingFilter.setIncludePayload(true);     // 프론트가 JSON으로 보낸 Body 데이터 보기
        loggingFilter.setMaxPayloadLength(10000);  // 너무 길면 1만 글자까지만 자르기
        loggingFilter.setIncludeHeaders(false);    // 복잡한 헤더 정보는 청소하기
        return loggingFilter;
    }
}
