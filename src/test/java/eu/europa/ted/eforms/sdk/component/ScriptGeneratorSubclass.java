package eu.europa.ted.eforms.sdk.component;

@SdkComponent(versions = "1", componentType = SdkComponentType.SCRIPT_GENERATOR, qualifier = "other")
class ScriptGeneratorSubclass extends ScriptGeneratorA {

  @Override
  public String testMethod() {
    return "Subclass";
  }
}
