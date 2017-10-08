package com.sanction.thunder.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PilotUserTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private final PilotUser pilotUser = new PilotUser(
      "test@test.com",
      "12345",
      "facebookAccessToken",
      "twitterAccessToken",
      "twitterAccessSecret");

  @Test
  public void testToJson() throws Exception {
    String expected = MAPPER.writeValueAsString(
        MAPPER.readValue(FixtureHelpers.fixture("fixtures/pilot_user.json"), PilotUser.class));

    assertEquals(MAPPER.writeValueAsString(pilotUser), expected);
  }

  @Test
  public void testFromJson() throws Exception {
    PilotUser fromJson = MAPPER.readValue(
        FixtureHelpers.fixture("fixtures/pilot_user.json"), PilotUser.class);

    assertEquals(fromJson, pilotUser);
  }

  @Test
  public void testHashCodeSame() {
    PilotUser userOne = new PilotUser(
        "email",
        "12345",
        "facebookAccessToken",
        "twitterAccessToken",
        "twitterAccessSecret");

    PilotUser userTwo = new PilotUser(
        "email",
        "54321",
        "differentFacebookAccessToken",
        "differentTwitterAccessToken",
        "differentTwitterAccessSecret");

    assertEquals(userOne.hashCode(), userTwo.hashCode());
  }

  @Test
  public void testHashCodeDifferent() {
    PilotUser userOne = new PilotUser(
        "email",
        "12345",
        "facebookAccessToken",
        "twitterAccessToken",
        "twitterAccessSecret");

    PilotUser userTwo = new PilotUser(
        "differentEmail",
        "12345",
        "facebookAccessToken",
        "twitterAccessToken",
        "twitterAccessSecret");

    assertNotEquals(userOne.hashCode(), userTwo.hashCode());
  }

  @Test
  public void testToString() {
    String expected = "PilotUser [email=test@test.com]";

    assertEquals(expected, pilotUser.toString());
  }
}
