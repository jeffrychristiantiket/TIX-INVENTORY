package com.tiket.inventory.service;

import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.request.QueryUnSetLacertaHash;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UnsetRoomLacertaHash extends BaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UnsetRoomLacertaHash.class);

  public static final List<String> HEADER = List.of("_id".trim());

  public void unsetLacertaHash(MultipartFile file){
    try (InputStream inputStream = file.getInputStream()) {
      String data = new String(FileCopyUtils.copyToByteArray(inputStream));
      List<String> lines = validateCsvHeaderAndReturnsCsvDataStrings(data, HEADER);
      int count = 0;
      for (String line : lines) {
        String[] split = line.trim().split(",");
        String mongoId = split[0].trim();

        LOGGER.info("SYNC : {}, {}", count++, mongoId);

        LinkedMultiValueMap<String, String> headers = defaultHeaders();

        String url;
        String urlTemplate;
        HttpEntity<String> entity;
        Map<String, String> params;

        String json;
        String queryUpdateType = "UNSET";
        String collectionName = "room";
        QueryUnSetLacertaHash body = QueryUnSetLacertaHash.builder().lacertaHash("").build();
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
          System.out.println("UNSET LACERTA_HASH SUCCESS - " + b + " - " + mongoId);
        }
        Thread.sleep(100L);
      }
    } catch (Exception e) {
      LOGGER.error("ERR : ", e);
    }
  }
}
