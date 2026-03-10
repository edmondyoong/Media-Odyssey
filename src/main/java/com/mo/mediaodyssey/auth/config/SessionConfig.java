package com.mo.mediaodyssey.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@Configuration
@EnableJdbcHttpSession
public class SessionConfig {
}

// TODO: In a future iteration, consider storing JSON instead of Binary into
// Postgres.