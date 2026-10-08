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
 * carry (e.g., the currency of an amount) with its data type, and the code list of the data types
 * whose values come from one.
 *
 * <p>
 * SDK 1 publishes no data types, so they come from the data-types.json resource of this library;
 * SDK 2 publishes them in fields/fwd/data-types.json (TEDEFO-5231). The
 * version-specific implementations are
 * {@link eu.europa.ted.eforms.sdk.entity.v1.SdkDataTypeV1} and
 * {@link eu.europa.ted.eforms.sdk.entity.v2.SdkDataTypeV2}, selected by
 * {@link SdkEntityFactory#getSdkDataType(String, JsonNode)}.
 * </p>
 */
public class SdkDataType {
  private final String id;
  private final String privacyMask;
  private final String attributeName;
  private final String attributeType;
  private final String listName;

  @SuppressWarnings("unused")
  private SdkDataType() {
    throw new UnsupportedOperationException();
  }

  public SdkDataType(final String id, final String privacyMask) {
    this.id = id;
    this.privacyMask = privacyMask;
    this.attributeName = null;
    this.attributeType = null;
    this.listName = null;
  }

  /**
   * Creates a data type from its entry in data-types.json.
   */
  public SdkDataType(final JsonNode dataType) {
    this.id = dataType.get("type").asText(null);
    this.privacyMask =
        dataType.hasNonNull("maskingValue") ? dataType.get("maskingValue").asText(null) : null;
    final JsonNode attribute = dataType.get("attribute");
    this.attributeName = attribute != null && attribute.hasNonNull("name")
        ? attribute.get("name").asText(null)
        : null;
    this.attributeType = attribute != null && attribute.hasNonNull("type")
        ? attribute.get("type").asText(null)
        : null;
    this.listName =
        dataType.hasNonNull("listName") ? dataType.get("listName").asText(null) : null;
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
   * Returns the data type of the attribute that the fields of this data type carry (e.g., "currency"
   * for an amount), or null if they carry none.
   */
  public String getAttributeType() {
    return this.attributeType;
  }

  /**
   * Returns the name of the root code list that the values of this data type come from (e.g.,
   * "timeperiod" for a duration unit), or null if they come from none.
   */
  public String getListName() {
    return this.listName;
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
