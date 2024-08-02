# eForms Core Library 1.4.0 Release Notes

The eForms Core Library is a collection of utilities that are used by our sample applications as well as the EFX Toolkit for Java Developers.

## In this release

This release adds the option to indicate a qualifier for SDK components. If there are 2 or more classes that have an @SdkComponent annotation with the same version and component type, this allows you to differentiate them and load the component with the matching qualifier.

The versions of various dependencies was updated: ANTLR 4.13.1, JAXB 4.0.4, logback 1.5.3, ph-genericode 7.1.1.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://central.sonatype.com/artifact/eu.europa.ted.eforms/eforms-core-java)
