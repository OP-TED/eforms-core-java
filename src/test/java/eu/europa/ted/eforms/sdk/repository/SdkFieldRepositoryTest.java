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
package eu.europa.ted.eforms.sdk.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import eu.europa.ted.eforms.sdk.entity.SdkField;
import eu.europa.ted.eforms.sdk.entity.v1.SdkFieldV1;
import eu.europa.ted.eforms.sdk.entity.v2.SdkFieldV2;

/**
 * Verifies where the fields of each SDK version are read from (TEDEFO-5229): fields/fields.json for
 * SDK 1, fields/fwd/fields.json for SDK 2, and the properties whose structure differs between the
 * two.
 */
class SdkFieldRepositoryTest {

  /** SDK 1 publishes its fields in fields/fields.json. */
  private static final String SDK_1 = "1.16";

  /** SDK 2 publishes its fields in fields/fwd/fields.json. */
  private static final String SDK_2 = "2.0";

  /**
   * The folders of the SDKs, one per version. The two hold different fields on purpose, so that a
   * test can tell which one was read.
   */
  private static final Path SDK_ROOT = Path.of("src", "test", "resources", "sdk-fixture");

  /** Only fields/fwd/fields.json holds this field. */
  private static final String FIELD_OF_THE_FWD_FILE = "BT-Fwd-Only";

  private static SdkFieldRepository ofSdk1() throws InstantiationException {
    final Path sdk = SDK_ROOT.resolve(SDK_1);
    return SdkRepositoryFactory.getSdkFieldRepository(SDK_1, sdk,
        SdkRepositoryFactory.getSdkNodeRepository(SDK_1, sdk));
  }

  private static SdkFieldRepository ofSdk2() throws InstantiationException {
    final Path sdk = SDK_ROOT.resolve(SDK_2);
    return SdkRepositoryFactory.getSdkFieldRepository(SDK_2, sdk,
        SdkRepositoryFactory.getSdkNodeRepository(SDK_2, sdk));
  }

  @Test
  void testSdk1ReadsTheFieldsFromFieldsJson() throws InstantiationException {
    SdkFieldRepository fields = ofSdk1();

    assertEquals(4, fields.size());
    assertNull(fields.get(FIELD_OF_THE_FWD_FILE),
        "the field of fields/fwd/fields.json must not reach SDK 1");
  }

  /**
   * The point of TEDEFO-5229: with SDK 2 the fields come from fields/fwd/fields.json. The fwd file
   * holds a field that fields/fields.json does not, so only it can be the source.
   */
  @Test
  void testSdk2ReadsTheFieldsFromTheFwdFile() throws InstantiationException {
    SdkFieldRepository fields = ofSdk2();

    assertEquals(10, fields.size());
    assertNotNull(fields.get(FIELD_OF_THE_FWD_FILE),
        "the fields of SDK 2 must come from fields/fwd/fields.json");
  }

  /** SDK 2 gives repeatable as a plain boolean, SDK 1 as an object with a value. */
  @Test
  void testRepeatableIsReadFromBothShapes() throws InstantiationException {
    SdkFieldRepository ofSdk1 = ofSdk1();
    SdkFieldRepository ofSdk2 = ofSdk2();

    assertTrue(ofSdk1.get("BT-Plain").isRepeatable(), "SDK 1 gives an object with a value");
    assertFalse(ofSdk1.get("BT-NotRepeatable").isRepeatable());

    assertTrue(ofSdk2.get("BT-Plain").isRepeatable(), "SDK 2 gives a plain boolean");
    assertFalse(ofSdk2.get("BT-NotRepeatable").isRepeatable());
  }

  /** SDK 2 gives the privacy code as the groupId of the disclosureControl block. */
  @Test
  void testThePrivacyCodeComesFromGroupIdForSdk2() throws InstantiationException {
    assertEquals("con-esti", ofSdk1().get("BT-Withheld").getPrivacyCode(),
        "SDK 1 gives it as privacy.code");
    assertEquals("con-esti", ofSdk2().get("BT-Withheld").getPrivacyCode(),
        "SDK 2 gives it as disclosureControl.groupId");
    assertNull(ofSdk2().get("BT-Plain").getPrivacyCode(), "a field that is not withheld has none");
  }

  /**
   * The two EFX expressions are present on the fields that are withheld only for some of their
   * instances, and are loaded as they are, without being translated.
   */
  @Test
  void testTheExpressionsOfTheConditionallyWithheldFields() throws InstantiationException {
    SdkField withheldSometimes = ofSdk2().get("BT-WithheldSometimes");

    assertEquals("cro-bor-law", withheldSometimes.getPrivacyCode());
    assertEquals("{BT-WithheldSometimes} ${BT-Plain == 'x'}",
        withheldSometimes.getWithholdingCondition());
    assertEquals("{ND-Root} ${BT-WithheldSometimes[BT-Plain == 'x']}",
        withheldSometimes.getUndisclosedFieldSelector());
  }

