package com.cos.security.controller;

import com.cos.security.config.auth.PrincipalDetails;
import com.cos.security.model.User;
import com.cos.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IndexController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping(value = {"", "/"})
    public String index() {
        return "index";
    }

    @GetMapping("/user")
    public String user() {
        return "user";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/manager")
    public String manager() {
        return "manager";
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm() {
        return "joinForm";
    }

    @GetMapping("/loginFailed")
    public String loginFailed(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요.");
        return "redirect:/loginForm";
    }

    @PostMapping("/join")
    public String join(User user) {
        System.out.println(user);

        user.setRole("ROLE_USER");

        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);

        user.setPassword(encPassword);

        userRepository.save(user);

        return "redirect:/loginForm";
    }

    // session 에서 유저 정보 찾는 법
    @GetMapping("/findUserName")
    @ResponseBody
    public String findUserName(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        System.out.println("login username/userid: " + principalDetails.getUsername());

        return principalDetails.getUsername();
    }

    @Secured(value = "ROLE_ADMIN")
    @GetMapping("/info")
    @ResponseBody
    public String info() {
        return "개인정보";
    }

    @PreAuthorize(value = "hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/data")
    @ResponseBody
    public String date() {
        return "데이터";
    }

}
