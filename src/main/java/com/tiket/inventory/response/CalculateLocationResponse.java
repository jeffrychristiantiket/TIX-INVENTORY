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
public class CalculateLocationResponse implements Serializable {

  private static final long serialVersionUID = -5655077089472491739L;
  private List<AdmPlaces> admPlaces;
  private List<FreeDistrict> freeDistricts;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AdmPlaces implements Serializable {

    private static final long serialVersionUID = -7052518827978329464L;
    private String id;
    private String name;
    private String admin_level;
    private List<String> alias;
    private List<AdmSources> adm_sources;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FreeDistrict implements Serializable {

    private static final long serialVersionUID = -5806134517713765441L;
    private String id;
    private String name;
    private List<String> alt_name;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AdmSources implements Serializable {

    private static final long serialVersionUID = -1368398742949306818L;
    private String external_id;
    private String source_name;
  }
}
