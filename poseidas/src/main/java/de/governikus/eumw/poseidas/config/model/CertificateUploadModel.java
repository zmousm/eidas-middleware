package de.governikus.eumw.poseidas.config.model;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateUploadModel
{

  @NotBlank(message = "May not be empty")
  String name;
}
