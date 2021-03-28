package com.ep.studyplatform.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 프로세서 개수를 가져온다.
        int processors = Runtime.getRuntime().availableProcessors();

        log.info("processors count {}",processors);

        // 프로세스 개수로 설정
        executor.setCorePoolSize(processors);

        executor.setMaxPoolSize(processors * 2);

        // 대기 상태의 풀에 줄을 세우는 역할
        // capacity가 초과하면 max pool size가 넘을 만큼 설정을 한다.
        // 큐의 기본값은 21억개이다.
        executor.setQueueCapacity(50);

        executor.setKeepAliveSeconds(60);

        // 로깅할 때 편하게 정보를 기록한다.
        executor.setThreadNamePrefix("AsyncExecutor-");

        executor.initialize();
        return executor;
    }
}
