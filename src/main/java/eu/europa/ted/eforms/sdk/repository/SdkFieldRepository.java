package eu.europa.ted.eforms.sdk.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    // First pass: create all field entities and add them to the map
    for (final JsonNode field : fields) {
      final SdkField sdkField = SdkEntityFactory.getSdkField(sdkVersion, field);
      put(sdkField.getId(), sdkField);

      if (nodes != null && sdkField.getParentNodeId() != null) {
        sdkField.setParentNode(nodes.get(sdkField.getParentNodeId()));
      }
    }

    // Second pass: resolve cross-field references
    for (final SdkField sdkField : this.values()) {
      if (sdkField.getPrivacySettings() != null) {
        SdkField.PrivacySettings privacy = sdkField.getPrivacySettings();

        if (privacy.getPrivacyCodeFieldId() != null) {
          privacy.setPrivacyCodeField(this.get(privacy.getPrivacyCodeFieldId()));
        }
        if (privacy.getJustificationCodeFieldId() != null) {
          privacy.setJustificationCodeField(this.get(privacy.getJustificationCodeFieldId()));
        }
        if (privacy.getJustificationDescriptionFieldId() != null) {
          privacy.setJustificationDescriptionField(
              this.get(privacy.getJustificationDescriptionFieldId()));
        }
        if (privacy.getPublicationDateFieldId() != null) {
          privacy.setPublicationDateField(this.get(privacy.getPublicationDateFieldId()));
        }
      }

      if (!sdkField.getAttributes().isEmpty()) {
        List<SdkField> attrFields = new ArrayList<>();
        for (String attrFieldId : sdkField.getAttributes()) {
          SdkField attrField = this.get(attrFieldId);
          if (attrField != null) {
            attrFields.add(attrField);
          }
        }
        sdkField.setAttributeFields(attrFields);
      }

      if (sdkField.getAttributeOf() != null) {
        sdkField.setAttributeOfField(this.get(sdkField.getAttributeOf()));
      }
    }
  }
}
