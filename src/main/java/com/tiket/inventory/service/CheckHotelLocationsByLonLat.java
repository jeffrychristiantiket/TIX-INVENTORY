package com.tiket.inventory.service;

import com.tiket.inventory.lib.WriteCsvFileHelper;
import com.tiket.inventory.response.BaseResponse;
import com.tiket.inventory.response.CalculateLocationResponse;
import com.tiket.inventory.response.CalculateLocationResponse.AdmPlaces;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
public class CheckHotelLocationsByLonLat extends BaseService {

  String[] CSV_HEADER_OUTPUT = {"notes","_id","hotelId","publicId","longitude","latitude","vendor","externalId","name"};

  public List<String[]> getInvalidHotelLonLat(MultipartFile file){
    List<String[]> invalidHotelLonLat = new ArrayList<>();
    List<String> lines = null;
    try (InputStream inputStream = file.getInputStream()) {
      List<String> headers = Arrays.asList("_id","hotelId","publicId","longitude","latitude","vendor","externalId","name");
      String data = new String(FileCopyUtils.copyToByteArray(inputStream));
      lines = validateCsvHeaderAndReturnsCsvDataStrings(data, headers);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (!CollectionUtils.isEmpty(lines)) {
      //hit atlas calculate location and check if response doesn't have level 2 or (level 3 & level 4)
      String url;
      String urlTemplate;
      HttpEntity<String> entity;
      Map<String, String> params;
      ResponseEntity<BaseResponse<CalculateLocationResponse>> response;
      LinkedMultiValueMap<String, String> headers = defaultHeaders();

      for (String line : lines) {
        String[] columns = line.split(",");
        url = hotelCoreHost + "/tix-hotel-core/atlas/calculate-location";
        entity = new HttpEntity<>(headers);
        urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("longitude", "{longitude}")
            .queryParam("latitude", "{latitude}")
            .encode()
            .toUriString();
        params = new HashMap<>();
        params.put("longitude", columns[3].trim());
        params.put("latitude", columns[4].trim());
        try {
          response = restTemplate.exchange(urlTemplate, HttpMethod.GET, entity,
              new ParameterizedTypeReference<>() {
              }, params);
          if (response.getStatusCode().is2xxSuccessful()) {
            final BaseResponse<CalculateLocationResponse> baseResponse = response.getBody();
            if (baseResponse != null) {
              if ("SUCCESS".equals(baseResponse.getCode())) {
                if (!CollectionUtils.isEmpty(baseResponse.getData().getAdmPlaces())) {
                  boolean isCityExists = baseResponse.getData().getAdmPlaces().stream()
                      .map(AdmPlaces::getAdmin_level).anyMatch(s -> s.equals("2"));
                  if (!isCityExists) {
                    //write to csv
                    LOG.info("INVALID HOTEL LON-LAT FOUND");
                    invalidHotelLonLat.add("Geo location does not have City or Adm Place Level 2".concat(",").concat(line.trim()).split(","));
                    continue;
                  }
                  boolean isAreaExists = baseResponse.getData().getAdmPlaces().stream()
                      .map(AdmPlaces::getAdmin_level).anyMatch(s -> s.equals("3") || s.equals("4"));
                  if (!isAreaExists) {
                    //write to csv
                    LOG.info("INVALID HOTEL LON-LAT FOUND");
                    invalidHotelLonLat.add("Geo location does not have Area or Adm Place Level 3 and 4".concat(",").concat(line.trim()).split(","));
                  }
                } else {
                  LOG.info("LON LAT NO DATA");
                  invalidHotelLonLat.add("Geo location does not return Adm Place data".concat(",").concat(line.trim()).split(","));
                }
              } else {
                LOG.error("FAILED");
              }
            }
          } else {
            LOG.error("FAILED");
          }
        } catch (Exception e) {
          LOG.error("ERROR : ", e);
        }
      }
      return invalidHotelLonLat;
    }
    return Collections.emptyList();
  }

  public void verifyAndExportToCsv(MultipartFile file) {
    String pathDir = System.getProperty("user.home") + "/Desktop/csv";
    String fileName = "/data-"+ new Date().getTime() +".csv";

    try {
      List<String[]> invalidHotelLonLat = getInvalidHotelLonLat(file);
      if (!CollectionUtils.isEmpty(invalidHotelLonLat)) {
        WriteCsvFileHelper.writeCsvFile(CSV_HEADER_OUTPUT, invalidHotelLonLat, pathDir, fileName);
      }
    } catch (Exception e) {
      LOG.error("ERROR : ", e);
    }
  }

  public static void main(String[] args) {
    String pathDir = System.getProperty("user.home") + "/Desktop/csv";

    String fileName = "/data.csv";
    String[] header = {"id", "name", "address", "phone"};
    try {
      WriteCsvFileHelper.writeCsvFile(header, createCsvDataSimple(), pathDir, fileName);
    } catch (Exception e) {
      LOG.error("ERROR : ", e);
    }
  }

  private static List<String[]> createCsvDataSimple() {
    String[] record1 = {"1", "first name", "address 1", "11111"};
    String[] record2 = {"2", "second name", "address 2", "22222"};

    List<String[]> list = new ArrayList<>();
    list.add(record1);
    list.add(record2);

    return list;
  }
}
