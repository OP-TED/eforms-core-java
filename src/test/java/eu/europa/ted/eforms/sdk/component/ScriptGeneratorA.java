package eu.europa.ted.eforms.sdk.component;

@SdkComponent(versions = "1", componentType = SdkComponentType.SCRIPT_GENERATOR)
class ScriptGeneratorA implements TestComponent {

  @Override
  public String testMethod() {
    return "A";
  }
}
