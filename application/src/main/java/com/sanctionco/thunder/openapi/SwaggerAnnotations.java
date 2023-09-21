package com.sanctionco.thunder.openapi;

import com.sanctionco.thunder.models.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code SwaggerAnnotations} annotation type contains annotation definitions
 * that are meta-annotations for Swagger annotations. These are defined here to keep
 * methods in {@code UserResource} and {@code VerificationResource} cleaner.
 */
public @interface SwaggerAnnotations {
  String JSON = "application/json";
  String HTML = "text/html";

  // Response codes
  String OK = "200";
  String CREATED = "201";
  String SEE_OTHER = "303";
  String BAD_REQUEST = "400";
  String UNAUTHORIZED = "401";
  String NOT_FOUND = "404";
  String CONFLICT = "409";
  String SERVER_ERROR = "500";
  String SERVICE_UNAVAILABLE = "503";

  // Common response code descriptions
  String CREATED_DESCRIPTION = "The user was successfully created";
  String BAD_REQUEST_DESCRIPTION = "The request was malformed";
  String UNAUTHORIZED_DESCRIPTION = "The request was unauthorized";
  String NOT_FOUND_DESCRIPTION = "The user was not found in the database";
  String SERVER_ERROR_DESCRIPTION = "The request failed for an unknown reason";
  String UNAVAILABLE_DESCRIPTION = "The database is currently unavailable";

  // Common parameter names and descriptions

  // Password header
  String PASSWORD = "password";
  String PASSWORD_DESCRIPTION = "The password of the user, necessary if headerPasswordCheck"
      + " is enabled.";

  // Email query param
  String EMAIL = "email";

  /**
   * Defines meta-annotations for HTTP methods.
   */
  @interface Methods {
    String CREATE_SUMMARY = "Create a new user";
    String CREATE_DESCRIPTION = "Creates a new user in the database and returns the created user.";
    String CREATE_CONFLICT_DESCRIPTION = "The user already exists in the database";
    String CREATE_BODY_DESCRIPTION = "The user object to create";

