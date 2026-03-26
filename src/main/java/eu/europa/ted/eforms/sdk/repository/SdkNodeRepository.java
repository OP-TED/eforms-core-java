package eu.europa.ted.eforms.sdk.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.entity.SdkEntityFactory;
import eu.europa.ted.eforms.sdk.entity.SdkNode;

public class SdkNodeRepository extends MapFromJson<SdkNode> {
  private static final long serialVersionUID = 1L;

  public SdkNodeRepository(String sdkVersion, Path jsonPath) throws InstantiationException {
    super(sdkVersion, jsonPath);
  }

  @Override
  protected void populateMap(final JsonNode json) throws InstantiationException {
    final ArrayNode nodes = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY);
    List<SdkNode> needsParentWiring = new ArrayList<>();

    // First pass: create all nodes, optimistically set parent if already loaded
    for (final JsonNode node : nodes) {
      final SdkNode sdkNode = SdkEntityFactory.getSdkNode(sdkVersion, node);
      put(sdkNode.getId(), sdkNode);

      if (sdkNode.getParentId() != null) {
        SdkNode parent = get(sdkNode.getParentId());
        if (parent != null) {
          sdkNode.setParent(parent);
        } else {
          needsParentWiring.add(sdkNode);
        }
      }
    }

    // Second pass: wire up any nodes whose parent wasn't loaded yet
    for (SdkNode sdkNode : needsParentWiring) {
      sdkNode.setParent(get(sdkNode.getParentId()));
    }
  }
}
