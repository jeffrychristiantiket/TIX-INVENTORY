package com.tiket.inventory.service.test;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SoftDeleteHotelRedirectionsByMongoId extends BaseTest {

  public void softDeleteHotelRedirectionsByMongoIdTest(){
    try {
      // soft delete hotel redirections by mongoId
      String mongoId = "6079314cfe675e7e8eee1997";
      String url = "http://192.168.64.39:7040/tix-hotel-core/hotel-redirection/{mongoId}";
      HttpEntity<String> entity = new HttpEntity<>(initHeaders());
      String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
          .queryParam("mongoId", "{mongoId}")
          .encode()
          .toUriString();
      Map<String, String> params = new HashMap<>();
      params.put("mongoId", mongoId);
      ResponseEntity<String> response = restTemplate.exchange(urlTemplate, HttpMethod.DELETE,
          entity, String.class, params);
      if (response.getStatusCode().is2xxSuccessful()) {
        String b = response.getBody();
        System.out.println(b);
      }
      if (!response.getStatusCode().is2xxSuccessful()) {
        LOGGER.error("ERROR soft delete hotel redirection | hotel mongoId : {}", mongoId);
      }
    } catch (Exception e) {
      LOGGER.error("ERROR at : {}", e.getMessage(), e);
    }
  }

}