    /**
     * The POST (create) users method.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(summary = CREATE_SUMMARY, description = CREATE_DESCRIPTION, tags = { "users" },
        responses = {
            @ApiResponse(responseCode = CREATED, description = CREATED_DESCRIPTION,
                content = @Content(
                    mediaType = JSON, schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESCRIPTION),
            @ApiResponse(responseCode = CONFLICT, description = CREATE_CONFLICT_DESCRIPTION),
            @ApiResponse(responseCode = SERVER_ERROR, description = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(responseCode = SERVICE_UNAVAILABLE, description = UNAVAILABLE_DESCRIPTION)
        },
        requestBody = @RequestBody(description = CREATE_BODY_DESCRIPTION, required = true,
            content = @Content(mediaType = JSON, schema = @Schema(implementation = User.class))))
    @interface Create {
    }

    String UPDATE_SUMMARY = "Update an existing user";
    String UPDATE_DESCRIPTION = "Updates an existing user in the database and returns"
        + " the updated user.";
    String UPDATE_OK_DESCRIPTION = "The user was successfully updated";
    String UPDATE_CONFLICT_DESCRIPTION = "A user with the new email address already exists";
    String UPDATE_BODY_DESCRIPTION = "The updated user object to insert";

    String EXISTING_EMAIL_DESC = "The existing email address of the user. Only necessary if"
        + " the email address is to be changed.";

    /**
     * The PUT (update) users method.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(summary = UPDATE_SUMMARY, description = UPDATE_DESCRIPTION, tags = { "users" },
        responses = {
            @ApiResponse(responseCode = OK, description = UPDATE_OK_DESCRIPTION,
                content = @Content(
                    mediaType = JSON, schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESCRIPTION),
            @ApiResponse(responseCode = UNAUTHORIZED, description = UNAUTHORIZED_DESCRIPTION),
            @ApiResponse(responseCode = NOT_FOUND, description = NOT_FOUND_DESCRIPTION),
            @ApiResponse(responseCode = CONFLICT, description = UPDATE_CONFLICT_DESCRIPTION),
            @ApiResponse(responseCode = SERVER_ERROR, description = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(responseCode = SERVICE_UNAVAILABLE, description = UNAVAILABLE_DESCRIPTION)
        },
        parameters = {
            @Parameter(name = PASSWORD, description = PASSWORD_DESCRIPTION, in = ParameterIn.HEADER,
                schema = @Schema(type = "string")),
            @Parameter(name = EMAIL, description = EXISTING_EMAIL_DESC, in = ParameterIn.QUERY,
                schema = @Schema(type = "string"))
        },
        requestBody = @RequestBody(description = UPDATE_BODY_DESCRIPTION, required = true,
            content = @Content(mediaType = JSON, schema = @Schema(implementation = User.class))))
    @interface Update {
    }

    String GET_SUMMARY = "Retrieve a user from the database";
    String GET_DESCRIPTION = "Retrieves a user from the database and returns the user.";
    String GET_OK_DESCRIPTION = "The user was found and returned";

    String GET_EMAIL_DESC = "The email address of the user to retrieve.";

    /**
     * The GET users method.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(summary = GET_SUMMARY, description = GET_DESCRIPTION, tags = { "users" },
        responses = {
            @ApiResponse(responseCode = OK, description = GET_OK_DESCRIPTION,
                content = @Content(
                    mediaType = JSON, schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESCRIPTION),
            @ApiResponse(responseCode = UNAUTHORIZED, description = UNAUTHORIZED_DESCRIPTION),
            @ApiResponse(responseCode = NOT_FOUND, description = NOT_FOUND_DESCRIPTION),
            @ApiResponse(responseCode = SERVICE_UNAVAILABLE, description = UNAVAILABLE_DESCRIPTION)
        },
        parameters = {
            @Parameter(name = PASSWORD, description = PASSWORD_DESCRIPTION, in = ParameterIn.HEADER,
                schema = @Schema(type = "string")),
            @Parameter(name = EMAIL, description = GET_EMAIL_DESC, in = ParameterIn.QUERY,
                schema = @Schema(type = "string"), required = true)
        })
    @interface Get {
    }

    String DELETE_SUMMARY = "Delete a user from the database";
    String DELETE_DESCRIPTION = "Deletes a user from the database and returns the deleted user.";
    String DELETE_OK_DESCRIPTION = "THe user was successfully deleted";

    String DELETE_EMAIL_DESC = "The email address of the user to delete.";

    /**
     * The DELETE users method.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(summary = DELETE_SUMMARY, description = DELETE_DESCRIPTION, tags = { "users" },
        responses = {
            @ApiResponse(responseCode = OK, description = DELETE_OK_DESCRIPTION,
                content = @Content(
                    mediaType = JSON, schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESCRIPTION),
            @ApiResponse(responseCode = UNAUTHORIZED, description = UNAUTHORIZED_DESCRIPTION),
            @ApiResponse(responseCode = NOT_FOUND, description = NOT_FOUND_DESCRIPTION),
            @ApiResponse(responseCode = SERVICE_UNAVAILABLE, description = UNAVAILABLE_DESCRIPTION)
        },
        parameters = {
            @Parameter(name = PASSWORD, description = PASSWORD_DESCRIPTION, in = ParameterIn.HEADER,
                schema = @Schema(type = "string")),
            @Parameter(name = EMAIL, description = DELETE_EMAIL_DESC, in = ParameterIn.QUERY,
                schema = @Schema(type = "string"), required = true)
        })
    @interface Delete {
    }

    String EMAIL_SUMMARY = "Send a verification email to the specified email address";
    String EMAIL_DESCRIPTION = "Initiates the user verification process by sending a verification"
        + " email to the email address provided as a query parameter. The user in the database will"
        + " be updated to include a unique verification token that is sent along with the email.";
    String EMAIL_OK_DESCRIPTION = "The email was successfully sent";

    String SEND_EMAIL_DESC = "The email address of an existing user to send the email to.";

    /**
     * The POST verify (send email) method.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(summary = EMAIL_SUMMARY, description = EMAIL_DESCRIPTION, tags = { "verify" },
        responses = {
            @ApiResponse(responseCode = OK, description = EMAIL_OK_DESCRIPTION,
                content = @Content(
                    mediaType = JSON, schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESCRIPTION),
            @ApiResponse(responseCode = UNAUTHORIZED, description = UNAUTHORIZED_DESCRIPTION),
            @ApiResponse(responseCode = NOT_FOUND, description = NOT_FOUND_DESCRIPTION),
            @ApiResponse(responseCode = SERVER_ERROR, description = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(responseCode = SERVICE_UNAVAILABLE, description = UNAVAILABLE_DESCRIPTION)
        },
        parameters = {
            @Parameter(name = PASSWORD, description = PASSWORD_DESCRIPTION, in = ParameterIn.HEADER,
                schema = @Schema(type = "string")),
            @Parameter(name = EMAIL, description = SEND_EMAIL_DESC, in = ParameterIn.QUERY,
                schema = @Schema(type = "string"), required = true)
        })
    @interface Email {
    }

    String VERIFY_SUMMARY = "Verify a user email address";
    String VERIFY_DESCRIPTION = "Used to verify a user email. Typically, the user will click on"
        + " this link in their email to verify their account. Upon verification, the user object"
        + " in the database will be updated to indicate that the email address is verified.";
    String VERIFY_OK_DESCRIPTION = "The user was successfully verified.";
    String SEE_OTHER_DESCRIPTION = "The user was successfully verified and the request will be"
        + "redirected to /verify/success";

    String TOKEN = "token";
    String RESP_TYPE = "response_type";
    String VERIFY_EMAIL_DESC = "The email address of the user to verify.";
    String VERIFY_TOKEN_DESC = "The verification token that matches the one sent via email.";
    String RESPONSE_TYPE_DESC = "The optional response type, either HTML or JSON. If HTML is"
        + " specified, the URL will redirect to /verify/success.";

    /**
     * The GET verify (verify email) method.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(summary = VERIFY_SUMMARY, description = VERIFY_DESCRIPTION, tags = { "verify" },
        responses = {
            @ApiResponse(responseCode = OK, description = VERIFY_OK_DESCRIPTION,
                content = @Content(
                    mediaType = JSON, schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = SEE_OTHER, description = SEE_OTHER_DESCRIPTION),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_DESCRIPTION),
            @ApiResponse(responseCode = NOT_FOUND, description = NOT_FOUND_DESCRIPTION),
            @ApiResponse(responseCode = SERVER_ERROR, description = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(responseCode = SERVICE_UNAVAILABLE, description = UNAVAILABLE_DESCRIPTION)
        },
        parameters = {
            @Parameter(name = EMAIL, description = VERIFY_EMAIL_DESC, in = ParameterIn.QUERY,
                schema = @Schema(type = "string"), required = true),
            @Parameter(name = TOKEN, description = VERIFY_TOKEN_DESC, in = ParameterIn.QUERY,
                schema = @Schema(type = "string"), required = true),
            @Parameter(name = RESP_TYPE, description = RESPONSE_TYPE_DESC, in = ParameterIn.QUERY,
                schema = @Schema(
                    type = "string", allowableValues = {"json", "html"}, defaultValue = "json"))
        })
    @interface Verify {
    }

    String RESET_SUMMARY = "Reset a user's email verification status";
    String RESET_DESCRIPTION = "Resets the verification status of a user  to unverified.";
    String RESET_OK_DESCRIPTION = "The user's verification status was successfully reset";

    String RESET_EMAIL_DESC = "The email address of the user to reset.";

    /**
     * The POST reset (verification reset) method.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(summary = RESET_SUMMARY, description = RESET_DESCRIPTION, tags = { "verify" },
        responses = {
            @ApiResponse(responseCode = OK, description = RESET_OK_DESCRIPTION,
                content = @Content(
                    mediaType = JSON, schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = BAD_REQUEST, description =  BAD_REQUEST_DESCRIPTION),
            @ApiResponse(responseCode = UNAUTHORIZED, description = UNAUTHORIZED_DESCRIPTION),
            @ApiResponse(responseCode = NOT_FOUND, description = NOT_FOUND_DESCRIPTION),
            @ApiResponse(responseCode = SERVER_ERROR, description = SERVER_ERROR_DESCRIPTION),
            @ApiResponse(responseCode = SERVICE_UNAVAILABLE, description = UNAVAILABLE_DESCRIPTION)
        },
        parameters = {
            @Parameter(name = PASSWORD, description = PASSWORD_DESCRIPTION, in = ParameterIn.HEADER,
                schema = @Schema(type = "string")),
            @Parameter(name = EMAIL, description = RESET_EMAIL_DESC, in = ParameterIn.QUERY,
                schema = @Schema(type = "string"), required = true)
        })
    @interface Reset {
    }

    String SUCCESS_SUMMARY = "Get verification success HTML";
    String SUCCESS_DESCRIPTION = """
        Returns an HTML success page that is shown after a user successfully verifies their account.
         GET /verify will redirect to this URL if the response_type query parameter is set to html.
        """;
    String SUCCESS_OK_DESCRIPTION = "The request was successful";
    String HTML_EXAMPLE = """
        <!DOCTYPE html>
        <html>
          <div class="alert alert-success">
            <div align="center"><strong>Success!</strong><br>Your account has been \
        verified.</div>
          </div>
          <link rel="stylesheet" \
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"/>
        </html>""";


    /**
     * The GET verification success method.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Operation(summary = SUCCESS_SUMMARY, description = SUCCESS_DESCRIPTION, tags = { "verify" },
        responses = {
            @ApiResponse(responseCode = OK, description = SUCCESS_OK_DESCRIPTION,
                content = @Content(mediaType = HTML, schema = @Schema(example = HTML_EXAMPLE)))
        })
    @interface Success {
    }
  }
}
