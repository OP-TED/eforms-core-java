package eu.europa.ted.eforms.sdk.domain.codelist;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CodelistForIndex implements Serializable {
  private static final long serialVersionUID = 4221672498810948002L;

  private String id;
  private String parentId;
  private String filename;
  private String description;

  @JsonProperty("_label")
  private String labelId;

  public String getId() {
    return id;
  }

  public String getParentId() {
    return parentId;
  }

  public String getFilename() {
    return filename;
  }

  public String getDescription() {
    return description;
  }

  @JsonProperty("_label")
  public String getLabel() {
    return labelId;
  }
}
