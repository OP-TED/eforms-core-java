package eu.europa.ted.eforms.sdk.entity.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.entity.v1.SdkNodeV1;

/**
 * A node is something like a section. Nodes can be parents of other nodes or parents of fields.
 */
@SdkComponent(versions = {"2"}, componentType = SdkComponentType.NODE)
public class SdkNodeV2 extends SdkNodeV1 {
  private final String alias;

  @JsonCreator
  public SdkNodeV2(
      @JsonProperty("id") String id,
      @JsonProperty("parentId") String parentId,
      @JsonProperty("xpathAbsolute") String xpathAbsolute,
      @JsonProperty("xpathRelative") String xpathRelative,
      @JsonProperty("repeatable") boolean repeatable,
      @JsonProperty("alias") String alias) {
    super(id, parentId, xpathAbsolute, xpathRelative, repeatable);
    this.alias = alias;
  }

  public SdkNodeV2(JsonNode node) {
    super(node);
    this.alias = node.has("alias") ? node.get("alias").asText(null) : null;
  }

  public String getAlias() {
    return alias;
  }
}
