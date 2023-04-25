package eu.europa.ted.eforms.sdk.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

@SdkComponent(versions = "0.5", componentType = SdkComponentType.EFX_EXPRESSION_TRANSLATOR)
class SdkComponentFactoryTest extends SdkComponentFactory implements TestComponent {

  @Override
  protected <T> T getComponentImpl(String sdkVersion, SdkComponentType componentType, Class<T> intf,
      Object... initArgs) throws InstantiationException {
    return super.getComponentImpl(sdkVersion, componentType, intf, initArgs);
  }

  @Override
  public String testMethod() {
    return null;
  }

  @Test
  void testGetComponentImpl() throws InstantiationException {
    Object impl =
        getComponentImpl("0.5", SdkComponentType.EFX_EXPRESSION_TRANSLATOR, TestComponent.class);
    assertEquals(getClass(), impl.getClass());
  }
}
