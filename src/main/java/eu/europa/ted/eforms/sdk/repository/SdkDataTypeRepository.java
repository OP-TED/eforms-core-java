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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.entity.SdkDataType;
import eu.europa.ted.eforms.sdk.entity.SdkEntityFactory;

/**
 * Repository of SDK data types.
 *
 * Currently uses hardcoded type definitions, in the data-types.json resource of this library.
 */
public class SdkDataTypeRepository extends HashMap<String, SdkDataType> {
  private static final long serialVersionUID = 1L;

  public SdkDataTypeRepository() {
    super();
  }

  /**
   * Creates the repository of the data types of the given SDK version, with their privacy masks and
   * the attributes that their fields carry. They are read from the data-types.json resource of
   * this library, which has the format of fields/fwd/data-types.json in SDK 2. SDK 1 has no such
   * file, so this is where its data types come from.
   */
  public SdkDataTypeRepository(final String sdkVersion) throws InstantiationException {
    this.populateMap(sdkVersion, readResource());
  }

  /**
   * Creates a repository with the default set of SDK data types and their privacy masks. This is a
   * temporary approach until data-types.json is available in the SDK.
   *
   * @deprecated Use {@link #SdkDataTypeRepository(String)}, which creates the data types of the
   *             given SDK version, with the attributes that their fields carry.
   */
  @Deprecated
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

  private void populateMap(final String sdkVersion, final JsonNode json)
      throws InstantiationException {
    for (final JsonNode dataType : json.get(SdkConstants.DATA_TYPES_JSON_DATA_TYPES_KEY)) {
      final SdkDataType sdkDataType = SdkEntityFactory.getSdkDataType(sdkVersion, dataType);
      this.put(sdkDataType.getId(), sdkDataType);
    }
  }

  private static JsonNode readResource() {
    try (InputStream input =
        SdkDataTypeRepository.class.getResourceAsStream(SdkConstants.DATA_TYPES_JSON_FILE_NAME)) {
      return new ObjectMapper().readTree(input);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
