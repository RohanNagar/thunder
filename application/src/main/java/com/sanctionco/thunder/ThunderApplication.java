package com.sanctionco.thunder;

import com.sanctionco.thunder.authentication.Key;
import com.sanctionco.thunder.dao.DaoModule;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbModule;
import com.sanctionco.thunder.email.EmailModule;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
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

/**
 * Starts up the Thunder application. The run method will add resources, health checks,
 * and authenticators to the Jersey servlet in order to start the application. See
 * {@code Application} in the {@code io.dropwizard} module for details on the base class. Also see
 * <a href="https://www.dropwizard.io/1.3.5/docs/manual/core.html#application">the Dropwizard
 * manual</a> to learn more.
 *
 * @see com.sanctionco.thunder.resources.UserResource UserResource
 * @see com.sanctionco.thunder.resources.VerificationResource VerificationResource
 */
public class ThunderApplication extends Application<ThunderConfiguration> {

  public static void main(String[] args) throws Exception {
    new ThunderApplication().run(args);
  }

  @Override
  public void initialize(Bootstrap<ThunderConfiguration> bootstrap) {

  }

  @Override
  public void run(ThunderConfiguration config, Environment env) {
    ThunderComponent component = DaggerThunderComponent.builder()
        .daoModule(new DaoModule(config.getDynamoConfiguration().getTableName()))
        .dynamoDbModule(new DynamoDbModule(config.getDynamoConfiguration()))
        .emailModule(new EmailModule(config.getEmailConfiguration()))
        .thunderModule(new ThunderModule(env.metrics(), config))
        .build();

    // Authentication
    env.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<Key>()
            .setAuthenticator(component.getThunderAuthenticator())
            .setRealm("THUNDER - AUTHENTICATION")
            .buildAuthFilter()));

    env.jersey().register(new AuthValueFactoryProvider.Binder<>(Key.class));

    // HealthChecks
    env.healthChecks().register("DynamoDB", component.getDynamoDbHealthCheck());

    // Resources
    env.jersey().register(component.getUserResource());

    // Only register verification resource if it is enabled
    if (config.getEmailConfiguration().isEnabled()) {
      env.jersey().register(component.getVerificationResource());
    }

    // OpenAPI
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
            .title("Thunder API")
            .version("2.1.0")
            .description("A fully customizable user management REST API.")
            .license(new License().name("MIT")
                .url("https://github.com/RohanNagar/thunder/blob/master/LICENSE.md"))
            .contact(new Contact().email("rohannagar11@gmail.com")))
        .externalDocs(new ExternalDocumentation()
            .description("Full Thunder documentation")
            .url("https://thunder-api.readthedocs.io/en/latest/index.html"))
        .tags(tags)
        .schemaRequirement("APIKey", securityScheme)
        .security(Collections.singletonList(new SecurityRequirement().addList("APIKey")));

    SwaggerConfiguration oasConfig = new SwaggerConfiguration()
        .openAPI(oas)
        .prettyPrint(true)
        .resourcePackages(Collections.singleton("com.sanctionco.thunder.resources"));

    // Only register OpenAPI resource if it is enabled
    if (config.getOpenApiConfiguration().isEnabled()) {
      env.jersey().register(new OpenApiResource().openApiConfiguration(oasConfig));
    }
  }
}
