package com.creativeyann17.app.configurations;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("security")
@Data
@Slf4j
public class SecurityConfiguration {

  private boolean enabled;
  private String apiKey;
  private List<String> publics = new ArrayList<>();

}
