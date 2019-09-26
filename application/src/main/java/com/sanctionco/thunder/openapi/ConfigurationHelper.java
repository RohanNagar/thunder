package com.sanctionco.thunder.openapi;

import io.dropwizard.Configuration;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import java.util.Optional;

/**
 * Wrapper around Dropwizard's configuration and the bundle's config that simplifies getting some
 * information from them.
 */
public class ConfigurationHelper {

  private final Configuration configuration;
  private final OpenApiConfiguration swaggerBundleConfiguration;

  /**
   * Constructor
   *
   * @param configuration Configuration
   * @param swaggerBundleConfiguration Bundle Configuration
   */
  public ConfigurationHelper(
      Configuration configuration, OpenApiConfiguration swaggerBundleConfiguration) {
    this.configuration = configuration;
    this.swaggerBundleConfiguration = swaggerBundleConfiguration;
  }

  public String getJerseyRootPath() {
    final ServerFactory serverFactory = configuration.getServerFactory();

    final Optional<String> rootPath;
    if (serverFactory instanceof SimpleServerFactory) {
      rootPath = ((SimpleServerFactory) serverFactory).getJerseyRootPath();
    } else {
      rootPath = ((DefaultServerFactory) serverFactory).getJerseyRootPath();
    }

    return stripUrlSlashes(rootPath.orElse("/"));
  }

  public String getUrlPattern() {
    final String applicationContextPath = getApplicationContextPath();
    final String rootPath = getJerseyRootPath();

    final String urlPattern;
    if ("/".equals(rootPath) && "/".equals(applicationContextPath)) {
      urlPattern = "/";
    } else if ("/".equals(rootPath) && !"/".equals(applicationContextPath)) {
      urlPattern = applicationContextPath;
    } else if (!"/".equals(rootPath) && "/".equals(applicationContextPath)) {
      urlPattern = rootPath;
    } else {
      urlPattern = applicationContextPath + rootPath;
    }

    return urlPattern;
  }

  public String getSwaggerUriPath() {
    final String jerseyRootPath = getJerseyRootPath();
    final String uriPathPrefix = jerseyRootPath.equals("/") ? "" : jerseyRootPath;
    return uriPathPrefix + "/swagger-static";
  }

  public String getOAuth2RedirectUriPath() {
    final String jerseyRootPath = getJerseyRootPath();
    final String uriPathPrefix = jerseyRootPath.equals("/") ? "" : jerseyRootPath;
    return uriPathPrefix + "/oauth2-redirect.html";
  }

  private String getApplicationContextPath() {
    final ServerFactory serverFactory = configuration.getServerFactory();

    final String applicationContextPath;
    if (serverFactory instanceof SimpleServerFactory) {
      applicationContextPath = ((SimpleServerFactory) serverFactory).getApplicationContextPath();
    } else {
      applicationContextPath = ((DefaultServerFactory) serverFactory).getApplicationContextPath();
    }

    return stripUrlSlashes(applicationContextPath);
  }

  private String stripUrlSlashes(String urlToStrip) {
    if (urlToStrip.endsWith("/*")) {
      urlToStrip = urlToStrip.substring(0, urlToStrip.length() - 1);
    }

    if (!urlToStrip.isEmpty() && urlToStrip.endsWith("/")) {
      urlToStrip = urlToStrip.substring(0, urlToStrip.length() - 1);
    }

    return urlToStrip;
  }
}
