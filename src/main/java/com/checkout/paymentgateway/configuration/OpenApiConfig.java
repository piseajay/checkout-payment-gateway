package com.checkout.paymentgateway.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenApiConfig {

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }

  @Bean
  public OpenAPI paymentGatewayOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Checkout.com Payment Gateway API")
                .description("Processes and Retrieves card payments.")
                .version("1.0.0"));
  }
}
