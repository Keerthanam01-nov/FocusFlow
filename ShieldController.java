package com.focusflow.controller; 
import org.springframework.web.bind.annotation.*; 
import java.time.*; import java.util.concurrent.*; 
import java.util.*; @RestController @RequestMapping("/api/shield") @CrossOrigin(origins="*") 
public class ShieldController{
     private static final Map<String,Long> active=new ConcurrentHashMap<>(); 
     @PostMapping("/start") public Map<String,Object> start(@RequestBody Map<String,Object> b)
     {String id=String.valueOf(b.getOrDefault("clientId","demo"));
     int mins=Integer.parseInt(String.valueOf(b.getOrDefault("minutes",25)));
     active.put(id,System.currentTimeMillis()+mins*60000L);return status(id);}
      @PostMapping("/stop") public Map<String,Object> stop(@RequestBody Map<String,Object>b)
      {String id=String.valueOf(b.getOrDefault("clientId","demo"));active.remove(id);return status(id);} @GetMapping("/status") public Map<String,Object> status(@RequestParam(defaultValue="demo")String clientId){long until=active.getOrDefault(clientId,0L); if(until<System.currentTimeMillis()){active.remove(clientId);until=0;}
 Map<String,Object>m=new HashMap<>();
 m.put("active",until>0);
 m.put("until",until);
 return m;
}
}