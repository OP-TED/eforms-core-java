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
import eu.europa.ted.eforms.sdk.entity.SdkField;

/**
 * The fields of an SDK, by identifier.
 *
 * <p>
 * Each SDK version keeps its fields in its own file. The repository of an SDK version, which knows
 * that file, is given by
 * {@link SdkRepositoryFactory#getSdkFieldRepository(String, Path, SdkNodeRepository)}.
 * </p>
 */
public class SdkFieldRepository extends MapFromJson<SdkField> {
  private static final long serialVersionUID = 1L;

  /**
   * The fields that have a special role, by their key (TEDEFO-5230). Null for the SDK versions
   * that publish no such map.
   *
   * <p>
   * Deliberately left without an initialiser: it is assigned while the map of fields is being
   * populated, which happens during the call to super, and a field initialiser would run after
   * that call and undo it.
   * </p>
   */
  private Map<String, String> specialPurpose;

  public SdkFieldRepository(String sdkVersion, Path jsonPath) throws InstantiationException {
    super(sdkVersion, jsonPath);
  }

  public SdkFieldRepository(String sdkVersion, Path jsonPath, SdkNodeRepository nodeRepository)
      throws InstantiationException {
    super(sdkVersion, jsonPath, nodeRepository);
  }

  @Override
  protected void populateMap(final JsonNode json) throws InstantiationException {
    this.populateMap(json, new Object[0]);
  }

  @Override
  protected void populateMap(final JsonNode json, final Object... context)
      throws InstantiationException {
    this.addFields((ArrayNode) json.get(SdkConstants.FIELDS_JSON_FIELDS_KEY),
        nodeRepositoryOf(context));
    this.resolveReferences();
  }

  /**
   * @return the repository of nodes given as context, to link each field to its parent node, or
   *         null to leave the parent nodes unlinked
   */
  protected static SdkNodeRepository nodeRepositoryOf(final Object... context) {
    return context.length > 0 && context[0] instanceof SdkNodeRepository
        ? (SdkNodeRepository) context[0]
        : null;
  }

  /**
   * Adds the given fields, each linked to its parent node when the nodes are given.
   */
  protected void addFields(final ArrayNode fields, final SdkNodeRepository nodes)
      throws InstantiationException {
    for (final JsonNode field : fields) {
      final SdkField sdkField = SdkEntityFactory.getSdkField(this.sdkVersion, field);
      this.put(sdkField.getId(), sdkField);
      if (nodes != null && sdkField.getParentNodeId() != null) {
        sdkField.setParentNode(nodes.get(sdkField.getParentNodeId()));
      }
    }
  }

  /**
   * Resolves the fields that each field refers to by identifier: its disclosure fields, its
   * attribute fields and the field it is an attribute of.
   */
  protected void resolveReferences() {
    for (final SdkField sdkField : this.values()) {
      if (sdkField.getPrivacySettings() != null) {
        final SdkField.PrivacySettings privacy = sdkField.getPrivacySettings();
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
        final List<SdkField> attrFields = new ArrayList<>();
        for (final String attrFieldId : sdkField.getAttributes()) {
          final SdkField attrField = this.get(attrFieldId);
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

  /**
   * Reads the map of the fields that have a special role, which the given file starts with.
   */
  protected void readSpecialPurpose(final JsonNode json) {
    this.specialPurpose = SpecialPurpose.read(json);
  }

  /**
   * @return the fields that have a special role, by their key, as the SDK gives them. Empty for
   *         the SDK versions that publish no such map.
   */
  public Map<String, String> getSpecialPurpose() {
    return this.specialPurpose == null ? Collections.emptyMap() : this.specialPurpose;
  }

  /**
   * @param key a special purpose key, such as "disclosureDate"
   * @return the identifier of the field that has that role, or null if the SDK names none. An
   *         unknown key is not an error.
   */
  public String getSpecialPurposeFieldId(final String key) {
    return this.getSpecialPurpose().get(key);
  }
}
