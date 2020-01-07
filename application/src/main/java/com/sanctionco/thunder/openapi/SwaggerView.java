package com.sanctionco.thunder.openapi;

import io.dropwizard.views.View;

import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

/**
 * Serves the content of Swagger's index page which has been templatized to support replacing the
 * directory in which Swagger's static content is located (i.e. JS files) and the path with which
 * requests to resources need to be prefixed.
 *
 * <p>The template is defined in {@code resources/com/sanctionco/thunder/openapi/index.ftl}. The
 * FTL file has references to properties that are held in this class. For example,
 * {@code ${swaggerAssetsPath}} is used in the FTL file and it relies on the
 * {@code getSwaggerAssetsPath()} method in this class.
 *
 * <p>Code originally taken from <a href="https://github.com/smoketurner/dropwizard-swagger">
 *   Dropwizard Swagger</a>, with modifications for this project.
 */
public class SwaggerView extends View {
  private static final String SWAGGER_URI_PATH = "/swagger-static";
  private static final String CONTEXT_PATH = "";
  private static final String TITLE = "Thunder Swagger UI";
  private static final String TEMPLATE_PATH = "index.ftl";

  public SwaggerView() {
    super(TEMPLATE_PATH, StandardCharsets.UTF_8);
  }

  /**
   * Returns the title for the browser header.
   *
   * @return the title
   */
  @Nullable
  public String getTitle() {
    return TITLE;
  }

  /**
   * Returns the path with which all requests for Swagger's static content need to be prefixed.
   *
   * @return the path for Swagger static content, defined by {@code SWAGGER_URI_PATH}
   */
  public String getSwaggerAssetsPath() {
    return SWAGGER_URI_PATH;
  }

  /**
   * Returns the path with which all requests made by Swagger's UI to Resources need to be
   * prefixed.
   *
   * @return the path to prefix to requests, defined by {@code CONTEXT_PATH}
   */
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  /**
   * Returns the location of the validator URL or null to disable.
   *
   * @return null for now
   */
  @Nullable
  public String getValidatorUrl() {
    return null;
  }
}
