package com.sanctionco.thunder.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.testing.FixtureHelpers;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class KeyTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final YamlConfigurationFactory<Key> FACTORY
      = new YamlConfigurationFactory<>(Key.class, Validators.newValidator(), MAPPER, "dw");

  // Test object should use the same values
  // as the JSON object in 'resources/fixtures/models/key.json'
  private final Key key = new Key("TestKeyName", "TestKeySecret");

  @Test
  void testJsonSerialization() throws Exception {
    String expected = MAPPER.writeValueAsString(
        MAPPER.readValue(FixtureHelpers.fixture("fixtures/models/key.json"), Key.class));

    assertEquals(expected, MAPPER.writeValueAsString(key));
  }

  @Test
  void testJsonDeserialization() throws Exception {
    Key fromJson = MAPPER.readValue(FixtureHelpers.fixture("fixtures/models/key.json"), Key.class);

    assertEquals(key, fromJson);
  }

  @Test
  void testFromYaml() throws Exception {
    Key key = FACTORY.build(new File(Resources.getResource(
        "fixtures/models/key.yaml").toURI()));

    assertAll("Key properties are correct",
        () -> assertEquals("TestKeyName", key.getName()),
        () -> assertEquals("TestKeySecret", key.getSecret()));
  }

  @Test
  void testHashCodeSame() {
    Key keyOne = new Key("name", "secret");
    Key keyTwo = new Key("name", "secret");

    assertAll("Assert equal key properties",
        () -> assertEquals(keyOne.hashCode(), keyTwo.hashCode()),
        () -> assertEquals(keyOne.getName(), keyTwo.getName()),
        () -> assertEquals(keyOne.getSecret(), keyTwo.getSecret()));
  }

  @Test
  void testHashCodeDifferent() {
    Key keyOne = new Key("name", "secret");
    Key keyTwo = new Key("differentName", "differentSecret");

    assertAll("Assert unequal key properties",
        () -> assertNotEquals(keyOne.hashCode(), keyTwo.hashCode()),
        () -> assertNotEquals(keyOne.getName(), keyTwo.getName()),
        () -> assertNotEquals(keyOne.getSecret(), keyTwo.getSecret()));
  }

  @Test
  @SuppressWarnings({"ConstantConditions", "ObjectEqualsNull"})
  void testEquals() {
    Key key = new Key("testName", "testSecret");

    assertAll("Basic equals properties",
        () -> assertFalse(key.equals(null), "Key must not be equal to null"),
        () -> assertFalse(key.equals(new Object()), "Key must not be equal to another type"),
        () -> assertEquals(key, key, "Key must be equal to itself"));

    // Create different Key objects to test against
    Key differentName = new Key("badName", "testSecret");
    Key differentSecret = new Key("testName", "badSecret");

    // Also test against an equal object
    Key sameKey = new Key("testName", "testSecret");

    assertAll("Verify against other created objects",
        () -> assertNotEquals(differentName, key),
        () -> assertNotEquals(differentSecret, key),
        () -> assertEquals(sameKey, key));
  }

  @Test
  void testToString() {
    Key key = new Key("testKey", "testSecret");
    String expected = "Key [name=testKey, secret=testSecret]";

    assertEquals(expected, key.toString());
  }
}
