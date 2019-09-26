package com.sanctionco.thunder.openapi;

import javax.annotation.Nullable;

/**
 * Contains all configurable parameters required to render the SwaggerUI View from the default
 * template
 */
public class SwaggerViewConfiguration {
  private static final String DEFAULT_TITLE = "Swagger UI";
  private static final String DEFAULT_TEMPLATE = "index.ftl";

  @Nullable private String pageTitle;

  @Nullable private String templateUrl;

  @Nullable private String validatorUrl;

  private boolean showApiSelector;
  private boolean showAuth;

  public SwaggerViewConfiguration() {
    this.pageTitle = DEFAULT_TITLE;
    this.templateUrl = DEFAULT_TEMPLATE;
    this.validatorUrl = null;
    this.showApiSelector = true;
    this.showAuth = true;
  }

  @Nullable
  public String getPageTitle() {
    return pageTitle;
  }

  @Nullable
  public String getTemplateUrl() {
    return templateUrl;
  }

  @Nullable
  public String getValidatorUrl() {
    return validatorUrl;
  }

  public boolean isShowApiSelector() {
    return showApiSelector;
  }

  public boolean isShowAuth() {
    return showAuth;
  }
}
