package com.sanctionco.thunder.validation;

import com.sanctionco.thunder.TestResources;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PropertyValidationRuleTest {

  @Test
  void testFromYaml() {
    PropertyValidationRule mapRule = TestResources.readResourceYaml(
        PropertyValidationRule.class,
        "fixtures/models/property-validation-rule-map.yaml");

    assertAll("PropertyValidationRule properties are correct",
        () -> assertEquals("testMapProperty", mapRule.getName()),
        () -> assertEquals(Map.class, mapRule.getType()));

    PropertyValidationRule defaultRule = TestResources.readResourceYaml(
        PropertyValidationRule.class,
        "fixtures/models/property-validation-rule-default.yaml");

    assertAll("PropertyValidationRule properties are correct",
        () -> assertEquals("testDefaultProperty", defaultRule.getName()),
        () -> assertEquals(Object.class, defaultRule.getType()));
  }

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
  @SuppressWarnings("ObjectEqualsNull")
  void testEquals() {
    PropertyValidationRule rule = new PropertyValidationRule("name", "string");

    assertAll("Basic equals properties",
        () -> assertNotEquals(rule, null,
            "PropertyValidationRule must not be equal to null"),
        () -> assertNotEquals(rule, new Object(),
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
