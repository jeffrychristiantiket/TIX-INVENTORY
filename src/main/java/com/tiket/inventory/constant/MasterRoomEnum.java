package com.tiket.inventory.constant;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MasterRoomEnum {

  ROOM_USAGE(new HashMap<>(){{
    put("610772cd01e07a822752462e", new String[]{
        "1 hours usage",
        "1 jam pemakaian",
        "1-hour usage",
        "penggunaan 1 jam",
        "1 jam",
        "1h",
        "1 hour",
        "one hour",
        "satu jam",
        "1hour",
        "1hr",
        "1-hour",
        "one-hour"
    });
    put("610772f3d7bf6bb6035ec75a", new String[]{
        "2 hours usage",
        "2 jam pemakaian",
        "2-hour usage",
        "penggunaan 2 jam",
        "2 jam",
        "2h",
        "2 hours",
        "2hours",
        "2hour",
        "2 hour",
        "two hours",
        "dua jam",
        "2hrs",
        "2-hour",
        "2-hours"
    });
    put("610771e301e07a8227524628", new String[]{
        "3 hours usage",
        "3 jam pemakaian",
        "3-hour usage",
        "penggunaan 3 jam",
        "3 jam",
        "tiga jam",
        "3h",
        "3 hour",
        "3 hours",
        "3hour",
        "3hours",
        "three hour",
        "three hours",
        "3-hour",
        "3-hours"
    });
    put("61077210d7bf6bb6035ec757", new String[]{
        "4 hours usage",
        "4 jam pemakaian",
        "4-hour usage",
        "penggunaan 4 jam",
        "4h",
        "4 hour",
        "4 hours",
        "four hour",
        "four hours",
        "4 jam",
        "empat jam",
        "4hours",
        "4-hour",
        "4-hours"
    });
    put("6107723a01e07a8227524629", new String[]{
        "5 hours usage",
        "5 jam pemakaian",
        "5-hour usage",
        "penggunaan 5 jam",
        "5h",
        "5 hours",
        "5 hour",
        "five hour",
        "five hours",
        "5 jam",
        "5jam",
        "lima jam",
        "5hrs",
        "5hours",
        "5-hour",
        "5-hours"
    });
    put("6107726301e07a822752462a", new String[]{
        "6 hours usage",
        "6 jam pemakaian",
        "6-hour usage",
        "penggunaan 6 jam",
        "6h",
        "6 jam",
        "enam jam",
        "6 hour",
        "6hour",
        "6 hours",
        "6hours",
        "six hours",
        "six hour",
        "6-hour",
        "6-hours"
    });
    put("6107728901e07a822752462b", new String[]{
        "7 hours usage",
        "7 jam pemakaian",
        "7-hour usage",
        "penggunaan 7 jam",
        "7h",
        "7 hours",
        "7 hour",
        "seven hours",
        "7 jam",
        "tujuh jam",
        "7hours",
        "7-hour",
        "7-hours"
    });
    put("610772b1d7bf6bb6035ec759", new String[]{
        "8 hours usage",
        "8 jam pemakaian",
        "8-hour usage",
        "penggunaan 8 jam",
        "8h",
        "8 hours",
        "8 hour",
        "eight hours",
        "eight hour",
        "8 jam",
        "delapan jam",
        "8-hour",
        "8-hours"
    });
    put("610a36d501e07a908b0537f8", new String[]{
        "9 hours usage",
        "9 jam pemakaian",
        "9-hour usage",
        "penggunaan 9 jam",
        "9 jam",
        "9 hour",
        "9 hours",
        "9hr",
        "sembilan jam",
        "nine hours",
        "9 hrs",
        "9hrs",
        "9-hour",
        "9-hours"
    });
    put("610a370bd7bf6bb56f978d1a", new String[]{
        "10 hours usage",
        "10 jam pemakaian",
        "10-hour usage",
        "penggunaan 10 jam",
        "10 hr",
        "10 hour",
        "10hour",
        "10hours",
        "10 hours",
        "10 jam",
        "sepuluh jam",
        "10-hour",
        "10-hours"
    });
    put("610a374101e07a908b0537f9", new String[]{
        "11 hours usage",
        "11 jam pemakaian",
        "11-hour usage",
        "penggunaan 11 jam",
        "11 jam",
        "11jam",
        "sebelas",
        "sebelas jam",
        "11hr",
        "11hrs",
        "11 hour",
        "11 hours",
        "11-hours",
        "11-hour"
    });
    put("610a379501e07a908b0537fc", new String[]{
        "12 hours usage",
        "12 jam pemakaian",
        "12 hours usage",
        "12-hour usage",
        "penggunaan 12 jam",
        "12hr",
        "12h",
        "12hrs",
        "12 hour",
        "12 hours",
        "12 jam",
        "dua belas jam",
        "12-hours",
        "12-hour"
    });
    put("610a37ce01e07a908b0537fd", new String[]{
        "24 hours usage",
        "24 jam pemakaian",
        "24-hour usage",
        "penggunaan 24 jam",
        "24hr",
        "24 hr",
        "24 h",
        "24 hour",
        "24 hours",
        "24hour",
        "24hours",
        "24 jam",
        "24jam",
        "dua puluh empat jam",
        "24-hours",
        "24-hour",
        "24hrs"
    });
  }});

  private final Map<String , String[]> data;
}
