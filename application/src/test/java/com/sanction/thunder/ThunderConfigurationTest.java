package com.sanction.thunder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.sanction.thunder.authentication.Key;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;

import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThunderConfigurationTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper(new YAMLFactory());

  @Test
  public void testFromYaml() throws Exception {
    ThunderConfiguration configuration = MAPPER.readValue(
        FixtureHelpers.fixture("fixtures/config.yaml"), ThunderConfiguration.class);

    assertEquals("sample-table", configuration.getDynamoTableName());
    assertEquals(
        Arrays.asList(new Key("test-app", "test-secret")),
        configuration.getApprovedKeys());
  }
}
