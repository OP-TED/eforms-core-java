package eu.europa.ted.eforms.sdk;

import java.util.ArrayList;
import java.util.Arrays;
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
    return this.version.getMajor().toString();
  }

  public String getMinor() {
    return this.version.getMinor().toString();
  }

  public String getPatch() {
    return this.version.getPatch() == null ? "0" : this.version.getPatch().toString();
  }

  public String getNextMajor() {
    return this.version.withIncMajor().toString();
  }

  public String getNextMinor() {
    return this.version.withIncMinor().toString();
  }

  public boolean isMajor() {
    return !this.isMinor() &&  this.version.getMajor() != null;
  }

  public boolean isMinor() {
    return !this.isPatch() && this.version.getMinor() != null;
  }

  public boolean isPatch() {
    return this.version.getPatch() != null;
  }

  public boolean isPreRelease() {
    return this.version.getSuffixTokens().length > 0;
  }

  public boolean isSnapshot() {
    return Arrays.asList(this.version.getSuffixTokens()).contains("SNAPSHOT");
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
    return this.version.toString();
  }

  @Override
  public int compareTo(SdkVersion that) {
    if (that == null) {
      return 1;
    }

    if (this.equals(that)) {
      return 0;
    }

    return this.version.compareTo(that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.version);
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
    return Objects.equals(this.version, other.version);
  }
}
