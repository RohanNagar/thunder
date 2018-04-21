package com.sanction.thunder.validation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PropertyValidationRuleTest {

  @Test
  public void testHashCodeSame() {
    PropertyValidationRule keyOne = new PropertyValidationRule("name", "string");
    PropertyValidationRule keyTwo = new PropertyValidationRule("name", "string");

    assertEquals(keyOne.hashCode(), keyTwo.hashCode());
    assertEquals(keyOne.getName(), keyTwo.getName());
    assertEquals(keyOne.getType(), keyTwo.getType());
  }

  @Test
  public void testHashCodeDifferent() {
    PropertyValidationRule keyOne = new PropertyValidationRule("name", "string");
    PropertyValidationRule keyTwo = new PropertyValidationRule("differentName", "integer");

    assertNotEquals(keyOne.hashCode(), keyTwo.hashCode());
    assertNotEquals(keyOne.getName(), keyTwo.getName());
    assertNotEquals(keyOne.getType(), keyTwo.getType());
  }

  @Test
  public void testEqualsSameObject() {
    PropertyValidationRule keyOne = new PropertyValidationRule("name", "list");

    assertTrue(keyOne.equals(keyOne));
  }

  @Test
  public void testEqualsDifferentObject() {
    PropertyValidationRule keyOne = new PropertyValidationRule("name", "map");
    Object objectTwo = new Object();

    assertFalse(keyOne.equals(objectTwo));
  }

  @Test
  public void testToString() {
    PropertyValidationRule key = new PropertyValidationRule("testName", "string");
    String expected = "PropertyValidationRule [name=testName, type=class java.lang.String]";

    assertEquals(expected, key.toString());
  }
}
