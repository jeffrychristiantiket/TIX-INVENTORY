package com.tiket.inventory.service.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.response.BaseResponse;
import com.tiket.inventory.response.HotelRedirection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GetHotelRedirectionsByTargetPublicId extends BaseTest {

  public void getHotelRedirectionsByTargetPublicIdTest(){
    try {
      String lastPublicId = "1-bedroom-ambassade-residence-404001618555212150";
      LinkedMultiValueMap<String, String> headers = initHeaders();

      // get hotel redirections by lastPublicId then do soft delete
      String url = "http://192.168.64.39:7040/tix-hotel-core/hotel-redirection/all-by-target-public-id/{targetPublicId}";
      HttpEntity<String> entity = new HttpEntity<>(headers);
      String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
          .queryParam("targetPublicId", "{targetPublicId}")
          .encode()
          .toUriString();

      Map<String, String> params = new HashMap<>();
      params.put("targetPublicId", lastPublicId);
      ResponseEntity<String> response = restTemplate.exchange(urlTemplate, HttpMethod.GET, entity,
          String.class, params);
      BaseResponse<List<HotelRedirection>> baseResponse;
      if (response.getStatusCode().is2xxSuccessful()) {
        String b = response.getBody();
        baseResponse = JSONHelper.convertJsonInStringToObject(b, new TypeReference<>() {});
        System.out.println(baseResponse);
      }
    } catch (Exception e) {
      LOGGER.error("ERROR at : {}", e.getMessage(), e);
    }
  }
}
