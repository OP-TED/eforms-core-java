package eu.europa.ted.eforms.sdk.selector.component;

import org.junit.jupiter.api.Test;

@VersionDependentComponent(versions = "0.5",
    componentType = VersionDependentComponentType.EFX_EXPRESSION_TRANSLATOR)
class VersionDependentComponentFactoryTest extends VersionDependentComponentFactory
    implements TestComponent {

  @Override
  protected <T> T getComponentImpl(String sdkVersion, VersionDependentComponentType componentType,
      Class<T> intf, Object... initArgs) throws InstantiationException {
    return super.getComponentImpl(sdkVersion, componentType, intf, initArgs);
  }

  @Override
  public String testMethod() {
    return null;
  }

  @Test
  void test() throws InstantiationException {
    getComponentImpl("0.5", VersionDependentComponentType.EFX_EXPRESSION_TRANSLATOR,
        TestComponent.class);
  }
}
