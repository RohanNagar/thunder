package com.sanction.thunder.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanction.thunder.models.Email;
import com.sanction.thunder.models.User;

import io.dropwizard.jackson.Jackson;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersDaoTest {
  private final ObjectMapper mapper = Jackson.newObjectMapper();
  private final ObjectMapper mockedMapper = mock(ObjectMapper.class);
  private final User testUser = new User(new Email("test", false, "token"), "password", null);

  @Test
  void testJsonProcessingException() throws Exception {
    when(mockedMapper.writeValueAsString(testUser)).thenThrow(JsonProcessingException.class);

    assertThrows(RuntimeException.class,
        () -> UsersDao.toJson(mockedMapper, testUser));
    verify(mockedMapper, times(1)).writeValueAsString(testUser);
  }

  @Test
  void testIoException() throws Exception {
    String userJson = mapper.writeValueAsString(testUser);

    when(mockedMapper.readValue(userJson, User.class)).thenThrow(IOException.class);

    assertThrows(RuntimeException.class,
        () -> UsersDao.fromJson(mockedMapper, userJson));
    verify(mockedMapper, times(1)).readValue(userJson, User.class);
  }
}
