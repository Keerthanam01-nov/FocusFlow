package com.focusflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class YouTubeService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public YouTubeService(@Value("${focusflow.youtube.api-key:}") String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public Map<String,Object> feed(String interest, int distractionPercent) {
        Map<String,Object> result = new LinkedHashMap<>();
        int learningPercent = Math.min(90, 50 + distractionPercent / 2);
        int entertainmentPercent = 100 - learningPercent;

        result.put("learningPercent", learningPercent);
        result.put("entertainmentPercent", entertainmentPercent);
        result.put("policy", "Adaptive Feed: the more distraction detected, the more learning Shorts are surfaced.");

        if (apiKey.isBlank()) {
            result.put("live", false);
            result.put("message", "Set YOUTUBE_API_KEY to enable live YouTube recommendations.");
            result.put("items", List.of());
            return result;
        }

        try {
            int learningCount = Math.max(3, Math.round(learningPercent / 20f));
            int entertainmentCount = Math.max(1, Math.round(entertainmentPercent / 25f));

            List<Map<String,Object>> items = new ArrayList<>();
            items.addAll(search(learningQuery(interest), "LEARNING", learningCount));
            items.addAll(search(interest + " shorts", "ENTERTAINMENT", entertainmentCount));

            // Deterministic shuffle so the ratio changes visibly without fake data.
            Collections.shuffle(items, new Random(System.currentTimeMillis()));
            result.put("live", true);
            result.put("items", items);
            return result;
        } catch (Exception e) {
            result.put("live", false);
            result.put("message", "YouTube API request failed: " + e.getMessage());
            result.put("items", List.of());
            return result;
        }
    }

    private String learningQuery(String interest) {
        String i = interest == null || interest.isBlank() ? "programming" : interest;
        return i + " educational shorts tutorial";
    }

    private List<Map<String,Object>> search(String query, String type, int maxResults) throws Exception {
        String url = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet&type=video&videoDuration=short&maxResults=" + Math.min(maxResults, 10)
                + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(8))
                .GET().build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("YouTube HTTP " + response.statusCode());
        }

        JsonNode root = mapper.readTree(response.body());
        List<Map<String,Object>> out = new ArrayList<>();

        for (JsonNode item : root.path("items")) {
            String id = item.path("id").path("videoId").asText("");
            if (id.isBlank()) continue;

            JsonNode snippet = item.path("snippet");
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("videoId", id);
            row.put("title", snippet.path("title").asText("YouTube Short"));
            row.put("channel", snippet.path("channelTitle").asText(""));
            row.put("publishedAt", snippet.path("publishedAt").asText(""));
            row.put("thumbnail", snippet.path("thumbnails").path("high").path("url").asText(""));
            row.put("url", "https://www.youtube.com/shorts/" + id);
            row.put("type", type);
            row.put("reason", "Matched to " + (type.equals("LEARNING") ? "your learning goal" : "your entertainment allowance"));
            out.add(row);
        }
        return out;
    }
}
