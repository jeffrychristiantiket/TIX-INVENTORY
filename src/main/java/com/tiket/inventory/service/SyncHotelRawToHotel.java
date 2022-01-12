package com.tiket.inventory.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
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
public class SyncHotelRawToHotel extends BaseService {

  public void sync(String vendorExternalIds, String vendor, Boolean unSyncRaw, MultipartFile file){
    if (Objects.nonNull(file)) {
      LOG.info("SYNC BY CSV STARTED..");
      List<String> lines = null;
      try (InputStream inputStream = file.getInputStream()) {
        List<String> headers = List.of("vendorId");
        String data = new String(FileCopyUtils.copyToByteArray(inputStream));
        lines = validateCsvHeaderAndReturnsCsvDataStrings(data, headers);
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (CollectionUtils.isEmpty(lines)) {
        LOG.info("SYNC BY CSV END.. NO DATA SYNCED");
        return;
      }

      syncHotelRawToHotel(lines.toArray(String[]::new), vendor, unSyncRaw);
      LOG.info("SYNC BY CSV END..");
    } else {
      if (StringUtils.isBlank(vendorExternalIds)) {
        return;
      }
      syncHotelRawToHotel(vendorExternalIds.split(","), vendor, unSyncRaw);
    }
  }

  private void syncHotelRawToHotel(String[] hotelIds, String vendor, Boolean unSyncRaw) {
    if (unSyncRaw) {
      unSyncHotelRawByVendorExternalIdAndVendorName(hotelIds, vendor);
    }

    syncHotelRaw(hotelIds, vendor);
  }

  private void syncHotelRaw(String[] hotelIds, String vendor) {
    String url;
    String urlTemplate;
    HttpEntity<String> entity;
    Map<String, String> params;
    ResponseEntity<String> response;
    LinkedMultiValueMap<String, String> headers = defaultHeaders();

    for (String hotelId : hotelIds) {
      try {
        Thread.sleep(200L);
        url = hotelCoreHost + "/tix-hotel-core/sync/hotel-blocking";
        entity = new HttpEntity<>(headers);
        urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("hotelId", "{hotelId}")
            .queryParam("vendorName", "{vendorName}")
            .encode()
            .toUriString();
        params = new HashMap<>();
        params.put("hotelId", hotelId.trim());
        params.put("vendorName", vendor);
        response = restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, String.class, params);
        if (response.getStatusCode().is2xxSuccessful()) {
          LOG.info("SYNC SUCCESS - hotelId {}, response : {}", hotelId.trim(), response);
        } else {
          LOG.info("SYNC FAILED - hotelId {}, response : {}", hotelId.trim(), response);
        }
      } catch (InterruptedException e) {
        LOG.error("Err -> ", e);
      }
    }
  }

  private void unSyncHotelRawByVendorExternalIdAndVendorName(String[] hotelIds, String vendor) {
    String url;
    String urlTemplate;
    HttpEntity<String> entity;
    Map<String, String> params;
    ResponseEntity<String> response;
    LinkedMultiValueMap<String, String> headers = defaultHeaders();

    for (String hotelId : hotelIds) {
      try {
        Thread.sleep(200L);
        url = hotelCoreHost + "/tix-hotel-core/hotel-raw/is-synced";
        entity = new HttpEntity<>(headers);
        urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("vendorExternalId", "{vendorExternalId}")
            .queryParam("vendorName", "{vendorName}")
            .encode()
            .toUriString();
        params = new HashMap<>();
        params.put("vendorExternalId", hotelId.trim());
        params.put("vendorName", vendor);
        response = restTemplate.exchange(urlTemplate, HttpMethod.PATCH, entity, String.class, params);
        if (response.getStatusCode().is2xxSuccessful()) {
          LOG.info("UN SYNC RAW SUCCESS - vendorExternalId {} vendorName {}, response : {}", hotelId.trim(),
              vendor, response);
        } else {
          LOG.info("UN SYNC RAW FAILED - vendorExternalId {} vendorName {}, response : {}", hotelId.trim(),
              vendor, response);
        }
      } catch (InterruptedException e) {
        LOG.error("Err -> ", e);
      }
    }
  }
}
