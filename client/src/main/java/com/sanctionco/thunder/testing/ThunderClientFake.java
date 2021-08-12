package com.sanctionco.thunder.testing;

import com.sanctionco.thunder.ThunderClient;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.ResponseType;
import com.sanctionco.thunder.models.User;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import okhttp3.ResponseBody;

import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Provides an in-memory fake to be used for testing. This client does not
 * make any network calls, but stores user data in-memory.
 */
public class ThunderClientFake implements ThunderClient {
  private final ConcurrentMap<String, User> inMemoryStore = new ConcurrentHashMap<>();

  private final boolean requirePasswordHeader;

  public ThunderClientFake(boolean requirePasswordHeader) {
    this.requirePasswordHeader = requirePasswordHeader;
  }

  @Override
  public CompletableFuture<User> postUser(User user) {
    if (inMemoryStore.containsKey(user.getEmail().getAddress())) {
      return fail(409);
    }

    inMemoryStore.put(user.getEmail().getAddress(), user);
    return CompletableFuture.completedFuture(user);
  }

  @Override
  public CompletableFuture<User> updateUser(User user, String existingEmail, String password) {
    var lookupEmail = Optional.ofNullable(existingEmail)
        .orElse(user.getEmail().getAddress());

    return getUser(lookupEmail, password)
        .thenApply(found -> {
          // Determine what verification information to use for the updated user object.
          boolean verified = lookupEmail.equals(user.getEmail().getAddress())
              && found.getEmail().isVerified();

          String verificationToken = lookupEmail.equals(user.getEmail().getAddress())
              ? found.getEmail().getVerificationToken()
              : null;

          return new User(
              new Email(user.getEmail().getAddress(), verified, verificationToken),
              user.getPassword(),
              user.getProperties());
        }).thenApply(userToInsert -> {
          inMemoryStore.put(userToInsert.getEmail().getAddress(), userToInsert);
          return userToInsert;
        });
  }

  @Override
  public CompletableFuture<User> getUser(String email, String password) {
    var user = inMemoryStore.get(email);

    if (user == null) {
      return fail(404);
    }

    if (requirePasswordHeader && !user.getPassword().equals(password)) {
      return fail(401);
    }

    return CompletableFuture.completedFuture(user);
  }

  @Override
  public CompletableFuture<User> deleteUser(String email, String password) {
    var user = inMemoryStore.get(email);

    if (user == null) {
      return fail(404);
    }

    if (requirePasswordHeader && !user.getPassword().equals(password)) {
      return fail(401);
    }

    inMemoryStore.remove(email);
    return CompletableFuture.completedFuture(user);
  }

  @Override
  public CompletableFuture<User> sendVerificationEmail(String email, String password) {
    // This does not actually send an email, but it does update the verification token of the user.
    var user = inMemoryStore.get(email);

    if (user == null) {
      return fail(404);
    }

    if (requirePasswordHeader && !user.getPassword().equals(password)) {
      return fail(401);
    }

    var updated = new User(
        new Email(
            user.getEmail().getAddress(),
            user.getEmail().isVerified(),
            UUID.randomUUID().toString()),
        user.getPassword(), user.getProperties());

    inMemoryStore.put(email, updated);
    return CompletableFuture.completedFuture(updated);
  }

  @Override
  public CompletableFuture<User> verifyUser(String email, String token) {
    var user = inMemoryStore.get(email);

    if (user == null) {
      return fail(404);
    }

    if (!token.equals(user.getEmail().getVerificationToken())) {
      return fail(400);
    }

    var updated = new User(
        user.getEmail().verifiedCopy(), user.getPassword(), user.getProperties());

    inMemoryStore.put(email, updated);

    return CompletableFuture.completedFuture(updated);
  }

  @Override
  public CompletableFuture<String> verifyUser(String email,
                                              String token,
                                              ResponseType responseType) {
    if (ResponseType.HTML.equals(responseType)) {
      return verifyUser(email, token).thenApply(ignored -> "Verified");
    }

    // else JSON - just return toString
    return verifyUser(email, token).thenApply(User::toString);
  }

  @Override
  public CompletableFuture<User> resetVerificationStatus(String email, String password) {
    var user = inMemoryStore.get(email);

    if (user == null) {
      return fail(404);
    }

    if (requirePasswordHeader && !user.getPassword().equals(password)) {
      return fail(401);
    }

    var updated = new User(Email.unverified(email), user.getPassword(), user.getProperties());

    inMemoryStore.put(email, updated);
    return CompletableFuture.completedFuture(updated);
  }

  private <T> CompletableFuture<T> fail(int responseCode) {
    return CompletableFuture.failedFuture(new HttpException(
        Response.error(responseCode, ResponseBody.create(null, ""))));
  }
}
