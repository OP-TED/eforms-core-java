package eu.europa.ted.eforms.sdk.entity.v1;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.entity.SdkField;

@SdkComponent(versions = {"1"}, componentType = SdkComponentType.FIELD)
public class SdkFieldV1 extends SdkField {

  public SdkFieldV1(final String id, final String type, final String parentNodeId,
      final String xpathAbsolute, final String xpathRelative, final String codelistId,
      final boolean repeatable) {
    super(id, type, parentNodeId, xpathAbsolute, xpathRelative, codelistId, repeatable);
  }

  public SdkFieldV1(final JsonNode field) {
    super(field);
  }

  @JsonCreator
  public SdkFieldV1(
      @JsonProperty("id") final String id,
      @JsonProperty("type") final String type,
      @JsonProperty("parentNodeId") final String parentNodeId,
      @JsonProperty("xpathAbsolute") final String xpathAbsolute,
      @JsonProperty("xpathRelative") final String xpathRelative,
      @JsonProperty("codeList") final Map<String, Map<String, String>> codelist,
      @JsonProperty("repeatable") final Map<String, Object> repeatable) {
    this(id, type, parentNodeId, xpathAbsolute, xpathRelative, getCodelistId(codelist),
        getRepeatable(repeatable));
  }

  // In SDK 1.x, "measure" semantically means "duration" (there was no "duration" type yet).
  @Override
  public String getType() {
    String type = super.getType();
    if ("measure".equals(type)) {
      return "duration";
    }
    return type;
  }

  protected static String getCodelistId(Map<String, Map<String, String>> codelist) {
    if (codelist == null) {
      return null;
    }

    Map<String, String> value = codelist.get("value");
    if (value == null) {
      return null;
    }

    return value.get("id");
  }

  protected static boolean getRepeatable(Map<String, Object> repeatable) {
    if (repeatable == null) {
      return false;
    }

    // If there are constraints, the field may repeat conditionally - treat as repeatable
    Object constraints = repeatable.get("constraints");
    if (constraints != null) {
      return true;
    }

    // Otherwise check the default value
    Object value = repeatable.get("value");
    return Boolean.TRUE.equals(value);
  }
}
