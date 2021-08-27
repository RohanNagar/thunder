package com.sanctionco.thunder.authentication.basic;

import com.sanctionco.thunder.TestResources;

import io.dropwizard.testing.FixtureHelpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class KeyTest {

  // Test object should use the same values
  // as the JSON object in 'resources/fixtures/models/key.json'
  private final Key key = new Key("TestKeyName", "TestKeySecret");

  @Test
  void testJsonSerialization() throws Exception {
    String expected = TestResources.MAPPER.writeValueAsString(
        TestResources.MAPPER.readValue(
            FixtureHelpers.fixture("fixtures/models/key.json"), Key.class));

    assertEquals(expected, TestResources.MAPPER.writeValueAsString(key));
  }

  @Test
  void testJsonDeserialization() throws Exception {
    Key fromJson = TestResources.MAPPER
        .readValue(FixtureHelpers.fixture("fixtures/models/key.json"), Key.class);

    assertEquals(key, fromJson);
  }

  @Test
  void testFromYaml() {
    Key key = TestResources.readResourceYaml(Key.class, "fixtures/models/key.yaml");

    assertAll("Key properties are correct",
        () -> assertEquals("TestKeyName", key.getName()),
        () -> assertEquals("TestKeySecret", key.secret()));
  }

  @Test
  void testHashCodeSame() {
    Key keyOne = new Key("name", "secret");
    Key keyTwo = new Key("name", "secret");

    assertAll("Assert equal key properties",
        () -> assertEquals(keyOne.hashCode(), keyTwo.hashCode()),
        () -> assertEquals(keyOne.getName(), keyTwo.getName()),
        () -> assertEquals(keyOne.secret(), keyTwo.secret()));
  }

  @Test
  void testHashCodeDifferent() {
    Key keyOne = new Key("name", "secret");
    Key keyTwo = new Key("differentName", "differentSecret");

    assertAll("Assert unequal key properties",
        () -> assertNotEquals(keyOne.hashCode(), keyTwo.hashCode()),
        () -> assertNotEquals(keyOne.getName(), keyTwo.getName()),
        () -> assertNotEquals(keyOne.secret(), keyTwo.secret()));
  }

  @Test
  @SuppressWarnings({"ConstantConditions", "ObjectEqualsNull"})
  void testEquals() {
    Key key = new Key("testName", "testSecret");

    assertAll("Basic equals properties",
        () -> assertFalse(key.equals(null), "Key must not be equal to null"),
        () -> assertFalse(key.equals(new Object()), "Key must not be equal to another type"),
        () -> assertEquals(key, key, "Key must be equal to itself"));

    // Create different Key objects to test against
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
    String expected = "Key[name=testKey, secret=testSecret]";

    assertEquals(expected, key.toString());
  }
}
