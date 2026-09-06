# eForms Core Library 1.9.0 Release Notes

The eForms Core Library is a collection of utilities used by the EFX Toolkit for Java Developers and other eForms applications.

## In this release

In version 1.8.0 we fixed an issue in `XPathProcessor.join` which used to compute valid but inaccurate XPaths when combining XPath steps. In version 1.9.0, we added a `Simplification` parameter to the same method, allowing the caller to control if and how the combined XPath is shortened. A backwards-compatible `XPathProcessor.join` method was kept, but deprecated for removal in the next major version.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://central.sonatype.com/artifact/eu.europa.ted.eforms/eforms-core-java)
