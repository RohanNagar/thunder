package com.sanction.thunder.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyValidationRuleTest {

  @Test
  void testHashCodeSame() {
    PropertyValidationRule ruleOne = new PropertyValidationRule("name", "string");
    PropertyValidationRule ruleTwo = new PropertyValidationRule("name", "string");

    assertAll("Assert equal PropertyValidationRule properties.",
        () -> assertEquals(ruleOne.hashCode(), ruleTwo.hashCode()),
        () -> assertEquals(ruleOne.getName(), ruleTwo.getName()),
        () -> assertEquals(ruleOne.getType(), ruleTwo.getType()));
  }

  @Test
  void testHashCodeDifferent() {
    PropertyValidationRule ruleOne = new PropertyValidationRule("name", "string");
    PropertyValidationRule ruleTwo = new PropertyValidationRule("differentName", "integer");

    assertAll("Assert unequal PropertyValidationRule properties.",
        () -> assertNotEquals(ruleOne.hashCode(), ruleTwo.hashCode()),
        () -> assertNotEquals(ruleOne.getName(), ruleTwo.getName()),
        () -> assertNotEquals(ruleOne.getType(), ruleTwo.getType()));
  }

  @Test
  @SuppressWarnings({"ConstantConditions", "ObjectEqualsNull"})
  void testEquals() {
    PropertyValidationRule rule = new PropertyValidationRule("name", "string");

    assertAll("Basic equals properties",
        () -> assertTrue(!rule.equals(null), "PropertyValidationRule must not be equal to null"),
        () -> assertTrue(!rule.equals(new Object()),
            "PropertyValidationRule must not be equal to another type"),
        () -> assertEquals(rule, rule, "PropertyValidationRule must be equal to itself"));

    // Create different PropertyValidationRule objects to test against
    PropertyValidationRule differentName = new PropertyValidationRule("badName", "string");
    PropertyValidationRule differentType = new PropertyValidationRule("name", "unknown");

    // Also test against an equal object
    PropertyValidationRule sameRule = new PropertyValidationRule("name", "string");

    assertAll("Verify against other created objects",
        () -> assertNotEquals(differentName, rule),
        () -> assertNotEquals(differentType, rule),
        () -> assertEquals(sameRule, rule));
  }

  @Test
  void testToString() {
    PropertyValidationRule rule = new PropertyValidationRule("testName", "string");
    String expected = "PropertyValidationRule [name=testName, type=class java.lang.String]";

    assertEquals(expected, rule.toString());
  }
}
