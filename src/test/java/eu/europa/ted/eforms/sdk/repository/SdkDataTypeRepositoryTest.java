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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import eu.europa.ted.eforms.sdk.entity.SdkDataType;
import eu.europa.ted.eforms.sdk.entity.v1.SdkDataTypeV1;
import eu.europa.ted.eforms.sdk.entity.v2.SdkDataTypeV2;

class SdkDataTypeRepositoryTest {

  /** SDK 1 publishes no data types: they come from the resource of this library. */
  private static final String SDK_1 = "1.16";

  /** SDK 2 publishes its data types in fields/fwd/data-types.json. */
  private static final String SDK_2 = "2.0";

  /** An SDK folder holding only the data types of SDK 2, deliberately different from the resource. */
  private static final Path SDK_ROOT = Path.of("src", "test", "resources", "sdk-fixture");

  private static SdkDataTypeRepository ofSdk1() throws InstantiationException {
    return new SdkDataTypeRepository(SDK_1, null);
  }

  private static SdkDataTypeRepository ofSdk2() throws InstantiationException {
    return new SdkDataTypeRepository(SDK_2, SDK_ROOT);
  }

  @Test
  void testAllDataTypesAreLoaded() throws InstantiationException {
    assertEquals(23, ofSdk1().size());
  }

  @Test
  void testAmountHasCurrencyAttribute() throws InstantiationException {
    SdkDataType amount = ofSdk1().get("amount");

    assertEquals("-1", amount.getPrivacyMask());
    assertEquals("currencyID", amount.getAttributeName());
    assertEquals("currency", amount.getAttributeType());
  }

  @Test
  void testDurationAndMeasureHaveTheirOwnUnits() throws InstantiationException {
    SdkDataTypeRepository repository = ofSdk1();

    assertEquals("duration-unit", repository.get("duration").getAttributeType());
    assertEquals("measurement-unit", repository.get("measure").getAttributeType());
  }

  @Test
  void testCodelistOfTheUnitsOfDuration() throws InstantiationException {
    SdkDataTypeRepository repository = ofSdk1();

    assertEquals("timeperiod",
        repository.get(repository.get("duration").getAttributeType()).getListName());
  }

  @Test
  void testTypesWithRootCodelist() throws InstantiationException {
    SdkDataTypeRepository repository = ofSdk1();

    assertEquals("currency", repository.get("currency").getListName());
    assertEquals("timeperiod", repository.get("duration-unit").getListName());
    assertEquals("language", repository.get("language").getListName());
    assertEquals("measurement-unit", repository.get("measurement-unit").getListName());
    assertNull(repository.get("currency").getAttributeName());
  }

  @Test
  void testAttributeOfTypeText() throws InstantiationException {
    SdkDataTypeRepository repository = ofSdk1();

    assertEquals("listName", repository.get("code").getAttributeName());
    assertEquals("text", repository.get("code").getAttributeType());
    assertNull(repository.get("code").getListName());
    assertEquals("schemeName", repository.get("id").getAttributeName());
    assertEquals("text", repository.get("id").getAttributeType());
  }

  @Test
  void testTypeWithoutAttribute() throws InstantiationException {
    SdkDataType date = ofSdk1().get("date");

    assertEquals("1970-01-01Z", date.getPrivacyMask());
    assertNull(date.getAttributeName());
    assertNull(date.getAttributeType());
    assertNull(date.getListName());
  }

  /**
   * The point of TEDEFO-5231: with SDK 2 the data types come from the SDK, not from the copy kept
   * in this library. The masking value of the fixture differs from the one of the resource, so
   * only the file can be the source.
   */
  @Test
  void testSdk2ReadsTheDataTypesFromTheSdk() throws InstantiationException {
    SdkDataTypeRepository repository = ofSdk2();

    assertEquals(5, repository.size(), "the SDK fixture holds fewer data types than the resource");
    assertEquals("undisclosed", repository.get("amount").getPrivacyMask());
    assertEquals("-1", ofSdk1().get("amount").getPrivacyMask(),
        "the resource of SDK 1 must be left alone");
  }

  @Test
  void testSdk2ReadsTheAttributesAndTheCodelistsFromTheSdk() throws InstantiationException {
    SdkDataTypeRepository repository = ofSdk2();

    assertEquals("unitCode", repository.get("duration").getAttributeName());
    assertEquals("duration-unit", repository.get("duration").getAttributeType());
    assertEquals("timeperiod", repository.get("duration-unit").getListName());
    assertEquals("currency", repository.get("currency").getListName());
  }

  /**
   * The file of the SDK also carries properties that a data type does not read (description,
   * valueType, displayTypes). They must be ignored, not break the loading.
   */
  @Test
  void testSdk2IgnoresThePropertiesThatADataTypeDoesNotRead() throws InstantiationException {
    SdkDataType date = ofSdk2().get("date");

    assertEquals("1970-01-01Z", date.getPrivacyMask());
    assertNull(date.getAttributeName());
    assertNull(date.getAttributeType());
    assertNull(date.getListName());
  }

  /** Each SDK version gets its own data type implementation, as the other entities do. */
  @Test
  void testEachSdkVersionGetsItsOwnDataTypes() throws InstantiationException {
    final SdkDataType ofSdk1 = ofSdk1().get("amount");
    final SdkDataType ofSdk2 = ofSdk2().get("amount");

    assertTrue(ofSdk1 instanceof SdkDataTypeV1, "SDK 1 gives " + ofSdk1.getClass());
    assertTrue(ofSdk2 instanceof SdkDataTypeV2, "SDK 2 gives " + ofSdk2.getClass());
  }

  @Test
  @SuppressWarnings("deprecation")
  void testCreateDefault_HasTheOriginalTypesWithTheirMasksOnly() throws InstantiationException {
    final SdkDataTypeRepository defaults = SdkDataTypeRepository.createDefault();
    final SdkDataTypeRepository dataTypes = ofSdk1();

    assertEquals(Set.of("text", "text-multilingual", "code", "internal-code", "id", "id-ref", "phone",
        "email", "url", "date", "zoned-date", "time", "zoned-time", "indicator", "integer", "number",
        "amount", "measure", "duration"), defaults.keySet());
    for (final SdkDataType dataType : defaults.values()) {
      assertEquals(dataTypes.get(dataType.getId()).getPrivacyMask(), dataType.getPrivacyMask());
      assertNull(dataType.getAttributeName());
      assertNull(dataType.getAttributeType());
      assertNull(dataType.getListName());
    }
  }

  /** The deprecated constructor still reads the resource, whatever the SDK version. */
  @Test
  @SuppressWarnings("deprecation")
  void testTheDeprecatedConstructorStillReadsTheResource() {
    assertEquals(23, new SdkDataTypeRepository().size());
  }
}
