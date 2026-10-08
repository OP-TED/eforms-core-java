package eu.europa.ted.eforms.sdk.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  private final String withholdingCondition;
  private final String undisclosedFieldSelector;
  private final List<String> attributes;
  private final String attributeOf;
  private final String attributeName;
  private SdkNode parentNode;
  private List<SdkField> attributeFields;
  private SdkField attributeOfField;
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
    this.withholdingCondition = null;
    this.undisclosedFieldSelector = null;
    this.attributes = Collections.emptyList();
    this.attributeOf = null;
    this.attributeName = null;
  }

  protected SdkField(final JsonNode fieldNode) {
    this.id = fieldNode.get("id").asText(null);
    this.parentNodeId = fieldNode.get("parentNodeId").asText(null);
    this.xpathAbsolute = fieldNode.get("xpathAbsolute").asText(null);
    this.xpathRelative = fieldNode.get("xpathRelative").asText(null);
    this.type = fieldNode.get("type").asText(null);
    this.codelistId = extractCodelistId(fieldNode);
    this.repeatable = extractRepeatable(fieldNode);
    this.privacyCode = extractPrivacyCode(fieldNode);
    this.privacySettings = extractPrivacySettings(fieldNode);
    this.withholdingCondition = extractWithholdingCondition(fieldNode);
    this.undisclosedFieldSelector = extractUndisclosedFieldSelector(fieldNode);
    this.attributes = extractAttributes(fieldNode);
    this.attributeOf = fieldNode.has("attributeOf") ? fieldNode.get("attributeOf").asText(null) : null;
    this.attributeName = fieldNode.has("attributeName") ? fieldNode.get("attributeName").asText(null) : null;
  }

  protected List<String> extractAttributes(final JsonNode fieldNode) {
    final JsonNode attributesNode = fieldNode.get("attributes");
    if (attributesNode == null || !attributesNode.isArray()) {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<>();
    for (JsonNode attr : attributesNode) {
      result.add(attr.asText());
    }
    return Collections.unmodifiableList(result);
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

  /**
   * Returns the privacy code of the field, from the privacy block of fields/fields.json. SDK 2
   * gives it as the groupId of the disclosureControl block instead (TEDEFO-5229).
   *
   * <p>
   * Called while this field is being constructed, so an override must read the given node only,
   * and no state of its own.
   * </p>
   */
  protected String extractPrivacyCode(final JsonNode fieldNode) {
    final JsonNode privacyNode = fieldNode.get("privacy");
    return privacyNode != null ? privacyNode.get("code").asText(null) : null;
  }

  /**
   * Returns the fields that hold the disclosure data of this field, from the privacy block of
   * fields/fields.json. SDK 2 no longer gives them per field: they are the same for every field,
   * and are linked to each field in TEDEFO-5230.
   *
   * <p>
   * Called while this field is being constructed, so an override must read the given node only,
   * and no state of its own.
   * </p>
   */
  protected PrivacySettings extractPrivacySettings(final JsonNode fieldNode) {
    return extractPrivacy(fieldNode.get("privacy"));
  }

  /**
   * Returns the EFX condition under which this field is withheld, for the fields that are withheld
   * only for some of their instances, or null for every other field. Only SDK 2 gives it, in the
   * disclosureControl block (TEDEFO-5229). It is not translated.
   *
   * <p>
   * Called while this field is being constructed, so an override must read the given node only,
   * and no state of its own.
   * </p>
   */
  protected String extractWithholdingCondition(final JsonNode fieldNode) {
    return null;
  }

  /**
   * Returns the EFX selector locating this field when it is withheld, for the fields that are
   * withheld only for some of their instances, or null for every other field. Only SDK 2 gives it,
   * in the disclosureControl block (TEDEFO-5229). It is not translated.
   *
   * <p>
   * Called while this field is being constructed, so an override must read the given node only,
   * and no state of its own.
   * </p>
   */
  protected String extractUndisclosedFieldSelector(final JsonNode fieldNode) {
    return null;
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

  /**
   * Returns the type as the SDK declares it, without any version-specific mapping. Lets a version
   * that must not inherit such a mapping bypass it (see SdkFieldV1's measure/duration mapping,
   * which applies to SDK 1 only).
   */
  protected final String getDeclaredType() {
    return this.type;
  }

  public String getCodelistId() {
    return this.codelistId;
  }

  public List<String> getAttributes() {
    return this.attributes;
  }

  public String getAttributeOf() {
    return this.attributeOf;
  }

  public String getAttributeName() {
    return this.attributeName;
  }

  public List<SdkField> getAttributeFields() {
    return this.attributeFields;
  }

  public void setAttributeFields(List<SdkField> attributeFields) {
    this.attributeFields = Collections.unmodifiableList(attributeFields);
  }

  public SdkField getAttributeOfField() {
    return this.attributeOfField;
  }

  public void setAttributeOfField(SdkField attributeOfField) {
    this.attributeOfField = attributeOfField;
  }

  /**
   * Returns the attribute field with the given XML attribute name (e.g. "unitCode", "listName"),
   * or null if this field has no such attribute.
   */
  public SdkField getAttributeField(String attrName) {
    if (this.attributeFields == null) {
      return null;
    }
    for (SdkField attrField : this.attributeFields) {
      if (attrName.equals(attrField.getAttributeName())) {
        return attrField;
      }
    }
    return null;
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

  /**
   * @return the EFX condition ({@code {fieldId} ${conditionEfx}}) under which this field is
   *         withheld, as the SDK gives it and untranslated, or null when this field is not withheld
   *         or is withheld for all of its instances. Only SDK 2 gives it (TEDEFO-5229).
   */
  public String getWithholdingCondition() {
    return this.withholdingCondition;
  }

  /**
   * @return the EFX selector ({@code {ND-Root} ${fieldId[conditionEfx]}}) locating this field when
   *         it is withheld, as the SDK gives it and untranslated, or null when this field is not
   *         withheld or is withheld for all of its instances. Only SDK 2 gives it (TEDEFO-5229).
   */
  public String getUndisclosedFieldSelector() {
    return this.undisclosedFieldSelector;
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
