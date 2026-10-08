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
import eu.europa.ted.eforms.sdk.entity.SdkField;
import eu.europa.ted.eforms.sdk.resource.SdkResourceLoader;

public class SdkFieldRepository extends MapFromJson<SdkField> {
  private static final long serialVersionUID = 1L;

  /**
   * The major version of the only SDK that publishes no fields of its own.
   */
  private static final String SDK_MAJOR_WITHOUT_OWN_FIELDS = "1";

  /**
   * The fields that have a special role, by their key (TEDEFO-5230). Empty for the SDK versions
   * that publish no such map.
   *
   * <p>
   * Deliberately left without an initialiser: it is assigned while the map of fields is being
   * populated, which happens during the call to super, and a field initialiser would run after
   * that call and undo it.
   * </p>
   */
  private Map<String, String> specialPurpose;

  /**
   * Creates the repository of the fields of the given SDK, reading them from the file that this SDK
   * version publishes them in: fields/fields.json for SDK 1, fields/fwd/fields.json for SDK 2
   * (TEDEFO-5229). The array of fields has the same key in both.
   *
   * @param sdkVersion the target SDK version, which decides which file the fields are read from
   * @param sdkRootPath path of the root SDK folder
   * @param nodeRepository the nodes of the same SDK, to link each field to its parent node; may be
   *        null to leave the parent nodes unlinked
   * @return the fields of that SDK, by identifier
   * @throws InstantiationException if a field cannot be created for this SDK version
   */
  @SuppressWarnings("deprecation")
  public static SdkFieldRepository forSdk(final String sdkVersion, final Path sdkRootPath,
      final SdkNodeRepository nodeRepository) throws InstantiationException {
    final SdkConstants.SdkResource fields =
        SDK_MAJOR_WITHOUT_OWN_FIELDS.equals(new SdkVersion(sdkVersion).getMajor())
            ? SdkConstants.SdkResource.FIELDS_JSON
            : SdkConstants.SdkResource.FIELDS_FWD_FIELDS_JSON;

    return new SdkFieldRepository(sdkVersion,
        SdkResourceLoader.getResourceAsPath(sdkVersion, fields, sdkRootPath), nodeRepository);
  }

  /**
   * @deprecated Use {@link #forSdk(String, Path, SdkNodeRepository)}, which reads the fields of
   *             SDK 2 from fields/fwd/fields.json, and links them to their parent nodes.
   */
  @Deprecated
  public SdkFieldRepository(String sdkVersion, Path jsonPath) throws InstantiationException {
    super(sdkVersion, jsonPath);
  }

  /**
   * @deprecated Use {@link #forSdk(String, Path, SdkNodeRepository)}, which reads the fields of
   *             SDK 2 from fields/fwd/fields.json.
   */
  @Deprecated
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

    this.specialPurpose = SpecialPurpose.read(json);

    final ArrayNode fields = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_FIELDS_KEY);

    // First pass: create all field entities and add them to the map
    for (final JsonNode field : fields) {
      final SdkField sdkField = SdkEntityFactory.getSdkField(sdkVersion, field);
      put(sdkField.getId(), sdkField);

      if (nodes != null && sdkField.getParentNodeId() != null) {
        sdkField.setParentNode(nodes.get(sdkField.getParentNodeId()));
      }
    }

    // SDK 2 names the disclosure fields once, in the special purpose map, instead of naming them
    // in the privacy block of every withheld field, so give them to each withheld field here. The
    // pass below then resolves them to fields, as it does for SDK 1 (TEDEFO-5230).
    linkTheDisclosureFields();

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

  /**
   * Gives the four disclosure fields of the special purpose map to every field that can be
   * withheld and does not name them itself, so that the EFX Toolkit resolves the privacy
   * properties of EFX 2 through the privacy settings of each field, as it does with
   * fields/fields.json.
   *
   * <p>
   * A field that already names them keeps what it read: only SDK 2 stopped naming them per field.
   * The disclosure fields are read from the map only, with no fallback to a privacy block.
   * </p>
   */
  private void linkTheDisclosureFields() {
    if (getSpecialPurpose().isEmpty()) {
      return;
    }

    final SdkField.PrivacySettings ofTheSdk = new SdkField.PrivacySettings(
        getSpecialPurposeFieldId(SdkConstants.SPECIAL_PURPOSE_DISCLOSURE_GROUP),
        getSpecialPurposeFieldId(SdkConstants.SPECIAL_PURPOSE_DISCLOSURE_JUSTIFICATION_CODE),
        getSpecialPurposeFieldId(
            SdkConstants.SPECIAL_PURPOSE_DISCLOSURE_JUSTIFICATION_DESCRIPTION),
        getSpecialPurposeFieldId(SdkConstants.SPECIAL_PURPOSE_DISCLOSURE_DATE));

    for (final SdkField sdkField : this.values()) {
      // A field that can be withheld has a privacy code; one that already names the disclosure
      // fields has its own settings, which must not be replaced.
      if (sdkField.getPrivacyCode() != null && sdkField.getPrivacySettings() == null) {
        // Each field gets its own settings: the pass below resolves the identifiers to fields on
        // them, and they must not be shared between fields.
        sdkField.setPrivacySettings(new SdkField.PrivacySettings(
            ofTheSdk.getPrivacyCodeFieldId(),
            ofTheSdk.getJustificationCodeFieldId(),
            ofTheSdk.getJustificationDescriptionFieldId(),
            ofTheSdk.getPublicationDateFieldId()));
      }
    }
  }

  /**
   * @return the fields that have a special role, by their key, as the SDK gives them. Empty for
   *         the SDK versions that publish no such map.
   */
  public Map<String, String> getSpecialPurpose() {
    return specialPurpose == null ? Collections.emptyMap() : specialPurpose;
  }

  /**
   * @param key a special purpose key, such as "disclosureDate"
   * @return the identifier of the field that has that role, or null if the SDK names none. An
   *         unknown key is not an error.
   */
  public String getSpecialPurposeFieldId(final String key) {
    return getSpecialPurpose().get(key);
  }
}
