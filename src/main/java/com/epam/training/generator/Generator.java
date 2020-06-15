package com.epam.training.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class Generator {
    private final GeneratorStarter starter;

    @Autowired
    public Generator(GeneratorStarter starter) {
        this.starter = starter;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        starter.generate();
    }
}
