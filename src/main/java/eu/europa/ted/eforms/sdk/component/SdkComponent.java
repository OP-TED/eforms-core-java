package eu.europa.ted.eforms.sdk.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an SDK component implementation for specific SDK versions.
 * Each annotated class must correspond to a specific component type and can optionally
 * specify a qualifier for multiple implementations of the same type.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SdkComponent {
  public String[] versions();

  public SdkComponentType componentType();

  public String qualifier() default "";
}
