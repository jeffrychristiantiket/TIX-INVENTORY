package com.tiket.inventory.lib;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfiguration {

  @Bean
  public RestTemplate restTemplate(){
    var factory = new HttpComponentsClientHttpRequestFactory();
    factory.setConnectTimeout(60000);
    factory.setReadTimeout(60000);
    return new RestTemplate(factory);
  }

}
