package com.sanction.thunder.email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageOptionsTest {

  @Test
  void testHashCodeSame() {
    MessageOptions messageOptionsOne = new MessageOptions(
        "subject", "bodyHtml", "bodyText", "htmlPlaceholder", "textPlaceholder", "successHtml");
    MessageOptions messageOptionsTwo = new MessageOptions(
        "subject", "bodyHtml", "bodyText", "htmlPlaceholder", "textPlaceholder", "successHtml");

    assertEquals(messageOptionsOne.hashCode(), messageOptionsTwo.hashCode());
  }

  @Test
  void testHashCodeDifferent() {
    MessageOptions messageOptionsOne = new MessageOptions(
        "subject", "bodyHtml", "bodyText", "htmlPlaceholder", "textPlaceholder", "successHtml");
    MessageOptions messageOptionsTwo = new MessageOptions(
        "new-subject", "bodyHtml", "bodyText", "htmlPlaceholder", "textPlaceholder", "successHtml");

    assertNotEquals(messageOptionsOne.hashCode(), messageOptionsTwo.hashCode());
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself"})
  void testEqualsSameObject() {
    MessageOptions messageOptions = new MessageOptions(
        "subject", "bodyHtml", "bodyText", "placeholder", "textPlaceholder", "successHtml");

    assertTrue(() -> messageOptions.equals(messageOptions));
  }

  @Test
  @SuppressWarnings("SimplifiableJUnitAssertion")
  void testEqualsDifferentObject() {
    MessageOptions messageOptions = new MessageOptions(
        "subject", "bodyHtml", "bodyText", "placeholder", "textPlaceholder", "successHtml");
    Object objectTwo = new Object();

    assertFalse(() -> messageOptions.equals(objectTwo));
  }

  @Test
  void testToString() {
    MessageOptions messageOptions = new MessageOptions(
        "subject", "bodyHtml", "bodyText", "htmlPlaceholder", "textPlaceholder", "successHtml");
    String expected = "MessageOptions "
        + "[subject=subject, bodyHtml=bodyHtml, bodyText=bodyText, "
        + "bodyHtmlUrlPlaceholder=htmlPlaceholder, bodyTextUrlPlaceholder=textPlaceholder, "
        + "successHtml=successHtml]";

    assertEquals(expected, messageOptions.toString());
  }
}
