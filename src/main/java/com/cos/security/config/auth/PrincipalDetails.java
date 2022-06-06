package com.cos.security.config.auth;

import com.cos.security.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/*
* Security가 /login 주소 요청이 오면 낚아채서 로그인을 진행시킴.
* 로그인 진행이 완료 되면, session을 만들어 준다. (세션 공간은 같은데, Security가 갖고 있는 세션이 있다. Security가 자신만의 Session 공간을 갖는다.)
* Key 값으로 구분 한다는 것인데, SecurityContextHolder 라는 이 Key 값에다 Session 정보를 저장시킨다.
* 저장 시킬 때, 여기 들어갈 수 있는 어떤 정보는, 즉 Security가 갖고 있는 session에 들어갈 수 있는 Object가 정해져 있다. (Authentication 타입의 객체)
* Authentication 안에, User정보가 있는데 그 type은 (User object type => UserDetails type 객체여야 함)
*
* Security Session 영역이 있고, 그 영역에 Session 정보를 저장해 주는데, 여기 들어갈 수 있는 객체는 Authentication 객체.
* Authentication 이라는 객체 안에 유저 정보 저장 시 UserDetails 타입이어야 함.
* Security Session 에 있는 Session 정보를 get 하여 꺼내면, Authentication 객체가 나옴. 이 안에서 UserDetails를 꺼내면 User Object에 접근 가능
* Security Session 내부에 Authentication 내부에 UserDetails
* */

public class PrincipalDetails implements UserDetails {

    private User user;

    public PrincipalDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole();
            }
        });

        return authorities;
    }

    @Override
    public String getPassword() {
        System.out.println("PrincipalDetails's getPassword() : " + user.getPassword());
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        System.out.println("PrincipalDetails's getUsername() : " + user.getUsername());
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
