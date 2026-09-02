# eForms Core Library 1.8.0 Release Notes

The eForms Core Library is a collection of utilities used by the EFX Toolkit for Java Developers and other eForms applications.

## In this release

### Fixes

- Predicates are no longer discarded when two paths are joined. `XPathProcessor.join` composed its result from the text of each step alone, leaving the predicates behind, so a reference written after a context override selected every instance of a field instead of the ones its predicate asked for. The leading separator of an absolute first operand was lost in the same place. A step and a parent step that cancel each other out are also only cancelled now when neither carries a predicate, since a predicate on either of them is a condition on the result (TEDEFO-5148).

- Predicates are no longer discarded when an axis is added to a path. `XPathProcessor.addAxis` had the same fault, and threw an unhelpful exception when given a path made only of parent steps. It now returns a valid path for every valid path it is given, and reads a step the same way however it is spelled. A missing axis or path is refused outright rather than answered with something that only looks like XPath (TEDEFO-5150).

### Paths

- `XPathInfo` now reports where a path starts from, through `getAnchor()` and `isAbsolute()`. The new `XPathAnchor` tells apart a path relative to its context, one anchored to the root of the document, and one searching from the root at any depth.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://central.sonatype.com/artifact/eu.europa.ted.eforms/eforms-core-java)
