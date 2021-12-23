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
public class SyncHotelToSearchRequest implements Serializable {

  private static final long serialVersionUID = -7619205799102645890L;
  private String hotelIds;
}
