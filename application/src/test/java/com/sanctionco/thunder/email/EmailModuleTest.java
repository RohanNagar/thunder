package com.sanctionco.thunder.email;

import com.codahale.metrics.MetricRegistry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailModuleTest {

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class,
        () -> new EmailModule(null));
  }

  @Test
  void testProvideUsersDao() {
    var factory = mock(EmailServiceFactory.class);
    var emailService = mock(EmailService.class);

    when(factory.createEmailService(any(MetricRegistry.class))).thenReturn(emailService);

    EmailModule module = new EmailModule(factory);

    assertEquals(emailService, module.provideEmailService(new MetricRegistry()));
  }

  @Test
  void testProvideDatabaseHealthCheck() {
    var factory = mock(EmailServiceFactory.class);
    var healthCheck = mock(EmailHealthCheck.class);

    when(factory.createHealthCheck()).thenReturn(healthCheck);

    EmailModule module = new EmailModule(factory);

    assertEquals(healthCheck, module.provideEmailHealthCheck());
  }
}
