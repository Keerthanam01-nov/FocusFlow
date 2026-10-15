package com.focusflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/login.html";
    }

    @GetMapping("/register")
    public String register() {
        return "redirect:/register.html";
    }

    @GetMapping("/goals")
    public String goals() {
        return "redirect:/goals.html";
    }

    @GetMapping("/habits")
    public String habits() {
        return "redirect:/habits.html";
    }

    @GetMapping("/pomodoro")
    public String pomodoro() {
        return "redirect:/pomodoro.html";
    }

    @GetMapping("/analytics")
    public String analytics() {
        return "redirect:/analytics.html";
    }

    @GetMapping("/reports")
    public String reports() {
        return "redirect:/reports.html";
    }

    @GetMapping("/profile")
    public String profile() {
        return "redirect:/profile.html";
    }
}