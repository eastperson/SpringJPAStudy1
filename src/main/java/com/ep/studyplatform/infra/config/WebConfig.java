package com.ep.studyplatform.infra.config;

import com.ep.studyplatform.modules.notification.NotificationInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
// @EnableWebMvc 스프링이 주는 WebMVC를 사용하기 위해서는 해당 애너테이션을 사용하면 안된다.
public class WebConfig implements WebMvcConfigurer {

    private final NotificationInterceptor notificationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 이렇게 작성하면 모든 요청에 적용이 된다.
        //registry.addInterceptor(notificationInterceptor);
        log.info("add interceptors ................");

        List<String> staticResourcesPath = Arrays.stream(StaticResourceLocation.values()).flatMap(StaticResourceLocation::getPatterns)
                .collect(Collectors.toList());
        staticResourcesPath.add("/node_modules/**");

        registry.addInterceptor(notificationInterceptor)
                .excludePathPatterns(staticResourcesPath);

    }
}