  /** A field withheld for all of its instances carries no expression. */
  @Test
  void testTheFieldsWithoutExpressions() throws InstantiationException {
    SdkFieldRepository ofSdk2 = ofSdk2();

    assertNull(ofSdk2.get("BT-Withheld").getWithholdingCondition());
    assertNull(ofSdk2.get("BT-Withheld").getUndisclosedFieldSelector());
    assertNull(ofSdk2.get("BT-Plain").getWithholdingCondition());
    assertNull(ofSdk2.get("BT-Plain").getUndisclosedFieldSelector());
  }

  /**
   * SDK 1 names the four disclosure fields in the privacy block of every withheld field.
   */
  @Test
  void testSdk1NamesTheDisclosureFieldsPerField() throws InstantiationException {
    SdkField.PrivacySettings ofSdk1 = ofSdk1().get("BT-Withheld").getPrivacySettings();

    assertNotNull(ofSdk1, "SDK 1 names them in the privacy block");
    assertEquals("BT-195(BT-Withheld)-Lot", ofSdk1.getPrivacyCodeFieldId());
    assertEquals("BT-197(BT-Withheld)-Lot", ofSdk1.getJustificationCodeFieldId());
    assertEquals("BT-196(BT-Withheld)-Lot", ofSdk1.getJustificationDescriptionFieldId());
    assertEquals("BT-198(BT-Withheld)-Lot", ofSdk1.getPublicationDateFieldId());
  }

  /**
   * SDK 2 names them once, by their stable identifiers, so every field that can be withheld gets
   * them from there instead (TEDEFO-5230). The EFX Toolkit resolves the privacy properties of
   * EFX 2 through these, so they must stay available per field.
   */
  @Test
  void testSdk2TakesTheDisclosureFieldsFromTheStableIds() throws InstantiationException {
    SdkField.PrivacySettings ofSdk2 = ofSdk2().get("BT-Withheld").getPrivacySettings();

    assertNotNull(ofSdk2, "a withheld field must still have its disclosure fields");
    assertEquals("BT-195-notice", ofSdk2.getPrivacyCodeFieldId());
    assertEquals("BT-197-notice", ofSdk2.getJustificationCodeFieldId());
    assertEquals("BT-196-notice", ofSdk2.getJustificationDescriptionFieldId());
    assertEquals("BT-198-notice", ofSdk2.getPublicationDateFieldId());
  }

  /** The identifiers are resolved to the fields themselves, as they are for SDK 1. */
  @Test
  void testTheDisclosureFieldsAreResolvedToFields() throws InstantiationException {
    SdkField.PrivacySettings ofSdk2 = ofSdk2().get("BT-Withheld").getPrivacySettings();

    assertEquals("BT-195-notice", ofSdk2.getPrivacyCodeField().getId());
    assertEquals("BT-197-notice", ofSdk2.getJustificationCodeField().getId());
    assertEquals("BT-196-notice", ofSdk2.getJustificationDescriptionField().getId());
    assertEquals("BT-198-notice", ofSdk2.getPublicationDateField().getId());
  }

  /** Only the fields that can be withheld get them. */
  @Test
  void testAFieldThatCannotBeWithheldHasNoDisclosureFields() throws InstantiationException {
    assertNull(ofSdk2().get("BT-Plain").getPrivacySettings(),
        "a field that is not withheld has no disclosure fields");
  }

  /** The stable identifiers of the fields are read in full, and each gives its field. */
  @Test
  void testTheStableIdsOfTheFieldsAreLoaded() throws InstantiationException {
    SdkFieldRepository ofSdk2 = ofSdk2();

    assertEquals(5, ofSdk2.getStableIds().size());
    assertEquals("BT-198-notice", ofSdk2.getFieldIdByStableId("disclosureDate"));
    assertEquals("OPP-070-notice", ofSdk2.getFieldIdByStableId("noticeSubType"));
  }

  /** An unknown key gives no result, and is not an error. */
  @Test
  void testAnUnknownStableIdGivesNothing() throws InstantiationException {
    assertNull(ofSdk2().getFieldIdByStableId("thereIsNoSuchKey"));
  }

  /** SDK 1 has no such map, so the lookup gives no result. */
  @Test
  void testSdk1HasNoStableIds() throws InstantiationException {
    SdkFieldRepository ofSdk1 = ofSdk1();

    assertTrue(ofSdk1.getStableIds().isEmpty());
    assertNull(ofSdk1.getFieldIdByStableId("disclosureDate"));
  }

