# eForms Core Library[^1]

## Introduction

This library provides a set of classes that can be used to solve some common "problems" in eForms Applications:

* Automatically discovering and downloading new versions of the eForms SDK.
* Maintaining and instantiating at runtime the correct application component versions for different major versions of the SDK.  

## Using the eForms Core Library

The eForms Core library requires Java 11 or later.

It is available as a Maven package on Maven Central and can be used by adding the following to the project's `pom.xml`.

```xml
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

```text
    mvn clean install
```

## Testing

Unit tests are available under `src/test/java/`.

After running the unit tests with `mvn test`, you can generate a coverage report with `mvn jacoco:report`.
The report is available under `target/site/jacoco/`, in HTML, CSV, and XML format.

[^1]: _Copyright 2022 European Union_  
_Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission –
subsequent versions of the EUPL (the "Licence");_
_You may not use this work except in compliance with the Licence. You may obtain [a copy of the Licence here](LICENSE)._  
_Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence._
