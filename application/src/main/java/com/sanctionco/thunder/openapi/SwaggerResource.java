package com.sanctionco.thunder.openapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Provides a API method to get Swagger UI for all available endpoints. The methods contained
 * in this class are available at the {@code /swagger} endpoint, and return a HTML
 * {@link io.dropwizard.views.View} in the response.
 *
 * <p>Code originally taken from <a href="https://github.com/smoketurner/dropwizard-swagger">
 *   Dropwizard Swagger</a>, with modifications for this project.
 */
@Path("/swagger")
@Produces(MediaType.TEXT_HTML)
public class SwaggerResource {

  /**
   * Constructs a new {@code SwaggerResource}.
   */
  SwaggerResource() {

  }

  /**
   * Provides the HTML view.
   *
   * @return the HTML view to display
   */
  @GET
  public SwaggerView get() {
    return new SwaggerView();
  }
}
