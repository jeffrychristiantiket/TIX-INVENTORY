package com.tiket.inventory.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelRedirection implements Serializable {

  private static final long serialVersionUID = 6516879132012196949L;
  private String id;
  private String sourceHotelId;
  private String sourcePublicId;
  private String targetPublicId;
}
