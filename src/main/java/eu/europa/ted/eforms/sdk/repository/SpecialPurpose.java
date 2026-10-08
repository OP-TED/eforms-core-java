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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ted.eforms.sdk.SdkConstants;

/**
 * Reads the map that names the fields or the nodes that have a special role, which
 * fields/fwd/fields.json and fields/fwd/nodes.json of SDK 2 each start with (TEDEFO-1877,
 * TEDEFO-5210, TEDEFO-5230).
 *
 * <p>
 * Applications use the key, such as "disclosureDate", instead of hard-coding the identifier. The
 * keys are plain strings, as the map is data and will grow.
 * </p>
 */
final class SpecialPurpose {

  private SpecialPurpose() {}

  /**
   * @param json the root of a file that may start with a special purpose map
   * @return the map as the file gives it, in the order of the file, or an empty map for the SDK
   *         versions whose files carry no such map
   */
  static Map<String, String> read(final JsonNode json) {
    final JsonNode specialPurpose = json.get(SdkConstants.SPECIAL_PURPOSE_KEY);
    if (specialPurpose == null || !specialPurpose.isObject()) {
      return Collections.emptyMap();
    }

    // Keep the order of the file, so that what is read back is what the SDK gives.
    final Map<String, String> byKey = new LinkedHashMap<>();
    final Iterator<String> keys = specialPurpose.fieldNames();
    while (keys.hasNext()) {
      final String key = keys.next();
      if (specialPurpose.hasNonNull(key)) {
        byKey.put(key, specialPurpose.get(key).asText(null));
      }
    }
    return Collections.unmodifiableMap(byKey);
  }
}
