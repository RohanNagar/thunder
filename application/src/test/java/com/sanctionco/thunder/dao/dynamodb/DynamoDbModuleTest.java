package com.sanctionco.thunder.dao.dynamodb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DynamoDbModuleTest {

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class,
        () -> new DynamoDbModule(null));
  }
}
