package com.sanction.thunder.dynamodb;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;

import javax.ws.rs.core.Response;

public class DynamoException extends Exception {

  public DynamoException() {
    super();
  }

  public DynamoException(String message) {
    super(message);
  }

  public DynamoException(String message, Throwable cause) {
    super(message, cause);
  }

  public DynamoException(Throwable cause) {
    super(cause);
  }

  private Response determineResponse() {
    if (getCause() instanceof ConditionalCheckFailedException) {
      return Response.status(Response.Status.CONFLICT)
          .entity("There was a conflict performing the operation on the database.")
          .build();
    }

    // TODO
    return null;
  }
}
