package com.sanction.thunder.dao.dynamodb;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DynamoDbModuleTest {
  private static final DynamoDbConfiguration DYNAMO_CONFIG = mock(DynamoDbConfiguration.class);

  private final DynamoDbModule module = new DynamoDbModule(DYNAMO_CONFIG);

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

  @Test
  void testProvideTable() {
    DynamoDB dynamo = mock(DynamoDB.class);
    Table table = mock(Table.class);

    when(dynamo.getTable("test-table")).thenReturn(table);

    Table result = module.provideTable(dynamo);

    assertEquals(table, result);
    verify(dynamo, times(1)).getTable("test-table");
  }

}
