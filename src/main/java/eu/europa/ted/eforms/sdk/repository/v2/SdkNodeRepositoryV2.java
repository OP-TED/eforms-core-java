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
import eu.europa.ted.eforms.sdk.repository.SdkNodeRepository;

/**
 * The nodes of an SDK 2, read from its fields/fwd/nodes.json (TEDEFO-5228), with the map of stable
 * identifiers, which the file starts with (TEDEFO-5230).
 */
@SdkComponent(versions = {"2"}, componentType = SdkComponentType.NODE_REPOSITORY)
public class SdkNodeRepositoryV2 extends SdkNodeRepository {
  private static final long serialVersionUID = 1L;

  /**
   * @param sdkVersion the version of the SDK
   * @param sdkPath the folder of the SDK
   */
  public SdkNodeRepositoryV2(final String sdkVersion, final Path sdkPath)
      throws InstantiationException {
    super(sdkVersion, sdkPath.resolve(SdkConstants.SdkResource.FIELDS_FWD_NODES_JSON.getPath()));
  }

  @Override
  protected void populateMap(final JsonNode json) throws InstantiationException {
    this.readStableIds(json);
    this.addNodes((ArrayNode) json.get(SdkConstants.NODES_JSON_NODES_KEY));
  }
}
