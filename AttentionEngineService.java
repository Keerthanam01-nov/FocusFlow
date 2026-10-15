package com.focusflow.service;

import com.focusflow.model.ActivityEvent;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Lightweight Java-only online classifier.
 * It estimates distraction probability from observable behaviour:
 * session duration, switching, late-night use and source/category signals.
 * The model is intentionally explainable so the user can see why a score changed.
 */
@Service
public class AttentionEngineService {

    public Map<String,Object> analyse(List<ActivityEvent> events) {
        double total = 0, distracted = 0, productive = 0;
        int switches = 0, shorts = 0, late = 0;

        for (ActivityEvent e : events) {
            int sec = safe(e.getDurationSeconds());
            total += sec;
            if (Boolean.TRUE.equals(e.getDistraction())) distracted += sec;
            if (Boolean.TRUE.equals(e.getProductive())) productive += sec;
            if ("tab-switch".equalsIgnoreCase(e.getSource()) ||
                "app-switch".equalsIgnoreCase(e.getSource())) switches++;
            if (Boolean.TRUE.equals(e.getYoutubeShort())) shorts += sec;
            if (e.getStartedAt() != null) {
                int h = e.getStartedAt().getHour();
                if (h >= 23 || h < 6) late += sec;
            }
        }

        double distractionRate = total == 0 ? 0 : distracted / total;
        double switchPressure = Math.min(1.0, switches / 30.0);
        double shortPressure = Math.min(1.0, shorts / 1800.0);
        double latePressure = Math.min(1.0, late / 3600.0);

        // Explainable logistic model.
        double z = -2.0
                + 3.2 * distractionRate
                + 1.3 * switchPressure
                + 1.4 * shortPressure
                + 0.9 * latePressure;
        double probability = 1.0 / (1.0 + Math.exp(-z));

        int score = (int)Math.round((1.0 - probability) * 100.0);
        score = Math.max(0, Math.min(100, score));

        String state;
        if (score >= 80) state = "FLOW";
        else if (score >= 60) state = "STABLE";
        else if (score >= 40) state = "DRIFT";
        else state = "DISTRACTION LOOP";

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("attentionScore", score);
        out.put("distractionProbability", Math.round(probability * 1000.0) / 1000.0);
        out.put("state", state);
        out.put("distractionRate", Math.round(distractionRate * 1000.0) / 1000.0);
        out.put("switchPressure", Math.round(switchPressure * 1000.0) / 1000.0);
        out.put("shortPressure", Math.round(shortPressure * 1000.0) / 1000.0);
        out.put("lateNightPressure", Math.round(latePressure * 1000.0) / 1000.0);
        out.put("explanation", explanation(score, switches, shorts, late, productive, distracted));
        return out;
    }

    private String explanation(int score, int switches, int shorts, int late,
                               double productive, double distracted) {
        if (shorts >= 900) return "Short-form video is consuming a large part of your attention budget.";
        if (switches >= 20) return "Rapid switching is fragmenting your attention. Protect one task for 25 minutes.";
        if (late >= 1800) return "Late-night activity is increasing your distraction risk.";
        if (distracted > productive * 1.5) return "Distraction currently outweighs productive activity.";
        if (score >= 80) return "Your attention pattern is healthy right now. Protect this momentum.";
        return "Your pattern is mixed. One uninterrupted focus session should improve the signal.";
    }

    private int safe(Integer v) { return v == null ? 0 : Math.max(0, v); }
}
