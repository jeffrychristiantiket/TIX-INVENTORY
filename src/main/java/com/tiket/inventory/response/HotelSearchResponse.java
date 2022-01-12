package com.tiket.inventory.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelSearchResponse implements Serializable {

  private static final long serialVersionUID = 5217997959576653348L;
  List<Content> content;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Content implements Serializable {

    private static final long serialVersionUID = 4837258593064687295L;
    private String id;
    private String hotelId;
    private String publicId;
    private String name;

  }
}
