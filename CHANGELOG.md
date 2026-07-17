# eForms Core Library 1.7.0 Release Notes

The eForms Core Library is a collection of utilities used by the EFX Toolkit for Java Developers and other eForms applications.

## In this release

### SDK constants and resources

- Added new SDK path, filename, and resource constants, including support for the validation folder and the `fwd` forward folder used by SDK 2.

### Performance

- Replaced Reflections with ClassGraph for component scanning and share a single classpath scan across factories, reducing startup cost (`SdkComponentFactory`).

### Fixes

- `SdkFieldV1` now maps the `measure` type to `duration`, consistent with the `duration`/`measure` split introduced in 1.6.0.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://central.sonatype.com/artifact/eu.europa.ted.eforms/eforms-core-java)
