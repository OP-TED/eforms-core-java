package eu.europa.ted.eforms.sdk.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    TestComponent implNoQualifier =
        factory.getComponentImpl("1.0", SdkComponentType.SCRIPT_GENERATOR, TestComponent.class);

    // No qualifier specified, so we get an instance of ScriptGeneratorA
    assertEquals(ScriptGeneratorA.class, implNoQualifier.getClass());
    assertEquals("A", implNoQualifier.testMethod());

    TestComponent implQualifierOther =
        factory.getComponentImpl("1.0", SdkComponentType.SCRIPT_GENERATOR, "other", TestComponent.class);

    // Qualifier is "other", so we get an instance of ScriptGeneratorSubclass
    assertEquals(ScriptGeneratorSubclass.class, implQualifierOther.getClass());
    assertEquals("Subclass", implQualifierOther.testMethod());
  }

  @Test
  void testComponentNotFound() throws InstantiationException {
    MyComponentFactory factory = new MyComponentFactory();

    // No component for this version
    assertThrows(IllegalArgumentException.class, () ->
        factory.getComponentImpl("2.0", SdkComponentType.SCRIPT_GENERATOR, TestComponent.class));

    // No component for this type
    assertThrows(IllegalArgumentException.class, () ->
        factory.getComponentImpl("1.0", SdkComponentType.CODELIST, TestComponent.class));

    // No component for this qualifier
    assertThrows(IllegalArgumentException.class, () ->
        factory.getComponentImpl("1.0", SdkComponentType.SCRIPT_GENERATOR, "BAD", TestComponent.class));

    // Only component for this version and type does not have a qualifier
    assertThrows(IllegalArgumentException.class, () ->
    factory.getComponentImpl("0.5", SdkComponentType.EFX_EXPRESSION_TRANSLATOR, "BAD", TestComponent.class));

    // Only component for this version and type has a qualifier
    assertThrows(IllegalArgumentException.class, () ->
    factory.getComponentImpl("1.0", SdkComponentType.NODE, TestComponent.class));
  }
}
