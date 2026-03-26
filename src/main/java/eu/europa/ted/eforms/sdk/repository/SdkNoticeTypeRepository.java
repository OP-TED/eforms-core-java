package eu.europa.ted.eforms.sdk.repository;

import java.nio.file.Path;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.entity.SdkEntityFactory;
import eu.europa.ted.eforms.sdk.entity.SdkNoticeSubtype;

/**
 * Repository for SDK notice types loaded from notice-types.json.
 * Maps notice subtype IDs (e.g., "1", "3", "CEI", "E1", "X01") to SdkNoticeSubtype objects.
 */
public class SdkNoticeTypeRepository extends MapFromJson<SdkNoticeSubtype> {
  private static final long serialVersionUID = 1L;

  public SdkNoticeTypeRepository(String sdkVersion, Path jsonPath) throws InstantiationException {
    super(sdkVersion, jsonPath);
  }

  @Override
  protected void populateMap(final JsonNode json) throws InstantiationException {
    final ArrayNode noticeSubtypes = (ArrayNode) json.get(SdkConstants.NOTICE_TYPES_JSON_SUBTYPES_KEY);
    for (final JsonNode noticeSubtype : noticeSubtypes) {
      final SdkNoticeSubtype sdkNoticeSubtype = SdkEntityFactory.getSdkNoticeSubtype(sdkVersion, noticeSubtype);
      put(sdkNoticeSubtype.getId(), sdkNoticeSubtype);
    }
  }
}
