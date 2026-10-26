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
package eu.europa.ted.eforms.sdk.repository.v1;

import java.nio.file.Path;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.repository.SdkNodeRepository;

/**
 * The nodes of an SDK 1, read from the xmlStructure array of its fields/fields.json.
 */
@SdkComponent(versions = {"1"}, componentType = SdkComponentType.NODE_REPOSITORY)
public class SdkNodeRepositoryV1 extends SdkNodeRepository {
  private static final long serialVersionUID = 1L;

  /**
   * @param sdkVersion the version of the SDK
   * @param sdkPath the folder of the SDK
   */
  public SdkNodeRepositoryV1(final String sdkVersion, final Path sdkPath)
      throws InstantiationException {
    super(sdkVersion, sdkPath.resolve(SdkConstants.SdkResource.FIELDS_JSON.getPath()));
  }
}
