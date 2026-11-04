/*
 * Copyright 2026 European Union
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European
 * Commission – subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.europa.ted.eforms.sdk.repository.v2;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.entity.SdkField;
import eu.europa.ted.eforms.sdk.repository.SdkFieldRepository;
import eu.europa.ted.eforms.sdk.repository.SdkNodeRepository;

/**
 * The fields of an SDK 2, read from its fields/fwd/fields.json (TEDEFO-5229), with the map of
 * stable identifiers, which the file starts with (TEDEFO-5230).
 *
 * <p>
 * SDK 2 names the four disclosure fields once, by their stable identifiers, instead of naming them
 * in the privacy block of each field that can be withheld. This repository gives them to each such
 * field, so that its privacy settings are the same as with SDK 1.
 * </p>
 */
@SdkComponent(versions = {"2"}, componentType = SdkComponentType.FIELD_REPOSITORY)
public class SdkFieldRepositoryV2 extends SdkFieldRepository {
  private static final long serialVersionUID = 1L;

  /**
   * The field of each disclosure stable identifier, for an SDK 2 whose fields/fwd/fields.json does
   * not give it, such as 2.0.0-alpha.5, which was exported without its map of stable identifiers.
   */
  // TODO: Remove these defaults once SDK 2.0.0 is released, as from then on every SDK 2 gives its
  // map of stable identifiers.
  private static final Map<String, String> DEFAULT_DISCLOSURE_FIELD_IDS =
      defaultDisclosureFieldIds();

  /**
   * @param sdkVersion the version of the SDK
   * @param sdkPath the folder of the SDK
   * @param nodeRepository the nodes of the same SDK, to link each field to its parent node; may be
   *        null to leave the parent nodes unlinked
   */
  // The constructor of the base class is deprecated because applications should not choose the
  // file to read. They use SdkRepositoryFactory, which creates this class, and this class passes
  // the file of its own SDK version.
  @SuppressWarnings("removal")
  public SdkFieldRepositoryV2(final String sdkVersion, final Path sdkPath,
      final SdkNodeRepository nodeRepository) throws InstantiationException {
    super(sdkVersion, sdkPath.resolve(SdkConstants.SdkResource.FIELDS_FWD_FIELDS_JSON.getPath()),
        nodeRepository);
  }

  @Override
  protected void populateMap(final JsonNode json, final Object... context)
      throws InstantiationException {
    this.readStableIds(json, DEFAULT_DISCLOSURE_FIELD_IDS);
    this.addFields((ArrayNode) json.get(SdkConstants.FIELDS_JSON_FIELDS_KEY),
        nodeRepositoryOf(context));
    this.linkTheDisclosureFields();
    this.resolveReferences();
  }

  /**
   * Gives the four disclosure fields, found by their stable identifiers, to each field that can be
   * withheld, that is each field that has a disclosure control, and therefore a privacy code. A
   * stable identifier that the file does not give falls back to
   * {@link #DEFAULT_DISCLOSURE_FIELD_IDS}, never to the privacy blocks of fields/fields.json.
   */
  private void linkTheDisclosureFields() {
    for (final SdkField sdkField : this.values()) {
      if (sdkField.getPrivacyCode() != null) {
        sdkField.setDisclosureControlFields(new SdkField.PrivacySettings(
            this.getFieldIdByStableId(SdkConstants.STABLE_ID_DISCLOSURE_GROUP),
            this.getFieldIdByStableId(SdkConstants.STABLE_ID_DISCLOSURE_JUSTIFICATION_CODE),
            this.getFieldIdByStableId(
                SdkConstants.STABLE_ID_DISCLOSURE_JUSTIFICATION_DESCRIPTION),
            this.getFieldIdByStableId(SdkConstants.STABLE_ID_DISCLOSURE_DATE)));
      }
    }
  }

  private static Map<String, String> defaultDisclosureFieldIds() {
    final Map<String, String> fieldIdByStableId = new LinkedHashMap<>();
    fieldIdByStableId.put(SdkConstants.STABLE_ID_DISCLOSURE_GROUP, "BT-195-notice");
    fieldIdByStableId.put(SdkConstants.STABLE_ID_DISCLOSURE_JUSTIFICATION_CODE, "BT-197-notice");
    fieldIdByStableId.put(SdkConstants.STABLE_ID_DISCLOSURE_JUSTIFICATION_DESCRIPTION,
        "BT-196-notice");
    fieldIdByStableId.put(SdkConstants.STABLE_ID_DISCLOSURE_DATE, "BT-198-notice");
    return Collections.unmodifiableMap(fieldIdByStableId);
  }
}
