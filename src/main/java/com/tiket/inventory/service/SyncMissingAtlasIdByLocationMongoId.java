package com.tiket.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.request.QueryReplaceAtlasIdRequest;
import com.tiket.inventory.response.BaseResponse;
import com.tiket.inventory.response.CalculateLocationResponse;
import com.tiket.inventory.response.CalculateLocationResponse.AdmPlaces;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SyncMissingAtlasIdByLocationMongoId extends BaseService {

  public void sync(MultipartFile file, String collectionName) {
    List<String> lines = null;
    try (InputStream inputStream = file.getInputStream()) {
      List<String> headers = List.of("_id");
      String data = new String(FileCopyUtils.copyToByteArray(inputStream));
      lines = validateCsvHeaderAndReturnsCsvDataStrings(data, headers);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (!CollectionUtils.isEmpty(lines)) {
      String url;
      String urlTemplate;
      HttpEntity<String> entity;
      Map<String, String> params;
      ResponseEntity<BaseResponse<CalculateLocationResponse>> response;
      LinkedMultiValueMap<String, String> headers = defaultHeaders();

      for (String mongoId : lines) {
        try {
          url = hotelCoreHost + "/tix-hotel-core/atlas/calculate-location-by-query";
          entity = new HttpEntity<>(headers);
          urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
              .queryParam("query", "{query}")
              .encode()
              .toUriString();
          params = new HashMap<>();
          params.put("query", "mongo_ids:".concat(mongoId.trim()));
          response = restTemplate.exchange(urlTemplate, HttpMethod.GET, entity,
              new ParameterizedTypeReference<>() {}, params);
          if (response.getStatusCode().is2xxSuccessful()) {
            final BaseResponse<CalculateLocationResponse> baseResponse = response.getBody();
            if (baseResponse == null) {
              continue;
            }
            if ("SUCCESS".equals(baseResponse.getCode()) && !CollectionUtils.isEmpty(baseResponse.getData().getAdmPlaces()) && baseResponse.getData().getAdmPlaces().size() == 1) {
              AdmPlaces places = baseResponse.getData().getAdmPlaces().get(0);
              if (places.getId() == null){
                continue;
              }
              final String atlasId = places.getId();
              setAtlasIdToLocationByMongoId(mongoId.trim(), collectionName, atlasId);
            }
          }
        } catch (Exception e) {
          LOG.error("ERROR SET ATLAS ID | collection {} | id {} | e -> ", collectionName, mongoId.trim(), e);
        }
      }
    }
  }

  public void setAtlasIdToLocationByMongoId(String mongoId, String collectionName, String atlasId)
      throws JsonProcessingException {
    LinkedMultiValueMap<String, String> headers = defaultHeaders();
    Map<String, String> params;
    HttpEntity<String> entity;
    String url;
    String urlTemplate;
    String json;
    String queryUpdateType = "SET";
    QueryReplaceAtlasIdRequest body = QueryReplaceAtlasIdRequest.builder().atlasId(atlasId).build();
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
      LOG.info("SET ATLAS ID SUCCESS | collection {} | id {} | atlasId {} | {}", collectionName, mongoId, atlasId, b);
    }
  }
}
