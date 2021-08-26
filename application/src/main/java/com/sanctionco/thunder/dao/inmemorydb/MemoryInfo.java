package com.sanctionco.thunder.dao.inmemorydb;

/**
 * Provides information on the current state of the JVM memory.
 */
public interface MemoryInfo {

  /**
   * Get the maximum amount of memory that the Java virtual machine
   * will attempt to use.  If there is no inherent limit then the value
   * {@link java.lang.Long#MAX_VALUE} will be returned.
   *
   * @return the maximum amount of memory that the virtual machine will
   *         attempt to use, measured in bytes
   */
  long maxMemory();

  /**
   * Get the amount of free memory in the Java Virtual Machine.
   *
   * @return an approximation to the total amount of memory currently
   *         available for future allocated objects, measured in bytes.
   */
  long freeMemory();
}
