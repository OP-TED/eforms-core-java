package eu.europa.ted.eforms.sdk.component;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdkComponentDescriptor<T> implements Serializable {
  private static final long serialVersionUID = -6237218459963821365L;

  private static final Logger logger = LoggerFactory.getLogger(SdkComponentDescriptor.class);

  private String sdkVersion;

  private SdkComponentType componentType;

  private String qualifier;

  private Class<T> implType;

  public SdkComponentDescriptor(String sdkVersion, SdkComponentType componentType,
      Class<T> implType) {
    this(sdkVersion, componentType, "", implType);
  }

  public SdkComponentDescriptor(String sdkVersion, SdkComponentType componentType, String qualifier,
      Class<T> implType) {
    this.sdkVersion = Validate.notBlank(sdkVersion, "Undefined SDK version");
    this.componentType = Validate.notNull(componentType, "Undefined component type");
    this.qualifier = Validate.notNull(qualifier, "Undefined qualifier");
    this.implType = Validate.notNull(implType, "Undefined implementation type");
  }

  @SuppressWarnings("unchecked")
  public T createInstance(Object... initArgs) throws InstantiationException {
    try {
      Class<?>[] paramTypes = Arrays
          .asList(Optional.ofNullable(initArgs).orElse(new Object[0]))
          .stream()
          .map((Object o) -> o == null ? null : o.getClass())
          .collect(Collectors.toList())
          .toArray(new Class[0]);

      logger.trace("Creating an instance of [{}] using constructor with parameter types: {}",
          implType, paramTypes);

      Constructor<T> constructor = Optional
          .ofNullable(
              (Constructor<T>) Arrays.asList(implType.getDeclaredConstructors()).stream()
                  .filter((Constructor<?> c) -> constructorHasExpectedParameters(c, paramTypes))
                  .collect(Collectors.collectingAndThen(Collectors.toList(),
                      this::getConstructorAfterAmbiguityCheck)))
          .orElseThrow(() -> new IllegalArgumentException(
              MessageFormat.format("No constructor found for [{0}] with parameter types: )) {1}",
                  implType, paramTypes)));

      return constructor.newInstance(initArgs);
    } catch (SecurityException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new InstantiationException(MessageFormat.format(
          "Failed to instantiate [{0}] as SDK component type [{1}] for SDK [{2}]. Error was: {3}",
          implType, componentType, sdkVersion, e.getMessage()));
    }
  }

  private Constructor<?> getConstructorAfterAmbiguityCheck(List<Constructor<?>> constructors) {
    Validate.isTrue(CollectionUtils.isNotEmpty(constructors), "No constructors found");

    if (constructors.size() > 1) {
      throw new IllegalStateException(
          "More than one constructors found with the same parameter types");
    }

    return constructors.get(0);
  }

  private boolean constructorHasExpectedParameters(Constructor<?> constructor,
      Class<?>[] paramTypes) {
    Class<?>[] declaredParamTypes = constructor.getParameterTypes();

    if (declaredParamTypes.length != paramTypes.length) {
      return false;
    }

    for (int i = 0; i < declaredParamTypes.length; i++) {
      if (paramTypes[i] != null && !declaredParamTypes[i].isAssignableFrom(paramTypes[i])) {
        return false;
      }
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(componentType, sdkVersion, qualifier, implType);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SdkComponentDescriptor<?> other = (SdkComponentDescriptor<?>) obj;
    return componentType == other.componentType 
        && Objects.equals(sdkVersion, other.sdkVersion)
        && Objects.equals(qualifier, other.qualifier)
        && Objects.equals(implType, other.implType);
  }

  public Class<T> getImplType() {
    return implType;
  }
}
