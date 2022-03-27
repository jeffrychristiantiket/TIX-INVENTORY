package com.tiket.inventory.service;

import com.tiket.inventory.lib.JSONHelper;
import com.tiket.inventory.request.QueryUnSyncRequest;
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
public class SoftDeleteHotelRawService extends BaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SoftDeleteHotelRawService.class);

  public static final List<String> HEADER = List.of("vendorId".trim(), "name".trim());

  public void softDelete(MultipartFile file){
    try (InputStream inputStream = file.getInputStream()) {
      String data = new String(FileCopyUtils.copyToByteArray(inputStream));
      List<String> lines = validateCsvHeaderAndReturnsCsvDataStrings(data, HEADER);
      int count = 0;
      for (String line : lines) {
        String[] split = line.trim().split(",");
        String vendorId = split[0].trim();

        LOGGER.info("SOFT DELETE {} VENDOR : {}, ID : {}, collection : {}", count++, "RAKUTEN", vendorId, "hotel_raw");

        LinkedMultiValueMap<String, String> headers = defaultHeaders();

        String url;
        String urlTemplate;
        HttpEntity<String> entity;
        Map<String, String> params;

        entity = new HttpEntity<>(headers);
        url = hotelCoreHost + "/tix-hotel-core/hotel-raw";
        urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("vendor", "{vendor}")
            .queryParam("hotelId", "{hotelId}")
            .encode()
            .toUriString();

        params = new HashMap<>();
        params.put("vendor", "RAKUTEN");
        params.put("hotelId", vendorId);
        ResponseEntity<String> response = restTemplate.exchange(urlTemplate, HttpMethod.DELETE, entity, String.class, params);
        if (response.getStatusCode().is2xxSuccessful()) {
          String b = response.getBody();
          System.out.println("SOFT DELETE RAW SUCCESS - " + b + " - " + vendorId);
        }
        Thread.sleep(250L);
      }
    } catch (Exception e) {
      LOGGER.error("ERR : ", e);
    }
  }
}
