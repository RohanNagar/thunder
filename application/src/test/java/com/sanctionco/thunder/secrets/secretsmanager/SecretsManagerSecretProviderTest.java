package com.sanctionco.thunder.secrets.secretsmanager;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.secrets.SecretProvider;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecretsManagerSecretProviderTest {

  @Test
  void shouldReturnNullWhenSecretIsNotSet() {
    SecretsManagerClient mockClient = mock(SecretsManagerClient.class);
    when(mockClient.getSecretValue(eq(GetSecretValueRequest.builder().secretId("test").build())))
        .thenThrow(SecretsManagerException.class);

    SecretsManagerClientBuilder mockBuilder = mock(SecretsManagerClientBuilder.class);
    when(mockBuilder.region(any(Region.class))).thenReturn(mockBuilder);
    when(mockBuilder.endpointOverride(any(URI.class))).thenReturn(mockBuilder);
    when(mockBuilder.build()).thenReturn(mockClient);

    SecretProvider secretProvider = TestResources.readResourceYaml(
        SecretProvider.class,
        "fixtures/configuration/secrets/secretsmanager-config.yaml");

    assertTrue(secretProvider instanceof SecretsManagerSecretProvider);

    try (MockedStatic<SecretsManagerClient> secretsManagerMock
             = mockStatic(SecretsManagerClient.class)) {

      secretsManagerMock.when(SecretsManagerClient::builder).thenReturn(mockBuilder);

      var value = secretProvider.lookup("test");

      assertNull(value);
      verify(mockBuilder, times(1)).build();

      // Call again and make sure client was not re-built
      secretProvider.lookup("test");

      verify(mockBuilder, times(1)).build();
    }
  }

  @Test
  void shouldThrowWhenRetryTimesOut() throws Exception {
    SecretsManagerClient mockClient = mock(SecretsManagerClient.class);
    when(mockClient.getSecretValue(eq(GetSecretValueRequest.builder().secretId("test").build())))
        .thenThrow(SdkClientException.class);

    SecretsManagerClientBuilder mockBuilder = mock(SecretsManagerClientBuilder.class);
    when(mockBuilder.region(any(Region.class))).thenReturn(mockBuilder);
    when(mockBuilder.endpointOverride(any(URI.class))).thenReturn(mockBuilder);
    when(mockBuilder.build()).thenReturn(mockClient);

    SecretProvider secretProvider = TestResources.readResourceYaml(
        SecretProvider.class,
        "fixtures/configuration/secrets/secretsmanager-config.yaml");

    assertTrue(secretProvider instanceof SecretsManagerSecretProvider);

    try (MockedStatic<SecretsManagerClient> secretsManagerMock
             = mockStatic(SecretsManagerClient.class)) {

      secretsManagerMock.when(SecretsManagerClient::builder).thenReturn(mockBuilder);

      assertThrows(SdkClientException.class, () -> secretProvider.lookup("test"));
    }
  }

  @Test
  void shouldReadFromSecretsManager() throws Exception {
    SecretsManagerClient mockClient = mock(SecretsManagerClient.class);
    when(mockClient.getSecretValue(eq(GetSecretValueRequest.builder().secretId("test").build())))
        .thenReturn(GetSecretValueResponse.builder().secretString("secretVal").build());

    SecretsManagerClientBuilder mockBuilder = mock(SecretsManagerClientBuilder.class);
    when(mockBuilder.region(any(Region.class))).thenReturn(mockBuilder);
    when(mockBuilder.endpointOverride(any(URI.class))).thenReturn(mockBuilder);
    when(mockBuilder.build()).thenReturn(mockClient);

    SecretProvider secretProvider = TestResources.readResourceYaml(
        SecretProvider.class,
        "fixtures/configuration/secrets/secretsmanager-config.yaml");

    assertTrue(secretProvider instanceof SecretsManagerSecretProvider);

    try (MockedStatic<SecretsManagerClient> secretsManagerMock
             = mockStatic(SecretsManagerClient.class)) {

      secretsManagerMock.when(SecretsManagerClient::builder).thenReturn(mockBuilder);

      var value = secretProvider.lookup("test");

      assertEquals("secretVal", value);
      verify(mockBuilder, times(1)).build();

      // Call again and make sure client was not re-built
      secretProvider.lookup("test");

      verify(mockBuilder, times(1)).build();
    }
  }
}
