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
public class QueryDeletedAndSyncRequest implements Serializable {

  private static final long serialVersionUID = 4070183540533260503L;
  private Integer isDeleted;
  private Integer isSynced;
}
