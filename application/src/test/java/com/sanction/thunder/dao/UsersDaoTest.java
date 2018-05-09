package com.sanction.thunder.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanction.thunder.models.Email;
import com.sanction.thunder.models.User;

import io.dropwizard.jackson.Jackson;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsersDaoTest {
  private final ObjectMapper mapper = Jackson.newObjectMapper();
  private final ObjectMapper mockedMapper = mock(ObjectMapper.class);
  private final User testUser = new User(new Email("test", false, "token"), "password", null);

  @Test
  public void testJsonProcessingException() throws Exception {
    when(mockedMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

    assertThrows(RuntimeException.class, () -> {
      UsersDao.toJson(mockedMapper, testUser);
    });
  }

  @Test
  public void testIoException() throws Exception {
    when(mockedMapper.readValue(any(String.class), eq(User.class))).thenThrow(IOException.class);

    assertThrows(RuntimeException.class, () -> {
      UsersDao.fromJson(mockedMapper, mapper.writeValueAsString(testUser));
    });
  }
}
