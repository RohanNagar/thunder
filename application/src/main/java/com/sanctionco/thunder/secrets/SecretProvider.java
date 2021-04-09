package com.sanctionco.thunder.secrets;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.dropwizard.jackson.Discoverable;

import org.apache.commons.text.lookup.StringLookup;

/**
 * Provides the base interface for the {@code SecretFetcher}.
 *
 * <p>This class is to be used within the Dropwizard configuration and provides polymorphic
 * configuration - which allows us to implement the {@code secrets} section of our configuration
 * with multiple configuration classes.
 *
 * <p>The {@code provider} property on the configuration object is used to determine which
 * implementing class to construct.
 *
 * <p>This class must be registered in
 * {@code /resources/META-INF/services/io.dropwizard.jackson.Discoverable}.
 *
 * <p>See the {@code ThunderConfiguration} class for usage.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "provider")
public interface SecretProvider extends Discoverable, StringLookup {

}
