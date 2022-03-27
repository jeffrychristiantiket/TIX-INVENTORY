package com.tiket.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.request.QueryDeletedAndSyncRequest;
import com.tiket.inventory.request.QueryDeletedRequest;
import com.tiket.inventory.response.BaseResponse;
import com.tiket.inventory.response.HotelSearchResponse;
import com.tiket.inventory.response.RoomRawResponse;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public class SyncIsDeletedAndIsSync extends BaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SyncIsDeletedAndIsSync.class);

  public static final List<String> HEADER =
      Arrays.asList("hotel_id".trim(), "room_id".trim());

  public String getRoomRaw(String hotelId, String roomId, String vendor){
    HttpEntity<String> entity;
    Map<String, String> params;
    String urlTemplate;
    ResponseEntity<String> response;
    String url;
    try {
      url = hotelCoreHost + "/tix-hotel-core/hotel/room-raw/get-by-vendor-id/{vendor}/{hotelId}/{roomId}";
      entity = new HttpEntity<>(defaultHeaders());
      urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
          .queryParam("vendor", "{vendor}")
          .queryParam("hotelId", "{hotelId}")
          .queryParam("roomId", "{roomId}")
          .encode()
          .toUriString();
      params = new HashMap<>();
      params.put("vendor", vendor.trim());
      params.put("hotelId", hotelId.trim());
      params.put("roomId", roomId.trim());
      response = restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, String.class, params);
      if (response.getStatusCode().is2xxSuccessful()) {
        String data = response.getBody();
        BaseResponse<RoomRawResponse> baseResponse = JSONHelper.convertJsonInStringToObject(data, new TypeReference<>() {});
        if ("SUCCESS".equals(baseResponse.getCode())){
          return Optional.ofNullable(baseResponse.getData()).map(RoomRawResponse::getId).orElse(null);
        }
        LOG.info("GET ROOM RAW SUCCESS - hotelId {}, response : {}", hotelId.trim(), response);
      } else {
        LOG.info("GET ROOM RAW FAILED - hotelId {}, response : {}", hotelId.trim(), response);
      }
    } catch (Exception e) {
      LOG.error("Err -> ", e);
    }
    return null;
  }

  public void replaceDeletedAndIsSync(MultipartFile file){
    try (InputStream inputStream = file.getInputStream()) {
      String data = new String(FileCopyUtils.copyToByteArray(inputStream));
      List<String> lines = validateCsvHeaderAndReturnsCsvDataStrings(data, HEADER);
      int count = 0;
      for (String line : lines) {
        String[] split = line.split(",");
        String hotelId = split[0].trim();
        String roomId = split[1].trim();
        String mongoId = getRoomRaw(hotelId, roomId, "TIKET");

        LOGGER.info("SYNC : {} {}, {}", count++, hotelId, roomId);

        if (mongoId == null){
          LOGGER.info("NO DATA SYNC : {}, {}", hotelId, roomId);
          continue;
        }

        LinkedMultiValueMap<String, String> headers = defaultHeaders();

        String url;
        String urlTemplate;
        HttpEntity<String> entity;
        Map<String, String> params;

        String json;
        String queryUpdateType = "SET";
        String collectionName = "room_raw";
        QueryDeletedAndSyncRequest body = QueryDeletedAndSyncRequest.builder().isDeleted(1).isSynced(0).build();
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
