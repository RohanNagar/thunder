package com.sanctionco.thunder.email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
  @SuppressWarnings({"ConstantConditions", "ObjectEqualsNull"})
  void testEquals() {
    MessageOptions messageOptions = new MessageOptions("subject", "bodyHtml", "bodyText",
        "htmlPlaceholder", "textPlaceholder", "successHtml");

    assertAll("Basic equals properties",
        () -> assertTrue(!messageOptions.equals(null),
            "MessageOptions must not be equal to null"),
        () -> assertTrue(!messageOptions.equals(new Object()),
            "MessageOptions must not be equal to another type"),
        () -> assertEquals(messageOptions, messageOptions,
            "MessageOptions must be equal to itself"));

    // Create different User objects to test against
    MessageOptions differentSubject = new MessageOptions("badSubject", "bodyHtml", "bodyText",
        "htmlPlaceholder", "textPlaceholder", "successHtml");
    MessageOptions differentBodyHtml = new MessageOptions("subject", "badBodyHtml", "bodyText",
        "htmlPlaceholder", "textPlaceholder", "successHtml");
    MessageOptions differentBodyText = new MessageOptions("subject", "bodyHtml", "badBodyText",
        "htmlPlaceholder", "textPlaceholder", "successHtml");
    MessageOptions differentHtmlPlaceholder = new MessageOptions("subject", "bodyHtml", "bodyText",
        "badHtmlPlaceholder", "textPlaceholder", "successHtml");
    MessageOptions differentTextPlaceholder = new MessageOptions("subject", "bodyHtml", "bodyText",
        "htmlPlaceholder", "badTextPlaceholder", "successHtml");
    MessageOptions differentSuccessHtml = new MessageOptions("subject", "bodyHtml", "bodyText",
        "htmlPlaceholder", "textPlaceholder", "badSuccessHtml");

    // Also test against an equal object
    MessageOptions sameMessageOptions = new MessageOptions("subject", "bodyHtml", "bodyText",
        "htmlPlaceholder", "textPlaceholder", "successHtml");

    assertAll("Verify against other created objects",
        () -> assertNotEquals(differentSubject, messageOptions),
        () -> assertNotEquals(differentBodyHtml, messageOptions),
        () -> assertNotEquals(differentBodyText, messageOptions),
        () -> assertNotEquals(differentHtmlPlaceholder, messageOptions),
        () -> assertNotEquals(differentTextPlaceholder, messageOptions),
        () -> assertNotEquals(differentSuccessHtml, messageOptions),
        () -> assertEquals(sameMessageOptions, messageOptions));
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
