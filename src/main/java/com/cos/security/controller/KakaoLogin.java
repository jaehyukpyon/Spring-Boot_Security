package com.cos.security.controller;

import ch.qos.logback.core.pattern.color.BoldCyanCompositeConverter;
import com.cos.security.model.KakaoProfile;
import com.cos.security.model.OAuthToken;
import com.cos.security.model.User;
import com.cos.security.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Controller
public class KakaoLogin {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    // Kakao developers에서 설정한 redirect URI
    @GetMapping("/auth/kakao/callback")
    @ResponseBody
    public ResponseEntity<String> kakaoCallback(String code) {

        // 사용자가 Kakao login시, Kakao에서 넘겨준 인가 코드를 이용하여 accessToken 발급받기
        ResponseEntity<String> accessToken = requestToken(code);

        ObjectMapper objectMapper = new ObjectMapper();
        OAuthToken oAuthToken = null;
        try {
            // Kakao에서 넘겨준 json 데이터를 (includes access_token, refresh_token, scope etc.)를 OAuthToken 객체로 변환
            oAuthToken = objectMapper.readValue(accessToken.getBody(), OAuthToken.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        // accessToken을 사용하여 Kakao User Profile 조회
        ResponseEntity<String> userProfile = getUserInformation(oAuthToken);

        ObjectMapper objectMapper2 = new ObjectMapper();
        objectMapper2.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        KakaoProfile kakaoProfile = null;
        try {
            // Kakao에서 넘겨준 json 데이터를 (includes access_token, refresh_token, scope etc.)를 OAuthToken 객체로 변환
            kakaoProfile = objectMapper2.readValue(userProfile.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("Kakao id(unique): " + kakaoProfile.getId());
        System.out.println("Kakao email: " + kakaoProfile.getKakaoAccount().getEmail());

        kakaoRegistration(kakaoProfile); // profile 정보를 활용한 강제 회원가입

        return userProfile;
    }

    private ResponseEntity<String> requestToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // HttpHeader object 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpBody object 생성
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("grant_type", "authorization_code");
        multiValueMap.add("client_id", "4e81aad6451ddd4384ecea1d994c6044");
        multiValueMap.add("redirect_uri", "http://localhost:9090/auth/kakao/callback");
        multiValueMap.add("code", code);

        // HttpHeader & HttpBody object를 하나의 object에 담기기
       HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(multiValueMap, headers);

       // Http 요청 (POST)
        ResponseEntity<String> responseEntity = restTemplate.exchange(
              "https://kauth.kakao.com/oauth/token",
              HttpMethod.POST,
              kakaoTokenRequest,
              String.class
        );

        return responseEntity;
    }

    private ResponseEntity<String> getUserInformation(OAuthToken oAuthToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        return responseEntity;
    }

    public void kakaoRegistration(KakaoProfile kakaoProfile) {
        // Kakao에서 받은 사용자의 정보를 이용하여 강제 회원가입
        String kakaoRegistrationUsername = kakaoProfile.getKakaoAccount().getEmail() + "_" + kakaoProfile.getId();
        String kakaoRegistrationEmail = kakaoProfile.getKakaoAccount().getEmail();
        String kakaoRegistrationRandomPassword = UUID.randomUUID().toString(); // DB 저장 시 사용할 random password 생성
        String encodedPassword = bCryptPasswordEncoder.encode(kakaoRegistrationRandomPassword);

        System.out.println("Kakao Registration username: " + kakaoRegistrationUsername);
        System.out.println("Kakao Registration email: " + kakaoRegistrationEmail);
        System.out.println("Kakao Registration password: " + kakaoRegistrationRandomPassword);

        User user = new User();
        user.setUsername(kakaoRegistrationUsername);
        user.setEmail(kakaoRegistrationEmail);
        user.setPassword(encodedPassword);
        user.setRole("ROLE_USER");

        userRepository.save(user);
    }

}
