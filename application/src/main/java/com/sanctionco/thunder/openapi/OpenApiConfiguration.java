package com.sanctionco.thunder.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Provides optional configuration options for generating OpenAPI documentation,
 * including enabling/disabling OpenAPI generation. See the {@code ThunderConfiguration}
 * class for more details.
 *
 * For the meaning of all these properties please refer to Swagger documentation or {@link
 * io.swagger.v3.oas.integration.SwaggerConfiguration}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiConfiguration {

  /**
   * This is the only property that is required for Swagger to work correctly.
   *
   * <p>It is a comma separated list of the all the packages that contain the {@link
   * io.swagger.v3.oas.annotations.OpenAPIDefinition} annotated resources
   */
  @NotEmpty
  @JsonProperty("resourcePackage")
  private final String resourcePackage = "com.sanctionco.thunder.resources";

  public String getResourcePackage() {
    return resourcePackage;
  }

  @JsonProperty("enabled")
  private final Boolean enabled = true;

  public boolean isEnabled() {
    return enabled;
  }

  @JsonProperty("title")
  private final String title = "Thunder API";

  public String getTitle() {
    return title;
  }

  @JsonProperty("version")
  private final String version = "2.1.0";

  public String getVersion() {
    return version;
  }

  @JsonProperty("description")
  private final String description = "A fully customizable user management REST API.";

  public String getDescription() {
    return description;
  }

  @JsonProperty("contact")
  private final String contact = "Rohan Nagar";

  public String getContact() {
    return contact;
  }

  @JsonProperty("contactEmail")
  private final String contactEmail = "rohannagar11@gmail.com";

  public String getContactEmail() {
    return contactEmail;
  }

  @JsonProperty("license")
  private final String license = "MIT";

  public String getLicense() {
    return license;
  }

  @JsonProperty("licenseUrl")
  private final String licenseUrl = "https://github.com/RohanNagar/thunder/blob/master/LICENSE.md";

  public String getLicenseUrl() {
    return licenseUrl;
  }

  @JsonProperty("view")
  private final SwaggerViewConfiguration swaggerViewConfiguration = new SwaggerViewConfiguration();

  public SwaggerViewConfiguration getSwaggerViewConfiguration() {
    return swaggerViewConfiguration;
  }

  @JsonProperty("oauth2")
  private final SwaggerOAuth2Configuration swaggerOAuth2Configuration = new SwaggerOAuth2Configuration();

  public SwaggerOAuth2Configuration getSwaggerOAuth2Configuration() {
    return swaggerOAuth2Configuration;
  }

  /**
   * For most of the scenarios this property is not needed.
   *
   * <p>This is not a property for Swagger but for bundle to set up Swagger UI correctly. It only
   * needs to be used of the root path or the context path is set programmatically and therefore
   * cannot be derived correctly. The problem arises in that if you set the root path or context
   * path in the run() method in your Application subclass the bundle has already been initialized
   * by that time and so does not know you set the path programmatically.
   */
  @Nullable private String uriPrefix;

  @JsonIgnore
  public SwaggerConfiguration build() {
    if (resourcePackage == null || resourcePackage.isEmpty()) {
      throw new IllegalStateException(
          "Resource package needs to be specified for Swagger to detect annotated resources.");
    }

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

    String[] exclusions = { "/swagger" };

    return new SwaggerConfiguration().openAPI(oas)
        .prettyPrint(true)
        .readAllResources(true)
        .ignoredRoutes(Arrays.stream(exclusions).collect(Collectors.toSet()))
        .resourcePackages(Arrays.stream(resourcePackage.split(",")).collect(Collectors.toSet()));
  }
}
