package com.tiket.inventory.service.test;

import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.request.QueryRequest;
import java.util.HashMap;
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
public class ReplaceHotelPublicId extends BaseTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceHotelPublicId.class);

  public void replaceHotelPublicIdTest(){
    try {
      String queryUpdateType = "SET";
      String collectionName = "hotel";
      String mongoId = "5b76769a25a8fa3b9b610b1f";
      String expectedPublicId = "23-lodge-nusa-dua-4110016381728716119";
      LinkedMultiValueMap<String, String> headers = initHeaders();
      // replace hotel publicId with expectedPublicId by mongoId
      QueryRequest body = QueryRequest.builder().publicId(expectedPublicId).build();
      String json = JSONHelper.convertObjectToJsonInString(body);
      HttpEntity<String> entity = new HttpEntity<>(json, headers);
      String url = "http://192.168.64.39:7040/tix-hotel-core/master/execute-query";
      String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
          .queryParam("queryUpdateType", "{queryUpdateType}")
          .queryParam("collectionName", "{collectionName}")
          .queryParam("id", "{id}")
          .encode()
          .toUriString();

      Map<String, String> params = new HashMap<>();
      params.put("queryUpdateType", queryUpdateType);
      params.put("collectionName", collectionName);
      params.put("id", mongoId);

      ResponseEntity<String> response = restTemplate.exchange(urlTemplate, HttpMethod.POST, entity, String.class, params);
      if (response.getStatusCode().is2xxSuccessful()) {
        String b = response.getBody();
        System.out.println(b);
      }

      if (!response.getStatusCode().is2xxSuccessful()) {
        LOGGER.error("ERROR replace hotel publicId | mongoId : {}, publicId : {}", mongoId, expectedPublicId);
      }
    } catch (Exception e) {
      LOGGER.error("ERROR at : {}", e.getMessage(), e);
    }
  }
}
