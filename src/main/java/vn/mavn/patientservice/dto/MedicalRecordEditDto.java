package vn.mavn.patientservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordEditDto {

  @NotNull(message = "err-medical-record-id-is-mandatory")
  private Long id;
  @NotNull(message = "err-medical-record-patient-id-is-mandatory")
  private Long patientId;
  @NotNull(message = "err-medical-record-advisory-date-is-mandatory")
  private LocalDateTime advisoryDate;
  @NotNull(message = "err-medical-record-disease-id-is-mandatory")
  private Long diseaseId;
  @NotNull(message = "err-medical-record-advertising-source-id-is-mandatory")
  private Long advertisingSourceId;
  @NotNull(message = "err-medical-record-disease-status-is-mandatory")
  private String diseaseStatus;
  @NotNull(message = "err-medical-record-consulting-status-code-is-mandatory")
  private String consultingStatusCode;
  private String note;
  @NotNull(message = "err-medical-record-patient-clinic-id-is-mandatory")
  private Long clinicId;
  @NotNull(message = "err-medical-record-examination-date-is-mandatory")
  private LocalDateTime examinationDate;
  private Long examinationTimes;
  @NotBlank(message = "err-medical-record-patient-remedy-type-is-mandatory")
  private String remedyType;
  @NotBlank(message = "err-medical-record-remedy-amount-is-mandatory")
  private String remedyAmount;
  @NotBlank(message = "err-medical-record-patient-remedies-is-mandatory")
  private String remedies;
  @NotNull(message = "err-medical-record-patient-total-amount-is-mandatory")
  private BigDecimal totalAmount;
  @NotNull(message = "err-medical-record-patient-transfer-amount-is-mandatory")
  private BigDecimal transferAmount;
  @NotNull(message = "err-medical-record-patient-cod-amount-is-mandatory")
  private BigDecimal codAmount;
  private String extraNote;
  private Boolean isActive;
  private List<MedicineMappingDto> medicineDtos;


}
