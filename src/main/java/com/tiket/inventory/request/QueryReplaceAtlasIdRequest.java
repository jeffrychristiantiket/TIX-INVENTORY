package com.tiket.inventory.request;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryReplaceAtlasIdRequest implements Serializable {

  private static final long serialVersionUID = -2153257075575521303L;
  private String atlasId;
}
