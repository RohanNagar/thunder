package com.sanctionco.thunder.openapi;

import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/swagger.{type:json|yaml}")
public class BackwardsCompatibleSwaggerResource extends BaseOpenApiResource {
  @Context ServletConfig config;
  @Context Application app;

  public BackwardsCompatibleSwaggerResource() {}

  @GET
  @Produces({"application/json", "application/yaml"})
  @Operation(hidden = true)
  public Response getOpenApi(
      @Context HttpHeaders headers, @Context UriInfo uriInfo, @PathParam("type") String type)
      throws Exception {
    return super.getOpenApi(headers, this.config, this.app, uriInfo, type);
  }
}
