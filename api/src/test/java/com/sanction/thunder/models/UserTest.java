package com.sanction.thunder.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;

import java.util.Collections;
import java.util.StringJoiner;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class UserTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private final Email email = new Email("test@test.com", true, "hashToken");
  private final Email emailTwo = new Email("testTwo@test.com", true, "hashTokenTwo");

  private final User user = new User(
      email, "12345",
      Collections.singletonMap("facebookAccessToken", "fb"));

  @Test
  public void testToJson() throws Exception {
    String expected = MAPPER.writeValueAsString(
        MAPPER.readValue(FixtureHelpers.fixture("fixtures/user.json"), User.class));

    assertEquals(expected, MAPPER.writeValueAsString(user));
  }

  @Test
  public void testFromJson() throws Exception {
    User fromJson = MAPPER.readValue(
        FixtureHelpers.fixture("fixtures/user.json"), User.class);

    assertEquals(user, fromJson);
  }

  @Test
  public void testEqualsSameObject() {
    assertTrue(user.equals(user));
  }

  @Test
  public void testEqualsDifferentObject() {
    Object objectTwo = new Object();

    assertFalse(user.equals(objectTwo));
  }

  @Test
  public void testHashCodeSame() {
    User userOne = new User(
        email, "12345",
        Collections.singletonMap("facebookAccessToken", "fb"));

    User userTwo = new User(
        email, "54321",
        Collections.singletonMap("facebookAccessToken", "fb"));

    assertEquals(userOne.hashCode(), userTwo.hashCode());
  }

  @Test
  public void testHashCodeDifferent() {
    User userOne = new User(
        email, "12345",
        Collections.singletonMap("facebookAccessToken", "fb"));

    User userTwo = new User(
        emailTwo, "12345",
        Collections.singletonMap("facebookAccessToken", "fb"));

    assertNotEquals(userOne.hashCode(), userTwo.hashCode());
  }

  @Test
  public void testToString() {
    String expected = new StringJoiner(", ", "User [", "]")
            .add(String.format("email=%s", email))
            .add(String.format("password=%s", "12345"))
            .add(String.format("properties=%s",
                Collections.singletonMap("facebookAccessToken", "fb")))
            .toString();

    assertEquals(expected, user.toString());
  }
}
