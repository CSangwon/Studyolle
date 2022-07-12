package com.sangwontest.studyolle.modules.account;

import com.sangwontest.studyolle.modules.zone.Zones;
import lombok.Data;

@Data
public class ZoneForm {

    private String zoneName;
    //TODO Seoul(서울)/none -> 이런 형태로 들어오는 것을 각각 잘라서 Zone에 넣어주는거임

    public String getCityName(){
        return zoneName.substring(0, zoneName.indexOf("("));
    }

    public String getProvinceName(){
        return zoneName.substring(zoneName.indexOf("/") + 1);
    }

    public String getLocalNameOfCity(){
        return zoneName.substring(zoneName.indexOf("(") + 1, zoneName.indexOf(")"));
    }

    public Zones getZone(){
        return Zones.builder()
                .city(this.getCityName())
                .localNameOfCity(this.getLocalNameOfCity())
                .province(this.getProvinceName())
                .build();
    }
}
