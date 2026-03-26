# Common Entities

The entities in this package can be used while reading data from the eForms SDK. The following entities are implemented here:

* `SdkField`: Can hold basic information about a field, including repeatability, parent node, XPath metadata, and privacy settings.
* `SdkNode`: Can hold basic information about a node and reconstruct the node hierarchy via parent references and ancestor chain traversal.
* `SdkCodelist`: Can hold codelist information including its codes.
* `SdkNoticeSubtype`: Can hold information about a notice subtype from the SDK's notice-types.json file.
* `SdkDataType`: Can hold field type-level metadata including privacy masking values.

All the classes are abstract so that they can have specific implementations for different major versions of the eForms SDK if needed.

This package also includes a factory class (`SdkEntityFactory`) that is meant to be used for instantiating concrete implementations of these abstract entity classes for different major versions of the eForms SDK.

