package com.tiket.inventory.service;

import com.tiket.inventory.response.BaseResponse;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class CronUploadImageToLightRoomCdn extends BaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CronUploadImageToLightRoomCdn.class);

  public Completable process(Integer isDeleted, String vendorName, String type, Integer limit){
    return Completable.create(emitter -> {
      boolean next = true;
      int count = 0;
      while (next) {
        if (count == Integer.MAX_VALUE) {
          next = false;
        }
        try {
          String url;
          String urlTemplate;
          HttpEntity<String> entity;
          Map<String, String> params;

          entity = new HttpEntity<>(defaultHeaders());
          url = hotelCoreHost + "/tix-hotel-core/images-raw/cron-upload-image-to-lightroom-cdn";
          urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
              .queryParam("isDeleted", "{isDeleted}")
              .queryParam("vendorName", "{vendorName}")
              .queryParam("type", "{type}")
              .queryParam("limit", "{limit}")
              .encode()
              .toUriString();

          params = new HashMap<>();
          params.put("isDeleted", String.valueOf(isDeleted));
          params.put("vendorName", vendorName);
          params.put("type", type);
          params.put("limit", String.valueOf(limit));
          ResponseEntity<BaseResponse<String>> response = restTemplate.exchange(urlTemplate,
              HttpMethod.POST, entity, new ParameterizedTypeReference<>() {}, params);
          if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.info("count : {}, SUCCESS : {}", count, response.getBody());
            if (response.getBody() != null)
              next = !"0".equals(response.getBody().getData());
          }
        } catch (Exception e) {
          LOGGER.error("count : {}, ERROR : {}", count, e);
        }
        count++;
      }
      LOGGER.info("count : {}, DONE!", count);
      emitter.onComplete();
    }).subscribeOn(Schedulers.io());
  }
}
