package eu.europa.ted.eforms.sdk.component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
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

  private Map<String, Map<SdkComponentType, SdkComponentDescriptor<?>>> componentsMap;

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

    new Reflections(ConfigurationBuilder.build().forPackages(availablePackages))
        .getTypesAnnotatedWith(annotationType).stream()
        .forEach((Class<?> clazz) -> {
          logger.trace("Processing type [{}]", clazz);

          SdkComponent annotation = clazz.getAnnotation(annotationType);

          String[] supportedSdkVersions = annotation.versions();
          SdkComponentType componentType = annotation.componentType();

          logger.trace("Class [{}] has a component type of [{}] and supports SDK versions [{}]",
              clazz, componentType, supportedSdkVersions);

          Arrays.asList(supportedSdkVersions).forEach((String sdkVersion) -> {
            SdkComponentDescriptor<?> component =
                new SdkComponentDescriptor<>(sdkVersion, componentType, clazz);

            Map<SdkComponentType, SdkComponentDescriptor<?>> components =
                componentsMap.get(sdkVersion);

            if (components != null) {
              SdkComponentDescriptor<?> existingComponent = components.get(componentType);

              if (existingComponent != null && !existingComponent.equals(component)) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "More than one components of type [{0}] have been found for SDK version [{1}]:\n\t- {2}\n\t- {3}",
                    componentType, sdkVersion, existingComponent.getImplType().getName(),
                    clazz.getName()));
              }
            } else {
              components = new EnumMap<>(SdkComponentType.class);
              componentsMap.put(sdkVersion, components);
            }

            components.put(componentType, component);
          });
        });
  }

  protected <T> T getComponentImpl(String sdkVersion, final SdkComponentType componentType,
      final Class<T> intf, Object... initArgs) throws InstantiationException {

    String normalizedVersion = normalizeVersion(sdkVersion);

    @SuppressWarnings("unchecked")
    SdkComponentDescriptor<T> descriptor =
        (SdkComponentDescriptor<T>) Optional.ofNullable(componentsMap.get(normalizedVersion))
            .orElseGet(Collections::emptyMap).get(componentType);

    if (descriptor == null) {
      logger.error("Failed to load required components of SDK [{}]", sdkVersion);
      throw new IllegalArgumentException(
          MessageFormat.format("No implementation found for component type [{0}] of SDK [{1}].",
              componentType, sdkVersion));
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
