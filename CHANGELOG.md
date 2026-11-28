# eForms Core Library 2.0.0-alpha.1 Release Notes

The eForms Core Library is a collection of utilities used by the EFX Toolkit for Java Developers and other eForms applications.

## In this release

This is the first pre-release of version 2.0.0. It contains all the changes of version 1.10.0, together with the following changes.

The library now requires Java 21 or later.

The alias of fields and nodes has been removed, as EFX 2 no longer uses aliases. `SdkFieldV2` and `SdkNodeV2` no longer provide `getAlias()`, and their constructors no longer take an alias parameter. Applications that call these constructors or this method must be adapted. JSON that is deserialised directly into these classes must no longer contain an `alias` property, as it is now rejected.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://central.sonatype.com/artifact/eu.europa.ted.eforms/eforms-core-java)
