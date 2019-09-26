package com.sanctionco.thunder.openapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/swagger")
@Produces(MediaType.TEXT_HTML)
public class SwaggerResource {
  private final SwaggerViewConfiguration viewConfiguration;
  private final SwaggerOAuth2Configuration oAuth2Configuration;
  private final String contextRoot;
  private final String urlPattern;

  public SwaggerResource(
      String urlPattern,
      SwaggerViewConfiguration viewConfiguration,
      SwaggerOAuth2Configuration oAuth2Configuration) {
    this.urlPattern = urlPattern;
    this.viewConfiguration = viewConfiguration;
    this.oAuth2Configuration = oAuth2Configuration;
    this.contextRoot = "/";
  }

  public SwaggerResource(
      String urlPattern,
      SwaggerViewConfiguration viewConfiguration,
      SwaggerOAuth2Configuration oAuth2Configuration,
      String contextRoot) {
    this.viewConfiguration = viewConfiguration;
    this.oAuth2Configuration = oAuth2Configuration;
    this.urlPattern = urlPattern;
    this.contextRoot = contextRoot;
  }

  @GET
  public SwaggerView get() {
    return new SwaggerView(contextRoot, urlPattern, viewConfiguration, oAuth2Configuration);
  }
}
