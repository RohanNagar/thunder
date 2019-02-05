package com.sanctionco.thunder.dao.dynamodb;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamoDbModuleTest {
  private static final DynamoDbConfiguration DYNAMO_CONFIG = mock(DynamoDbConfiguration.class);

  @BeforeAll
  static void setup() {
    when(DYNAMO_CONFIG.getEndpoint()).thenReturn("test.dynamo.com");
    when(DYNAMO_CONFIG.getRegion()).thenReturn("test-region-1");
    when(DYNAMO_CONFIG.getTableName()).thenReturn("test-table");
  }

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class,
        () -> new DynamoDbModule(null));
  }
}
