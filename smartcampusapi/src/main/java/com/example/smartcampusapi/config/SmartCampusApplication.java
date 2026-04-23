package com.example.smartcampusapi.config;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public static final String DOCUMENTATION_URL =
            "https://github.com/replace-with-your-public-repo/SmartCampusAPI#report-answers";

    public SmartCampusApplication() {
        packages("com.example.smartcampusapi.resources",
                 "com.example.smartcampusapi.exception",
                 "com.example.smartcampusapi.filter");
        register(JacksonFeature.class);
    }
}
