package com.focusflow.controller;

import com.focusflow.model.ActivityEvent;
import com.focusflow.repository.ActivityEventRepository;
import com.focusflow.service.AttentionEngineService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/attention")
@CrossOrigin(origins = "*")
public class AttentionController {
    private final ActivityEventRepository activities;
    private final AttentionEngineService engine;

    public AttentionController(ActivityEventRepository activities, AttentionEngineService engine) {
        this.activities = activities;
        this.engine = engine;
    }

    @GetMapping("/state")
    public Map<String,Object> state(@RequestParam(defaultValue = "all") String clientId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        List<ActivityEvent> events = "all".equalsIgnoreCase(clientId)
                ? activities.findByStartedAtAfter(start)
                : activities.findByClientIdAndStartedAtAfter(clientId, start);

        Map<String,Object> result = new LinkedHashMap<>(engine.analyse(events));
        int screen = events.stream().mapToInt(e -> safe(e.getDurationSeconds())).sum();
        int distraction = events.stream()
                .filter(e -> Boolean.TRUE.equals(e.getDistraction()))
                .mapToInt(e -> safe(e.getDurationSeconds())).sum();

        int allowance = 20 * 60; // adaptive entertainment budget starts at 20 min/day
        int remaining = Math.max(0, allowance - distraction);
        int distractionPercent = screen == 0 ? 0 : (int)Math.round(distraction * 100.0 / screen);

        result.put("screenSeconds", screen);
        result.put("distractionSeconds", distraction);
        result.put("distractionPercent", distractionPercent);
        result.put("dailyEntertainmentBudgetSeconds", allowance);
        result.put("remainingEntertainmentBudgetSeconds", remaining);
        result.put("learningTargetPercent", Math.min(90, 50 + distractionPercent / 2));
        result.put("adaptiveRule",
                "Entertainment allowance shrinks as distraction rises; learning recommendations expand automatically.");
        return result;
    }

    private int safe(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }
}
