package com.sanctionco.thunder.dao.inmemorydb;

/**
 * A {@link MemoryInfo} implementation that uses the {@link Runtime} instance
 * to get memory information.
 */
public class RuntimeMemoryInfo implements MemoryInfo {
  private final Runtime runtime;

  /**
   * Construct a new instance of {@code RuntimeMemoryInfo}.
   *
   * @param runtime the JVM {@link Runtime} instance to use to query memory state
   */
  public RuntimeMemoryInfo(Runtime runtime) {
    this.runtime = runtime;
  }

  @Override
  public long maxMemory() {
    return runtime.maxMemory();
  }

  @Override
  public long freeMemory() {
    return runtime.freeMemory();
  }

  @Override
  public long totalMemory() {
    return runtime.totalMemory();
  }
}
