package eu.europa.ted.eforms.sdk.entity;

import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ted.eforms.xpath.XPathInfo;
import eu.europa.ted.eforms.xpath.XPathProcessor;

public abstract class SdkField implements Comparable<SdkField> {
  private final String id;
  private final String xpathAbsolute;
  private final String xpathRelative;
  private final String parentNodeId;
  private final String type;
  private final String codelistId;
  private final boolean repeatable;
  private final String privacyCode;
  private final PrivacySettings privacySettings;
  private SdkNode parentNode;
  private XPathInfo xpathInfo;

  /**
   * Privacy settings for fields that can be withheld from publication.
   */
  public static class PrivacySettings {
    private final String privacyCodeFieldId;
    private final String justificationCodeFieldId;
    private final String justificationDescriptionFieldId;
    private final String publicationDateFieldId;
    private SdkField privacyCodeField;
    private SdkField justificationCodeField;
    private SdkField justificationDescriptionField;
    private SdkField publicationDateField;

    public PrivacySettings(final String privacyCodeFieldId,
        final String justificationCodeFieldId, final String justificationDescriptionFieldId,
        final String publicationDateFieldId) {
      this.privacyCodeFieldId = privacyCodeFieldId;
      this.justificationCodeFieldId = justificationCodeFieldId;
      this.justificationDescriptionFieldId = justificationDescriptionFieldId;
      this.publicationDateFieldId = publicationDateFieldId;
    }

    public String getPrivacyCodeFieldId() {
      return this.privacyCodeFieldId;
    }

    public String getJustificationCodeFieldId() {
      return this.justificationCodeFieldId;
    }

    public String getJustificationDescriptionFieldId() {
      return this.justificationDescriptionFieldId;
    }

    public String getPublicationDateFieldId() {
      return this.publicationDateFieldId;
    }

    public SdkField getPrivacyCodeField() {
      return this.privacyCodeField;
    }

    public void setPrivacyCodeField(SdkField privacyCodeField) {
      this.privacyCodeField = privacyCodeField;
    }

    public SdkField getJustificationCodeField() {
      return this.justificationCodeField;
    }

    public void setJustificationCodeField(SdkField justificationCodeField) {
      this.justificationCodeField = justificationCodeField;
    }

    public SdkField getJustificationDescriptionField() {
      return this.justificationDescriptionField;
    }

    public void setJustificationDescriptionField(SdkField justificationDescriptionField) {
      this.justificationDescriptionField = justificationDescriptionField;
    }

    public SdkField getPublicationDateField() {
      return this.publicationDateField;
    }

    public void setPublicationDateField(SdkField publicationDateField) {
      this.publicationDateField = publicationDateField;
    }
  }

  @SuppressWarnings("unused")
  private SdkField() {
    throw new UnsupportedOperationException();
  }

  protected SdkField(final String id, final String type, final String parentNodeId,
      final String xpathAbsolute, final String xpathRelative, final String codelistId) {
    this(id, type, parentNodeId, xpathAbsolute, xpathRelative, codelistId, false);
  }

  protected SdkField(final String id, final String type, final String parentNodeId,
      final String xpathAbsolute, final String xpathRelative, final String codelistId,
      final boolean repeatable) {
    this.id = id;
    this.parentNodeId = parentNodeId;
    this.xpathAbsolute = xpathAbsolute;
    this.xpathRelative = xpathRelative;
    this.type = type;
    this.codelistId = codelistId;
    this.repeatable = repeatable;
    this.privacyCode = null;
    this.privacySettings = null;
  }

  protected SdkField(final JsonNode fieldNode) {
    this.id = fieldNode.get("id").asText(null);
    this.parentNodeId = fieldNode.get("parentNodeId").asText(null);
    this.xpathAbsolute = fieldNode.get("xpathAbsolute").asText(null);
    this.xpathRelative = fieldNode.get("xpathRelative").asText(null);
    this.type = fieldNode.get("type").asText(null);
    this.codelistId = extractCodelistId(fieldNode);
    this.repeatable = extractRepeatable(fieldNode);
    final JsonNode privacyNode = fieldNode.get("privacy");
    this.privacyCode = privacyNode != null ? privacyNode.get("code").asText(null) : null;
    this.privacySettings = extractPrivacy(privacyNode);
  }

  protected String extractCodelistId(final JsonNode fieldNode) {
    final JsonNode codelistNode = fieldNode.get("codeList");
    if (codelistNode == null) {
      return null;
    }

    final JsonNode valueNode = codelistNode.get("value");
    if (valueNode == null) {
      return null;
    }

    return valueNode.get("id").asText(null);
  }

  protected boolean extractRepeatable(final JsonNode fieldNode) {
    final JsonNode repeatableNode = fieldNode.get("repeatable");
    if (repeatableNode == null) {
      return false;
    }

    final JsonNode valueNode = repeatableNode.get("value");
    if (valueNode == null) {
      return false;
    }

    return valueNode.asBoolean(false);
  }

  protected PrivacySettings extractPrivacy(final JsonNode privacyNode) {
    if (privacyNode == null) {
      return null;
    }

    final String privacyCodeFieldId = privacyNode.get("unpublishedFieldId").asText(null);
    final String justificationCodeFieldId = privacyNode.get("reasonCodeFieldId").asText(null);
    final String justificationDescriptionFieldId =
        privacyNode.get("reasonDescriptionFieldId").asText(null);
    final String publicationDateFieldId = privacyNode.get("publicationDateFieldId").asText(null);

    return new PrivacySettings(privacyCodeFieldId, justificationCodeFieldId,
        justificationDescriptionFieldId, publicationDateFieldId);
  }

  public String getId() {
    return this.id;
  }

  public String getParentNodeId() {
    return this.parentNodeId;
  }

  public String getXpathAbsolute() {
    return this.xpathAbsolute;
  }

  public String getXpathRelative() {
    return this.xpathRelative;
  }

  public String getType() {
    return this.type;
  }

  public String getCodelistId() {
    return this.codelistId;
  }

  public boolean isRepeatable() {
    return this.repeatable;
  }

  public String getPrivacyCode() {
    return this.privacyCode;
  }

  public PrivacySettings getPrivacySettings() {
    return this.privacySettings;
  }

  public SdkNode getParentNode() {
    return this.parentNode;
  }

  public void setParentNode(SdkNode parentNode) {
    this.parentNode = parentNode;
  }

  /**
   * Returns parsed XPath information for this field.
   * Provides access to attribute info, path decomposition, and predicate checks.
   * Lazily initialized on first access.
   */
  public XPathInfo getXpathInfo() {
    if (this.xpathInfo == null) {
      this.xpathInfo = XPathProcessor.parse(this.xpathAbsolute);
    }
    return this.xpathInfo;
  }

  /**
   * Helps with hash maps collisions. Should be consistent with equals.
   */
  @Override
  public int compareTo(SdkField o) {
    return this.getId().compareTo(o.getId());
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
    SdkField other = (SdkField) obj;
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
