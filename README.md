# eForms Core Library

_Copyright 2022 European Union_

_Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission –
subsequent versions of the EUPL (the "Licence");_
_You may not use this work except in compliance with the Licence._
_You may obtain a copy of the Licence at:_ 
_https://joinup.ec.europa.eu/software/page/eupl5_

_Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence._

---

## Introduction

This is a library with common API and tools for use by eForms applications.

## Using the eFroms Core Library

The eForms Core library requires Java 11 or later.

It is available as a Maven package on Maven Central and can be used by adding the following to the project's `pom.xml`.

```
<dependencies>
  ...
  <dependency>
    <groupId>eu.europa.ted.eforms</groupId>
    <artifactId>eforms-core-java</artifactId>
    <version>${eforms-core.version}</version>
  </dependency>
  ...
</dependencies>
```

Replace `${eforms-core.version}` with the latest version available, or define the corresponding property in your `pom.xml`.

## Building

Requirements:

* Java 11 or higher
* Maven 3.8, other versions probably also work

Execute the following on the root folder of this project:

    mvn clean install
