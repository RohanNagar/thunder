package com.sanction.thunder.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;

import java.util.StringJoiner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Email EMAIL = new Email("test@test.com", true, "token");

  @Test
  void testToJson() throws Exception {
    String expected = MAPPER.writeValueAsString(
        MAPPER.readValue(FixtureHelpers.fixture("fixtures/email.json"), Email.class));

    assertEquals(expected, MAPPER.writeValueAsString(EMAIL));
  }

  @Test
  void testFromJson() throws Exception {
    Email fromJson = MAPPER.readValue(FixtureHelpers.fixture("fixtures/email.json"), Email.class);

    assertEquals(EMAIL, fromJson);
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself"})
  void testEqualsSameObject() {
    assertTrue(EMAIL.equals(EMAIL));
  }

  @Test
  @SuppressWarnings("SimplifiableJUnitAssertion")
  void testEqualsDifferentObjectType() {
    Object objectTwo = new Object();

    assertFalse(EMAIL.equals(objectTwo));
  }

  @Test
  void testHashCodeSame() {
    Email emailOne = new Email("test@test.com", true, "token");
    Email emailTwo = new Email("test@test.com", true, "token");

    assertEquals(emailOne.hashCode(), emailTwo.hashCode());
  }

  @Test
  void testHashCodeDifferent() {
    Email emailOne = new Email("test@test.com", true, "token");
    Email emailTwo = new Email("differentTest@test.com", true, "token");

    assertNotEquals(emailOne.hashCode(), emailTwo.hashCode());
  }

  @Test
  void testToString() {
    String expected = new StringJoiner(", ", "Email [", "]")
        .add(String.format("address=%s", "test@test.com"))
        .add(String.format("verified=%b", "true"))
        .add(String.format("verificationToken=%s", "token"))
        .toString();

    assertEquals(expected, EMAIL.toString());
  }
}
