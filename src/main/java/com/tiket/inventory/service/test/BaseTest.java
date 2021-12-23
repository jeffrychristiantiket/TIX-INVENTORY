package com.tiket.inventory.service.test;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

public class BaseTest {
  protected static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);

  protected RestTemplate restTemplate = new RestTemplate();

  protected LinkedMultiValueMap<String, String> initHeaders(){
    LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
    headers.add("storeId", "TIKETCOM");
    headers.add("channelId", "WEB");
    headers.add("requestId", UUID.randomUUID().toString());
    headers.add("serviceId", "TIX-INVENTORY");
    headers.add("username", "tiket");
    headers.add("lang", "en");
    return headers;
  }
}
