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
package eu.europa.ted.eforms.sdk.entity.v2;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.entity.v1.SdkDataTypeV1;

/**
 * An SDK 2 data type, read from fields/fwd/data-types.json of the SDK (TEDEFO-5231).
 *
 * <p>
 * The file has the same format as the SDK 1 resource for the properties this entity reads, so
 * there is nothing to add here yet. The class exists so that the two versions can diverge without
 * changing the callers, as for the other entities that differ per version.
 * </p>
 */
@SdkComponent(versions = {"2"}, componentType = SdkComponentType.DATA_TYPE)
public class SdkDataTypeV2 extends SdkDataTypeV1 {

  public SdkDataTypeV2(final String id, final String privacyMask) {
    super(id, privacyMask);
  }

  public SdkDataTypeV2(final JsonNode dataType) {
    super(dataType);
  }
}
