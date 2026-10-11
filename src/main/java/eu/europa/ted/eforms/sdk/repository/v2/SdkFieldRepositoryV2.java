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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.entity.SdkField;
import eu.europa.ted.eforms.sdk.repository.SdkFieldRepository;
import eu.europa.ted.eforms.sdk.repository.SdkNodeRepository;

/**
 * The fields of an SDK 2, read from its fields/fwd/fields.json (TEDEFO-5229), with the map of the
 * fields that have a special role, which the file starts with (TEDEFO-5230).
 *
 * <p>
 * SDK 2 names the four disclosure fields once, in the special purpose map, instead of naming them
 * in the privacy block of each field that can be withheld. This repository gives them to each such
 * field, so that its privacy settings are the same as with SDK 1.
 * </p>
 */
@SdkComponent(versions = {"2"}, componentType = SdkComponentType.FIELD_REPOSITORY)
public class SdkFieldRepositoryV2 extends SdkFieldRepository {
  private static final long serialVersionUID = 1L;

  /**
   * @param sdkVersion the version of the SDK
   * @param sdkPath the folder of the SDK
   * @param nodeRepository the nodes of the same SDK, to link each field to its parent node; may be
   *        null to leave the parent nodes unlinked
   */
  public SdkFieldRepositoryV2(final String sdkVersion, final Path sdkPath,
      final SdkNodeRepository nodeRepository) throws InstantiationException {
    super(sdkVersion, sdkPath.resolve(SdkConstants.SdkResource.FIELDS_FWD_FIELDS_JSON.getPath()),
        nodeRepository);
  }

  @Override
  protected void populateMap(final JsonNode json, final Object... context)
      throws InstantiationException {
    this.readSpecialPurpose(json);
    this.addFields((ArrayNode) json.get(SdkConstants.FIELDS_JSON_FIELDS_KEY),
        nodeRepositoryOf(context));
    this.linkTheDisclosureFields();
    this.resolveReferences();
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
    if (this.getSpecialPurpose().isEmpty()) {
      return;
    }
    final SdkField.PrivacySettings ofTheSdk = new SdkField.PrivacySettings(
        this.getSpecialPurposeFieldId(SdkConstants.SPECIAL_PURPOSE_DISCLOSURE_GROUP),
        this.getSpecialPurposeFieldId(SdkConstants.SPECIAL_PURPOSE_DISCLOSURE_JUSTIFICATION_CODE),
        this.getSpecialPurposeFieldId(
            SdkConstants.SPECIAL_PURPOSE_DISCLOSURE_JUSTIFICATION_DESCRIPTION),
        this.getSpecialPurposeFieldId(SdkConstants.SPECIAL_PURPOSE_DISCLOSURE_DATE));
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
}
