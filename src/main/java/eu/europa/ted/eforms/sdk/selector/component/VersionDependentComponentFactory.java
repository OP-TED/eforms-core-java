package eu.europa.ted.eforms.sdk.selector.component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VersionDependentComponentFactory {
  private static final Logger logger =
      LoggerFactory.getLogger(VersionDependentComponentFactory.class);

  private Map<String, Map<VersionDependentComponentType, VersionDependentComponentDescriptor<?>>> componentsMap;

  protected static final String[] DEFAULT_PACKAGES = new String[] {"eu.europa.ted"};

  /**
   * A factory for instantiating classes annotated with {@link VersionDependentComponent}. 
   * @param packages The package(s) to scan for annotated classes
   */
  protected VersionDependentComponentFactory(String... packages) {
    populateComponents(Optional.ofNullable(packages).orElse(DEFAULT_PACKAGES));
  }

  private void populateComponents(String... packages) {
    if (ArrayUtils.isEmpty(packages)) {
      packages = DEFAULT_PACKAGES;
    }

    Class<VersionDependentComponent> annotationType = VersionDependentComponent.class;

    logger.debug("Looking in the classpath for types annotated with {}", annotationType);

    if (componentsMap == null) {
      componentsMap = new HashMap<>();
    }

    new Reflections(ConfigurationBuilder.build().forPackages(packages))
        .getTypesAnnotatedWith(annotationType).stream()
        .forEach((Class<?> clazz) -> {
          logger.trace("Processing type [{}]", clazz);

          VersionDependentComponent annotation =
              clazz.getAnnotation(VersionDependentComponent.class);

          String[] supportedSdkVersions = annotation.versions();
          VersionDependentComponentType componentType = annotation.componentType();

          logger.trace("Class [{}] has a component type of [{}] and supports SDK versions [{}]",
              clazz, componentType, supportedSdkVersions);

          Arrays.asList(supportedSdkVersions).forEach((String sdkVersion) -> {
            VersionDependentComponentDescriptor<?> component =
                new VersionDependentComponentDescriptor<>(sdkVersion, componentType, clazz);

            Map<VersionDependentComponentType, VersionDependentComponentDescriptor<?>> components =
                componentsMap.get(sdkVersion);

            if (components != null) {
              VersionDependentComponentDescriptor<?> existingComponent =
                  components.get(componentType);

              if (existingComponent != null && !existingComponent.equals(component)) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "More than one components of type [{0}] have been found for SDK version [{1}]:\n\t- {2}\n\t- {3}",
                    componentType, sdkVersion, existingComponent.getImplType().getName(),
                    clazz.getName()));
              }
            } else {
              components = new EnumMap<>(VersionDependentComponentType.class);
              componentsMap.put(sdkVersion, components);
            }

            components.put(componentType, component);
          });
        });
  }

  @SuppressWarnings("unchecked")
  protected <T> T getComponentImpl(String sdkVersion,
      final VersionDependentComponentType componentType,
      final Class<T> intf, Object... initArgs) throws InstantiationException {

    String normalizedVersion = normalizeVersion(sdkVersion);

    VersionDependentComponentDescriptor<T> descriptor =
        (VersionDependentComponentDescriptor<T>) Optional
            .ofNullable(componentsMap.get(normalizedVersion))
            .orElseGet(Collections::emptyMap).get(componentType);

    if (descriptor == null) {
      logger.error("Failed to load required components of SDK [{}]", sdkVersion);
      throw new IllegalArgumentException(MessageFormat
          .format("No implementation found for component type [{0}] of SDK [{1}].", componentType,
              sdkVersion));
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
