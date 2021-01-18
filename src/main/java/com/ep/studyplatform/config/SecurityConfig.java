package com.ep.studyplatform.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity // 직접 스프링 시큐리티 설정을 직접 할 수 있다.
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // ctrl + O 를 눌러서 필요한 메서드만 오버라이딩을 할 수 있다.
    // http configure를 오버라이딩을 한다. 원하는 특별한 요청들을 시큐리팅, authorize를 하지 않도록 거를 수 있다.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 해당하는 url은 시큐리티 대상에서 제외된다.
        http.authorizeRequests()
                .mvcMatchers("/","/login","/sign-up","/check-email","/check-email-token"
                                ,"/email-login","/check-email-login","/login-link")
                .permitAll()
                // 프로필에 대한 접근은 GET만 허용한다.
                .mvcMatchers(HttpMethod.GET,"/profile/*").permitAll()
                // 나머지는 로그인을 해야만 접근이 가능하다.
                .anyRequest().authenticated();

        // 시큐리는 기본적으로 csrf 기능이 활성화되어있다.
        // 타 사이트에서 REST방식으로 공격할 수 있다. 이것을 방지하기 위해서 하나의 세션당 하나의 CSRF 토큰을 발행한다.
        // 타임리프 form으로 만들었을 때 자동으로 csrf 기능을 지원해준다.
        // form 안에 자동으로 <input type="hidden" name="_csrf" value="db0ae85c-0a9d-4955-b20c-37fbb85c5e0d">가 들어가있다.

    }

    // static resources들을 인증없이 허가하는 것으로 허용한다.
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
