package com.tiket.inventory.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class JSONHelper {
  private static ObjectMapper mapper;

  static {
    mapper = new ObjectMapper();
  }

  private JSONHelper() {
  }

  public static <T> String convertObjectToJsonInString(T data) throws JsonProcessingException {
    return mapper.writeValueAsString(data);
  }

  public static <T> T convertJsonInStringToObject(String jsonInString, Class<T> clazz) throws IOException {
    return mapper.readValue(jsonInString, clazz);
  }

  public static <T> T convertJsonInStringToObject(String jsonInString, TypeReference<T> typeReference) throws IOException {
    return mapper.readValue(jsonInString, typeReference);
  }
}
