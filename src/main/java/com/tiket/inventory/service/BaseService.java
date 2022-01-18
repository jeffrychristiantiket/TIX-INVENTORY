package com.tiket.inventory.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component(value = "baseService")
public class BaseService {

  protected static final Logger LOG = LoggerFactory.getLogger(BaseService.class);
  public static final String CSV_SPLIT_REGEX = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

  @Value("${hotel.core.host}")
  protected String hotelCoreHost;

  @Autowired
  protected RestTemplate restTemplate;

  protected LinkedMultiValueMap<String, String> defaultHeaders() {
    LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.set(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
    headers.add("storeId", "TIKETCOM");
    headers.add("channelId", "WEB");
    headers.add("requestId", UUID.randomUUID().toString());
    headers.add("serviceId", "SWAGGER");
    headers.add("username", "support@tiket.com");
    headers.add("lang", "en");
    return headers;
  }


  protected static Boolean isCsvHeaderValid(List<String> expectedHeaders, List<String> actualHeaders) {
    Integer countActual = actualHeaders.size();
    Integer countExpected = 0;

    for (String header : expectedHeaders) {
      if (actualHeaders.contains(header.trim())) {
        countExpected = countExpected + 1;
      }
    }

    return countExpected.equals(countActual);
  }

  protected static List<String> validateCsvHeaderAndReturnsCsvDataStrings(String csvDataString,
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
