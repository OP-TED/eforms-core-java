package eu.europa.ted.eforms.sdk.component;

class MyComponentFactory extends SdkComponentFactory {

  @Override
  protected <T> T getComponentImpl(String sdkVersion, SdkComponentType componentType, Class<T> intf,
      Object... initArgs) throws InstantiationException {
    return super.getComponentImpl(sdkVersion, componentType, intf, initArgs);
  }
}