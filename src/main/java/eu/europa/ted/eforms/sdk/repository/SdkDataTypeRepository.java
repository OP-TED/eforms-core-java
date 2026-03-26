/*
 * Copyright 2026 European Union
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European
 * Commission – subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.europa.ted.eforms.sdk.repository;

import java.util.HashMap;
import eu.europa.ted.eforms.sdk.entity.SdkDataType;

/**
 * Repository of SDK data types.
 *
 * Currently uses hardcoded type definitions. When data-types.json is added to the SDK, this class
 * will be updated to load type metadata from JSON.
 */
public class SdkDataTypeRepository extends HashMap<String, SdkDataType> {
  private static final long serialVersionUID = 1L;

  /**
   * Creates a repository with the default set of SDK data types and their privacy masks. This is a
   * temporary approach until data-types.json is available in the SDK.
   */
  public static SdkDataTypeRepository createDefault() {
    SdkDataTypeRepository repository = new SdkDataTypeRepository();

    repository.addType("text", "unpublished");
    repository.addType("text-multilingual", "unpublished");
    repository.addType("code", "unpublished");
    repository.addType("internal-code", "unpublished");
    repository.addType("id", "unpublished");
    repository.addType("id-ref", "unpublished");
    repository.addType("phone", "unpublished");
    repository.addType("email", "unpublished");
    repository.addType("url", "unpublished");
    repository.addType("date", "1970-01-01Z");
    repository.addType("zoned-date", "1970-01-01Z");
    repository.addType("time", "00:00:00Z");
    repository.addType("zoned-time", "00:00:00Z");
    repository.addType("indicator", "0");
    repository.addType("integer", "-1");
    repository.addType("number", "-1");
    repository.addType("amount", "-1");
    repository.addType("measure", "-1");
    repository.addType("duration", "-1");

    return repository;
  }

  private void addType(String id, String privacyMask) {
    this.put(id, new SdkDataType(id, privacyMask));
  }
}
