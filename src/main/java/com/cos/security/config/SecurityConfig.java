package com.cos.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity // Spring Security Filter가 Spring Filter Chain에 등록됨.
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /*
    * @EnableWebSecurity 어노테이션 설정 및 WebSecurityConfigureAdapter를 상속받고,
    * configure 메서드를 overrides 하는 것 만으로도 (메서드 안이 비어 있어도 상관 없음),
    * Spring Security가 default로 사용하게 해주었던 /login 페이지 설정이 off 된다.
    * */

   @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.authorizeRequests()
                .antMatchers("/user/**").authenticated()
                .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
                .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll() // 여기까지만 작성하고 서버 실행 후, 로그인을 하지 않은 상태에서 localhost:9090 접근 가능. 다만, /user에 접근하려고 하면, 403 error가 발생했다고 알려주는 Whitelabel Error Page가 뜬다.
                .and() // 사용자가 로그인 하지 않은 상태에서, 인증되어야 접근할 수 있는 페이지에 접근하려고 할 때, 내가 정의한 페이지로 redirect 시키기 위한 설정 => .and().formLogin().loginPage("/login")
                .formLogin().loginPage("/loginForm");
    }

    @Bean
    public BCryptPasswordEncoder encodePwd() {
       return new BCryptPasswordEncoder();
    }

}
