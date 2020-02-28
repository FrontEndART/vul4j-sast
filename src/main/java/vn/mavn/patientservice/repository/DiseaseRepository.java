package vn.mavn.patientservice.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import vn.mavn.patientservice.entity.Disease;

public interface DiseaseRepository extends BaseRepository<Disease, Long> {

  @Query("select d from Disease d where d.name = :name")
  Optional<Disease> findByName(String name);

  @Query("select d from Disease d where d.id = :id and d.isActive = true ")
  Disease findDiseaseById(Long id);

  @Query("select d from Disease d where d.id =:id and d.isActive = true")
  Optional<Disease> findActiveById(Long id);

  @Query("select d from Disease d where d.id in :diseaseIds and d.isActive = true")
  List<Disease> findAllByIdIn(List<Long> diseaseIds);
}
