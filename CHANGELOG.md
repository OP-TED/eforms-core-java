# eForms Core Library 1.10.0 Release Notes

The eForms Core Library is a collection of utilities used by the EFX Toolkit for Java Developers and other eForms applications.

## In this release

This release introduces support for the SDK 2 metadata files located in `fields/fwd`. The new class `SdkRepositoryFactory` creates the repositories of nodes, fields and data types for a given SDK version, for both SDK 1 and SDK 2. The repository constructors that read a single file are deprecated in favour of this factory.

The SDK downloader now authenticates with the repositories declared in the Maven `settings.xml` file, and takes into account the profiles that are active by default.

This is the final release of the 1.x line. It requires Java 11 or later.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://central.sonatype.com/artifact/eu.europa.ted.eforms/eforms-core-java)
