package com.sangwontest.studyolle.modules.zone;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zones, Long> {
    Zones findByCityAndProvince(String cityName, String provinceName);
}
