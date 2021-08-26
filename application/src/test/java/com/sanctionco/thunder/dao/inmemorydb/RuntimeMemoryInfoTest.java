package com.sanctionco.thunder.dao.inmemorydb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeMemoryInfoTest {

  @Test
  void shouldCallProvidedRuntime() {
    var memoryInfo = new RuntimeMemoryInfo(Runtime.getRuntime());

    assertTrue(memoryInfo.freeMemory() < memoryInfo.maxMemory());
    assertTrue(memoryInfo.freeMemory() < memoryInfo.totalMemory());
    assertTrue(memoryInfo.totalMemory() < memoryInfo.maxMemory());
  }
}
