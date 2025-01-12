package de.governikus.eumw.poseidas.config.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCertificateFromKeystoreModel
{

  @NotBlank(message = "May not be empty")
  private String name;

  @NotNull(message = "Please select alias")
  private String alias;

  private String keystore;
}
