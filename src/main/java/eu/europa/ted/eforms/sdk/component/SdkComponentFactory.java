package eu.europa.ted.eforms.sdk.component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import eu.europa.ted.eforms.sdk.SdkVersion;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for instantiating classes annotated with {@link SdkComponent}.
 */
public abstract class SdkComponentFactory {
  private static final Logger logger = LoggerFactory.getLogger(SdkComponentFactory.class);

  /**
   * Lazy-init holder for the JVM-wide classpath scan. Triggered the first time any
   * {@link SdkComponentFactory} subclass is constructed; the resulting list is shared
   * by all subclasses so that the (potentially expensive) classpath walk runs only once
   * per JVM rather than once per factory subclass.
   */
  private static final class AnnotatedClassesHolder {
    static final List<Class<?>> CLASSES = scan();

    private static List<Class<?>> scan() {
      logger.debug("Scanning the classpath for types annotated with {}", SdkComponent.class);
      try (ScanResult result = new ClassGraph()
          .enableAnnotationInfo()
          .ignoreClassVisibility()
          .scan()) {
        return result.getClassesWithAnnotation(SdkComponent.class).loadClasses();
      }
    }
  }

  private Map<String, Map<ComponentSelector, SdkComponentDescriptor<?>>> componentsMap;

  class ComponentSelector {
    private final SdkComponentType componentType;
    private final String qualifier;

    public ComponentSelector(SdkComponentType componentType, String qualifier) {
      this.componentType = componentType;
      this.qualifier = qualifier;
    }

    @Override
    public int hashCode() {
      return Objects.hash(componentType, qualifier);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ComponentSelector other = (ComponentSelector) obj;
      if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
        return false;
      return componentType == other.componentType && Objects.equals(qualifier, other.qualifier);
    }

    private SdkComponentFactory getEnclosingInstance() {
      return SdkComponentFactory.this;
    }
  }

  protected SdkComponentFactory() {
    populateComponents();
  }

  private void populateComponents() {
    Class<SdkComponent> annotationType = SdkComponent.class;

    if (componentsMap == null) {
      componentsMap = new HashMap<>();
    }

    AnnotatedClassesHolder.CLASSES.forEach((Class<?> clazz) -> {
      logger.trace("Processing type [{}]", clazz);

      SdkComponent annotation = clazz.getAnnotation(annotationType);

      String[] supportedSdkVersions = annotation.versions();
      SdkComponentType componentType = annotation.componentType();
      String qualifier = annotation.qualifier();
      ComponentSelector selector = new ComponentSelector(componentType, qualifier);

      logger.trace("Class [{}] has a component type of [{}] and supports SDK versions [{}]",
          clazz, componentType, supportedSdkVersions);

      Arrays.asList(supportedSdkVersions).forEach((String sdkVersion) -> {
        SdkComponentDescriptor<?> component =
            new SdkComponentDescriptor<>(sdkVersion, componentType, clazz);

        Map<ComponentSelector, SdkComponentDescriptor<?>> components =
            componentsMap.get(sdkVersion);

        if (components != null) {
          SdkComponentDescriptor<?> existingComponent = components.get(selector);

          if (existingComponent != null && !existingComponent.equals(component)) {
            throw new IllegalArgumentException(MessageFormat.format(
                "More than one components of type [{0}] have been found for SDK version [{1}]:\n\t- {2}\n\t- {3}",
                componentType, sdkVersion, existingComponent.getImplType().getName(),
                clazz.getName()));
          }
        } else {
          components = new HashMap<>();
          componentsMap.put(sdkVersion, components);
        }

        components.put(selector, component);
      });
    });
  }

  protected <T> T getComponentImpl(String sdkVersion, final SdkComponentType componentType,
      final Class<T> intf, Object... initArgs) throws InstantiationException {

    return getComponentImpl(sdkVersion, componentType, "", intf, initArgs);
  }

  protected <T> T getComponentImpl(String sdkVersion, final SdkComponentType componentType,
      final String qualifier, final Class<T> intf, Object... initArgs)
      throws InstantiationException {

    String normalizedVersion = normalizeVersion(sdkVersion);

    ComponentSelector selector = new ComponentSelector(componentType, qualifier);

    Map<ComponentSelector, SdkComponentDescriptor<?>> map =
        Optional.ofNullable(componentsMap.get(normalizedVersion))
            .orElseGet(Collections::emptyMap);

    if (logger.isTraceEnabled()) {
      logger.trace("Looking for component with version=[{}], componentType=[{}], qualifier=[{}]",
          normalizedVersion, selector.componentType, selector.qualifier);
      for (Entry<ComponentSelector, SdkComponentDescriptor<?>> entry : map.entrySet()) {
        logger.trace(
            "Available component for this version: "
                + "componentType=[{}], qualifier=[{}], value=[{}]",
            entry.getKey().componentType,
            entry.getKey().qualifier,
            entry.getValue().getImplType().getName());
      }
    }

    @SuppressWarnings("unchecked")
    SdkComponentDescriptor<T> descriptor =
        (SdkComponentDescriptor<T>) map.get(selector);

    if (descriptor == null) {
      logger.error("Failed to load required components of SDK [{}]", sdkVersion);
      throw new IllegalArgumentException(
          MessageFormat.format(
              "No implementation found for SDK [{0}], component type [{1}] and qualifier [{2}].",
              sdkVersion, componentType, qualifier));
    }

    return descriptor.createInstance(initArgs);
  }

  private static String normalizeVersion(final String sdkVersion) {
    SdkVersion version = new SdkVersion(sdkVersion);
    int major = Integer.parseInt(version.getMajor());
    if (major > 0) {
      return version.getMajor();
    }
    return version.getMajor() + "." + version.getMinor();
  }
}
