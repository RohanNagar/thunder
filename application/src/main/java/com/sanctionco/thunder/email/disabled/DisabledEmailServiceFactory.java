package com.sanctionco.thunder.email.disabled;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sanctionco.thunder.email.EmailHealthCheck;
import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.email.EmailServiceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeName("none")
public class DisabledEmailServiceFactory extends EmailServiceFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DisabledEmailServiceFactory.class);

  @Override
  public Boolean isEnabled() {
    return false;
  }

  @Override
  public EmailService createEmailService(MetricRegistry metrics) {
    LOG.info("Email is disabled, returning a null EmailService.");

    return null;
  }

  @Override
  public EmailHealthCheck createHealthCheck() {
    LOG.info("Email is disabled, returning a null EmailHealthCheck.");

    return null;
  }
}
