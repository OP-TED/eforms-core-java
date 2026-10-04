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
import java.util.Set;
import org.junit.jupiter.api.Test;
import eu.europa.ted.eforms.sdk.entity.SdkDataType;

class SdkDataTypeRepositoryTest {

  @Test
  void testAllDataTypesAreLoaded() {
    assertEquals(23, new SdkDataTypeRepository().size());
  }

  @Test
  void testAmountHasCurrencyAttribute() {
    SdkDataType amount = new SdkDataTypeRepository().get("amount");

    assertEquals("-1", amount.getPrivacyMask());
    assertEquals("currencyID", amount.getAttributeName());
    assertEquals("currency", amount.getAttributeType());
  }

  @Test
  void testDurationAndMeasureHaveTheirOwnUnits() {
    SdkDataTypeRepository repository = new SdkDataTypeRepository();

    assertEquals("duration-unit", repository.get("duration").getAttributeType());
    assertEquals("measurement-unit", repository.get("measure").getAttributeType());
  }

  @Test
  void testCodelistOfTheUnitsOfDuration() {
    SdkDataTypeRepository repository = new SdkDataTypeRepository();

    assertEquals("timeperiod",
        repository.get(repository.get("duration").getAttributeType()).getListName());
  }

  @Test
  void testTypesWithRootCodelist() {
    SdkDataTypeRepository repository = new SdkDataTypeRepository();

    assertEquals("currency", repository.get("currency").getListName());
    assertEquals("timeperiod", repository.get("duration-unit").getListName());
    assertEquals("language", repository.get("language").getListName());
    assertEquals("measurement-unit", repository.get("measurement-unit").getListName());
    assertNull(repository.get("currency").getAttributeName());
  }

  @Test
  void testAttributeOfTypeText() {
    SdkDataTypeRepository repository = new SdkDataTypeRepository();

    assertEquals("listName", repository.get("code").getAttributeName());
    assertEquals("text", repository.get("code").getAttributeType());
    assertNull(repository.get("code").getListName());
    assertEquals("schemeName", repository.get("id").getAttributeName());
    assertEquals("text", repository.get("id").getAttributeType());
  }

  @Test
  void testTypeWithoutAttribute() {
    SdkDataType date = new SdkDataTypeRepository().get("date");

    assertEquals("1970-01-01Z", date.getPrivacyMask());
    assertNull(date.getAttributeName());
    assertNull(date.getAttributeType());
    assertNull(date.getListName());
  }

  @Test
  @SuppressWarnings("deprecation")
  void testCreateDefault_HasTheOriginalTypesWithTheirMasksOnly() {
    final SdkDataTypeRepository defaults = SdkDataTypeRepository.createDefault();
    final SdkDataTypeRepository dataTypes = new SdkDataTypeRepository();

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
}
