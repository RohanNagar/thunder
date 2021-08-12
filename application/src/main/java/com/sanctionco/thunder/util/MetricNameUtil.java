package com.sanctionco.thunder.util;

import com.codahale.metrics.MetricRegistry;
import com.sanctionco.thunder.resources.UserResource;

/**
 * Provides static references to metric names used throughout the application.
 */
public class MetricNameUtil {

  private MetricNameUtil() {
  }

  // Timeout counters
  public static String CREATE_TIMEOUTS = MetricRegistry.name(
      UserResource.class, "create.timeouts");
  public static String GET_TIMEOUTS = MetricRegistry.name(
      UserResource.class, "get.timeouts");
  public static String UPDATE_TIMEOUTS = MetricRegistry.name(
      UserResource.class, "update.timeouts");
  public static String DELETE_TIMEOUTS = MetricRegistry.name(
      UserResource.class, "delete.timeouts");
  public static String SEND_EMAIL_TIMEOUTS = MetricRegistry.name(
      UserResource.class, "send-email.timeouts");
  public static String VERIFY_TIMEOUTS = MetricRegistry.name(
      UserResource.class, "verify.timeouts");
  public static String VERIFICATION_RESET_TIMEOUTS = MetricRegistry.name(
      UserResource.class, "verification-reset.timeouts");
}
