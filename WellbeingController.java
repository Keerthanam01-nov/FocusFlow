package com.focusflow.controller;

import com.focusflow.model.*;
import com.focusflow.repository.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/wellbeing")
@CrossOrigin(origins="*")
public class WellbeingController {
    private final ActivityEventRepository activities;
    private final FocusSessionRepository sessions;
    private final GoalItemRepository goals;
    private final HabitItemRepository habits;

    public WellbeingController(ActivityEventRepository a, FocusSessionRepository s,
                               GoalItemRepository g, HabitItemRepository h) {
        activities=a; sessions=s; goals=g; habits=h;
    }

    @PostMapping("/activity")
    public ActivityEvent activity(@RequestBody ActivityEvent e) {
        if(e.getClientId()==null || e.getClientId().isBlank()) e.setClientId("browser");
        if(e.getStartedAt()==null) e.setStartedAt(LocalDateTime.now());
        if(e.getDurationSeconds()==null) e.setDurationSeconds(1);
        e.setDurationSeconds(Math.min(Math.max(e.getDurationSeconds(),1),300));
        if(e.getDistraction()==null) e.setDistraction(isDistracting(e.getCategory(),e.getUrl(),e.getTitle()));
        if(e.getProductive()==null) e.setProductive(isProductive(e.getCategory(),e.getUrl()));
        if(e.getYoutubeShort()==null)
            e.setYoutubeShort(e.getUrl()!=null && e.getUrl().contains("youtube.com/shorts"));
        return activities.save(e);
    }

    @PostMapping("/focus")
    public FocusSession focus(@RequestBody FocusSession s) {
        if(s.getClientId()==null || s.getClientId().isBlank()) s.setClientId("browser");
        if(s.getStartedAt()==null) s.setStartedAt(LocalDateTime.now());
        if(s.getDurationSeconds()==null) s.setDurationSeconds(0);
        if(s.getEndedAt()==null) s.setEndedAt(LocalDateTime.now());
        return sessions.save(s);
    }

    @GetMapping("/metrics")
    public Map<String,Object> metrics(@RequestParam(defaultValue="all") String clientId) {
        LocalDateTime start=LocalDate.now().atStartOfDay();
        List<ActivityEvent> a = "all".equalsIgnoreCase(clientId)
                ? activities.findByStartedAtAfter(start)
                : activities.findByClientIdAndStartedAtAfter(clientId,start);
        List<FocusSession> f = "all".equalsIgnoreCase(clientId)
                ? sessions.findByStartedAtAfter(start)
                : sessions.findByClientIdAndStartedAtAfter(clientId,start);

        int screen=0, distraction=0, productive=0, shorts=0, switches=0, events=a.size();
        Map<String,Integer> appTimes=new HashMap<>();
        Map<String,Integer> categoryTimes=new HashMap<>();

        for(ActivityEvent e:a) {
            int sec=nz(e.getDurationSeconds());
            screen += sec;
            if(Boolean.TRUE.equals(e.getDistraction())) distraction += sec;
            if(Boolean.TRUE.equals(e.getProductive())) productive += sec;
            if(Boolean.TRUE.equals(e.getYoutubeShort())) shorts += sec;
            if("tab-switch".equals(e.getSource()) || "app-switch".equals(e.getSource())) switches++;
            String app=e.getApplication();
            if(app==null || app.isBlank()) app=e.getCategory()==null?"Other":e.getCategory();
            appTimes.merge(app,sec,Integer::sum);
            String cat=e.getCategory()==null?"Other":e.getCategory();
            categoryTimes.merge(cat,sec,Integer::sum);
        }

        int focus=f.stream().mapToInt(x->nz(x.getDurationSeconds())).sum();
        int totalAttention=Math.max(1,productive+distraction+focus);
        int score=Math.min(100,(int)Math.round(((productive+focus)*100.0)/totalAttention));
        int wellness=Math.max(0,Math.min(100,100-(distraction/60)));
        int xp=Math.min(9999,focus/60*2 + productive/60);
        int level=1+xp/100;

        Map<String,Object> m=new LinkedHashMap<>();
        m.put("screenTimeSeconds",screen);
        m.put("focusSeconds",focus);
        m.put("distractionSeconds",distraction);
        m.put("productiveSeconds",productive);
        m.put("productivityScore",score);
        m.put("focusScore",Math.min(100,(int)Math.round(focus*100.0/Math.max(1,focus+distraction))));
        m.put("wellnessScore",wellness);
        m.put("tabSwitches",switches);
        m.put("youtubeShortSeconds",shorts);
        m.put("events",events);
        m.put("focusSessions",f.size());
        m.put("xp",xp);
        m.put("level",level);
        int[] hourlyFocus=new int[24], hourlyDistraction=new int[24];
        for(ActivityEvent e:a){
            if(e.getStartedAt()==null) continue;
            int hour=e.getStartedAt().getHour();
            int sec=nz(e.getDurationSeconds());
            if(Boolean.TRUE.equals(e.getDistraction())) hourlyDistraction[hour]+=sec;
            if(Boolean.TRUE.equals(e.getProductive())) hourlyFocus[hour]+=sec;
        }
        m.put("hourlyFocus",hourlyFocus);
        m.put("hourlyDistraction",hourlyDistraction);
        m.put("trackingSources",List.of("Windows activity agent","Browser extension","Focus sessions"));
        m.put("apps",top(appTimes));
        m.put("categories",top(categoryTimes));
        m.put("insight",insight(distraction,focus,shorts,switches,screen));
        return m;
    }

