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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import eu.europa.ted.eforms.sdk.entity.SdkNode;
import eu.europa.ted.eforms.sdk.entity.v1.SdkNodeV1;
import eu.europa.ted.eforms.sdk.entity.v2.SdkNodeV2;

/**
 * Verifies where the nodes of each SDK version are read from (TEDEFO-5228): the xmlStructure array
 * of fields/fields.json for SDK 1, the nodes array of fields/fwd/nodes.json for SDK 2.
 */
class SdkNodeRepositoryTest {

  /** SDK 1 publishes its nodes in fields/fields.json. */
  private static final String SDK_1 = "1.16";

  /** SDK 2 publishes its nodes in fields/fwd/nodes.json. */
  private static final String SDK_2 = "2.0";

  /**
   * An SDK folder holding both files. The two hold different nodes on purpose, so that a test can
   * tell which one was read.
   */
  private static final Path SDK_ROOT = Path.of("src", "test", "resources", "sdk-fixture");

  /** Only fields/fwd/nodes.json holds this node. */
  private static final String NODE_OF_THE_FWD_FILE = "ND-Fwd-Only";

  @Test
  void testSdk1ReadsTheNodesFromFieldsJson() throws InstantiationException {
    SdkNodeRepository nodes = SdkNodeRepository.forSdk(SDK_1, SDK_ROOT);

    assertEquals(2, nodes.size());
    assertEquals("/*/cac:ProcurementProjectLot", nodes.get("ND-Lot").getXpathAbsolute());
    assertNull(nodes.get(NODE_OF_THE_FWD_FILE),
        "the node of fields/fwd/nodes.json must not reach SDK 1");
  }

  /**
   * The point of TEDEFO-5228: with SDK 2 the nodes come from fields/fwd/nodes.json. The fwd file
   * holds a node that fields/fields.json does not, so only it can be the source.
   */
  @Test
  void testSdk2ReadsTheNodesFromTheFwdFile() throws InstantiationException {
    SdkNodeRepository nodes = SdkNodeRepository.forSdk(SDK_2, SDK_ROOT);

    assertEquals(3, nodes.size());
    assertNotNull(nodes.get(NODE_OF_THE_FWD_FILE),
        "the nodes of SDK 2 must come from fields/fwd/nodes.json");
    assertEquals("/*/cac:ProcurementProjectLot/cac:FwdOnly",
        nodes.get(NODE_OF_THE_FWD_FILE).getXpathAbsolute());
  }

  /** The nodes have the same properties in both files, so the node entities do not change. */
  @Test
  void testTheNodePropertiesAreTheSameInBothFiles() throws InstantiationException {
    SdkNode ofSdk1 = SdkNodeRepository.forSdk(SDK_1, SDK_ROOT).get("ND-Lot");
    SdkNode ofSdk2 = SdkNodeRepository.forSdk(SDK_2, SDK_ROOT).get("ND-Lot");

    assertEquals(ofSdk1.getId(), ofSdk2.getId());
    assertEquals(ofSdk1.getParentId(), ofSdk2.getParentId());
    assertEquals(ofSdk1.getXpathAbsolute(), ofSdk2.getXpathAbsolute());
    assertEquals(ofSdk1.getXpathRelative(), ofSdk2.getXpathRelative());
    assertEquals(ofSdk1.isRepeatable(), ofSdk2.isRepeatable());
    assertTrue(ofSdk2.isRepeatable(), "the fixture's lot is repeatable");
  }

  /** The parents are wired whichever file the nodes come from, so the ancestry resolves. */
  @Test
  void testTheParentsAreWiredForBothVersions() throws InstantiationException {
    SdkNodeRepository ofSdk1 = SdkNodeRepository.forSdk(SDK_1, SDK_ROOT);
    SdkNodeRepository ofSdk2 = SdkNodeRepository.forSdk(SDK_2, SDK_ROOT);

    assertEquals("ND-Root", ofSdk1.get("ND-Lot").getParent().getId());
    assertNull(ofSdk1.get("ND-Root").getParent(), "the root has no parent");

    // The fwd file declares its nodes before their parents in one case, which the second pass wires.
    assertEquals("ND-Lot", ofSdk2.get(NODE_OF_THE_FWD_FILE).getParent().getId());
    assertEquals(Arrays.asList(NODE_OF_THE_FWD_FILE, "ND-Lot", "ND-Root"),
        ofSdk2.get(NODE_OF_THE_FWD_FILE).getAncestry());
  }

  /** Each SDK version gets its own node implementation, as before. */
  @Test
  void testEachSdkVersionGetsItsOwnNodes() throws InstantiationException {
    SdkNode ofSdk1 = SdkNodeRepository.forSdk(SDK_1, SDK_ROOT).get("ND-Root");
    SdkNode ofSdk2 = SdkNodeRepository.forSdk(SDK_2, SDK_ROOT).get("ND-Root");

    assertTrue(ofSdk1 instanceof SdkNodeV1, "SDK 1 gives " + ofSdk1.getClass());
    assertTrue(ofSdk2 instanceof SdkNodeV2, "SDK 2 gives " + ofSdk2.getClass());
    assertEquals("root", ((SdkNodeV2) ofSdk2).getAlias(), "SDK 2 also reads the alias");
  }

  /** The deprecated constructor still reads the xmlStructure array of the file it is given. */
  @Test
  @SuppressWarnings("deprecation")
  void testTheDeprecatedConstructorStillReadsXmlStructure() throws InstantiationException {
    SdkNodeRepository nodes = new SdkNodeRepository(SDK_1,
        SDK_ROOT.resolve(Path.of("1.16", "fields", "fields.json")));

    assertEquals(2, nodes.size());
    assertEquals("ND-Root", nodes.get("ND-Lot").getParent().getId());
  }
}
