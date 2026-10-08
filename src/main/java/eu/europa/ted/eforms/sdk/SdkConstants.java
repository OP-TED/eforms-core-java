package eu.europa.ted.eforms.sdk;

import java.nio.file.Path;
import eu.europa.ted.eforms.sdk.resource.PathResource;

public class SdkConstants {

  public static final Path DEFAULT_SDK_ROOT = Path.of("eforms-sdk");

  public static final String CODELISTS_DIR_NAME = "codelists";
  public static final String CODELISTS_JSON_FILE_NAME = "codelists.json";

  public static final String FIELDS_JSON_XML_STRUCTURE_KEY = "xmlStructure";
  public static final String FIELDS_JSON_FIELDS_KEY = "fields";
  public static final String FIELDS_DIR_NAME = "fields";

  public static final String NODES_JSON_FILE_NAME = "nodes.json";

  /**
   * Key for the array of nodes of fields/fwd/nodes.json in SDK 2, where fields/fields.json uses
   * {@link #FIELDS_JSON_XML_STRUCTURE_KEY} (TEDEFO-5228).
   */
  public static final String NODES_JSON_NODES_KEY = "nodes";

  public static final String FIELDS_JSON_FILE_NAME = "fields.json";
  public static final String DATA_TYPES_JSON_FILE_NAME = "data-types.json";
  public static final String DATA_TYPES_JSON_DATA_TYPES_KEY = "dataTypes";
  public static final String BUSINESS_TERMS_JSON_FILE_NAME = "business-terms.json";
  public static final String BUSINESS_ENTITIES_PROPERTIES_JSON_FILE_NAME = "business-entities-properties.json";
  public static final String BUSINESS_ENTITIES_HIERARCHIES_JSON_FILE_NAME = "business-entities-hierarchies.json";
  public static final String BUSINESS_ENTITIES_COMPOSITION_JSON_FILE_NAME = "business-entities-composition.json";

  public static final String MIGRATION_DIR_NAME = "migration";
  public static final String MIGRATION_JSON_FILE_NAME = "migration.json";

  public static final String NOTICE_TYPES_DIR_NAME = "notice-types";
  public static final String NOTICE_TYPES_JSON_FILE_NAME = "notice-types.json";
  public static final String NOTICE_TYPES_JSON_SUBTYPES_KEY = "noticeSubTypes";
  public static final String NOTICE_TYPES_JSON_DOCUMENT_TYPES_KEY = "documentTypes";
  public static final String NOTICE_TYPES_JSON_DOCUMENT_TYPE_KEY = "documentType";
  public static final String NOTICE_TYPES_JSON_NAMESPACE_KEY = "namespace";
  public static final String NOTICE_TYPES_JSON_ROOT_ELEMENT_KEY = "rootElement";
  public static final String NOTICE_CUSTOMIZATION_ID_VERSION_PREFIX = "eforms-sdk-";

  public static final String SDK_GROUP_ID = "eu.europa.ted.eforms";
  public static final String SDK_ARTIFACT_ID = "eforms-sdk";
  public static final String SDK_PACKAGING = "jar";

  public static final String SCHEMAS_DIR_NAME = "schemas";
  public static final String SCHEMAS_JSON_FILE_NAME = "schemas.json";
  public static final String SCHEMATRONS_DIR_NAME = "schematrons";

  public static final String TRANSLATIONS_DIR_NAME = "translations";
  public static final String TRANSLATIONS_JSON_FILE_NAME = "translations.json";
  public static final String VALIDATION_DIR_NAME = "validation";
  public static final String VALIDATION_RULES_EFX_FILE_NAME = "rules.efx";
  public static final String VALIDATION_DEPENDENCIES_JSON_FILE_NAME = "dependencies.json";

  public static final String VIEW_TEMPLATES_DIR_NAME = "view-templates";
  public static final String VIEW_TEMPLATES_JSON_FILE_NAME = "view-templates.json";

  /**
   * Forward folder of SDK2+, it can exist in some folders.
   * Files in that folder can be used to preview SDK2 features.
   */
  public static final String FWD_DIR_NAME = "fwd";

  /**
   * Key of the map that names the fields or the nodes that have a special role, in
   * fields/fwd/fields.json and fields/fwd/nodes.json of SDK 2 (TEDEFO-1877, TEDEFO-5210). The keys
   * of the map itself are plain strings, as the map is data and will grow (TEDEFO-5230).
   */
  public static final String SPECIAL_PURPOSE_KEY = "specialPurpose";

  /**
   * Special purpose key of the field holding the disclosure group of a withheld field, which SDK 1
   * named per field as privacy.unpublishedFieldId.
   */
  public static final String SPECIAL_PURPOSE_DISCLOSURE_GROUP = "disclosureGroup";

  /**
   * Special purpose key of the field holding the justification code of a withheld field, which
   * SDK 1 named per field as privacy.reasonCodeFieldId.
   */
  public static final String SPECIAL_PURPOSE_DISCLOSURE_JUSTIFICATION_CODE =
      "disclosureJustificationCode";

  /**
   * Special purpose key of the field holding the justification description of a withheld field,
   * which SDK 1 named per field as privacy.reasonDescriptionFieldId.
   */
  public static final String SPECIAL_PURPOSE_DISCLOSURE_JUSTIFICATION_DESCRIPTION =
      "disclosureJustificationDescription";

