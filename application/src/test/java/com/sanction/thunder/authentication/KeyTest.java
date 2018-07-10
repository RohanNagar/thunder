package com.sanction.thunder.authentication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyTest {

  @Test
  void testHashCodeSame() {
    Key keyOne = new Key("name", "secret");
    Key keyTwo = new Key("name", "secret");

    assertAll("Assert equal key properties",
        () -> assertEquals(keyOne.hashCode(), keyTwo.hashCode()),
        () -> assertEquals(keyOne.getName(), keyTwo.getName()),
        () -> assertEquals(keyOne.getSecret(), keyTwo.getSecret()));
  }

  @Test
  void testHashCodeDifferent() {
    Key keyOne = new Key("name", "secret");
    Key keyTwo = new Key("differentName", "differentSecret");

    assertAll("Assert unequal key properties",
        () -> assertNotEquals(keyOne.hashCode(), keyTwo.hashCode()),
        () -> assertNotEquals(keyOne.getName(), keyTwo.getName()),
        () -> assertNotEquals(keyOne.getSecret(), keyTwo.getSecret()));
  }

  @Test
  @SuppressWarnings({"ConstantConditions", "ObjectEqualsNull"})
  void testEquals() {
    Key key = new Key("testName", "testSecret");

    assertAll("Basic equals properties",
        () -> assertTrue(!key.equals(null), "Key must not be equal to null"),
        () -> assertTrue(!key.equals(new Object()), "Key must not be equal to another type"),
        () -> assertEquals(key, key, "Key must be equal to itself"));

    // Create different User objects to test against
    Key differentName = new Key("badName", "testSecret");
    Key differentSecret = new Key("testName", "badSecret");

    // Also test against an equal object
    Key sameKey = new Key("testName", "testSecret");

    assertAll("Verify against other created objects",
        () -> assertNotEquals(differentName, key),
        () -> assertNotEquals(differentSecret, key),
        () -> assertEquals(sameKey, key));
  }

  @Test
  void testToString() {
    Key key = new Key("testKey", "testSecret");
    String expected = "Key [name=testKey, secret=testSecret]";

    assertEquals(expected, key.toString());
  }
}
