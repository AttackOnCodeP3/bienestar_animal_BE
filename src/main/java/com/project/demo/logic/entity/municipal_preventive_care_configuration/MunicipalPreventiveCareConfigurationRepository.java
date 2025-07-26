package com.project.demo.logic.entity.municipal_preventive_care_configuration;

import com.project.demo.logic.entity.municipality.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing MunicipalPreventiveCareConfiguration entities.
 * Provides methods to perform CRUD operations and custom queries.
 *
 * @author dgutierrez
 */
public interface MunicipalPreventiveCareConfigurationRepository extends JpaRepository<MunicipalPreventiveCareConfiguration,Long> {
    boolean existsByMunicipalityAndType(Municipality municipality, MunicipalPreventiveCareConfigurationEnum type);
}
