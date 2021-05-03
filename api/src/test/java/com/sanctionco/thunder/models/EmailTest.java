package com.sanctionco.thunder.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;

import java.util.StringJoiner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        () -> assertFalse(email.equals(null), "Email must not be equal to null"),
        () -> assertFalse(email.equals(new Object()), "Email must not be equal to another type"),
        () -> assertEquals(email, email, "Email must be equal to itself"));

    // Create different Email objects to test against
    var differentAddress = new Email("bad@email.com", true, "token");
    var differentVerified = new Email("test@test.com", false, "token");
    var differentToken = new Email("test@test.com", true, "badToken");

    // Also test against an equal object
    var sameEmail = new Email("test@test.com", true, "token");

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

  @Test
  void unverifiedShouldCreateUnverifiedEmail() {
    Email created = Email.unverified("test@test.com");

    assertAll("Unverified email has correct properties",
        () -> assertEquals("test@test.com", created.getAddress()),
        () -> assertFalse(created.isVerified()),
        () -> assertNull(created.getVerificationToken()));
  }

  @Test
  void verifiedCopyShouldBeCorrect() {
    Email created = Email.unverified("test@test.com");
    Email verified = created.verifiedCopy();

    assertAll("Unverified email has correct properties",
        () -> assertEquals("test@test.com", verified.getAddress()),
        () -> assertTrue(verified.isVerified()),
        () -> assertNull(verified.getVerificationToken()));
  }
}
