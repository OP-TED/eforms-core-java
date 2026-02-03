package eu.europa.ted.eforms.sdk.entity.v2;

import java.util.List;
import java.util.Optional;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.entity.v1.SdkCodelistV1;

/**
 * Representation of an SdkCodelist for usage in the symbols map.
 *
 * @author rouschr
 */
@SdkComponent(versions = {"2"}, componentType = SdkComponentType.CODELIST)
public class SdkCodelistV2 extends SdkCodelistV1 {

  public SdkCodelistV2(final String codelistId, final String codelistVersion,
      final List<String> codes, final Optional<String> parentId) {
    super(codelistId, codelistVersion, codes, parentId);
  }
}