  /** The fields are linked to the nodes loaded from the file of the same SDK version. */
  @Test
  void testTheFieldsAreLinkedToTheirParentNode() throws InstantiationException {
    assertEquals("ND-Lot", ofSdk1().get("BT-Plain").getParentNode().getId());
    assertEquals("ND-Lot", ofSdk2().get("BT-Plain").getParentNode().getId());
  }

  /**
   * The nodes are optional: without them, the fields are read without their parent node, and the
   * disclosure fields of SDK 2 are still attached and resolved.
   */
  @Test
  void testWithoutTheNodesTheFieldsHaveNoParentNode() throws InstantiationException {
    final SdkFieldRepository ofSdk1 =
        SdkRepositoryFactory.getSdkFieldRepository(SDK_1, SDK_ROOT.resolve(SDK_1), null);
    final SdkFieldRepository ofSdk2 =
        SdkRepositoryFactory.getSdkFieldRepository(SDK_2, SDK_ROOT.resolve(SDK_2), null);

    assertNull(ofSdk1.get("BT-Plain").getParentNode());
    assertNull(ofSdk2.get("BT-Plain").getParentNode());
    assertEquals("BT-198-notice",
        ofSdk2.get("BT-Withheld").getPrivacySettings().getPublicationDateField().getId());
  }

  /**
   * SDK 1 maps measure onto duration, because it had no duration type. SDK 2 separates the two, so
   * it must report the type the SDK declares.
   */
  @Test
  void testMeasureIsOnlyMappedOntoDurationForSdk1() throws InstantiationException {
    assertEquals("duration", ofSdk1().get("BT-Measure").getType(),
        "SDK 1 has no duration type, so measure means duration");
    assertEquals("measure", ofSdk2().get("BT-Measure").getType(),
        "SDK 2 separates duration from measure");
  }

  /** Each SDK version gets its own field implementation, as before. */
  @Test
  void testEachSdkVersionGetsItsOwnFields() throws InstantiationException {
    SdkField ofSdk1 = ofSdk1().get("BT-Plain");
    SdkField ofSdk2 = ofSdk2().get("BT-Plain");

    assertTrue(ofSdk1 instanceof SdkFieldV1, "SDK 1 gives " + ofSdk1.getClass());
    assertTrue(ofSdk2 instanceof SdkFieldV2, "SDK 2 gives " + ofSdk2.getClass());
    assertEquals("plain", ((SdkFieldV2) ofSdk2).getAlias(), "SDK 2 also reads the alias");
  }

  /** The constructor that takes a fields.json reads the file it is given. */
  @Test
  void testTheConstructorReadsTheFileItIsGiven() throws InstantiationException {
    SdkFieldRepository fields = new SdkFieldRepository(SDK_1,
        SDK_ROOT.resolve(Path.of("1.16", "fields", "fields.json")));

    assertEquals(4, fields.size());
    assertEquals("con-esti", fields.get("BT-Withheld").getPrivacyCode());
  }

  /**
   * The EFX Toolkit still reads the fields of SDK 2 from fields/fields.json, with the constructor
   * that takes the file. There an SDK 2 field takes repeatable from the object, and its privacy
   * code and disclosure fields from its privacy block.
   */
  @Test
  void testSdk2FieldsJsonIsReadWithItsPrivacyBlocks() throws InstantiationException {
    final Path fieldsJson = SDK_ROOT.resolve(Path.of(SDK_2, "fields", "fields.json"));
    final SdkFieldRepository fields = new SdkFieldRepository(SDK_2, fieldsJson,
        new SdkNodeRepository(SDK_2, fieldsJson));

    assertTrue(fields.get("BT-Plain") instanceof SdkFieldV2);
    assertTrue(fields.get("BT-Plain").isRepeatable());
    assertFalse(fields.get("BT-Withheld").isRepeatable());
    assertEquals("con-esti", fields.get("BT-Withheld").getPrivacyCode());
    final SdkField.PrivacySettings privacy = fields.get("BT-Withheld").getPrivacySettings();
    assertEquals("BT-195-notice", privacy.getPrivacyCodeField().getId());
    assertEquals("BT-197-notice", privacy.getJustificationCodeField().getId());
    assertEquals("BT-196-notice", privacy.getJustificationDescriptionField().getId());
    assertEquals("BT-198-notice", privacy.getPublicationDateField().getId());
    assertEquals("measure", fields.get("BT-Measure").getType(),
        "SDK 2 separates duration from measure, whichever file it is read from");
  }
}
