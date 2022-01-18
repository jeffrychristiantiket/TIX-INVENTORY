package com.tiket.inventory.service;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SyncHotelRawToHotel extends BaseService {

  public Completable sync(String vendorExternalIds, String vendor, Boolean unSyncRaw, MultipartFile file){
    return Completable.create(emitter -> {
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
          LOG.info("SYNC BY CSV END.. NO DATA SYNCED no data in csv");
          emitter.onComplete();
          return;
        }

        syncHotelRawToHotel(lines.toArray(String[]::new), vendor, unSyncRaw);
      } else {
        if (StringUtils.isBlank(vendorExternalIds)) {
          LOG.info("SYNC BY CSV END.. NO DATA SYNCED plase input vendorExternalIds");
          emitter.onComplete();
          return;
        }
        syncHotelRawToHotel(vendorExternalIds.split(","), vendor, unSyncRaw);
      }
      LOG.info("SYNC BY CSV END..");
      emitter.onComplete();
    }).subscribeOn(Schedulers.io());
  }

  private void syncHotelRawToHotel(String[] hotelIds, String vendor, Boolean unSyncRaw) {
    if (unSyncRaw) {
      unSyncHotelRawByVendorExternalIdAndVendorName(hotelIds, vendor);
    }

    if (!unSyncRaw) {
      syncHotelRaw(hotelIds, vendor);
    }
  }

  private void syncHotelRaw(String[] hotelIds, String vendor) {
    List<Single<Boolean>> tasks = new ArrayList<>();
    int httpDelay = 0;
    for (String hotelId : hotelIds) {
      if (httpDelay >= 100) {
        try {
          Thread.sleep(2500L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        httpDelay = 0;
      }
      httpDelay++;
      tasks.add(process(vendor, hotelId));
      if (tasks.size() >= 2) {
        Single.zip(tasks, objects -> true).blockingGet();
        LOG.info("SYNCED {} DATA..", tasks.size());
        tasks = new ArrayList<>();
      }
    }
    if (!CollectionUtils.isEmpty(tasks)) {
      Single.zip(tasks, objects -> true).blockingGet();
      LOG.info("SYNCED {} DATA..", tasks.size());
    }
  }

  private Single<Boolean> process(String vendor, String hotelId) {
    return Single.defer(() -> {
      HttpEntity<String> entity;
      Map<String, String> params;
      String urlTemplate;
      ResponseEntity<String> response;
      String url;
      try {
        url = hotelCoreHost + "/tix-hotel-core/sync/hotel-blocking";
        entity = new HttpEntity<>(defaultHeaders());
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
      } catch (Exception e) {
        LOG.error("Err -> ", e);
      }
      return Single.just(true);
    }).subscribeOn(Schedulers.io());
  }

  private void unSyncHotelRawByVendorExternalIdAndVendorName(String[] hotelIds, String vendor) {
    String url;
    String urlTemplate;
    HttpEntity<String> entity;
    Map<String, String> params;
    ResponseEntity<String> response;

    for (String hotelId : hotelIds) {
      try {
        Thread.sleep(200L);
        url = hotelCoreHost + "/tix-hotel-core/hotel-raw/is-synced";
        entity = new HttpEntity<>(defaultHeaders());
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
