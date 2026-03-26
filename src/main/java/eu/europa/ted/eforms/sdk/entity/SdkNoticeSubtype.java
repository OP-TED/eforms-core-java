package eu.europa.ted.eforms.sdk.entity;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a notice subtype from the SDK's notice-types.json file.
 */
public abstract class SdkNoticeSubtype implements Comparable<SdkNoticeSubtype> {
  private static final Pattern ID_PATTERN = Pattern.compile("^([A-Za-z_-]*)(\\d+)([A-Za-z_-][A-Za-z0-9_-]*)?$");

  private final String subTypeId;
  private final String documentType;
  private final String type;
  private final String prefix;
  private final int number;
  private final String suffix;

  protected SdkNoticeSubtype(String subTypeId, String documentType, String type) {
    this.subTypeId = subTypeId;
    this.documentType = documentType;
    this.type = type;

    Matcher m = ID_PATTERN.matcher(subTypeId != null ? subTypeId : "");
    if (m.matches()) {
      this.prefix = m.group(1);
      this.number = Integer.parseInt(m.group(2));
      this.suffix = m.group(3) != null ? m.group(3) : "";
    } else {
      this.prefix = subTypeId != null ? subTypeId : "";
      this.number = 0;
      this.suffix = "";
    }
  }

  protected SdkNoticeSubtype(JsonNode json) {
    this(json.get("subTypeId").asText(null),
        json.get("documentType").asText(null),
        json.get("type").asText(null));
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
  public int compareTo(SdkNoticeSubtype o) {
    int cmp = this.prefix.compareTo(o.prefix);
    if (cmp != 0) {
      return cmp;
    }
    cmp = Integer.compare(this.number, o.number);
    if (cmp != 0) {
      return cmp;
    }
    return this.suffix.compareTo(o.suffix);
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
