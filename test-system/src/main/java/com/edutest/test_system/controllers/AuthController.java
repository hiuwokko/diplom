package com.edutest.test_system.controllers;

import com.edutest.test_system.models.UserEntity;
import com.edutest.test_system.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {
    
    private static final String REDIRECT_INDEX = "redirect:/";
    private static final String REDIRECT_LOGIN = "redirect:/login";

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("userEmail") != null) return REDIRECT_INDEX;
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            session.setAttribute("userEmail", userOpt.get().getEmail());
            session.setAttribute("userName", userOpt.get().getFirstName());
            return REDIRECT_INDEX;
        }
        model.addAttribute("error", "Невірний email або пароль!");
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(HttpSession session) {
        if (session.getAttribute("userEmail") != null) return REDIRECT_INDEX;
        return "register";
    }

    @PostMapping("/register")
    public String handleRegistration(@RequestParam String email, @RequestParam String password, @RequestParam String role, 
                                     @RequestParam String firstName, @RequestParam String lastName, Model model) {
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Користувач вже існує!");
            return "register";
        }
        userRepository.save(new UserEntity(email, password, role, firstName, lastName));
        return REDIRECT_LOGIN + "?success=true";
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate();
        return REDIRECT_LOGIN;
    }
}