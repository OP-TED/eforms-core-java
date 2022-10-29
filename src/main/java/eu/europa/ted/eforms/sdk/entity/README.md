# Common Entities

The entities in this package can be used while reading data from the eForms SDK. Currently there are only three entities implemented here:

* `SdkField`: Can hold basic information about a field.
* `SdkNode`: Can hold basic information about a node and reconstruct the node hierarchy.
* `SdkCodelist`: Can hold codelist information including its codes.

All the classes are abstract so that they can have specific implementations for different major versions of the eForms SDK if needed.

This package also includes a factory class (`SdkEntityFactory`) that is meant to be used for instantiating concrete implementations of these abstract entity classes for different major versions of the eForms SDK.

_There is no rocket science in the code in this package. You are welcome to reuse it. It is intended to be used primarily by the EFX Toolkit and our sample applications._
