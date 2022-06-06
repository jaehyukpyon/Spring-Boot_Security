package com.cos.security.config.auth;

import com.cos.security.model.User;
import com.cos.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PrincipalDetailsService implements UserDetailsService {

    /*
    * SecurityConfig에서 설정해 놓은, loginProcessingUrl("/login").
    * "/login" 요청이 오면, 자동으로 UserDetailsService type으로 Bean 등록 되어 있는 loadUserByUsername 메서드가 실행됨.
    * */

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(username);
        } else {
            return new PrincipalDetails(user);
        }

    }

}
