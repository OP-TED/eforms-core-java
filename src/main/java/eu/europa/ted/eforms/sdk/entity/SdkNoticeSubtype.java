package eu.europa.ted.eforms.sdk.entity;

import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a notice subtype from the SDK's notice-types.json file.
 */
public abstract class SdkNoticeSubtype {
  private final String subTypeId;
  private final String documentType;
  private final String type;

  protected SdkNoticeSubtype(String subTypeId, String documentType, String type) {
    this.subTypeId = subTypeId;
    this.documentType = documentType;
    this.type = type;
  }

  protected SdkNoticeSubtype(JsonNode json) {
    this.subTypeId = json.get("subTypeId").asText();
    this.documentType = json.get("documentType").asText();
    this.type = json.get("type").asText();
  }

  /**
   * Returns the notice subtype ID (e.g., "1", "3", "CEI", "E1", "X01").
   * This is the primary identifier used for phase generation.
   */
  public String getId() {
    return subTypeId;
  }

  public String getSubTypeId() {
    return subTypeId;
  }

  public String getDocumentType() {
    return documentType;
  }

  public String getType() {
    return type;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SdkNoticeSubtype other = (SdkNoticeSubtype) obj;
    return Objects.equals(subTypeId, other.subTypeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subTypeId);
  }

  @Override
  public String toString() {
    return subTypeId;
  }
}
