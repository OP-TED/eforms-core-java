# eForms Core Library 1.6.0 Release Notes

The eForms Core Library is a collection of utilities used by the EFX Toolkit for Java Developers and other eForms applications.

## In this release

### SDK entity improvements

- Versioned SDK entity classes (`SdkFieldV1`, `SdkFieldV2`, `SdkNodeV1`, `SdkNodeV2`, etc.) have been moved from the EFX Toolkit into the core library, consolidating version-specific implementations in a single location.
- `SdkNode` now supports parent node references and ancestor chain traversal via `getAncestry()`. The return type is `List` (ordered) rather than `Set`.
- `SdkField` now exposes repeatability information, parent node references, and parsed XPath metadata via `getXpathInfo()`.
- Repository classes (`SdkNodeRepository`, `SdkFieldRepository`) now use two-pass loading to wire parent-child relationships during initialization.

### Privacy and data type support

- Added `PrivacySettings` to `SdkField`, providing access to privacy code, justification, publication date, and related field references.
- Introduced `SdkDataType` entity and `SdkDataTypeRepository` for field type-level metadata including privacy masking values.
- Separated `duration` as a distinct data type from `measure`.

### Notice subtype management

- Added `SdkNoticeSubtype` entity with intelligent ID parsing (prefix/number/suffix decomposition) and correct sorting order.
- Added `SdkNoticeTypeRepository` to load and manage notice subtypes.
- Renamed `getSdkNoticeType()` to `getSdkNoticeSubtype()` for semantic accuracy.

### Utilities

- Moved `NoticeDocument` and `SafeDocumentBuilder` from the eforms-notice-viewer into the core library. `NoticeDocument` provides secure XML parsing with accessors for notice subtype, SDK version, and language detection. `SafeDocumentBuilder` implements XXE prevention following OWASP guidelines.

### Component registry

- Added component types for dependency extraction (`EFX_COMPUTE_DEPENDENCY_EXTRACTOR`, `EFX_VALIDATION_DEPENDENCY_EXTRACTOR`) and EFX rules translation (`EFX_RULES_TRANSLATOR`).
- Renamed `VALIDATOR_MARKUP_GENERATOR` to `VALIDATOR_GENERATOR`.

### Dependencies

- Updated versions of various dependencies.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://central.sonatype.com/artifact/eu.europa.ted.eforms/eforms-core-java)
