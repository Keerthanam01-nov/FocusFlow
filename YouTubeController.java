package com.focusflow.controller;

import com.focusflow.service.YouTubeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/youtube")
@CrossOrigin(origins = "*")
public class YouTubeController {
    private final YouTubeService youtube;

    public YouTubeController(YouTubeService youtube) {
        this.youtube = youtube;
    }

    @GetMapping("/feed")
    public Map<String,Object> feed(
            @RequestParam(defaultValue = "Programming") String interest,
            @RequestParam(defaultValue = "0") int distractionPercent) {
        return youtube.feed(interest, Math.max(0, Math.min(100, distractionPercent)));
    }
}
