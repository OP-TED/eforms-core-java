package eu.europa.ted.eforms.sdk.entity.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.entity.SdkNode;

/**
 * A node is something like a section. Nodes can be parents of other nodes or parents of fields.
 */
@SdkComponent(versions = {"1"}, componentType = SdkComponentType.NODE)
public class SdkNodeV1 extends SdkNode {

  @JsonCreator
  public SdkNodeV1(
      @JsonProperty("id") String id,
      @JsonProperty("parentId") String parentId,
      @JsonProperty("xpathAbsolute") String xpathAbsolute,
      @JsonProperty("xpathRelative") String xpathRelative,
      @JsonProperty("repeatable") boolean repeatable) {
    super(id, parentId, xpathAbsolute, xpathRelative, repeatable);
  }

  public SdkNodeV1(JsonNode node) {
    super(node);
  }
}
