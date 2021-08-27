package com.sanctionco.thunder.authentication.oauth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class OAuthPrincipalTest {

  @Test
  void testHashCodeSame() {
    OAuthPrincipal principal = new OAuthPrincipal("name");
    OAuthPrincipal principalTwo = new OAuthPrincipal("name");

    assertAll("Assert equal principal properties",
        () -> assertEquals(principal.hashCode(), principalTwo.hashCode()),
        () -> assertEquals(principal.getName(), principalTwo.getName()));
  }

  @Test
  void testHashCodeDifferent() {
    OAuthPrincipal principal = new OAuthPrincipal("name");
    OAuthPrincipal principalTwo = new OAuthPrincipal("differentName");

    assertAll("Assert unequal principal properties",
        () -> assertNotEquals(principal.hashCode(), principalTwo.hashCode()),
        () -> assertNotEquals(principal.getName(), principalTwo.getName()));
  }

  @Test
  @SuppressWarnings("ObjectEqualsNull")
  void testEquals() {
    OAuthPrincipal principal = new OAuthPrincipal("name");

    assertAll("Basic equals properties",
        () -> assertNotEquals(null, principal, "Principal must not be equal to null"),
        () -> assertNotEquals(new Object(), principal,
            "Principal must not be equal to another type"),
        () -> assertEquals(principal, principal,
            "Principal must be equal to itself"));

    // Create different OAuthPrincipal object to test against
    OAuthPrincipal differentName = new OAuthPrincipal("badName");

    // Also test against an equal object
    OAuthPrincipal samePrincipal = new OAuthPrincipal("name");

    assertAll("Verify against other created objects",
        () -> assertNotEquals(differentName, principal),
        () -> assertEquals(samePrincipal, principal));
  }

  @Test
  void testToString() {
    OAuthPrincipal principal = new OAuthPrincipal("testName");
    String expected = "OAuthPrincipal[name=testName]";

    assertEquals(expected, principal.toString());
  }
}
