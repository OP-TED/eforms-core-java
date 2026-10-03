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
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import eu.europa.ted.eforms.sdk.entity.SdkDataType;
import eu.europa.ted.eforms.sdk.entity.v1.SdkDataTypeV1;
import eu.europa.ted.eforms.sdk.entity.v2.SdkDataTypeV2;

class SdkDataTypeRepositoryTest {

  @Test
  void testSdk1DataTypes() throws InstantiationException {
    assertEquals(SdkDataTypeV1.class, new SdkDataTypeRepository("1.10.0").get("amount").getClass());
  }

  @Test
  void testSdk2DataTypes() throws InstantiationException {
    assertEquals(SdkDataTypeV2.class,
        new SdkDataTypeRepository("eforms-sdk-2.0").get("amount").getClass());
  }

  @Test
  void testSdk2DataTypesFromThePathOfTheSdkFile() throws InstantiationException {
    // The path is not read until TEDEFO-5231, so any path will do.
    SdkDataTypeRepository repository =
        new SdkDataTypeRepository("2.0.0", Path.of("fields", "fwd", "data-types.json"));

    assertEquals(SdkDataTypeV2.class, repository.get("amount").getClass());
    assertEquals("currency", repository.get("amount").getAttributeCodelistId());
  }

  @Test
  void testAllDataTypesAreLoaded() throws InstantiationException {
    assertEquals(19, new SdkDataTypeRepository("2.0.0").size());
  }

  @Test
  void testAmountHasCurrencyAttribute() throws InstantiationException {
    SdkDataType amount = new SdkDataTypeRepository("2.0.0").get("amount");

    assertEquals("-1", amount.getPrivacyMask());
    assertEquals("currencyID", amount.getAttributeName());
    assertEquals("currency", amount.getAttributeCodelistId());
  }

  @Test
  void testDurationAndMeasureHaveTheirOwnUnits() throws InstantiationException {
    SdkDataTypeRepository repository = new SdkDataTypeRepository("2.0.0");

    assertEquals("duration-unit", repository.get("duration").getAttributeCodelistId());
    assertEquals("measurement-unit", repository.get("measure").getAttributeCodelistId());
  }

  @Test
  void testAttributeWithoutCodelist() throws InstantiationException {
    SdkDataType code = new SdkDataTypeRepository("2.0.0").get("code");

    assertEquals("listName", code.getAttributeName());
    assertNull(code.getAttributeCodelistId());
  }

  @Test
  void testTypeWithoutAttribute() throws InstantiationException {
    SdkDataType date = new SdkDataTypeRepository("2.0.0").get("date");

    assertEquals("1970-01-01Z", date.getPrivacyMask());
    assertNull(date.getAttributeName());
  }
}
