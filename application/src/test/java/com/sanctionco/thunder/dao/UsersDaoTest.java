package com.sanctionco.thunder.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;

import io.dropwizard.jackson.Jackson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersDaoTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final ObjectMapper MOCKED_MAPPER = mock(ObjectMapper.class);
  private static final User TEST_USER = new User(
      new Email("test", false, "token"),
      "password",
      null);

  @Test
  void testJsonProcessingException() throws Exception {
    when(MOCKED_MAPPER.writeValueAsString(TEST_USER)).thenThrow(JsonProcessingException.class);

    assertThrows(RuntimeException.class,
        () -> UsersDao.toJson(MOCKED_MAPPER, TEST_USER));
    verify(MOCKED_MAPPER, times(1)).writeValueAsString(TEST_USER);
  }

  @Test
  void testIoException() throws Exception {
    String userJson = MAPPER.writeValueAsString(TEST_USER);

    when(MOCKED_MAPPER.readValue(userJson, User.class)).thenThrow(JsonProcessingException.class);

    assertThrows(RuntimeException.class,
        () -> UsersDao.fromJson(MOCKED_MAPPER, userJson));
    verify(MOCKED_MAPPER, times(1)).readValue(userJson, User.class);
  }
}
