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

  @Test
  void testMultipleComponentImpl() throws InstantiationException {
    MyComponentFactory factory = new MyComponentFactory();
    TestComponent impl =
        factory.getComponentImpl("1.0", SdkComponentType.SCRIPT_GENERATOR, TestComponent.class);

    // impl could be either ScriptGeneratorA, ScriptGeneratorB, or ScriptGeneratorSubclass
    // depending on which was the last found in SdkComponentFactory.populateComponents()
    assertEquals(ScriptGeneratorSubclass.class, impl.getClass());
    assertEquals("Subclass", impl.testMethod());
  }
}
