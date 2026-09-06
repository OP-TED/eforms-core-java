# eForms Core Library 1.9.0 Release Notes

The eForms Core Library is a collection of utilities used by the EFX Toolkit for Java Developers and other eForms applications.

## In this release

### Paths

- The caller now says how much of a joined path may be shortened. Where a path goes down to a step and immediately comes back up, the two steps can be removed: `a/b/../c` becomes `a/c`. The result is shorter but it does not mean the same thing — the long form selects nothing when `b` is absent from the document, whereas the short form selects `c` whether `b` is there or not. That difference matters to some callers and not to others, so `XPathProcessor.join` now takes a `Simplification` saying which answer is wanted: `NONE` keeps every step as it was written, `PRESERVE_PREDICATES` cancels a pair of steps unless one of them carries a predicate, and `FULL` cancels it even then (TEDEFO-5169).

- The two-argument `join` is deprecated. It applies `PRESERVE_PREDICATES`, which is what 1.8.0 did, so a caller that does not change keeps the result it had. It will be removed in the next major version.

## Download

You can download the latest eForms Core library from Maven Central.

[![Maven Central](https://img.shields.io/maven-central/v/eu.europa.ted.eforms/eforms-core-java?label=Download%20&style=flat-square)](https://central.sonatype.com/artifact/eu.europa.ted.eforms/eforms-core-java)
