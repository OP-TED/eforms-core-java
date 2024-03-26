package eu.europa.ted.eforms.sdk.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class SdkComponentFactoryTest {

  @Test
  void testGetComponentImpl() throws InstantiationException {
    MyComponentFactory factory = new MyComponentFactory();
    TestComponent impl =
        factory.getComponentImpl("0.5", SdkComponentType.EFX_EXPRESSION_TRANSLATOR, TestComponent.class);
    assertEquals(MyComponent.class, impl.getClass());
  }
}
