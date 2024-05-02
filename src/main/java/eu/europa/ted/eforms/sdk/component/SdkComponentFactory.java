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
import java.util.stream.Stream;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for instantiating classes annotated with {@link SdkComponent}.
 */
public abstract class SdkComponentFactory {
  private static final Logger logger = LoggerFactory.getLogger(SdkComponentFactory.class);

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

    logger.debug("Looking in the classpath for types annotated with {}", annotationType);

    if (componentsMap == null) {
      componentsMap = new HashMap<>();
    }

    // Get a list of all the packages loaded by the available classloaders.
    // This can be a bit expensive in some situations, so this method factory should
    // be ensured to run as less as possible (ideally only once).
    String[] availablePackages = Arrays
        .stream(ClasspathHelper.classLoaders())
        .map(ClassLoader::getDefinedPackages)
        .flatMap(Stream::of)
        .map(Package::getName)
        .toArray(String[]::new);

    if (logger.isDebugEnabled()) {
      final List<String> packages = Arrays.asList(availablePackages);

      logger.debug("eforms eu packages:");
      packages.stream().sorted()
          .filter(p -> p.contains("eu.") && !p.contains("digit"))
          .forEach(p -> logger.debug(p));

      logger.debug("viewer package");
      packages.stream().sorted()
          .filter(p -> p.contains("eu.europa.ted.eforms.viewer"))
          .forEach(p -> logger.debug(p));
    }

    new Reflections(ConfigurationBuilder.build().forPackages(availablePackages))
        .getTypesAnnotatedWith(annotationType).stream()
        .forEach((Class<?> clazz) -> {
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

    if (logger.isDebugEnabled()) {
      logger.debug("selector componentType={}", selector.componentType);
      logger.debug("selector qualifier={}", selector.qualifier);
      logger.debug("normalized version={}", normalizedVersion);
      for (Entry<ComponentSelector, SdkComponentDescriptor<?>> entry : map.entrySet()) {
        logger.debug(
            "entry key componentType={}, key qualifier={}, value={}",
            entry.getKey().componentType,
            entry.getKey().qualifier,
            entry.getValue().getImplType().getName());
      }
    }

    @SuppressWarnings("unchecked")
    SdkComponentDescriptor<T> descriptor =
        (SdkComponentDescriptor<T>) map.get(selector);
    logger.debug("descriptor descriptor={}", descriptor);

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
    String normalizedVersion = sdkVersion;

    if (normalizedVersion.startsWith("eforms-sdk-")) {
      normalizedVersion = normalizedVersion.substring(11);
    }

    String[] numbers = normalizedVersion.split("\\.", -2);

    if (numbers.length < 1) {
      throw new IllegalArgumentException("Invalid SDK version: " + sdkVersion);
    }

    return numbers[0]
        + ((numbers.length > 1 && Integer.parseInt(numbers[0]) > 0) ? "" : "." + numbers[1]);
  }
}
