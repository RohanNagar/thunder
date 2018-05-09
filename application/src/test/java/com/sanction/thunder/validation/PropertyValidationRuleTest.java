package com.sanction.thunder.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertyValidationRuleTest {

  @Test
  public void testHashCodeSame() {
    PropertyValidationRule ruleOne = new PropertyValidationRule("name", "string");
    PropertyValidationRule ruleTwo = new PropertyValidationRule("name", "string");

    assertAll("Assert equal PropertyValidationRule properties.",
        () -> assertEquals(ruleOne.hashCode(), ruleTwo.hashCode()),
        () -> assertEquals(ruleOne.getName(), ruleTwo.getName()),
        () -> assertEquals(ruleOne.getType(), ruleTwo.getType()));
  }

  @Test
  public void testHashCodeDifferent() {
    PropertyValidationRule ruleOne = new PropertyValidationRule("name", "string");
    PropertyValidationRule ruleTwo = new PropertyValidationRule("differentName", "integer");

    assertAll("Assert unequal PropertyValidationRule properties.",
        () -> assertNotEquals(ruleOne.hashCode(), ruleTwo.hashCode()),
        () -> assertNotEquals(ruleOne.getName(), ruleTwo.getName()),
        () -> assertNotEquals(ruleOne.getType(), ruleTwo.getType()));
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself"})
  public void testEqualsSameObject() {
    PropertyValidationRule ruleOne = new PropertyValidationRule("name", "list");

    assertTrue(ruleOne.equals(ruleOne));
  }

  @Test
  @SuppressWarnings("SimplifiableJUnitAssertion")
  public void testEqualsDifferentObject() {
    PropertyValidationRule ruleOne = new PropertyValidationRule("name", "map");
    Object objectTwo = new Object();

    assertFalse(ruleOne.equals(objectTwo));
  }

  @Test
  public void testToString() {
    PropertyValidationRule rule = new PropertyValidationRule("testName", "string");
    String expected = "PropertyValidationRule [name=testName, type=class java.lang.String]";

    assertEquals(expected, rule.toString());
  }
}
