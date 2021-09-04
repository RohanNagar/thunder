package com.sanctionco.thunder.validation.email;

import com.sanctionco.thunder.TestResources;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailValidationConfigurationTest {

  @Test
  void testFromYaml() {
    var conf = TestResources.readResourceYaml(
        EmailValidationConfiguration.class,
        "fixtures/configuration/validation/email-validation-conf.yaml");

    assertEquals(4, conf.getRules().size());

    var ruleOne = conf.getRules().get(0);
    assertEquals(EmailValidationRule.Check.STARTSWITH, ruleOne.getCheck());
    assertEquals("test", ruleOne.getValue());

    var ruleTwo = conf.getRules().get(1);
    assertEquals(EmailValidationRule.Check.ENDSWITH, ruleTwo.getCheck());
    assertEquals("com", ruleTwo.getValue());

    var ruleThree = conf.getRules().get(2);
    assertEquals(EmailValidationRule.Check.CONTAINS, ruleThree.getCheck());
    assertEquals("test", ruleThree.getValue());

    var ruleFour = conf.getRules().get(3);
    assertEquals(EmailValidationRule.Check.DOESNOTCONTAIN, ruleFour.getCheck());
    assertEquals("invalid", ruleFour.getValue());
  }
}
