package com.sanction.thunder.authentication;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class KeyTest {

  @Test
  public void testHashCodeSame() {
    Key keyOne = new Key("name", "secret");
    Key keyTwo = new Key("name", "secret");

    assertEquals(keyOne.hashCode(), keyTwo.hashCode());
  }

  @Test
  public void testHashCodeDifferent() {
    Key keyOne = new Key("name", "secret");
    Key keyTwo = new Key("differentName", "secret");

    assertNotEquals(keyOne.hashCode(), keyTwo.hashCode());
  }
}
