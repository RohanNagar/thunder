package com.sanctionco.thunder.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Email EMAIL = new Email("test@test.com", true, "hashToken");
  private static final String PASSWORD = "12345";
  private static final Map<String, Object> MULTIPLE_PROPERTY_MAP = new TreeMap<>();

  // Test objects should have the same values as in 'resources/fixtures/*_user.json'
  private final User emptyPropertiesUser = new User(EMAIL, PASSWORD, Collections.emptyMap());
  private final User multiplePropertiesUser = new User(EMAIL, PASSWORD, MULTIPLE_PROPERTY_MAP);

  @BeforeAll
  static void setup() {
    MULTIPLE_PROPERTY_MAP.put("customString", "value");
    MULTIPLE_PROPERTY_MAP.put("customInt", 1);
    MULTIPLE_PROPERTY_MAP.put("customDouble", 1.2);
    MULTIPLE_PROPERTY_MAP.put("customBoolean", true);
    MULTIPLE_PROPERTY_MAP.put("customList", Arrays.asList("hello", "world"));
  }

  @Test
  void testNoPropertiesJsonSerialization() throws Exception {
    String expected = MAPPER.writeValueAsString(MAPPER.readValue(
        FixtureHelpers.fixture("fixtures/no_properties_user.json"), User.class));

    assertEquals(expected, MAPPER.writeValueAsString(emptyPropertiesUser));
  }

  @Test
  void testNoPropertiesJsonDeserialization() throws Exception {
    User fromJson = MAPPER.readValue(
        FixtureHelpers.fixture("fixtures/no_properties_user.json"), User.class);

    assertEquals(emptyPropertiesUser, fromJson);
  }

  @Test
  void testEmptyPropertiesJsonSerialization() throws Exception {
    String expected = MAPPER.writeValueAsString(MAPPER.readValue(
        FixtureHelpers.fixture("fixtures/empty_properties_user.json"),User.class));

    assertEquals(expected, MAPPER.writeValueAsString(emptyPropertiesUser));
  }

  @Test
  void testEmptyPropertiesJsonDeserialization() throws Exception {
    User fromJson = MAPPER.readValue(
        FixtureHelpers.fixture("fixtures/empty_properties_user.json"), User.class);

    assertEquals(emptyPropertiesUser, fromJson);
  }

  @Test
  void testMultiplePropertiesJsonSerialization() throws Exception {
    String expected = MAPPER.writeValueAsString(MAPPER.readValue(
        FixtureHelpers.fixture("fixtures/multiple_properties_user.json"), User.class));

    assertEquals(expected, MAPPER.writeValueAsString(multiplePropertiesUser));
  }

  @Test
  void testMultiplePropertiesJsonDeserialization() throws Exception {
    User fromJson = MAPPER.readValue(
        FixtureHelpers.fixture("fixtures/multiple_properties_user.json"), User.class);

    assertEquals(multiplePropertiesUser, fromJson);
  }

  @Test
  @SuppressWarnings({"ConstantConditions", "ObjectEqualsNull"})
  void testEquals() {
    assertAll("Basic equals properties",
        () -> assertTrue(!emptyPropertiesUser.equals(null),
            "User must not be equal to null"),
        () -> assertTrue(!emptyPropertiesUser.equals(new Object()),
            "User must not be equal to another type"),
        () -> assertEquals(emptyPropertiesUser, emptyPropertiesUser,
            "User must be equal to itself"));

    // Create different User objects to test against
    User differentEmail = new User(new Email("bad@test.com", false, "token"),
        PASSWORD, Collections.emptyMap());
    User differentPassword = new User(EMAIL, "54321", Collections.emptyMap());
    User differentProperties = new User(EMAIL, PASSWORD, Collections.singletonMap("Test", "Map"));

    // Also test against an equal object
    User sameUser = new User(EMAIL, PASSWORD, Collections.emptyMap());

    assertAll("Verify against other created objects",
        () -> assertNotEquals(differentEmail, emptyPropertiesUser),
        () -> assertNotEquals(differentPassword, emptyPropertiesUser),
        () -> assertNotEquals(differentProperties, emptyPropertiesUser),
        () -> assertEquals(sameUser, emptyPropertiesUser));
  }

  @Test
  void testHashCodeSame() {
    User userOne = new User(EMAIL, PASSWORD, Collections.singletonMap("customKey", 1));
    User userTwo = new User(EMAIL, PASSWORD, Collections.singletonMap("customKey", 1));

    assertEquals(userOne.hashCode(), userTwo.hashCode());
  }

  @Test
  void testHashCodeDifferent() {
    User userOne = new User(EMAIL, PASSWORD, Collections.singletonMap("customKey", 1));
    User userTwo = new User(EMAIL, PASSWORD, Collections.singletonMap("customKey", 2));

    assertNotEquals(userOne.hashCode(), userTwo.hashCode());
  }

  @Test
  void testToString() {
    String expected = new StringJoiner(", ", "User [", "]")
            .add(String.format("email=%s", EMAIL))
            .add(String.format("password=%s", PASSWORD))
            .add(String.format("properties=%s", MULTIPLE_PROPERTY_MAP))
            .toString();

    assertEquals(expected, multiplePropertiesUser.toString());
  }
}
