package com.sanctionco.thunder;

import com.sanctionco.thunder.validation.email.EmailValidationConfiguration;
import com.sanctionco.thunder.validation.email.EmailValidationRule;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThunderModuleTest {

  @Test
  void provideEmailValidatorShouldUseCustomRules() {
    var config = mock(ThunderConfiguration.class);
    var emailValidationConfig = mock(EmailValidationConfiguration.class);

    when(config.getEmailValidationConfiguration()).thenReturn(emailValidationConfig);
    when(emailValidationConfig.getRules()).thenReturn(List.of(
        new EmailValidationRule(EmailValidationRule.Check.STARTSWITH, "test"),
        new EmailValidationRule(EmailValidationRule.Check.CONTAINS, "hello")));

    var module = new ThunderModule(TestResources.METRICS, config);

    var emailValidator = module.provideEmailValidator();

    assertTrue(emailValidator.isValid("test@hello.com"));
    assertFalse(emailValidator.isValid("test@test.com"));
  }
}
