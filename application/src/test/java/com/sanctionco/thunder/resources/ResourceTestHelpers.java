package com.sanctionco.thunder.resources;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Response;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Provides helper methods that are common between Resource classes.
 */
class ResourceTestHelpers {
  private static final User USER = new User(
      Email.unverified("test"), "pass", Collections.emptyMap());

  /**
   * Test that timeouts are set correctly within resource methods.
   *
   * @param operation the operation to run
   * @param counterName the name of the timeout counter that should be incremented
   * @param usersDao the {@link UsersDao} instance that should be used
   */
  public static void runTimeoutTest(Consumer<AsyncResponse> operation,
                                    String counterName,
                                    UsersDao usersDao) {
    var asyncResponse = mock(AsyncResponse.class);
    var handlerCaptor = ArgumentCaptor.forClass(TimeoutHandler.class);

    doNothing().when(asyncResponse).setTimeoutHandler(handlerCaptor.capture());

    doAnswer(ignored -> {
      // Timeout during the find call
      handlerCaptor.getValue().handleTimeout(asyncResponse);
      return CompletableFuture.completedFuture(USER);
    }).when(usersDao).findByEmail(anyString());

    operation.accept(asyncResponse);

    var responseCaptor = ArgumentCaptor.forClass(Response.class);
    verify(asyncResponse, timeout(100).times(2)).resume(responseCaptor.capture());

    var firstResume = responseCaptor.getAllValues().get(0);
    assertEquals(Response.Status.REQUEST_TIMEOUT, firstResume.getStatusInfo());

    assertEquals(1, TestResources.METRICS.counter(counterName).getCount());
  }
}
