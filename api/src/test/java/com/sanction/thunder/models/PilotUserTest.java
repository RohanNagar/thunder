package com.sanction.thunder.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PilotUserTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  @Test
  public void testToJson() throws Exception {
    PilotUser pilotUser = new PilotUser(
        "test@test.com",
        "12345",
        "facebookAccessToken",
        "twitterAccessToken",
        "twitterAccessSecret");

    String expected = MAPPER.writeValueAsString(
        MAPPER.readValue(FixtureHelpers.fixture("fixtures/pilot_user.json"), PilotUser.class));

    assertEquals(MAPPER.writeValueAsString(pilotUser), expected);
  }

  @Test
  public void testFromJson() throws Exception {
    PilotUser pilotUser = new PilotUser(
        "test@test.com",
        "12345",
        "facebookAccessToken",
        "twitterAccessToken",
        "twitterAccessSecret");

    PilotUser fromJson = MAPPER.readValue(
        FixtureHelpers.fixture("fixtures/pilot_user.json"), PilotUser.class);

    assertEquals(fromJson, pilotUser);
  }
}
