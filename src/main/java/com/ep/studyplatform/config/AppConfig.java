package com.ep.studyplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){

        // 클래스 들어가서 내용을 잘 확인하자.
        // bcrypt에는 입력한 값과 hash값ㅇ
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
