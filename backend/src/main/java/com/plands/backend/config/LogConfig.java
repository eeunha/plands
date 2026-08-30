package com.plands.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * 이슈 추적 및 디버깅 목적의 HTTP 요청 데이터 로깅 설정
 */
@Configuration
public class LogConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();

        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);  // 대용량 요청으로 인한 메모리 과부하 방지 (10KB 제한)
        loggingFilter.setIncludeHeaders(false);    // 민감한 인증 정보(Authorization, Cookie) 유출 방지

        return loggingFilter;
    }
}
