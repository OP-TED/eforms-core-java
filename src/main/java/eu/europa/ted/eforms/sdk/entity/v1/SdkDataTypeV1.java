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
package eu.europa.ted.eforms.sdk.entity.v1;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.entity.SdkDataType;

/**
 * An SDK 1 data type. SDK 1 publishes no data types of its own, so these are read from the
 * data-types.json resource of this library (TEDEFO-5231).
 */
@SdkComponent(versions = {"1"}, componentType = SdkComponentType.DATA_TYPE)
public class SdkDataTypeV1 extends SdkDataType {

  public SdkDataTypeV1(final String id, final String privacyMask) {
    super(id, privacyMask);
  }

  public SdkDataTypeV1(final JsonNode dataType) {
    super(dataType);
  }
}
