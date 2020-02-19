package vn.mavn.patientservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDto {

  private String name;
  private String phone;
  private String address;
  private String description;
  private List<ClinicDto> clinics;

}
