package eu.europa.ted.eforms.sdk.entity.v2;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.entity.v1.SdkFieldV1;

/**
 * A field of SDK 2, read from fields/fwd/fields.json (TEDEFO-5229).
 *
 * <p>
 * Compared with fields/fields.json, the properties this entity reads differ as follows: repeatable
 * is a plain boolean instead of an object with a value; the privacy block is replaced by
 * disclosureControl, whose groupId is the privacy code and which also carries the two EFX
 * expressions of the fields that are withheld only for some of their instances; and the four
 * fields that held the disclosure data are no longer given per field, so they are not read here
 * (TEDEFO-5230 links them to each field instead).
 * </p>
 */
@SdkComponent(versions = {"2"}, componentType = SdkComponentType.FIELD)
public class SdkFieldV2 extends SdkFieldV1 {

  /**
   * Replaces the privacy block of fields/fields.json (TEDEFO-5128).
   */
  private static final String DISCLOSURE_CONTROL = "disclosureControl";

  private final String alias;

  public SdkFieldV2(String id, String type, String parentNodeId, String xpathAbsolute,
      String xpathRelative, String rootCodelistId, boolean repeatable, String alias) {
    super(id, type, parentNodeId, xpathAbsolute, xpathRelative, rootCodelistId, repeatable);
    this.alias = alias;
  }

  public SdkFieldV2(JsonNode fieldNode) {
    super(fieldNode);
    this.alias = fieldNode.has("alias") ? fieldNode.get("alias").asText(null) : null;
  }

  @JsonCreator
  public SdkFieldV2(
      @JsonProperty("id") final String id,
      @JsonProperty("type") final String type,
      @JsonProperty("parentNodeId") final String parentNodeId,
      @JsonProperty("xpathAbsolute") final String xpathAbsolute,
      @JsonProperty("xpathRelative") final String xpathRelative,
      @JsonProperty("codeList") final Map<String, Map<String, String>> codelist,
      @JsonProperty("repeatable") final Map<String, Object> repeatable,
      @JsonProperty("alias") final String alias) {
    this(id, type, parentNodeId, xpathAbsolute, xpathRelative, getCodelistId(codelist),
        getRepeatable(repeatable), alias);
  }

  public String getAlias() {
    return alias;
  }

  /**
   * fields/fwd/fields.json gives repeatable as a plain boolean, where fields/fields.json gives an
   * object with a value. Both are read, because an SDK 2 field is read from either file.
   */
  @Override
  protected boolean extractRepeatable(final JsonNode fieldNode) {
    final JsonNode repeatableNode = fieldNode.get("repeatable");
    if (repeatableNode != null && repeatableNode.isBoolean()) {
      return repeatableNode.booleanValue();
    }
    return super.extractRepeatable(fieldNode);
  }

  /**
   * fields/fwd/fields.json gives the privacy code as the groupId of the disclosureControl block,
   * where fields/fields.json gives it as the code of the privacy block.
   */
  @Override
  protected String extractPrivacyCode(final JsonNode fieldNode) {
    if (disclosureControlOf(fieldNode) == null) {
      return super.extractPrivacyCode(fieldNode);
    }
    return textOfDisclosureControl(fieldNode, "groupId");
  }

  /**
   * fields/fwd/fields.json no longer gives the fields that hold the disclosure data per field: they
   * are the same for every field, and TEDEFO-5230 links them to each field instead. They are still
   * read from the privacy block of fields/fields.json.
   */
  @Override
  protected PrivacySettings extractPrivacySettings(final JsonNode fieldNode) {
    if (disclosureControlOf(fieldNode) == null) {
      return super.extractPrivacySettings(fieldNode);
    }
    return null;
  }

  @Override
  protected String extractWithholdingCondition(final JsonNode fieldNode) {
    return textOfDisclosureControl(fieldNode, "withholdingCondition");
  }

  @Override
  protected String extractUndisclosedFieldSelector(final JsonNode fieldNode) {
    return textOfDisclosureControl(fieldNode, "undisclosedFieldSelector");
  }

  /**
   * SDK 2 separates duration from measure, so the mapping of measure onto duration that
   * {@link SdkFieldV1} applies for SDK 1 must not apply here.
   */
  @Override
  public String getType() {
    return getDeclaredType();
  }

  private static JsonNode disclosureControlOf(final JsonNode fieldNode) {
    return fieldNode.get(DISCLOSURE_CONTROL);
  }

  private static String textOfDisclosureControl(final JsonNode fieldNode, final String property) {
    final JsonNode disclosureControl = disclosureControlOf(fieldNode);
    if (disclosureControl == null || !disclosureControl.hasNonNull(property)) {
      return null;
    }
    return disclosureControl.get(property).asText(null);
  }
}
