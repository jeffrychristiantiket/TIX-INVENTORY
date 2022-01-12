package com.tiket.inventory.service;

import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.request.QueryDeletedRequest;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SyncIsDeleted extends BaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SyncIsDeleted.class);

  public static final List<String> HOTEL_HEADER =
      Arrays.asList("_id".trim());


  public void replaceDeleted(MultipartFile file){
    try (InputStream inputStream = file.getInputStream()) {
      String data = new String(FileCopyUtils.copyToByteArray(inputStream));
      List<String> lines = validateCsvHeaderAndReturnsCsvDataStrings(data, HOTEL_HEADER);
      for (String line : lines) {
        String[] split = line.split(",");
        String mongoId = split[0].trim();
        LOGGER.info("SYNC ID : {}", mongoId);
        LinkedMultiValueMap<String, String> headers = defaultHeaders();

        String url;
        String urlTemplate;
        HttpEntity<String> entity;
        Map<String, String> params;

        String json;
        String queryUpdateType = "SET";
        String collectionName = "area";
        QueryDeletedRequest body = QueryDeletedRequest.builder().isDeleted(1).build();
        json = JSONHelper.convertObjectToJsonInString(body);
        entity = new HttpEntity<>(json, headers);
        url = hotelCoreHost + "/tix-hotel-core/master/execute-query";
        urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("queryUpdateType", "{queryUpdateType}")
            .queryParam("collectionName", "{collectionName}")
            .queryParam("id", "{id}")
            .encode()
            .toUriString();

        params = new HashMap<>();
        params.put("queryUpdateType", queryUpdateType);
        params.put("collectionName", collectionName);
        params.put("id", mongoId);
        ResponseEntity<String> response = restTemplate.exchange(urlTemplate, HttpMethod.POST, entity, String.class, params);
        if (response.getStatusCode().is2xxSuccessful()) {
          String b = response.getBody();
          System.out.println("REPLACE DELETED SUCCESS - " + b + " - " + mongoId);
        }
        Thread.sleep(400L);
      }
    } catch (Exception e) {
      LOGGER.error("ERR : ", e);
    }
  }
}
