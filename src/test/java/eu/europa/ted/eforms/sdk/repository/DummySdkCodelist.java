package eu.europa.ted.eforms.sdk.repository;

import java.util.List;
import java.util.Optional;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.sdk.entity.SdkCodelist;

@SdkComponent(versions = "999", componentType = SdkComponentType.CODELIST)
public class DummySdkCodelist extends SdkCodelist {
  public DummySdkCodelist(String codelistId, String codelistVersion, List<String> codes,
      Optional<String> parentId) {
    super(codelistId, codelistVersion, codes, parentId);
  }
}
