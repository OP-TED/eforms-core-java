package eu.europa.ted.eforms.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;

public class SdkVersion implements Comparable<SdkVersion> {

  private final Semver version;

  public SdkVersion(final String version) {
    Validate.notBlank(version, "Undefined version");

    // LOOSE because we need to accept MAJOR.MINOR
    this.version = new Semver(version, SemverType.LOOSE);

    // Check that we did get a MINOR part
    Validate.notNull(this.version.getMinor());
  }

  public String getMajor() {
    return version.getMajor().toString();
  }

  public String getMinor() {
    return version.getMinor().toString();
  }

  public String getPatch() {
    return version.getPatch() == null ? "0" : version.getPatch().toString();
  }

  public String getNextMajor() {
    return version.withIncMajor().toString();
  }

  public String getNextMinor() {
    return version.withIncMinor().toString();
  }

  public boolean isPatch() {
    return version.getPatch() != null;
  }

  public String toNormalisedString(boolean withPatch) {
    List<String> parts = new ArrayList<>();

    parts.add(getMajor());
    parts.add(getMinor());

    if (withPatch) {
      parts.add(getPatch());
    }

    return StringUtils.join(parts, ".");
  }

  public String toStringWithoutPatch() {
    return toNormalisedString(false);
  }

  @Override
  public String toString() {
    return version.toString();
  }

  @Override
  public int compareTo(SdkVersion that) {
    if (that == null) {
      return 1;
    }

    if (this.equals(that)) {
      return 0;
    }

    return version.compareTo(that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SdkVersion other = (SdkVersion) obj;
    return Objects.equals(version, other.version);
  }
}
