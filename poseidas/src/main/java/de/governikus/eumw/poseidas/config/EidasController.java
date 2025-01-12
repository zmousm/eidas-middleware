package de.governikus.eumw.poseidas.config;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.governikus.eumw.config.ConnectorMetadataType;
import de.governikus.eumw.config.ContactType;
import de.governikus.eumw.config.EidasMiddlewareConfig;
import de.governikus.eumw.config.OrganizationType;
import de.governikus.eumw.eidascommon.ContextPaths;
import de.governikus.eumw.poseidas.config.model.forms.EidasConfigModel;
import de.governikus.eumw.poseidas.server.idprovider.config.ConfigurationService;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping(ContextPaths.ADMIN_CONTEXT_PATH + ContextPaths.EIDAS_CONFIG)
@RequiredArgsConstructor
public class EidasController
{

  public static final String INDEX_TEMPLATE = "pages/eidasConfiguration";

  public static final String REDIRECT_TO_INDEX = "redirect:" + ContextPaths.ADMIN_CONTEXT_PATH
                                                 + ContextPaths.EIDAS_CONFIG;

  private final ConfigurationService configurationService;

  @Value("#{'${hsm.type:}' == 'PKCS11'}")
  private boolean isHsmInUse;

  @GetMapping("")
  public String index(Model model, @ModelAttribute String error, @ModelAttribute String msg)
  {
    if (error != null && !error.isBlank())
    {
      model.addAttribute("error", error);
    }
    if (msg != null && !msg.isBlank())
    {
      model.addAttribute("msg", msg);
    }

    model.addAttribute("eidasConfigModel",
                       toConfigModel(configurationService.getConfiguration()
                                                         .map(EidasMiddlewareConfig::getServerUrl)
                                                         .orElse(""),
                                     configurationService.getConfiguration()
                                                         .map(EidasMiddlewareConfig::getEidasConfiguration)
                                                         .orElse(new EidasMiddlewareConfig.EidasConfiguration())));

    return INDEX_TEMPLATE;
  }

  @PostMapping("")
  public String save(@Valid @ModelAttribute EidasConfigModel configModel,
                     BindingResult bindingResult,
                     RedirectAttributes redirectAttributes)
  {


    // Return with error if hsm is not used
    // or hsm is used and there are more errors than at the field "signatureKeyPairName"
    // because the input field for the signature key pair is not displayed when hsm us used
    if (bindingResult.hasErrors() && !(isHsmInUse && bindingResult.getErrorCount() == bindingResult.getFieldErrorCount()
                                       && !bindingResult.getFieldErrors()
                                                        .parallelStream()
                                                        .anyMatch(e -> !"signatureKeyPairName".equals(e.getField()))))
    {
      return INDEX_TEMPLATE;
    }

    final EidasMiddlewareConfig eidasMiddlewareConfig = configurationService.getConfiguration()
                                                                            .orElse(new EidasMiddlewareConfig());
    eidasMiddlewareConfig.setEidasConfiguration(toConfigType(configModel));
    eidasMiddlewareConfig.setServerUrl(configModel.getServerUrl());
    configurationService.saveConfiguration(eidasMiddlewareConfig, false);


    redirectAttributes.addFlashAttribute("msg", "eIDAS configuration saved!");
    return REDIRECT_TO_INDEX;
  }


  private EidasConfigModel toConfigModel(String serverUrl, EidasMiddlewareConfig.EidasConfiguration config)
  {
    if (config.getContactPerson() == null)
    {
      config.setContactPerson(new ContactType());
    }

    if (config.getOrganization() == null)
    {
      config.setOrganization(new OrganizationType());
    }

    return new EidasConfigModel(config.getPublicServiceProviderName(), serverUrl, config.getCountryCode(),
                                config.getContactPerson().getCompany(), config.getContactPerson().getGivenname(),
                                config.getContactPerson().getSurname(), config.getContactPerson().getEmail(),
                                config.getContactPerson().getTelephone(), config.getOrganization().getDisplayname(),
                                config.getOrganization().getName(), config.getOrganization().getLanguage(),
                                config.getOrganization().getUrl(), config.getSignatureKeyPairName(), config.isDoSign());
  }

  private EidasMiddlewareConfig.EidasConfiguration toConfigType(EidasConfigModel model)
  {
    final List<ConnectorMetadataType> metadata = configurationService.getConfiguration()
                                                                     .map(EidasMiddlewareConfig::getEidasConfiguration)
                                                                     .map(EidasMiddlewareConfig.EidasConfiguration::getConnectorMetadata)
                                                                     .orElse(List.of());

    return new EidasMiddlewareConfig.EidasConfiguration(metadata, model.isSignMetadata(), 0, model.getCountryCode(),
                                                        new ContactType(model.getContactPersonCompanyName(),
                                                                        model.getContactPersonName(),
                                                                        model.getContactPersonSurname(),
                                                                        model.getContactPersonMail(),
                                                                        model.getContactPersonTel()),
                                                        new OrganizationType(model.getOrganizationDisplayname(),
                                                                             model.getOrganizationName(),
                                                                             model.getOrganizationLanguage(),
                                                                             model.getOrganizationUrl()),
                                                        model.getPublicServiceProviderName(), null,
                                                        model.getSignatureKeyPairName(),
                                                        configurationService.getConfiguration()
                                                                            .map(EidasMiddlewareConfig::getEidasConfiguration)
                                                                            .map(EidasMiddlewareConfig.EidasConfiguration::getMetadataSignatureVerificationCertificateName)
                                                                            .orElse(null));
  }
}
