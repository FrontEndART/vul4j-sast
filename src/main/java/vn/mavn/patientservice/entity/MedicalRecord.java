package vn.mavn.patientservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.mavn.patientservice.entity.base.BaseIdEntity;
import vn.mavn.patientservice.entity.listener.EntityListener;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pm_medical_record")
@EntityListeners(EntityListener.class)
public class MedicalRecord extends BaseIdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long patientId;
  private String userCode;
  private LocalDateTime advisoryDate;
  private Long diseaseId;
  private Long advertisingSourceId;
  private String diseaseStatus;
  private String consultingStatusCode;
  private String note;
  private Long clinicId;
  private LocalDateTime examinationDate;
  private Long examinationTimes;
  private Long remedyAmount;
  private String remedyType;
  private String remedies;
  private BigDecimal totalAmount;
  private BigDecimal transferAmount;
  private BigDecimal codAmount;
  private String extraNote;
  private Boolean isActive;
}
