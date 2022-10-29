# SDK Component instantiation
This package provides classes that implement a solution for the "SDK component instantiation problem" (see below).

The general idea is that different versions of each component are decorated with an annotation (`SdkComponent`) that specifies the major version of the SDK it corresponds to. An abstract factory is then used to instantiate the correct version of the component that is needed each time.  

This package contains:
* `SdkComponent`: A Java annotation used to correlate an application component with a specific major version of the eForms SDK.
* `SdkComponentFactory`: An abstract class that provides the base implementation of an abstract factory that can instantiate components decorated with the `SdkComponent` annotation.  
* `SdkComponentType`: An `enum` listing the different types (kinds) of components that can be defined. This is used to designate the purpose (usage) of the component. _For example a component that "loads a codelist", or "translates an EFX expression" etc)._ 
* `SdkComponentDescriptor`: A helper class used by the `SdkComponentFactory`.

## The SDK component instantiation "problem"
It is not really a "problem" as such, because it has several solutions, one of which is implemented here.

Here is the "problem" definition: *Different major versions of the SDK may need a different algorithm (a different version of a component) in order to be read correctly. How does one maintain different versions of the same component running in parallel in the same application so that the correct version can be instantiated and used on demand?*

For example, if you want to read SDK 1.x.x you may have a component in your application called `SdkReaderV1`. When SDK 2.0.0 is released you may need to create a new version of the same component `SdkReaderV2`. You cannot replace `SdkReaderV1` with `SdkReaderV2` because if you do, then your application will not be able to read future patches of SDK 1.x.x. So you need to have both `SdkReaderV1` and `SdkReaderV2` available at runtime and instantiate each time the one that corresponds to the version of the SDK that you want to read.

