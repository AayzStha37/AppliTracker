package com.projects.applitracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class AppliTrackerApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(AppliTrackerApplication.class, args);
    }

}
