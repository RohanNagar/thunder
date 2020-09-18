package com.sanctionco.thunder;

import com.sanctionco.thunder.dao.DatabaseHealthCheck;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.dao.UsersDaoFactory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ThunderApplicationTest {
  private static final ThunderConfiguration CONFIG = mock(ThunderConfiguration.class);

  private static final UsersDaoFactory DAO_FACTORY = mock(UsersDaoFactory.class);
  private static final DatabaseHealthCheck HEALTH_CHECK = mock(DatabaseHealthCheck.class);
  private static final UsersDao USERS_DAO = mock(UsersDao.class);

  private final ThunderApplication application = new ThunderApplication();

//  @BeforeAll
//  static void setup() {
//    when(EMAIL_CONFIG.isEnabled()).thenReturn(true);
//    when(EMAIL_CONFIG.getEndpoint()).thenReturn("http://localhost");
//    when(EMAIL_CONFIG.getRegion()).thenReturn("us-east-1");
//    when(EMAIL_CONFIG.getFromAddress()).thenReturn("testAddress@test.com");
//    when(EMAIL_CONFIG.getMessageOptionsConfiguration()).thenReturn(null);
//
//    when(DAO_FACTORY.createHealthCheck()).thenReturn(HEALTH_CHECK);
//    when(DAO_FACTORY.createUsersDao(any(ObjectMapper.class))).thenReturn(USERS_DAO);
//
//    // ThunderConfiguration NotNull fields
//    when(CONFIG.getApprovedKeys()).thenReturn(new ArrayList<>());
//    when(CONFIG.getUsersDaoFactory()).thenReturn(DAO_FACTORY);
//    when(CONFIG.getEmailServiceFactory()).thenReturn(EMAIL_CONFIG);
//    when(CONFIG.getHashConfiguration()).thenReturn(new PasswordHashConfiguration());
//    when(CONFIG.getOpenApiConfiguration()).thenReturn(new OpenApiConfiguration());
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testInitialize() {
//    var captor = ArgumentCaptor.forClass(OpenApiBundle.class);
//    var bootstrap = mock(Bootstrap.class);
//
//    application.initialize(bootstrap);
//
//    // Verify OpenApiBundle was added
//    verify(bootstrap, times(1)).addBundle(captor.capture());
//
//    // Verify getOpenApiConfiguration works
//    OpenApiConfiguration openApiConfiguration = captor.getValue().getOpenApiConfiguration(CONFIG);
//    assertTrue(openApiConfiguration.isEnabled());
//  }
//
//  @Test
//  void testRun() {
//    var environment = mock(Environment.class);
//    var jersey = mock(JerseyEnvironment.class);
//    var healthChecks = mock(HealthCheckRegistry.class);
//    var metrics = mock(MetricRegistry.class);
//
//    when(environment.jersey()).thenReturn(jersey);
//    when(environment.healthChecks()).thenReturn(healthChecks);
//    when(environment.metrics()).thenReturn(metrics);
//
//    var captor = ArgumentCaptor.forClass(Object.class);
//
//    application.run(CONFIG, environment);
//
//    // Verify register was called on jersey and healthChecks
//    verify(jersey, atLeastOnce()).register(captor.capture());
//    verify(healthChecks, times(1)).register(eq("Database"), any(DatabaseHealthCheck.class));
//    verify(healthChecks, times(1)).register(eq("Email"), any(EmailHealthCheck.class));
//
//    // Make sure each class that should have been registered on jersey was registered
//    List<Object> values = captor.getAllValues();
//
//    assertAll("Assert all objects were registered to Jersey",
//        () -> assertEquals(1,
//            values.stream().filter(v -> v instanceof AuthDynamicFeature).count()),
//        () -> assertEquals(1,
//            values.stream().filter(v -> v instanceof UserResource).count()),
//        () -> assertEquals(1,
//            values.stream().filter(v -> v instanceof VerificationResource).count()));
//  }
//
//  @Test
//  void testRunWithoutVerification() {
//    var environment = mock(Environment.class);
//    var jersey = mock(JerseyEnvironment.class);
//    var healthChecks = mock(HealthCheckRegistry.class);
//    var metrics = mock(MetricRegistry.class);
//    var emailConfig = mock(EmailConfiguration.class);
//    var config = mock(ThunderConfiguration.class);
//
//    when(environment.jersey()).thenReturn(jersey);
//    when(environment.healthChecks()).thenReturn(healthChecks);
//    when(environment.metrics()).thenReturn(metrics);
//
//    when(emailConfig.isEnabled()).thenReturn(false);
//
//    when(config.getApprovedKeys()).thenReturn(new ArrayList<>());
//    when(config.getUsersDaoFactory()).thenReturn(DAO_FACTORY);
//    when(config.getEmailServiceFactory()).thenReturn(emailConfig);
//    when(config.getHashConfiguration()).thenReturn(new PasswordHashConfiguration());
//    when(config.getOpenApiConfiguration()).thenReturn(new OpenApiConfiguration());
//
//    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
//
//    application.run(config, environment);
//
//    // Verify register was called on jersey and healthChecks
//    verify(jersey, atLeastOnce()).register(captor.capture());
//    verify(healthChecks, times(1)).register(eq("Database"), any(DatabaseHealthCheck.class));
//    verify(healthChecks, times(0)).register(eq("Email"), any(EmailHealthCheck.class));
//
//    // Make sure each class that should have been registered on jersey was registered
//    List<Object> values = captor.getAllValues();
//
//    assertAll("Assert all objects were registered to Jersey",
//        () -> assertEquals(1,
//            values.stream().filter(v -> v instanceof AuthDynamicFeature).count()),
//        () -> assertEquals(1,
//            values.stream().filter(v -> v instanceof UserResource).count()),
//        () -> assertEquals(0,
//            values.stream().filter(v -> v instanceof VerificationResource).count()));
//  }
}
