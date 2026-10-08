package eu.europa.ted.eforms.sdk.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.SdkVersion;
import eu.europa.ted.eforms.sdk.entity.SdkEntityFactory;
import eu.europa.ted.eforms.sdk.entity.SdkNode;
import eu.europa.ted.eforms.sdk.resource.SdkResourceLoader;

/**
 * Repository of the nodes of an SDK.
 *
 * <p>
 * SDK 1 publishes its nodes in the xmlStructure array of fields/fields.json. SDK 2 publishes them
 * in the nodes array of fields/fwd/nodes.json, in the forward-compatible structure planned for
 * SDK 3 (TEDEFO-5228). The nodes have the same properties in both, so the node entities are the
 * same. This repository decides where the nodes of each SDK version come from, so that its callers
 * do not have to check the SDK version.
 * </p>
 */
public class SdkNodeRepository extends MapFromJson<SdkNode> {
  private static final long serialVersionUID = 1L;

  /**
   * The major version of the only SDK that publishes no nodes of its own.
   */
  private static final String SDK_MAJOR_WITHOUT_OWN_NODES = "1";

  /**
   * The nodes that have a special role, by their key (TEDEFO-5230). Empty for the SDK versions
   * that publish no such map.
   *
   * <p>
   * Deliberately left without an initialiser: it is assigned while the map of nodes is being
   * populated, which happens during the call to super, and a field initialiser would run after
   * that call and undo it.
   * </p>
   */
  private Map<String, String> specialPurpose;

  /**
   * Creates the repository of the nodes of the given SDK, reading them from the file that this SDK
   * version publishes them in.
   *
   * @param sdkVersion the target SDK version, which decides which file the nodes are read from
   * @param sdkRootPath path of the root SDK folder
   * @return the nodes of that SDK, by identifier
   * @throws InstantiationException if a node cannot be created for this SDK version
   */
  public static SdkNodeRepository forSdk(final String sdkVersion, final Path sdkRootPath)
      throws InstantiationException {
    if (SDK_MAJOR_WITHOUT_OWN_NODES.equals(new SdkVersion(sdkVersion).getMajor())) {
      return new SdkNodeRepository(sdkVersion,
          SdkResourceLoader.getResourceAsPath(sdkVersion, SdkConstants.SdkResource.FIELDS_JSON,
              sdkRootPath),
          SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY);
    }
    return new SdkNodeRepository(sdkVersion,
        SdkResourceLoader.getResourceAsPath(sdkVersion,
            SdkConstants.SdkResource.FIELDS_FWD_NODES_JSON, sdkRootPath),
        SdkConstants.NODES_JSON_NODES_KEY);
  }

  /**
   * Creates the repository of the nodes from the xmlStructure array of the given fields.json.
   *
   * @deprecated Use {@link #forSdk(String, Path)}, which reads the nodes of SDK 2 from
   *             fields/fwd/nodes.json instead of fields/fields.json.
   */
  @Deprecated
  public SdkNodeRepository(String sdkVersion, Path jsonPath) throws InstantiationException {
    this(sdkVersion, jsonPath, SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY);
  }

  private SdkNodeRepository(final String sdkVersion, final Path jsonPath,
      final String nodesArrayKey) throws InstantiationException {
    // The key travels as context: it must be known while the map is being populated, which
    // happens during this call to super.
    super(sdkVersion, jsonPath, nodesArrayKey);
  }

  @Override
  protected void populateMap(final JsonNode json) throws InstantiationException {
    populateMap(json, SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY);
  }

  @Override
  protected void populateMap(final JsonNode json, final Object... context)
      throws InstantiationException {
    final String nodesArrayKey = context.length > 0 && context[0] instanceof String
        ? (String) context[0]
        : SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY;

    this.specialPurpose = SpecialPurpose.read(json);

    final ArrayNode nodes = (ArrayNode) json.get(nodesArrayKey);
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

  /**
   * @return the nodes that have a special role, by their key, as the SDK gives them. Empty for the
   *         SDK versions that publish no such map.
   */
  public Map<String, String> getSpecialPurpose() {
    return specialPurpose == null ? Collections.emptyMap() : specialPurpose;
  }

  /**
   * @param key a special purpose key, such as "root"
   * @return the identifier of the node that has that role, or null if the SDK names none. An
   *         unknown key is not an error.
   */
  public String getSpecialPurposeNodeId(final String key) {
    return getSpecialPurpose().get(key);
  }
}
