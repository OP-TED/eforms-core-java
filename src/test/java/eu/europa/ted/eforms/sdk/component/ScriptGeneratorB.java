package eu.europa.ted.eforms.sdk.component;

@SdkComponent(versions = "1", componentType = SdkComponentType.SCRIPT_GENERATOR)
class ScriptGeneratorB implements TestComponent {

  @Override
  public String testMethod() {
    return "B";
  }
}