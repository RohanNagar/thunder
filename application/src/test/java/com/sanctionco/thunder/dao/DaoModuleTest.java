package com.sanctionco.thunder.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DaoModuleTest {

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class,
        () -> new DaoModule(null));
  }

  @Test
  void testProvideUsersDao() {
    var factory = mock(UsersDaoFactory.class);
    var usersDao = mock(UsersDao.class);

    when(factory.createUsersDao(any(ObjectMapper.class))).thenReturn(usersDao);

    DaoModule module = new DaoModule(factory);

    assertEquals(usersDao, module.provideUsersDao(new ObjectMapper()));
  }

  @Test
  void testProvideDatabaseHealthCheck() {
    var factory = mock(UsersDaoFactory.class);
    var healthCheck = mock(DatabaseHealthCheck.class);

    when(factory.createHealthCheck()).thenReturn(healthCheck);

    DaoModule module = new DaoModule(factory);

    assertEquals(healthCheck, module.provideHealthCheck());
  }
}
