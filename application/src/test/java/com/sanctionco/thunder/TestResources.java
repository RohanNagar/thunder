package com.sanctionco.thunder;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import javax.validation.Validator;

public class TestResources {
  public static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  public static final Validator VALIDATOR = Validators.newValidator();
  public static final MetricRegistry METRICS = new MetricRegistry();
}
