package com.sanctionco.thunder.validation.email;

import com.sanctionco.jmail.Email;
import com.sanctionco.jmail.JMail;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidationRuleTest {
  private static final Email PASSING_EMAIL = JMail.tryParse("startcontain@domain.com").get();
  private static final Email FAILING_EMAIL = JMail.tryParse("strtcontan@invalid.co").get();

  @ParameterizedTest
  @MethodSource("provideRulesForPredicateTest")
  void ensureChecksReturnCorrectPredicate(EmailValidationRule rule) {
    assertTrue(rule.getRule().test(PASSING_EMAIL));
    assertFalse(rule.getRule().test(FAILING_EMAIL));
  }

  private static Stream<Arguments> provideRulesForPredicateTest() {
    return Stream.of(
        Arguments.of(new EmailValidationRule(EmailValidationRule.Check.STARTSWITH, "start")),
        Arguments.of(new EmailValidationRule(EmailValidationRule.Check.ENDSWITH, "com")),
        Arguments.of(new EmailValidationRule(EmailValidationRule.Check.CONTAINS, "contain")),
        Arguments.of(new EmailValidationRule(EmailValidationRule.Check.DOESNOTCONTAIN, "invalid")));
  }

  @Test
  void testHashCodeSame() {
    var ruleOne = new EmailValidationRule(EmailValidationRule.Check.STARTSWITH, "string");
    var ruleTwo = new EmailValidationRule(EmailValidationRule.Check.STARTSWITH, "string");

    assertAll("Assert equal EmailValidationRule properties.",
        () -> assertEquals(ruleOne.hashCode(), ruleTwo.hashCode()),
        () -> assertEquals(ruleOne.getCheck(), ruleTwo.getCheck()),
        () -> assertEquals(ruleOne.getValue(), ruleTwo.getValue()));
  }

  @Test
  void testHashCodeDifferent() {
    var ruleOne = new EmailValidationRule(EmailValidationRule.Check.STARTSWITH, "string");
    var ruleTwo = new EmailValidationRule(EmailValidationRule.Check.ENDSWITH, "integer");

    assertAll("Assert unequal EmailValidationRule properties.",
        () -> assertNotEquals(ruleOne.hashCode(), ruleTwo.hashCode()),
        () -> assertNotEquals(ruleOne.getCheck(), ruleTwo.getCheck()),
        () -> assertNotEquals(ruleOne.getValue(), ruleTwo.getValue()));
  }

  @Test
  @SuppressWarnings("ObjectEqualsNull")
  void testEquals() {
    var rule = new EmailValidationRule(EmailValidationRule.Check.STARTSWITH, "string");

    assertAll("Basic equals properties",
        () -> assertNotEquals(rule, null,
            "EmailValidationRule must not be equal to null"),
        () -> assertNotEquals(rule, new Object(),
            "EmailValidationRule must not be equal to another type"),
        () -> assertEquals(rule, rule, "EmailValidationRule must be equal to itself"));

    // Create different PropertyValidationRule objects to test against
    var differentName = new EmailValidationRule(EmailValidationRule.Check.ENDSWITH, "string");
    var differentType = new EmailValidationRule(EmailValidationRule.Check.STARTSWITH, "unknown");

    // Also test against an equal object
    var sameRule = new EmailValidationRule(EmailValidationRule.Check.STARTSWITH, "string");

    assertAll("Verify against other created objects",
        () -> assertNotEquals(differentName, rule),
        () -> assertNotEquals(differentType, rule),
        () -> assertEquals(sameRule, rule));
  }

  @Test
  void testToString() {
    var rule = new EmailValidationRule(EmailValidationRule.Check.STARTSWITH, "string");
    var expected = "EmailValidationRule [check=STARTSWITH, value=string]";

    assertEquals(expected, rule.toString());
  }
}
