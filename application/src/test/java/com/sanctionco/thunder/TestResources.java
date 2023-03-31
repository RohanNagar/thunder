package com.sanctionco.thunder;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import jakarta.validation.Validator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TestResources {
  public static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  public static final Validator VALIDATOR = Validators.newValidator();
  public static final MetricRegistry METRICS = new MetricRegistry();

  private static final Map<String, YamlConfigurationFactory> factories = new ConcurrentHashMap<>();

  public static String getPathOfResource(String resourcePath) {
    try {
      return new File(Resources.getResource(resourcePath).toURI()).getAbsolutePath();
    } catch (Exception e) {
      fail("Failed test, unable to get path of resource file in TestResources.getPathOfResource");

      throw new RuntimeException(e);
    }
  }

  public static String readResourceFile(String resourcePath) {
    try {
      return Resources.toString(Resources.getResource(resourcePath), StandardCharsets.UTF_8);
    } catch (Exception e) {
      fail("Failed test, unable to read resource file in TestResources.readResourceFile");

      throw new RuntimeException(e);
    }
  }

  public static <T> T readResourceYaml(Class<T> clazz,
                                       String resourcePath,
                                       boolean expectConfigException) {
    try {
      return createYamlConfigurationFactory(clazz)
          .build(new File(Resources.getResource(resourcePath).toURI()));
    } catch (Exception e) {
      if (expectConfigException) {
        // If we expect this to fail, make sure it is a ConfigurationValidationException
        assertTrue(e instanceof ConfigurationValidationException);
        return null;
      }

      System.out.println(e.getMessage());

      // Otherwise, we didn't expect to fail
      fail("Failed test, unable to read configuration YAML in TestResources.readResourceYaml");

      throw new RuntimeException(e);
    }
  }

  public static <T> T readResourceYaml(Class<T> clazz, String resourcePath) {
    return readResourceYaml(clazz, resourcePath, false);
  }

  static <T> YamlConfigurationFactory<T> createYamlConfigurationFactory(Class<T> clazz) {
    return factories.computeIfAbsent(clazz.getName(),
        name -> new YamlConfigurationFactory<>(clazz, VALIDATOR, MAPPER, "dw"));
  }
}
