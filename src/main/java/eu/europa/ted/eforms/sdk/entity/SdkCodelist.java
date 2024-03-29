package eu.europa.ted.eforms.sdk.entity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Representation of an SdkCodelist for usage in the symbols map.
 *
 * @author rouschr
 */
public abstract class SdkCodelist implements Comparable<SdkCodelist> {
  private final String codelistId;

  /**
   * Could avoid issues if the versions differ but the identifier is the same.
   */
  private final String codelistVersion;

  private final List<String> codes;

  private final Optional<String> parentId;

  @SuppressWarnings("unused")
  private SdkCodelist() {
    throw new UnsupportedOperationException();
  }

  /**
   * @param codelistId The identifier, not really unique as the version also matters, see .gc
   *        LongName tag. Inside the same SDK we should not have different versions of the same
   *        file.
   * @param codelistVersion The codelist version string, see Version tag in .gc files. This is NOT
   *        the SDK version. It can be useful for debug purposes and to avoid conflicts.
   * @param parentId The identifier of the parent codelist, (required only for tailored codelists).
   */
  protected SdkCodelist(final String codelistId, final String codelistVersion,
      final List<String> codes, final Optional<String> parentId) {
    this.codelistId = codelistId;
    this.codelistVersion = codelistVersion;
    this.codes = codes;
    this.parentId = parentId;
  }

  public String getCodelistId() {
    return codelistId;
  }

  public String getVersion() {
    return codelistVersion;
  }

  public List<String> getCodes() {
    return codes;
  }

  public String getRootCodelistId() {
    return parentId.isPresent() ? parentId.get() : codelistId;
  }

  @Override
  public String toString() {
    return codelistId + "-" + codelistVersion;
  }

  @Override
  public int compareTo(final SdkCodelist cl) {
    return Objects.compare(this.getCodelistId() + this.getVersion(),
        cl.getCodelistId() + cl.getVersion(), String::compareTo);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SdkCodelist other = (SdkCodelist) obj;
    return Objects.equals(codelistId, other.codelistId)
        && Objects.equals(codelistVersion, other.codelistVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codelistId, codelistVersion);
  }
}
