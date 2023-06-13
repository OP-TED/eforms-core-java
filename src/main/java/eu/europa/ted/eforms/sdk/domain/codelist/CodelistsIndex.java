package eu.europa.ted.eforms.sdk.domain.codelist;

import java.io.Serializable;
import java.util.List;

public class CodelistsIndex implements Serializable {
  private static final long serialVersionUID = -6549217565224309697L;

  private List<CodelistForIndex> codelists;

  public List<CodelistForIndex> getCodelists() {
    return codelists;
  }
}
