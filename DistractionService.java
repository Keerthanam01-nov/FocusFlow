package com.focusflow.service;

import com.focusflow.model.Distraction;
import com.focusflow.repository.DistractionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistractionService {

    private final DistractionRepository repository;

    public DistractionService(DistractionRepository repository) {
        this.repository = repository;
    }

    public Distraction save(Distraction distraction) {
        return repository.save(distraction);
    }

    public List<Distraction> getAll() {
        return repository.findAll();
    }
}