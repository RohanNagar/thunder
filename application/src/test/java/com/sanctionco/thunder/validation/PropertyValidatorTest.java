package com.sanctionco.thunder.validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PropertyValidatorTest {
  private static final List<PropertyValidationRule> VALIDATION_RULES = Arrays.asList(
      new PropertyValidationRule("firstProperty", "string"),
      new PropertyValidationRule("mapProperty", "map"));

  private static final PropertyValidationConfiguration BOTH_TRUE_VALIDATION_CONFIGURATION
      = mock(PropertyValidationConfiguration.class);
  private static final PropertyValidationConfiguration ALLOW_SUBSET_VALIDATION_CONFIGURATION
      = mock(PropertyValidationConfiguration.class);
  private static final PropertyValidationConfiguration ALLOW_SUPERSET_VALIDATION_CONFIGURATION
      = mock(PropertyValidationConfiguration.class);
  private static final PropertyValidationConfiguration BOTH_FALSE_VALIDATION_CONFIGURATION
      = mock(PropertyValidationConfiguration.class);

  private static final Map<String, Object> PARAM_TEST_TYPES = Map.of(
      "string", "testValue",
      "integer", 1,
      "boolean", true,
      "double", 1.5,
      "list", Collections.emptyList(),
      "map", Collections.emptyMap(),
      "object", new Object());

  @BeforeAll
  static void setup() {
    when(BOTH_TRUE_VALIDATION_CONFIGURATION.allowSubset()).thenReturn(true);
    when(BOTH_TRUE_VALIDATION_CONFIGURATION.allowSuperset()).thenReturn(true);
    when(BOTH_TRUE_VALIDATION_CONFIGURATION.getValidationRules()).thenReturn(VALIDATION_RULES);

    when(ALLOW_SUBSET_VALIDATION_CONFIGURATION.allowSubset()).thenReturn(true);
    when(ALLOW_SUBSET_VALIDATION_CONFIGURATION.allowSuperset()).thenReturn(false);
    when(ALLOW_SUBSET_VALIDATION_CONFIGURATION.getValidationRules()).thenReturn(VALIDATION_RULES);

    when(ALLOW_SUPERSET_VALIDATION_CONFIGURATION.allowSubset()).thenReturn(false);
    when(ALLOW_SUPERSET_VALIDATION_CONFIGURATION.allowSuperset()).thenReturn(true);
    when(ALLOW_SUPERSET_VALIDATION_CONFIGURATION.getValidationRules()).thenReturn(VALIDATION_RULES);

    when(BOTH_FALSE_VALIDATION_CONFIGURATION.allowSubset()).thenReturn(false);
    when(BOTH_FALSE_VALIDATION_CONFIGURATION.allowSuperset()).thenReturn(false);
    when(BOTH_FALSE_VALIDATION_CONFIGURATION.getValidationRules()).thenReturn(VALIDATION_RULES);
  }

  @Test
  void nullConfigurationShouldThrow() {
    assertThrows(NullPointerException.class, () -> new PropertyValidator(null));
  }

  /* MARK -- Both allowSubset and allowSuperset are true */

  @Test
  void bothTrueShouldAllowEmptyMap() {
    PropertyValidator validator = new PropertyValidator(BOTH_TRUE_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Collections.emptyMap();

    assertTrue(validator.isValidPropertiesMap(properties));
  }

  @Test
  void bothTrueEmptyConfigurationShouldAllowAny() {
    PropertyValidationConfiguration config = mock(PropertyValidationConfiguration.class);
    when(config.allowSubset()).thenReturn(true);
    when(config.allowSuperset()).thenReturn(true);
    when(config.getValidationRules()).thenReturn(Collections.emptyList());

    PropertyValidator validator = new PropertyValidator(config);
    Map<String, Object> properties = Collections.emptyMap();

    assertTrue(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "anything", 1.0,
        "should", true,
        "work", Collections.emptyList());

    assertTrue(validator.isValidPropertiesMap(properties));
  }

  @Test
  void bothTrueShouldAllowFewerProperties() {
    PropertyValidator validator = new PropertyValidator(BOTH_TRUE_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("firstProperty", "test");

    assertTrue(validator.isValidPropertiesMap(properties));
  }

  @Test
  void bothTrueShouldAllowExtraProperties() {
    PropertyValidator validator = new PropertyValidator(BOTH_TRUE_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("extraProperty", 10);

    assertTrue(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "extraProperty", 10,
        "propertyTwo", false,
        "property100", 1.2);

    assertTrue(validator.isValidPropertiesMap(properties));
  }

  @Test
  void bothTrueShouldFailOnIncorrectTypeForProperty() {
    PropertyValidator validator = new PropertyValidator(BOTH_TRUE_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("firstProperty", 10);

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  /* MARK -- Only allowSubset is true */

  @Test
  void allowSubsetShouldAllowEmptyMap() {
    PropertyValidator validator = new PropertyValidator(ALLOW_SUBSET_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Collections.emptyMap();

    assertTrue(validator.isValidPropertiesMap(properties));
  }

  @Test
  void allowSubsetEmptyConfigurationShouldAllowNone() {
    PropertyValidationConfiguration config = mock(PropertyValidationConfiguration.class);
    when(config.allowSubset()).thenReturn(true);
    when(config.allowSuperset()).thenReturn(false);
    when(config.getValidationRules()).thenReturn(Collections.emptyList());

    PropertyValidator validator = new PropertyValidator(config);
    Map<String, Object> properties = Collections.emptyMap();

    assertTrue(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "anything", 1.0,
        "should", true,
        "not", Collections.emptyMap(),
        "work", Collections.emptyList());

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  @Test
  void allowSubsetShouldAllowFewerProperties() {
    PropertyValidator validator = new PropertyValidator(ALLOW_SUBSET_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("firstProperty", "test");

    assertTrue(validator.isValidPropertiesMap(properties));
  }

  @Test
  void allowSubsetShouldNotAllowExtraProperties() {
    PropertyValidator validator = new PropertyValidator(ALLOW_SUBSET_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("extraProperty", 10);

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "extraProperty", 10,
        "propertyTwo", false,
        "property100", 1.2);

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "firstProperty", "test",
        "mapProperty", Collections.emptyMap(),
        "propertyExtra", 1.2);

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  @Test
  void allowSubsetShouldFailOnIncorrectTypeForProperty() {
    PropertyValidator validator = new PropertyValidator(ALLOW_SUBSET_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("firstProperty", 10);

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  /* MARK -- Only allowSuperset is true */

  @Test
  void allowSupersetShouldNotAllowEmptyMap() {
    PropertyValidator validator = new PropertyValidator(ALLOW_SUPERSET_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Collections.emptyMap();

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  @Test
  void allowSupersetEmptyConfigurationShouldAllowAll() {
    PropertyValidationConfiguration config = mock(PropertyValidationConfiguration.class);
    when(config.allowSubset()).thenReturn(false);
    when(config.allowSuperset()).thenReturn(true);
    when(config.getValidationRules()).thenReturn(Collections.emptyList());

    PropertyValidator validator = new PropertyValidator(config);
    Map<String, Object> properties = Collections.emptyMap();

    assertTrue(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "anything", 1.0,
        "should", true,
        "work", Collections.emptyList());

    assertTrue(validator.isValidPropertiesMap(properties));
  }

  @Test
  void allowSupersetShouldNotAllowFewerProperties() {
    PropertyValidator validator = new PropertyValidator(ALLOW_SUPERSET_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("firstProperty", "test");

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  @Test
  void allowSupersetShouldAllowExtraPropertiesButNoLess() {
    PropertyValidator validator = new PropertyValidator(ALLOW_SUPERSET_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("extraProperty", 10);

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "extraProperty", 10,
        "propertyTwo", false,
        "property100", 1.2);

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "firstProperty", "test",
        "mapProperty", 100,
        "propertyExtra", 1.2);

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "firstProperty", "test",
        "mapProperty", Collections.emptyMap(),
        "propertyExtra", 1.2);

    assertTrue(validator.isValidPropertiesMap(properties));
  }

  @Test
  void allowSupersetShouldFailOnIncorrectTypeForProperty() {
    PropertyValidator validator = new PropertyValidator(ALLOW_SUPERSET_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("firstProperty", 10);

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  /* MARK -- Both allowSubset and allowSuperset are false */

  @Test
  void bothFalseShouldNotAllowEmptyMap() {
    PropertyValidator validator = new PropertyValidator(BOTH_FALSE_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Collections.emptyMap();

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  @Test
  void bothFalseEmptyConfigurationShouldAllowNone() {
    PropertyValidationConfiguration config = mock(PropertyValidationConfiguration.class);
    when(config.allowSubset()).thenReturn(false);
    when(config.allowSuperset()).thenReturn(false);
    when(config.getValidationRules()).thenReturn(Collections.emptyList());

    PropertyValidator validator = new PropertyValidator(config);
    Map<String, Object> properties = Collections.emptyMap();

    assertTrue(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "anything", 1.0,
        "should", true,
        "not", Collections.emptyMap(),
        "work", Collections.emptyList());

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  @Test
  void bothFalseShouldNotAllowFewerProperties() {
    PropertyValidator validator = new PropertyValidator(BOTH_FALSE_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("firstProperty", "test");

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  @Test
  void bothFalseShouldNotAllowExtraProperties() {
    PropertyValidator validator = new PropertyValidator(BOTH_FALSE_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of("extraProperty", 10);

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "extraProperty", 10,
        "propertyTwo", false,
        "property100", 1.2);

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of(
        "firstProperty", "test",
        "mapProperty", Collections.emptyMap(),
        "propertyExtra", 1.2);

    assertFalse(validator.isValidPropertiesMap(properties));
  }

  @Test
  void bothFalseShouldFailOnIncorrectNameForProperty() {
    PropertyValidator validator = new PropertyValidator(BOTH_FALSE_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of(
        "firstProperti", "test",
        "mapProperty", Collections.emptyMap());

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of("firstProperty", "test", "mapProperti", Collections.emptyMap());

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of("firstProperty", "test", "mapProperty", Collections.emptyMap());

    assertTrue(validator.isValidPropertiesMap(properties));
  }

  @Test
  void bothFalseShouldFailOnIncorrectTypeForProperty() {
    PropertyValidator validator = new PropertyValidator(BOTH_FALSE_VALIDATION_CONFIGURATION);
    Map<String, Object> properties = Map.of(
        "firstProperty", 10,
        "mapProperty", Collections.emptyMap());

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of("firstProperty", "test", "mapProperty", 10);

    assertFalse(validator.isValidPropertiesMap(properties));

    properties = Map.of("firstProperty", "test", "mapProperty", Collections.emptyMap());

    assertTrue(validator.isValidPropertiesMap(properties));
  }

  @ParameterizedTest
  @ValueSource(strings = { "string", "integer", "boolean", "double", "list", "map" })
  void mismatchTypeShouldReturnFalse(String type) {
    PropertyValidationConfiguration config = mock(PropertyValidationConfiguration.class);
    when(config.allowSubset()).thenReturn(false);
    when(config.allowSuperset()).thenReturn(false);
    when(config.getValidationRules()).thenReturn(Collections.singletonList(
        new PropertyValidationRule("testProperty", type)));

    PropertyValidator validator = new PropertyValidator(config);

    for (Map.Entry<String, Object> entry : PARAM_TEST_TYPES.entrySet()) {
      if (!type.equals(entry.getKey())) {
        assertFalse(validator.isValidPropertiesMap(
            Collections.singletonMap("testProperty", entry.getValue())));
      }
    }
  }

  @ParameterizedTest
  @ValueSource(strings = { "string", "integer", "boolean", "double", "list", "map" })
  void typeShouldValidate(String type) {
    PropertyValidationConfiguration config = mock(PropertyValidationConfiguration.class);
    when(config.allowSubset()).thenReturn(false);
    when(config.allowSuperset()).thenReturn(false);
    when(config.getValidationRules()).thenReturn(Collections.singletonList(
        new PropertyValidationRule("testProperty", type)));

    PropertyValidator validator = new PropertyValidator(config);

    assertTrue(validator.isValidPropertiesMap(
        Collections.singletonMap("testProperty", PARAM_TEST_TYPES.get(type))));
  }

  @Test
  void testGetType() {
    assertAll("Assert equal return value from getType PropertyValidator method.",
        () -> assertEquals(String.class, PropertyValidator.getType("string")),
        () -> assertEquals(String.class, PropertyValidator.getType("str")),
        () -> assertEquals(Integer.class, PropertyValidator.getType("integer")),
        () -> assertEquals(Integer.class, PropertyValidator.getType("int")),
        () -> assertEquals(Boolean.class, PropertyValidator.getType("boolean")),
        () -> assertEquals(Boolean.class, PropertyValidator.getType("bool")),
        () -> assertEquals(Double.class, PropertyValidator.getType("double")),
        () -> assertEquals(List.class, PropertyValidator.getType("list")),
        () -> assertEquals(Map.class, PropertyValidator.getType("map")),
        () -> assertEquals(Object.class, PropertyValidator.getType("unknown")));
  }
}
