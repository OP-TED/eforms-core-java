# Reading SDK files

This package contains classes used to create [SDK entities](../entity/) form the `.json` and `.gc` files in the SDK.  

The implementations provided here are very basic and are only intended as a quick solution for reading eForms metadata from specific SDK files. To instantiate the correct components for the intended major version of the SDK, the repository classes use the [`SdkEntityFactory`](../entity/SdkEntityFactory.java).


This package contains:

* `SdkFieldRepository`: can populate a `HashMap` with `SdkField` objects read form `fields.json`
* `SdkNodeRepository`: can populate a `HashMap` with `SdkNode` objects read form `fields.json`
* `SdkCodelistRepository`: can populate a `HashMap` with `SdkCodelist` objects (including all codelist codes), by reading the `.gc` files from the `codelists` folder of the eForms SDK.