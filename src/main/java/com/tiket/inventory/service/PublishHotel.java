package com.tiket.inventory.service;

import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.request.SyncHotelToSearchRequest;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PublishHotel extends BaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PublishHotel.class);

  //hotel_mongo_id, hotelId, lastPublicId, expectedPublicId
  public static final List<String> HOTEL_HEADER = List.of("hotelId".trim());

  public Completable publishHotel(MultipartFile file){
    return Completable.create(emitter -> {
      try (InputStream inputStream = file.getInputStream()) {
        String data = new String(FileCopyUtils.copyToByteArray(inputStream));
        List<String> lines = validateCsvHeaderAndReturnsCsvDataStrings(data, HOTEL_HEADER);
        String hotelId = "";
        int count = 0;
        Set<String> set = new HashSet<>();
        for (String line : lines) {
          String[] split = line.split(",");
          hotelId = split[0].trim();
          set.add(hotelId);
        }

        final String id = StringUtils.join(set, ",");

        System.out.println("count : " + ++count);
        System.out.println("Hotel IDs : " + id);

        LinkedMultiValueMap<String, String> headers = defaultHeaders();

        String url;
        HttpEntity<String> entity;
        ResponseEntity<String> response;

        String json;
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        url = hotelCoreHost + "/tix-hotel-core/hotel/sync-hotel-to-search";
        SyncHotelToSearchRequest request = SyncHotelToSearchRequest.builder().hotelIds(id).build();
        json = JSONHelper.convertObjectToJsonInString(request);
        entity = new HttpEntity<>(json, headers);
        response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
          String b = response.getBody();
          System.out.println("PUBLISH HOTEL TO B2C SUCCESS - " + b + " - " + id);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
          LOGGER.error("ERROR publish to b2c hotel hotelId : {}, hotelId : {}", id);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      emitter.onComplete();
    }).subscribeOn(Schedulers.io());
  }

}
