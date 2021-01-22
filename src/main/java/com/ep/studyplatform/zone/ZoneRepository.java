package com.ep.studyplatform.zone;

import com.ep.studyplatform.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone,Long> {
    Zone findByCityAndProvince(String cityName, String provinceName);
}
