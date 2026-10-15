package com.focusflow.model;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="focus_sessions") public class FocusSession {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private String clientId; private String mode; private Integer durationSeconds; private LocalDateTime startedAt; private LocalDateTime endedAt;
 public FocusSession(){} public Long getId(){return id;} public String getClientId(){return clientId;} public void setClientId(String v){clientId=v;} public String getMode(){return mode;} public void setMode(String v){mode=v;} public Integer getDurationSeconds(){return durationSeconds;} public void setDurationSeconds(Integer v){durationSeconds=v;} public LocalDateTime getStartedAt(){return startedAt;} public void setStartedAt(LocalDateTime v){startedAt=v;} public LocalDateTime getEndedAt(){return endedAt;} public void setEndedAt(LocalDateTime v){endedAt=v;}
}
