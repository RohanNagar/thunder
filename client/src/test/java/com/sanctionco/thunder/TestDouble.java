package com.sanctionco.thunder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a class or method as a test double, which will disable the Javadoc
 * requirement imposed by checkstyle.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
@interface TestDouble {
}
