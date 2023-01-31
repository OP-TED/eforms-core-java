# eForms Core Library 1.0.1 Release Notes

_The library is a collection of utilities that are used by our sample applications as well as the EFX Toolkit for Java Developers._

## In this release:
This patch fixes the Sdk Downloader facility to handle the case where a VERSION file already exists on the root folder of the downloaded eForms SDK.
It also fixes the following two issues:
- The SDK Downloader was considering a snapshot of a minor version to be the latest of the previous minor version.
- SdkComponentFactory was scanning for implementation classes only in the current classloader. It now scans in all available (loaded) classloaders.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://search.maven.org/search?q=g:%22eu.europa.ted.eforms%22%20AND%20a:%22eforms-core-java%22)
