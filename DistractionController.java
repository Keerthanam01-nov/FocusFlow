package com.focusflow.controller;

import com.focusflow.model.Distraction;
import com.focusflow.service.DistractionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/distraction")
@CrossOrigin("*")
public class DistractionController {

    private final DistractionService service;

    public DistractionController(DistractionService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public Distraction add(@RequestBody Distraction distraction) {
        return service.save(distraction);
    }

    @GetMapping("/all")
    public List<Distraction> all() {
        return service.getAll();
    }
}