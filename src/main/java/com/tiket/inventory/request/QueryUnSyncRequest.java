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
public class QueryUnSyncRequest implements Serializable {

  private static final long serialVersionUID = 6399094064258877059L;
  private Integer isSynced;
}
