package eu.europa.ted.eforms.sdk.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.entity.SdkEntityFactory;
import eu.europa.ted.eforms.sdk.entity.SdkNode;

/**
 * The nodes of an SDK, by identifier.
 *
 * <p>
 * Each SDK version keeps its nodes in its own file. The repository of an SDK version, which knows
 * that file, is given by {@link SdkRepositoryFactory#getSdkNodeRepository(String, Path)}.
 * </p>
 */
public class SdkNodeRepository extends MapFromJson<SdkNode> {
  private static final long serialVersionUID = 1L;

  /**
   * The nodes that have a special role, by their key (TEDEFO-5230). Null for the SDK versions that
   * publish no such map.
   *
   * <p>
   * Deliberately left without an initialiser: it is assigned while the map of nodes is being
   * populated, which happens during the call to super, and a field initialiser would run after
   * that call and undo it.
   * </p>
   */
  private Map<String, String> specialPurpose;

  /**
   * Creates the repository of the nodes from the xmlStructure array of the given fields.json.
   */
  public SdkNodeRepository(String sdkVersion, Path jsonPath) throws InstantiationException {
    super(sdkVersion, jsonPath);
  }

  @Override
  protected void populateMap(final JsonNode json) throws InstantiationException {
    this.addNodes((ArrayNode) json.get(SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY));
  }

  /**
   * Adds the given nodes, each linked to its parent node.
   */
  protected void addNodes(final ArrayNode nodes) throws InstantiationException {
    final List<SdkNode> needsParentWiring = new ArrayList<>();

    // First pass: create all nodes, optimistically set parent if already loaded
    for (final JsonNode node : nodes) {
      final SdkNode sdkNode = SdkEntityFactory.getSdkNode(this.sdkVersion, node);
      this.put(sdkNode.getId(), sdkNode);

      if (sdkNode.getParentId() != null) {
        final SdkNode parent = this.get(sdkNode.getParentId());
        if (parent != null) {
          sdkNode.setParent(parent);
        } else {
          needsParentWiring.add(sdkNode);
        }
      }
    }

    // Second pass: wire up any nodes whose parent wasn't loaded yet
    for (final SdkNode sdkNode : needsParentWiring) {
      sdkNode.setParent(this.get(sdkNode.getParentId()));
    }
  }

  /**
   * Reads the map of the nodes that have a special role, which the given file starts with.
   */
  protected void readSpecialPurpose(final JsonNode json) {
    this.specialPurpose = SpecialPurpose.read(json);
  }

  /**
   * @return the nodes that have a special role, by their key, as the SDK gives them. Empty for the
   *         SDK versions that publish no such map.
   */
  public Map<String, String> getSpecialPurpose() {
    return this.specialPurpose == null ? Collections.emptyMap() : this.specialPurpose;
  }

  /**
   * @param key a special purpose key, such as "root"
   * @return the identifier of the node that has that role, or null if the SDK names none. An
   *         unknown key is not an error.
   */
  public String getSpecialPurposeNodeId(final String key) {
    return this.getSpecialPurpose().get(key);
  }
}