    @GetMapping("/events")
    public List<ActivityEvent> events(@RequestParam(defaultValue="all") String clientId) {
        return "all".equalsIgnoreCase(clientId)
                ? activities.findTop100ByOrderByStartedAtDesc()
                : activities.findTop100ByClientIdOrderByStartedAtDesc(clientId);
    }

    @PostMapping("/goals")
    public GoalItem goal(@RequestBody GoalItem g){ return goals.save(g); }

    @GetMapping("/goals")
    public List<GoalItem> goals(@RequestParam(defaultValue="browser")String clientId){return goals.findByClientId(clientId);}

    @PostMapping("/habits")
    public HabitItem habit(@RequestBody HabitItem h){
        if(h.getCompletedToday()==null)h.setCompletedToday(false);
        if(h.getStreak()==null)h.setStreak(0);
        return habits.save(h);
    }

    @GetMapping("/habits")
    public List<HabitItem> habits(@RequestParam(defaultValue="browser")String clientId){return habits.findByClientId(clientId);}

    @PutMapping("/habits/{id}/toggle")
    public HabitItem toggle(@PathVariable Long id){
        HabitItem h=habits.findById(id).orElseThrow();
        boolean done=!Boolean.TRUE.equals(h.getCompletedToday());
        h.setCompletedToday(done);
        h.setStreak(done?nz(h.getStreak())+1:Math.max(0,nz(h.getStreak())-1));
        return habits.save(h);
    }

    private int nz(Integer v){return v==null?0:v;}

    private boolean isDistracting(String c,String u,String t){
        String s=((c==null?"":c)+" "+(u==null?"":u)+" "+(t==null?"":t)).toLowerCase();
        return s.matches(".*(social|entertainment|gaming|shorts|instagram|facebook|tiktok|reddit|memes|celebrity).*");
    }

    private boolean isProductive(String c,String u){
        String s=((c==null?"":c)+" "+(u==null?"":u)).toLowerCase();
        return s.matches(".*(programming|coding|learning|education|study|work|documentation|github|stackoverflow|leetcode|coursera|udemy|khanacademy).*");
    }

    private List<Map<String,Object>> top(Map<String,Integer> map){
        return map.entrySet().stream()
            .sorted((x,y)->Integer.compare(y.getValue(),x.getValue()))
            .limit(8)
            .map(x->{Map<String,Object>m=new LinkedHashMap<>();m.put("name",x.getKey());m.put("seconds",x.getValue());return m;})
            .toList();
    }

    private String insight(int d,int f,int shorts,int switches,int screen){
        if(screen<60) return "Keep tracking. FocusFlow needs real activity history before making strong predictions.";
        if(shorts>900) return "Attention loop detected: YouTube Shorts has crossed 15 minutes. Try Learning Swap before another scroll.";
        if(switches>20) return "High switching detected. Your next best move is a 25-minute single-task window.";
        if(d>f*2+600) return "Distraction is dominating today. FocusFlow recommends a short Focus Rescue instead of a hard block.";
        if(f>d) return "Your focused time is currently ahead of distraction. Protect the next high-attention window.";
        return "Your attention is mixed. One uninterrupted session will give FocusFlow better behavioural evidence.";
    }
}
