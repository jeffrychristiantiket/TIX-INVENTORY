package com.tiket.inventory.service.test;

import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.request.SyncHotelToSearchRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

@Service
public class PublishHotelToB2c extends BaseTest {

  public void publishHotelToB2cTest(){
    try {
      LinkedMultiValueMap<String, String> headers = initHeaders();
      headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
      // publish hotel to b2c
      // text/plain;charset=ISO-8859-1
      String hotelId = "58a3009f-71e8-4d4b-904e-d5c6b7a7c48d";
      String url = "http://192.168.64.39:7040/tix-hotel-core/hotel/sync-hotel-to-search";
      SyncHotelToSearchRequest request = SyncHotelToSearchRequest.builder().hotelIds(hotelId).build();
      String json = JSONHelper.convertObjectToJsonInString(request);
      HttpEntity<String> entity = new HttpEntity<>(json, headers);
      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        String b = response.getBody();
        System.out.println(b);
      }

      if (!response.getStatusCode().is2xxSuccessful()) {
        LOGGER.error("ERROR publish to b2c hotel | hotelId : {}", hotelId);
      }
    } catch (Exception e) {
      LOGGER.error("ERROR at : {}", e.getMessage(), e);
    }
  }
}
