package com.tiket.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.request.QueryRequest;
import com.tiket.inventory.request.SyncHotelToSearchRequest;
import com.tiket.inventory.response.BaseResponse;
import com.tiket.inventory.response.HotelRedirection;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SyncService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SyncService.class);
  public static final String CSV_SPLIT_REGEX = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

  //hotel_mongo_id, hotelId, lastPublicId, expectedPublicId
  public static final List<String> HOTEL_HEADER =
      Arrays.asList("hotel_mongo_id".trim(), "hotelId".trim(), "lastPublicId".trim(), "expectedPublicId".trim());

  @Value("${hotel.core.host}")
  private String hotelCoreHost;

  /**
   * replace hotel publicId with expectedPublicId by mongoId
   * http://192.168.64.39:7040/swagger-ui.html#!/master45controller/executeQueryUsingPOST
   * POST http://192.168.64.39:7040/tix-hotel-core/master/execute-query
   * {"publicId" : ""}
   *
   * publish hotel to b2c
   * http://192.168.64.39:7040/swagger-ui.html#!/hotel45controller/syncHotelToSearchUsingPOST
   * POST http://192.168.64.39:7040/tix-hotel-core/hotel/sync-hotel-to-search
   * {
   *   "hotelIds": ""
   * }
   *
   * get hotel redirections by lastPublicId then do soft delete
   * http://192.168.64.39:7040/swagger-ui.html#!/hotel45redirection45controller/findAllByTargetPublicIdUsingGET
   * GET http://192.168.64.39:7040/tix-hotel-core/hotel-redirection/all-by-target-public-id/{targetPublicId}
   *
   * soft delete hotel redirections by mongoId
   * http://192.168.64.39:7040/swagger-ui.html#!/hotel45redirection45controller/deleteUsingDELETE_4
   * DELETE http://192.168.64.39:7040/tix-hotel-core/hotel-redirection/{mongoId}
   *
   * publish hotel redirection by mongoId
   * http://192.168.64.39:7040/swagger-ui.html#!/hotel45redirection45controller/publishUsingGET
   * GET http://192.168.64.39:7040/tix-hotel-core/hotel-redirection/publish/{id}
   *
   *
   */
  public void rollbackHotelPublicId(MultipartFile file) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(15000);
    factory.setReadTimeout(15000);
    RestTemplate restTemplate = new RestTemplate(factory);
      try (InputStream inputStream = file.getInputStream()) {
        String data = new String(FileCopyUtils.copyToByteArray(inputStream));
        List<String> lines = validateCsvHeaderAndReturnsCsvDataStrings(data, HOTEL_HEADER);
        String hotelMongoId = "";
        String hotelId = "";
        String lastPublicId = "";
        String expectedPublicId = "";
        int count = 0;
        for (String line : lines) {
          System.out.println("count : " + ++count);
          try {
            Thread.sleep(100L);
            String[] split = line.split(",");
            hotelMongoId = split[0];
            hotelId = split[1];
            lastPublicId = split[2];
            expectedPublicId = split[3];
            System.out.println("hotelMongoId : " + hotelMongoId);
            System.out.println("Hotel ID : " + hotelId);
            System.out.println("lastPublicId : " + lastPublicId);
            System.out.println("expectedPublicId : " + expectedPublicId);

            LinkedMultiValueMap<String, String> headers = defaultHeaders();

            String url;
            String urlTemplate;
            HttpEntity<String> entity;
            Map<String, String> params;
            ResponseEntity<String> response;

            // replace hotel publicId with expectedPublicId by mongoId
            response = replaceHotelPublicIdWithExpectedPublicIdByMongoId(hotelMongoId,
                expectedPublicId, restTemplate, headers);

            if (!response.getStatusCode().is2xxSuccessful()) {
              LOGGER.error("ERROR replace hotel mongoId : {}, hotelId : {}, publicId : {}",
                  hotelMongoId, hotelId, expectedPublicId);
              continue;
            }

            // publish hotel to b2c by hotelId
            Thread.sleep(100L);
            headers = publishHotelToB2c(hotelMongoId, hotelId, expectedPublicId, restTemplate);
            if (headers == null) {
              continue;
            }

            // get hotel redirections by lastPublicId then do soft delete
            Thread.sleep(100L);
            response = getHotelRedirectionsByLastPublicId(lastPublicId, restTemplate, headers);
            BaseResponse<List<HotelRedirection>> baseResponse = null;
            if (response.getStatusCode().is2xxSuccessful()) {
              String b = response.getBody();
              System.out.println("GET HOTEL REDIRECTION SUCCESS");
              baseResponse = JSONHelper.convertJsonInStringToObject(b, new TypeReference<>() {
              });
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
              LOGGER.error(
                  "ERROR get hotel redirections | hotel mongoId : {}, hotelId : {}, publicId : {}",
                  hotelMongoId, hotelId, expectedPublicId);
              continue;
            }

            if (baseResponse != null && !CollectionUtils.isEmpty(baseResponse.getData())) {
              for (HotelRedirection redirection : baseResponse.getData()) {
                // soft delete hotel redirections by mongoId
                url = hotelCoreHost + "/tix-hotel-core/hotel-redirection/{mongoId}";
                entity = new HttpEntity<>(headers);
                urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("mongoId", "{mongoId}")
                    .encode()
                    .toUriString();
                params = new HashMap<>();
                params.put("mongoId", redirection.getId());
                Thread.sleep(100L);
                response = restTemplate.exchange(urlTemplate, HttpMethod.DELETE, entity,
                    String.class, params);
                if (response.getStatusCode().is2xxSuccessful()) {
                  String b = response.getBody();
                  System.out.println("SOFT DELETE SUCCESS - " + b);
                }

                if (!response.getStatusCode().is2xxSuccessful()) {
                  LOGGER.error(
                      "ERROR soft delete hotel redirection | hotel mongoId : {}, hotelId : {}, publicId : {}",
                      hotelMongoId, hotelId, expectedPublicId);
                  continue;
                }

                //publish hotel redirection by mongoId
                url = hotelCoreHost + "/tix-hotel-core/hotel-redirection/publish/{id}";
                entity = new HttpEntity<>(headers);
                urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("id", "{id}")
                    .encode()
                    .toUriString();
                params = new HashMap<>();
                params.put("id", redirection.getId());
                Thread.sleep(100L);
                response = restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, String.class,
                    params);
                if (response.getStatusCode().is2xxSuccessful()) {
                  String b = response.getBody();
                  System.out.println("PUBLISH HOTEL REDIRECT SUCCESS - " + b);
                }
                if (!response.getStatusCode().is2xxSuccessful()) {
                  LOGGER.error(
                      "ERROR publish hotel redirection | hotel mongoId : {}, hotelId : {}, publicId : {}",
                      hotelMongoId, hotelId, expectedPublicId);
                }
              }
            } else {
              LOGGER.warn(
                  "hotel does not have redirection | hotel mongoId : {}, hotelId : {}, publicId : {}",
                  hotelMongoId, hotelId, expectedPublicId);
            }
          } catch (Exception e) {
            System.out.println("ERROR hotel id : " + hotelId + " - msg - " + e.getMessage() + e);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  private ResponseEntity<String> getHotelRedirectionsByLastPublicId(String lastPublicId,
      RestTemplate restTemplate, LinkedMultiValueMap<String, String> headers) {
    headers = defaultHeaders();
    String urlTemplate;
    Map<String, String> params;
    ResponseEntity<String> response;
    String url;
    HttpEntity<String> entity;
    url = hotelCoreHost + "/tix-hotel-core/hotel-redirection/all-by-target-public-id/{targetPublicId}";
    entity = new HttpEntity<>(headers);
    urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
        .queryParam("targetPublicId", "{targetPublicId}")
        .encode()
        .toUriString();

    params = new HashMap<>();
    params.put("targetPublicId", lastPublicId);
    response = restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, String.class, params);
    return response;
  }

  private LinkedMultiValueMap<String, String> publishHotelToB2c(
      String hotelMongoId, String hotelId, String expectedPublicId, RestTemplate restTemplate)
      throws JsonProcessingException {
    String url;
    String json;
    LinkedMultiValueMap<String, String> headers = defaultHeaders();
    HttpEntity<String> entity;
    ResponseEntity<String> response;
    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    url = hotelCoreHost + "/tix-hotel-core/hotel/sync-hotel-to-search";
    SyncHotelToSearchRequest request = SyncHotelToSearchRequest.builder().hotelIds(hotelId).build();
    json = JSONHelper.convertObjectToJsonInString(request);
    entity = new HttpEntity<>(json, headers);
    response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    if (response.getStatusCode().is2xxSuccessful()) {
      String b = response.getBody();
      System.out.println("PUBLISH HOTEL TO B2C SUCCESS - " + b);
    }

    if (!response.getStatusCode().is2xxSuccessful()) {
      LOGGER.error("ERROR publish to b2c hotel mongoId : {}, hotelId : {}, publicId : {}",
          hotelMongoId, hotelId, expectedPublicId);
      return null;
    }
    return headers;
  }

  private LinkedMultiValueMap<String, String> defaultHeaders() {
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

  private ResponseEntity<String> replaceHotelPublicIdWithExpectedPublicIdByMongoId(String hotelMongoId,
      String expectedPublicId, RestTemplate restTemplate,
      LinkedMultiValueMap<String, String> headers) throws JsonProcessingException {
    headers = defaultHeaders();
    Map<String, String> params;
    HttpEntity<String> entity;
    String url;
    String urlTemplate;
    String json;
    String queryUpdateType = "SET";
    String collectionName = "hotel";
    QueryRequest body = QueryRequest.builder().publicId(expectedPublicId).build();
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
    params.put("id", hotelMongoId);
    ResponseEntity<String> response = restTemplate.exchange(urlTemplate, HttpMethod.POST, entity, String.class, params);
    if (response.getStatusCode().is2xxSuccessful()) {
      String b = response.getBody();
      System.out.println("REPLACE PUBLIC ID SUCCESS - " + b);
    }
    return response;
  }

  public static Boolean isCsvHeaderValid(List<String> expectedHeaders, List<String> actualHeaders) {
    Integer countActual = actualHeaders.size();
    Integer countExpected = 0;

    for (String header : expectedHeaders) {
      if (actualHeaders.contains(header.trim())) {
        countExpected = countExpected + 1;
      }
    }

    return countExpected.equals(countActual);
  }

  public static List<String> validateCsvHeaderAndReturnsCsvDataStrings(String csvDataString,
      List<String> allowedHeader) throws Exception {
    List<String> csvRows = Arrays.stream(csvDataString.split("\n"))
        .collect(Collectors.toList());
    List<String> csvHeader = Arrays.asList(csvRows.remove(0)
        .split(CSV_SPLIT_REGEX));
    if (!isCsvHeaderValid(csvHeader, allowedHeader)) {
      throw new Exception();
    }
    return csvRows;
  }

}
