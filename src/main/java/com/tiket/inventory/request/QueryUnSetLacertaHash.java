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
public class QueryUnSetLacertaHash implements Serializable {

  private static final long serialVersionUID = 5413115037405230865L;
  private String lacertaHash;
}
