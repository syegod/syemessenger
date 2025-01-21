package io.syemessenger;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "io.syemessenger")
public class AppConfiguration {

  @Bean
  public WebSocketServer webSocketServer(ServiceConfig config, WebSocketServlet servlet) {
    return WebSocketServer.launch(config.port(), servlet);
  }

  @Bean
  public JsonMapper jsonMapper() {
    return JsonMappers.jsonMapper();
  }
}
