package com.sanctionco.thunder.openapi;

import io.dropwizard.views.View;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;

/**
 * Serves the content of Swagger's index page which has been "templatized" to support replacing the
 * directory in which Swagger's static content is located (i.e. JS files) and the path with which
 * requests to resources need to be prefixed.
 */
public class SwaggerView extends View {

  private static final String SWAGGER_URI_PATH = "/swagger-static";

  private final String swaggerAssetsPath;
  private final String contextPath;

  private final SwaggerViewConfiguration viewConfiguration;
  private final SwaggerOAuth2Configuration oauth2Configuration;

  public SwaggerView(
      final String contextRoot,
      final String urlPattern,
      final SwaggerViewConfiguration viewConfiguration,
      final SwaggerOAuth2Configuration oauth2Configuration) {
    super(viewConfiguration.getTemplateUrl(), StandardCharsets.UTF_8);

    String contextRootPrefix = "/".equals(contextRoot) ? "" : contextRoot;

    // swagger-static should be found on the root context
    if (!contextRootPrefix.isEmpty()) {
      swaggerAssetsPath = contextRootPrefix + SWAGGER_URI_PATH;
    } else {
      swaggerAssetsPath =
          (urlPattern.equals("/") ? SWAGGER_URI_PATH : (urlPattern + SWAGGER_URI_PATH));
    }

    contextPath = urlPattern.equals("/") ? contextRootPrefix : (contextRootPrefix + urlPattern);

    this.viewConfiguration = viewConfiguration;
    this.oauth2Configuration = oauth2Configuration;
  }

  /**
   * Returns the title for the browser header
   *
   * @return String
   */
  @Nullable
  public String getTitle() {
    return viewConfiguration.getPageTitle();
  }

  /**
   * Returns the path with which all requests for Swagger's static content need to be prefixed
   *
   * @return String
   */
  public String getSwaggerAssetsPath() {
    return swaggerAssetsPath;
  }

  /**
   * Returns the path with with which all requests made by Swagger's UI to Resources need to be
   * prefixed
   *
   * @return String
   */
  public String getContextPath() {
    return contextPath;
  }

  /**
   * Returns the location of the validator URL or null to disable
   *
   * @return String
   */
  @Nullable
  public String getValidatorUrl() {
    return viewConfiguration.getValidatorUrl();
  }

  /**
   * Returns whether to display the authorization input boxes
   *
   * @return String
   */
  public boolean getShowAuth() {
    return viewConfiguration.isShowAuth();
  }

  /**
   * Returns whether to display the swagger spec selector
   *
   * @return boolean
   */
  public boolean getShowApiSelector() {
    return viewConfiguration.isShowApiSelector();
  }

  /** @return {@link SwaggerOAuth2Configuration} containing every properties to init oauth2 */
  public SwaggerOAuth2Configuration getOauth2Configuration() {
    return oauth2Configuration;
  }
}
