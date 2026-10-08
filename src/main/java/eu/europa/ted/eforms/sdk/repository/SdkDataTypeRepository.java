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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.SdkVersion;
import eu.europa.ted.eforms.sdk.entity.SdkDataType;
import eu.europa.ted.eforms.sdk.entity.SdkEntityFactory;
import eu.europa.ted.eforms.sdk.resource.SdkResourceLoader;

/**
 * Repository of SDK data types.
 *
 * <p>
 * SDK 1 publishes no data types, so they are read from the data-types.json resource of this
 * library. SDK 2 publishes them in fields/fwd/data-types.json, which has the same format, so for
 * SDK 2 they are read from the SDK itself (TEDEFO-5231). This repository decides where the data
 * types of each SDK version come from, so that its callers do not have to check the SDK version.
 * </p>
 */
public class SdkDataTypeRepository extends HashMap<String, SdkDataType> {
  private static final long serialVersionUID = 1L;

  /**
   * The major version of the only SDK that publishes no data types of its own.
   */
  private static final String SDK_MAJOR_WITHOUT_DATA_TYPES = "1";

  /**
   * Creates the repository of the data types of the given SDK, with their privacy masks, the
   * attributes that their fields carry, and the code lists of their values.
   *
   * @param sdkVersion the target SDK version, which decides where the data types are read from
   * @param sdkRootPath path of the root SDK folder, used for the SDK versions that publish their
   *        data types; may be null for SDK 1, which does not
   * @throws InstantiationException if a data type cannot be created for this SDK version
   */
  public SdkDataTypeRepository(final String sdkVersion, final Path sdkRootPath)
      throws InstantiationException {
    // The file of the SDK has the same format as the resource, so the loop is the same for both.
    for (final JsonNode dataType : readDataTypes(sdkVersion, sdkRootPath)
        .get(SdkConstants.DATA_TYPES_JSON_DATA_TYPES_KEY)) {
      final SdkDataType sdkDataType = SdkEntityFactory.getSdkDataType(sdkVersion, dataType);
      this.put(sdkDataType.getId(), sdkDataType);
    }
  }

  /**
   * Creates the repository of the data types from the data-types.json resource of this library,
   * whatever the SDK version.
   *
   * @deprecated Use {@link #SdkDataTypeRepository(String, Path)}, which reads the data types of
   *             SDK 2 from the SDK instead of this copy.
   */
  @Deprecated
  public SdkDataTypeRepository() {
    for (final JsonNode dataType : readResource().get(SdkConstants.DATA_TYPES_JSON_DATA_TYPES_KEY)) {
      final SdkDataType sdkDataType = new SdkDataType(dataType);
      this.put(sdkDataType.getId(), sdkDataType);
    }
  }

  private SdkDataTypeRepository(final Map<String, SdkDataType> dataTypes) {
    super(dataTypes);
  }

  /**
   * Creates a repository with the default set of SDK data types and their privacy masks. This is a
   * temporary approach until data-types.json is available in the SDK.
   *
   * @deprecated Use {@link #SdkDataTypeRepository()}, which also gives the attributes that the
   *             fields of each data type carry.
   */
  @Deprecated
  public static SdkDataTypeRepository createDefault() {
    SdkDataTypeRepository repository = new SdkDataTypeRepository(Map.of());

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

  /**
   * @return the data types of the given SDK version, from the SDK itself when it publishes them,
   *         and from the resource of this library for the SDK versions that do not
   */
  private static JsonNode readDataTypes(final String sdkVersion, final Path sdkRootPath) {
    if (SDK_MAJOR_WITHOUT_DATA_TYPES.equals(new SdkVersion(sdkVersion).getMajor())) {
      return readResource();
    }
    return readSdkFile(SdkResourceLoader.getResourceAsPath(sdkVersion,
        SdkConstants.SdkResource.FIELDS_FWD_DATA_TYPES, sdkRootPath));
  }

  private static JsonNode readResource() {
    try (InputStream input =
        SdkDataTypeRepository.class.getResourceAsStream(SdkConstants.DATA_TYPES_JSON_FILE_NAME)) {
      return new ObjectMapper().readTree(input);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static JsonNode readSdkFile(final Path dataTypesPath) {
    try (InputStream input = Files.newInputStream(dataTypesPath)) {
      return new ObjectMapper().readTree(input);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
