# eForms Core Library 1.9.1 Release Notes

The eForms Core Library is a collection of utilities used by the EFX Toolkit for Java Developers and other eForms applications.

## In this release

SDK downloads now ignore Maven proxy definitions marked as inactive in `settings.xml`. Previously, a disabled proxy could still receive repository requests and cause SDK downloads to fail. Active proxies and proxy definitions that omit the `active` setting continue to work as before.

The `SdkNode.setParent` documentation now clarifies the requirements for deferred parent-link initialization, cached ancestry, and access to shared node hierarchies.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://central.sonatype.com/artifact/eu.europa.ted.eforms/eforms-core-java)
