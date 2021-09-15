package com.sanctionco.thunder.testing;

import com.sanctionco.thunder.ThunderClient;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.ResponseType;
import com.sanctionco.thunder.models.User;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import retrofit2.HttpException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThunderClientFakeTest {
  private static final String ADDRESS = "test@test.com";
  private static final String PASSWORD = "password";

  @Test
  void ensurePostWorks() {
    var client = ThunderClient.fake();
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    // First post should work
    client.postUser(user).whenComplete((created, throwable) -> {
      assertNull(throwable);
      assertEquals(user, created);
    }).join();

    // Second post should fail
    assertFailure(() -> client.postUser(user).get(), 409);
  }

  @Test
  void ensureGetWorks() {
    var client = ThunderClient.fake();
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    // First get should fail
    assertFailure(() -> client.getUser(ADDRESS, PASSWORD).get(), 404);

    // Create the user and then get should succeed
    client.postUser(user).join();

    var getUser = client.getUser(ADDRESS, PASSWORD).join();

    assertEquals(user, getUser);

    // The wrong password should fail
    assertFailure(() -> client.getUser(ADDRESS, "WRONG").get(), 401);
  }

  @Test
  void ensureGetWorksWithoutPassword() {
    var client = ThunderClient.fake(false);
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    client.postUser(user).join();

    var getUser = client.getUser(ADDRESS, "WRONG").join();

    assertEquals(user, getUser);
  }

  @Test
  void ensureUpdateWorks() {
    var client = ThunderClient.fake();
    var originalUser = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());
    var newUser = new User(Email.unverified(ADDRESS), PASSWORD, Collections.singletonMap("k", 2));

    // Update on nonexistent user should fail
    assertFailure(() -> client.updateUser(newUser, null, PASSWORD).get(), 404);
    assertFailure(() -> client.updateUser(newUser, "?", PASSWORD).get(), 404);

    client.postUser(originalUser).join();

    // Update with incorrect password should fail
    assertFailure(() -> client.updateUser(newUser, null, "WRONG").get(), 401);

    // Update should succeed
    var updatedUser = client.updateUser(newUser, null, PASSWORD).join();

    assertEquals(newUser, updatedUser);

    // Update with new email should succeed
    var newEmailUser = new User(
        Email.unverified("NEW").verifiedCopy(), PASSWORD, Collections.emptyMap());

    var newEmailUpdatedUser = client.updateUser(newEmailUser, ADDRESS, PASSWORD).join();

    assertAll("Properties are correct",
        () -> assertEquals("NEW", newEmailUpdatedUser.getEmail().getAddress()),
        () -> assertFalse(newEmailUpdatedUser.getEmail().isVerified()),
        () -> assertNull(newEmailUpdatedUser.getEmail().getVerificationToken()),
        () -> assertEquals(PASSWORD, newEmailUpdatedUser.getPassword()),
        () -> assertEquals(Collections.emptyMap(), newEmailUpdatedUser.getProperties()));

    // Verify
    var emailedUser = client.sendVerificationEmail("NEW", PASSWORD).join();
    var verifiedUser = client
        .verifyUser("NEW", emailedUser.getEmail().getVerificationToken()).join();

    assertAll("Properties are correct",
        () -> assertEquals("NEW", verifiedUser.getEmail().getAddress()),
        () -> assertTrue(verifiedUser.getEmail().isVerified()),
        () -> assertNotNull(verifiedUser.getEmail().getVerificationToken()),
        () -> assertEquals(PASSWORD, verifiedUser.getPassword()),
        () -> assertEquals(Collections.emptyMap(), verifiedUser.getProperties()));

    // Update again when it's verified to make sure it stays the same
    var finalUserToUpdate = new User(
        newEmailUser.getEmail(), PASSWORD, Collections.singletonMap("k", 2));

    var finalUpdatedUser = client.updateUser(finalUserToUpdate, null, PASSWORD).join();

    assertAll("Properties are correct",
        () -> assertEquals("NEW", finalUpdatedUser.getEmail().getAddress()),
        () -> assertTrue(finalUpdatedUser.getEmail().isVerified()),
        () -> assertNotNull(finalUpdatedUser.getEmail().getVerificationToken()),
        () -> assertEquals(PASSWORD, finalUpdatedUser.getPassword()),
        () -> assertEquals(Collections.singletonMap("k", 2), finalUpdatedUser.getProperties()));
  }

  @Test
  void ensureUpdateWorksWithoutPassword() {
    var client = ThunderClient.fake(false);
    var originalUser = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());
    var newUser = new User(Email.unverified(ADDRESS), PASSWORD, Collections.singletonMap("k", 2));

    client.postUser(originalUser).join();

    // Update with incorrect password should fail
    var updatedUser = client.updateUser(newUser, null, "WRONG").join();

    assertEquals(newUser, updatedUser);
  }

  @Test
  void ensureDeleteWorks() {
    var client = ThunderClient.fake();
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    // First delete should fail
    assertFailure(() -> client.deleteUser(ADDRESS, PASSWORD).get(), 404);

    // Create the user and then delete should succeed
    client.postUser(user).join();

    var deleteUser = client.deleteUser(ADDRESS, PASSWORD).join();

    assertEquals(user, deleteUser);

    // The wrong password should fail
    client.postUser(user).join();
    assertFailure(() -> client.deleteUser(ADDRESS, "WRONG").get(), 401);
  }

  @Test
  void ensureDeleteWorksWithoutPassword() {
    var client = ThunderClient.fake(false);
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    client.postUser(user).join();

    var deleteUser = client.deleteUser(ADDRESS, "WRONG").join();

    assertEquals(user, deleteUser);
  }

  @Test
  void ensureSendEmailWorks() {
    var client = ThunderClient.fake();
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    // First send should fail
    assertFailure(() -> client.sendVerificationEmail(ADDRESS, PASSWORD).get(), 404);

    // Create the user and then send should succeed
    client.postUser(user).join();

    var sentEmailUser = client.sendVerificationEmail(ADDRESS, PASSWORD).join();

    assertAll("Properties are correct",
        () -> assertEquals(ADDRESS, sentEmailUser.getEmail().getAddress()),
        () -> assertFalse(sentEmailUser.getEmail().isVerified()),
        () -> assertNotNull(sentEmailUser.getEmail().getVerificationToken()),
        () -> assertEquals(PASSWORD, sentEmailUser.getPassword()),
        () -> assertEquals(Collections.emptyMap(), sentEmailUser.getProperties()));

    // The wrong password should fail
    assertFailure(() -> client.sendVerificationEmail(ADDRESS, "WRONG").get(), 401);
  }

  @Test
  void ensureSendEmailWorksWithoutPassword() {
    var client = ThunderClient.fake(false);
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    client.postUser(user).join();

    var sentEmailUser = client.sendVerificationEmail(ADDRESS, "WRONG").join();

    assertAll("Properties are correct",
        () -> assertEquals(ADDRESS, sentEmailUser.getEmail().getAddress()),
        () -> assertFalse(sentEmailUser.getEmail().isVerified()),
        () -> assertNotNull(sentEmailUser.getEmail().getVerificationToken()),
        () -> assertEquals(PASSWORD, sentEmailUser.getPassword()),
        () -> assertEquals(Collections.emptyMap(), sentEmailUser.getProperties()));
  }

  @Test
  void ensureVerifyWorks() {
    var client = ThunderClient.fake();
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    // First verify should fail
    assertFailure(() -> client.verifyUser(ADDRESS, "token").get(), 404);

    // Create the user
    client.postUser(user).join();

    // Verify should fail without sending the email
    assertFailure(() -> client.verifyUser(ADDRESS, "token").get(), 400);

    var sentEmailUser = client.sendVerificationEmail(ADDRESS, PASSWORD).join();

    // Verify should fail with wrong token
    assertFailure(() -> client.verifyUser(ADDRESS, "token").get(), 400);

    // Verify should succeed
    var verifiedUser = client
        .verifyUser(ADDRESS, sentEmailUser.getEmail().getVerificationToken()).join();

    assertAll("Properties are correct",
        () -> assertEquals(ADDRESS, verifiedUser.getEmail().getAddress()),
        () -> assertTrue(verifiedUser.getEmail().isVerified()),
        () -> assertNotNull(verifiedUser.getEmail().getVerificationToken()),
        () -> assertEquals(PASSWORD, verifiedUser.getPassword()),
        () -> assertEquals(Collections.emptyMap(), verifiedUser.getProperties()));
  }

  @Test
  void ensureVerifyWorksWithResponseType() {
    var client = ThunderClient.fake();
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    client.postUser(user).join();

    var token = client.sendVerificationEmail(ADDRESS, PASSWORD).join()
        .getEmail().getVerificationToken();

    assertEquals("Verified", client.verifyUser(ADDRESS, token, ResponseType.HTML).join());

    var expectedUserString = new User(
        new Email(ADDRESS, true, token), PASSWORD, Collections.emptyMap()).toString();

    assertEquals(expectedUserString, client.verifyUser(ADDRESS, token, ResponseType.JSON).join());
  }

  @Test
  void ensureResetVerificationWorks() {
    var client = ThunderClient.fake();
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    // First reset should fail
    assertFailure(() -> client.resetVerificationStatus(ADDRESS, PASSWORD).get(), 404);

    // Create the user and then reset should succeed
    client.postUser(user).join();
    client.sendVerificationEmail(ADDRESS, PASSWORD).join();

    var resetUser = client.resetVerificationStatus(ADDRESS, PASSWORD).join();

    assertAll("Properties are correct",
        () -> assertEquals(ADDRESS, resetUser.getEmail().getAddress()),
        () -> assertFalse(resetUser.getEmail().isVerified()),
        () -> assertNull(resetUser.getEmail().getVerificationToken()),
        () -> assertEquals(PASSWORD, resetUser.getPassword()),
        () -> assertEquals(Collections.emptyMap(), resetUser.getProperties()));

    // The wrong password should fail
    assertFailure(() -> client.resetVerificationStatus(ADDRESS, "WRONG").get(), 401);
  }

  @Test
  void ensureResetVerificationWorksWithoutPassword() {
    var client = ThunderClient.fake(false);
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    client.postUser(user).join();
    client.sendVerificationEmail(ADDRESS, PASSWORD).join();

    var resetUser = client.resetVerificationStatus(ADDRESS, "WRONG").join();

    assertAll("Properties are correct",
        () -> assertEquals(ADDRESS, resetUser.getEmail().getAddress()),
        () -> assertFalse(resetUser.getEmail().isVerified()),
        () -> assertNull(resetUser.getEmail().getVerificationToken()),
        () -> assertEquals(PASSWORD, resetUser.getPassword()),
        () -> assertEquals(Collections.emptyMap(), resetUser.getProperties()));
  }

  @Test
  void ensureFakeClientPersists() {
    var client = ThunderClient.fake();
    var user = new User(Email.unverified(ADDRESS), PASSWORD, Collections.emptyMap());

    assertEquals(user, client.postUser(user).join());
    assertEquals(user, client.getUser(ADDRESS, PASSWORD).join());

    var userWithNewProperties = new User(
        new Email(user.getEmail().getAddress(),
            user.getEmail().isVerified(),
            user.getEmail().getVerificationToken()),
        user.getPassword(),
        Collections.singletonMap("prop1", "val1"));

    assertEquals(userWithNewProperties, client.updateUser(userWithNewProperties, PASSWORD).join());
    assertEquals(userWithNewProperties, client.getUser(ADDRESS, PASSWORD).join());

    // Assert correct initial verification state
    assertNull(client.getUser(ADDRESS, PASSWORD).join()
        .getEmail().getVerificationToken());
    assertFalse(client.getUser(ADDRESS, PASSWORD).join()
        .getEmail().isVerified());

    // Send email to set token
    assertNotNull(client.sendVerificationEmail(ADDRESS, PASSWORD).join()
        .getEmail().getVerificationToken());

    var userWithToken = client.getUser(ADDRESS, PASSWORD).join();

    assertNotNull(userWithToken.getEmail().getVerificationToken());
    assertFalse(userWithToken.getEmail().isVerified());

    // Verify user
    assertTrue(client.verifyUser(ADDRESS, userWithToken.getEmail().getVerificationToken()).join()
        .getEmail().isVerified());

    var verifiedUser = client.getUser(ADDRESS, PASSWORD).join();

    assertEquals(userWithToken.getEmail().getVerificationToken(),
        verifiedUser.getEmail().getVerificationToken());
    assertTrue(verifiedUser.getEmail().isVerified());

    // Reset verification
    assertFalse(client.resetVerificationStatus(ADDRESS, PASSWORD).join()
        .getEmail().isVerified());

    var resetUser = client.getUser(ADDRESS, PASSWORD).join();

    assertNull(resetUser.getEmail().getVerificationToken());
    assertFalse(resetUser.getEmail().isVerified());

    assertEquals(resetUser, client.deleteUser(ADDRESS, PASSWORD).join());
    assertFailure(() -> client.getUser(ADDRESS, PASSWORD).get(), 404);
  }

  private void assertFailure(Executable executable, Integer expectedCode) {
    var expectedMessage = "HTTP " + expectedCode.toString();
    var exception = assertThrows(ExecutionException.class, executable);

    assertAll(
        () -> assertTrue(exception.getCause() instanceof HttpException, "Correct cause"),
        () -> assertTrue(exception.getMessage().contains(expectedMessage), "Correct message"));
  }
}
