package com.focusflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_events", indexes = {
        @Index(name="idx_activity_client_time", columnList="clientId,startedAt"),
        @Index(name="idx_activity_time", columnList="startedAt")
})
public class ActivityEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;
    private String source;
    private String application;
    private String title;
    @Column(length=1000) private String url;
    private String category;
    private Integer durationSeconds;
    private Boolean distraction;
    private Boolean productive;
    private Boolean youtubeShort;
    private LocalDateTime startedAt;

    public ActivityEvent(){}

    public Long getId(){return id;}
    public String getClientId(){return clientId;}
    public void setClientId(String v){clientId=v;}
    public String getSource(){return source;}
    public void setSource(String v){source=v;}
    public String getApplication(){return application;}
    public void setApplication(String v){application=v;}
    public String getTitle(){return title;}
    public void setTitle(String v){title=v;}
    public String getUrl(){return url;}
    public void setUrl(String v){url=v;}
    public String getCategory(){return category;}
    public void setCategory(String v){category=v;}
    public Integer getDurationSeconds(){return durationSeconds;}
    public void setDurationSeconds(Integer v){durationSeconds=v;}
    public Boolean getDistraction(){return distraction;}
    public void setDistraction(Boolean v){distraction=v;}
    public Boolean getProductive(){return productive;}
    public void setProductive(Boolean v){productive=v;}
    public Boolean getYoutubeShort(){return youtubeShort;}
    public void setYoutubeShort(Boolean v){youtubeShort=v;}
    public LocalDateTime getStartedAt(){return startedAt;}
    public void setStartedAt(LocalDateTime v){startedAt=v;}
}