  /**
   * Special purpose key of the field holding the disclosure date of a withheld field, which SDK 1
   * named per field as privacy.publicationDateFieldId.
   */
  public static final String SPECIAL_PURPOSE_DISCLOSURE_DATE = "disclosureDate";

  private SdkConstants() {}

  public enum SdkResource implements PathResource {
    CODELISTS(Path.of(CODELISTS_DIR_NAME)),
    CODELISTS_JSON(Path.of(CODELISTS_DIR_NAME, CODELISTS_JSON_FILE_NAME)),

    EFX_GRAMMAR(Path.of("efx-grammar")),

    FIELDS(Path.of(FIELDS_DIR_NAME)),
    FIELDS_FWD(Path.of(FIELDS_DIR_NAME, FWD_DIR_NAME)),

    /**
     * Fields and nodes.
     */
    FIELDS_JSON(Path.of(FIELDS_DIR_NAME, FIELDS_JSON_FILE_NAME)),

    FIELDS_FWD_BUSINESS_ENTITIES_COMPOSITION(Path.of(FIELDS_DIR_NAME, FWD_DIR_NAME, BUSINESS_ENTITIES_COMPOSITION_JSON_FILE_NAME)),
    FIELDS_FWD_BUSINESS_ENTITIES_HIERARCHIES(Path.of(FIELDS_DIR_NAME, FWD_DIR_NAME, BUSINESS_ENTITIES_HIERARCHIES_JSON_FILE_NAME)),
    FIELDS_FWD_BUSINESS_ENTITIES_PROPERTIES(Path.of(FIELDS_DIR_NAME, FWD_DIR_NAME, BUSINESS_ENTITIES_PROPERTIES_JSON_FILE_NAME)),
    FIELDS_FWD_BUSINESS_TERMS(Path.of(FIELDS_DIR_NAME, FWD_DIR_NAME, BUSINESS_TERMS_JSON_FILE_NAME)),
    FIELDS_FWD_DATA_TYPES(Path.of(FIELDS_DIR_NAME, FWD_DIR_NAME, DATA_TYPES_JSON_FILE_NAME)),

    /**
     * JSON with array of fields (forward folder).
     */
    FIELDS_FWD_FIELDS_JSON(Path.of(FIELDS_DIR_NAME, FWD_DIR_NAME, FIELDS_JSON_FILE_NAME)),

    /**
     * JSON with array of nodes (forward folder).
     */
    FIELDS_FWD_NODES_JSON(Path.of(FIELDS_DIR_NAME, FWD_DIR_NAME, NODES_JSON_FILE_NAME)),

    /**
     * Related to asset migration.
     */
    MIGRATION(Path.of(MIGRATION_DIR_NAME)),
    MIGRATION_JSON(Path.of(MIGRATION_DIR_NAME, MIGRATION_JSON_FILE_NAME)),

    /**
     * Notice Types Definitions (NTD).
     */
    NOTICE_TYPES(Path.of(NOTICE_TYPES_DIR_NAME)),
    NOTICE_TYPES_JSON(Path.of(NOTICE_TYPES_DIR_NAME, NOTICE_TYPES_JSON_FILE_NAME)),

    /**
     * Schema files.
     */
    SCHEMAS(Path.of(SCHEMAS_DIR_NAME)),
    SCHEMAS_JSON(Path.of(SCHEMAS_DIR_NAME, SCHEMAS_JSON_FILE_NAME)),

    SCHEMAS_COMMON(Path.of(SCHEMAS_DIR_NAME, "common")),
    SCHEMAS_MAINDOC(Path.of(SCHEMAS_DIR_NAME, "maindoc")),

    SCHEMATRONS(Path.of(SCHEMATRONS_DIR_NAME)),
    SCHEMATRONS_DYNAMIC(Path.of(SCHEMATRONS_DIR_NAME, "dynamic")),
    SCHEMATRONS_STATIC(Path.of(SCHEMATRONS_DIR_NAME, "static")),

    /**
     * Internal usage, tedweb.
     */
    TED(Path.of(".ted")),
    TED_TEDWEB(Path.of(".ted", "tedweb")),
    TED_TEDWEB_REPORT_METADATA(Path.of(".ted", "tedweb", "report-metadata.json")),
    TED_TEDWEB_SEARCH_METADATA(Path.of(".ted", "tedweb", "search-metadata.json")),

    /**
     * Internationalisation, labels.
     */
    TRANSLATIONS(Path.of(TRANSLATIONS_DIR_NAME)),

    /**
     * The index file for translations, only present in SDK 1.10.0 and later.
     */
    TRANSLATIONS_JSON(Path.of(TRANSLATIONS_DIR_NAME, TRANSLATIONS_JSON_FILE_NAME)),

    VALIDATION(Path.of(VALIDATION_DIR_NAME)),
    VALIDATION_RULES_EFX(Path.of(VALIDATION_DIR_NAME, VALIDATION_RULES_EFX_FILE_NAME)),
    VALIDATION_DEPENDENCIES_JSON(Path.of(VALIDATION_DIR_NAME, VALIDATION_DEPENDENCIES_JSON_FILE_NAME)),

    VIEW_TEMPLATES(Path.of(VIEW_TEMPLATES_DIR_NAME)),
    VIEW_TEMPLATES_JSON(Path.of(VIEW_TEMPLATES_DIR_NAME, VIEW_TEMPLATES_JSON_FILE_NAME));

    private Path path;

    private SdkResource(final Path path) {
      this.path = path;
    }

    @Override
    public Path getPath() {
      return path;
    }
  }
}
