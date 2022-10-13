package eu.europa.ted.eforms.sdk.selector.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VersionDependentComponent {
  public String[] versions();

  public VersionDependentComponentType componentType();
}
