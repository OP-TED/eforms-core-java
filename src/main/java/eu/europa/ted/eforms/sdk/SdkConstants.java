package eu.europa.ted.eforms.sdk;

import java.nio.file.Path;
import eu.europa.ted.eforms.sdk.resource.PathResource;

public class SdkConstants {
  public static final String FIELDS_JSON_XML_STRUCTURE_KEY = "xmlStructure";
  public static final String FIELDS_JSON_FIELDS_KEY = "fields";

  public static final String NOTICE_TYPES_JSON_DOCUMENT_TYPES_KEY = "documentTypes";
  public static final String NOTICE_TYPES_JSON_DOCUMENT_TYPE_KEY = "documentType";
  public static final String NOTICE_TYPES_JSON_NAMESPACE_KEY = "namespace";
  public static final String NOTICE_TYPES_JSON_ROOT_ELEMENT_KEY = "rootElement";
  public static final String NOTICE_CUSTOMIZATION_ID_VERSION_PREFIX = "eforms-sdk-";

  public static final Path DEFAULT_SDK_ROOT = Path.of("eforms-sdk");

  public static final String SDK_GROUP_ID = "eu.europa.ted.eforms";
  public static final String SDK_ARTIFACT_ID = "eforms-sdk";
  public static final String SDK_PACKAGING = "jar";

  private SdkConstants() {}

  public enum SdkResource implements PathResource {
    CODELISTS(Path.of("codelists")), //
    CODELISTS_JSON(Path.of("codelists", "codelists.json")), //

    EFX_GRAMMAR(Path.of("efx-grammar")), //

    FIELDS(Path.of("fields")), //
    FIELDS_JSON(Path.of("fields", "fields.json")), //

    NOTICE_TYPES(Path.of("notice-types")), //
    NOTICE_TYPES_JSON(Path.of("notice-types", "notice-types.json")), //

    /**
     * XSD files.
     */
    SCHEMAS(Path.of("schemas")), //
    SCHEMAS_COMMON(Path.of("schemas", "common")), //
    SCHEMAS_MAINDOC(Path.of("schemas", "maindoc")), //

    SCHEMATRONS(Path.of("schematrons")), //
    SCHEMATRONS_DYNAMIC(Path.of("schematrons", "dynamic")), //
    SCHEMATRONS_STATIC(Path.of("schematrons", "static")), //

    /**
     * Internal usage, tedweb.
     */
    TED(Path.of(".ted")), //
    TED_TEDWEB(Path.of(".ted", "tedweb")), //
    TED_TEDWEB_REPORT_METADATA(Path.of(".ted", "tedweb", "report-metadata.json")), //
    TED_TEDWEB_SEARCH_METADATA(Path.of(".ted", "tedweb", "search-metadata.json")), //

    /**
     * Internationalisation, labels.
     */
    TRANSLATIONS(Path.of("translations")), //

    VIEW_TEMPLATES(Path.of("view-templates")), //
    VIEW_TEMPLATES_JSON(Path.of("view-templates", "view-templates.json"));

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
