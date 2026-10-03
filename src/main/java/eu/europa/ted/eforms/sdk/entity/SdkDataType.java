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
package eu.europa.ted.eforms.sdk.entity;

import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents an eForms SDK data type.
 *
 * Each field in the SDK has a type (e.g., "text", "date", "amount"). This entity captures
 * type-level metadata such as the privacy masking value, the attribute that the fields of the type
 * carry (e.g., the currency of an amount), and the code list of the units of the types whose values
 * have one. Currently hardcoded; will be loaded from data-types.json when it is added to the SDK.
 */
public class SdkDataType {
  private final String id;
  private final String privacyMask;
  private final String attributeName;
  private final String unitsCodelistId;

  @SuppressWarnings("unused")
  private SdkDataType() {
    throw new UnsupportedOperationException();
  }

  public SdkDataType(final String id, final String privacyMask) {
    this.id = id;
    this.privacyMask = privacyMask;
    this.attributeName = null;
    this.unitsCodelistId = null;
  }

  /**
   * Creates a data type from its entry in data-types.json.
   */
  protected SdkDataType(final JsonNode dataType) {
    this.id = dataType.get("type").asText(null);
    this.privacyMask =
        dataType.hasNonNull("maskingValue") ? dataType.get("maskingValue").asText(null) : null;
    final JsonNode attribute = dataType.get("attribute");
    this.attributeName = attribute != null && attribute.hasNonNull("name")
        ? attribute.get("name").asText(null)
        : null;
    this.unitsCodelistId = dataType.hasNonNull("unitsCodelistId")
        ? dataType.get("unitsCodelistId").asText(null)
        : null;
  }

  public String getId() {
    return this.id;
  }

  public String getPrivacyMask() {
    return this.privacyMask;
  }

  /**
   * Returns the name of the attribute that the fields of this data type carry (e.g., "currencyID"
   * for an amount), or null if they carry none.
   */
  public String getAttributeName() {
    return this.attributeName;
  }

  /**
   * Returns the identifier of the code list of the units of this data type (e.g., "currency" for an
   * amount), or null if its values have no unit.
   */
  public String getUnitsCodelistId() {
    return this.unitsCodelistId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SdkDataType other = (SdkDataType) obj;
    return Objects.equals(this.id, other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id);
  }

  @Override
  public String toString() {
    return this.id;
  }
}
