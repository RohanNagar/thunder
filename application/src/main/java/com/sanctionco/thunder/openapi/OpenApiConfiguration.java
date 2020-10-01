package com.sanctionco.thunder.openapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Provides optional configuration options for generating OpenAPI documentation,
 * including enabling/disabling OpenAPI generation. See the {@code ThunderConfiguration}
 * class for more details.
 *
 * <p>For the meaning of all these properties please refer to Swagger documentation or {@link
 * io.swagger.v3.oas.integration.SwaggerConfiguration}
 *
 * <p>Code originally taken from <a href="https://github.com/smoketurner/dropwizard-swagger">
 *   Dropwizard Swagger</a>, with modifications for this project.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiConfiguration {
  private static final Set<String> RESOURCES
      = Collections.singleton("com.sanctionco.thunder.resources");
  private static final String DEFAULT_TITLE = "Thunder API";
  private static final String DEFAULT_VERSION = "3.0.0";
  private static final String DEFAULT_DESCRIPTION = "A fully customizable user management REST API";
  private static final String DEFAULT_LICENSE = "MIT";
  private static final String DEFAULT_LICENSE_URL
      = "https://github.com/RohanNagar/thunder/blob/master/LICENSE.md";

  /**
   * Constructs a new instance of {@code OpenApiConfiguration} with default values.
   */
  public OpenApiConfiguration() {
    this.enabled = true;

    this.title = DEFAULT_TITLE;
    this.version = DEFAULT_VERSION;
    this.description = DEFAULT_DESCRIPTION;
    this.license = DEFAULT_LICENSE;
    this.licenseUrl = DEFAULT_LICENSE_URL;

    this.contact = null;
    this.contactEmail = null;
  }

  @JsonProperty("enabled")
  private final Boolean enabled;

  public boolean isEnabled() {
    return enabled;
  }

  @JsonProperty("title")
  private final String title;

  public String getTitle() {
    return title;
  }

  @JsonProperty("version")
  private final String version;

  public String getVersion() {
    return version;
  }

  @JsonProperty("description")
  private final String description;

  public String getDescription() {
    return description;
  }

  @JsonProperty("contact")
  private final String contact;

  public String getContact() {
    return contact;
  }

  @JsonProperty("contactEmail")
  private final String contactEmail;

  public String getContactEmail() {
    return contactEmail;
  }

  @JsonProperty("license")
  private final String license;

  String getLicense() {
    return license;
  }

  @JsonProperty("licenseUrl")
  private final String licenseUrl;

  String getLicenseUrl() {
    return licenseUrl;
  }

  /**
   * Builds the OpenAPI Swagger configuration for Thunder using the configuration options
   * set in this class.
   *
   * @return the built configuration object
   */
  @JsonIgnore
  public SwaggerConfiguration build() {
    SecurityScheme securityScheme = new SecurityScheme()
        .name("APIKey")
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("TOKEN");

    List<Tag> tags = new ArrayList<>();
    tags.add(new Tag().name("users").description("Operations about users"));
    tags.add(new Tag().name("verify").description("Operations about user verification"));

    OpenAPI oas = new OpenAPI()
        .info(new Info()
            .title(title)
            .version(version)
            .description(description)
            .contact(new Contact().email(contactEmail).name(contact))
            .license(new License().name(license).url(licenseUrl)))
        .externalDocs(new ExternalDocumentation()
            .description("Full Thunder documentation")
            .url("https://thunder-api.readthedocs.io/en/latest/index.html"))
        .tags(tags)
        .schemaRequirement("APIKey", securityScheme)
        .security(Collections.singletonList(new SecurityRequirement().addList("APIKey")));;

    Set<String> exclusions = Collections.singleton("/swagger");

    return new SwaggerConfiguration().openAPI(oas)
        .prettyPrint(true)
        .readAllResources(true)
        .ignoredRoutes(exclusions)
        .resourcePackages(RESOURCES);
  }
}
