package eu.europa.ted.eforms.sdk.repository;

import java.nio.file.Path;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.entity.SdkEntityFactory;
import eu.europa.ted.eforms.sdk.entity.SdkField;

public class SdkFieldRepository extends MapFromJson<SdkField> {
  private static final long serialVersionUID = 1L;

  public SdkFieldRepository(String sdkVersion, Path jsonPath) throws InstantiationException {
    super(sdkVersion, jsonPath);
  }

  public SdkFieldRepository(String sdkVersion, Path jsonPath, SdkNodeRepository nodeRepository)
      throws InstantiationException {
    super(sdkVersion, jsonPath, nodeRepository);
  }

  @Override
  protected void populateMap(final JsonNode json) throws InstantiationException {
    populateMap(json, new Object[0]);
  }

  @Override
  protected void populateMap(final JsonNode json, final Object... context)
      throws InstantiationException {
    SdkNodeRepository nodes = (context.length > 0 && context[0] instanceof SdkNodeRepository)
        ? (SdkNodeRepository) context[0]
        : null;

    final ArrayNode fields = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_FIELDS_KEY);
    for (final JsonNode field : fields) {
      final SdkField sdkField = SdkEntityFactory.getSdkField(sdkVersion, field);
      put(sdkField.getId(), sdkField);

      if (nodes != null && sdkField.getParentNodeId() != null) {
        sdkField.setParentNode(nodes.get(sdkField.getParentNodeId()));
      }
    }
  }
}
