# eForms Core Library 1.2.0 Release Notes

The eForms Core Library is a collection of utilities that are used by our sample applications as well as the EFX Toolkit for Java Developers.

## In this release

This release adds some XPath processing capabilities, via the new XPathProcessor class. Those capabilities are available in the EFX Toolkit version 1.x, but as they are also useful if you are not using EFX, we are moving them to the eForms Core Library. The corresponding API will be removed from the EFX Toolkit in its next major version (2.0.0).

The SdkResource enum now has a value corresponding to the index file in the `translations` folder, named `translations.json`. This file is added in SDK 1.10.0.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://central.sonatype.com/artifact/eu.europa.ted.eforms/eforms-core-java)
