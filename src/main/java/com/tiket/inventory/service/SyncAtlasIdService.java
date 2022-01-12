package com.tiket.inventory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.response.BaseResponse;
import com.tiket.inventory.response.HotelSearchResponse;
import com.tiket.inventory.response.HotelSearchResponse.Content;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class SyncAtlasIdService extends BaseService {

  public static final List<String> LOCATION_HEADER =
      Arrays.asList("countryId".trim(), "regionId".trim(), "cityId".trim(), "areaId".trim());

  public void sync(MultipartFile file){
    List<String> lines = null;
    int count = 0;
    try (InputStream inputStream = file.getInputStream()) {
      String data = new String(FileCopyUtils.copyToByteArray(inputStream));
      lines = validateCsvHeaderAndReturnsCsvDataStrings(data, LOCATION_HEADER);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (!CollectionUtils.isEmpty(lines)) {
      String url;
      String urlTemplate;
      HttpEntity<String> entity;
      Map<String, String> params;
      ResponseEntity<String> response;
      LinkedMultiValueMap<String, String> headers = defaultHeaders();

      for (String line : lines) {
        String[] columns = line.split(",");
        url = hotelCoreHost + "/tix-hotel-core/hotel";
        entity = new HttpEntity<>(headers);
        urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("countryId", "{countryId}")
            .queryParam("regionId", "{regionId}")
            .queryParam("cityId", "{cityId}")
            .queryParam("areaId", "{areaId}")
            .encode()
            .toUriString();
        params = new HashMap<>();
        params.put("countryId", columns[0].trim());
        params.put("regionId", columns[1].trim());
        params.put("cityId", columns[2].trim());
        params.put("areaId", columns[3].trim());
        response = restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, String.class, params);
        if (response.getStatusCode().is2xxSuccessful()) {
          String b = response.getBody();
          System.out.println(count++ + " - area id : " + columns[3].trim() + " - SUCCESS - " + b);
          try {
            BaseResponse<HotelSearchResponse> baseResponse = JSONHelper.convertJsonInStringToObject(b, new TypeReference<>() {});
            if (baseResponse.getCode().equals("SUCCESS")) {
              if (!CollectionUtils.isEmpty(baseResponse.getData().getContent())) {
                for (Content content : baseResponse.getData().getContent()) {
                  String hotelId = content.getHotelId();
                  syncHotelAtlasIdByLonLat(hotelId);
                }
              }
            }
          } catch (Exception e) {
            LOG.error("err : ", e);
          }
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
          LOG.error("ERROR status : {}, body : {}, at {}", response.getStatusCode(), response.getBody(), line);
        }
      }
    }
  }

  public void syncHotelAtlasIdByLonLat(String hotelId){

    String url;
    String urlTemplate;
    HttpEntity<String> entity;
    Map<String, String> params;
    ResponseEntity<String> response;
    LinkedMultiValueMap<String, String> headers = defaultHeaders();

    url = hotelCoreHost + "/tix-hotel-core/atlas/location-by-longlat/hotel/{hotelId}";
    entity = new HttpEntity<>(headers);
    urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
        .queryParam("hotelId", "{hotelId}")
        .queryParam("updateMasterLocation", "{updateMasterLocation}")
        .encode()
        .toUriString();
    params = new HashMap<>();
    params.put("hotelId", hotelId);
    params.put("updateMasterLocation", "true");
    response = restTemplate.exchange(urlTemplate, HttpMethod.PATCH, entity, String.class, params);
    if (response.getStatusCode().is2xxSuccessful()) {
      String b = response.getBody();
      System.out.println(b);
    }
    if (!response.getStatusCode().is2xxSuccessful()) {
      LOG.error("ERROR status : {}, body : {}, at hotelId {}", response.getStatusCode(), response.getBody(), hotelId);
    }
  }
}
