package com.sanctionco.thunder.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;

import java.util.StringJoiner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  // Test object should use the same values as the JSON object in 'resources/fixtures/email.json'
  private final Email email = new Email("test@test.com", true, "token");

  @Test
  void shouldSerializeToJson() throws Exception {
    String expected = MAPPER.writeValueAsString(
        MAPPER.readValue(FixtureHelpers.fixture("fixtures/email.json"), Email.class));

    assertEquals(expected, MAPPER.writeValueAsString(email));
  }

  @Test
  void shouldDeserializeFromJson() throws Exception {
    Email fromJson = MAPPER.readValue(FixtureHelpers.fixture("fixtures/email.json"), Email.class);

    assertEquals(email, fromJson);
  }

  @Test
  @SuppressWarnings({"ConstantConditions", "ObjectEqualsNull"})
  void equalsShouldWorkCorrectly() {
    assertAll("Basic equals properties",
        () -> assertTrue(!email.equals(null), "Email must not be equal to null"),
        () -> assertTrue(!email.equals(new Object()), "Email must not be equal to another type"),
        () -> assertEquals(email, email, "Email must be equal to itself"));

    // Create different Email objects to test against
    Email differentAddress = new Email("bad@email.com", true, "token");
    Email differentVerified = new Email("test@test.com", false, "token");
    Email differentToken = new Email("test@test.com", true, "badToken");

    // Also test against an equal object
    Email sameEmail = new Email("test@test.com", true, "token");

    assertAll("Verify against other created objects",
        () -> assertNotEquals(differentAddress, email),
        () -> assertNotEquals(differentVerified, email),
        () -> assertNotEquals(differentToken, email),
        () -> assertEquals(sameEmail, email));
  }

  @Test
  void hashCodeShouldBeConsistent() {
    Email emailOne = new Email("test@test.com", true, "token");
    Email emailTwo = new Email("test@test.com", true, "token");

    assertEquals(emailOne.hashCode(), emailTwo.hashCode());
  }

  @Test
  void hashCodeShouldNotCollide() {
    Email emailOne = new Email("test@test.com", true, "token");
    Email emailTwo = new Email("differentTest@test.com", true, "token");

    assertNotEquals(emailOne.hashCode(), emailTwo.hashCode());
  }

  @Test
  void toStringShouldBeCorrect() {
    String expected = new StringJoiner(", ", "Email [", "]")
        .add(String.format("address=%s", "test@test.com"))
        .add(String.format("verified=%b", "true"))
        .add(String.format("verificationToken=%s", "token"))
        .toString();

    assertEquals(expected, email.toString());
  }
}
