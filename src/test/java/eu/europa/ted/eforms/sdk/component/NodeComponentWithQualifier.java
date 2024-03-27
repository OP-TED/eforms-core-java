package eu.europa.ted.eforms.sdk.component;

@SdkComponent(versions = "1", componentType = SdkComponentType.NODE, qualifier = "qualifier")
public class NodeComponentWithQualifier implements TestComponent {

  @Override
  public String testMethod() {
    return "Node";
  }
}
