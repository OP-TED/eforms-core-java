package eu.europa.ted.eforms.sdk;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class SdkVersion implements Comparable<SdkVersion> {
  private static final String FORMAT_PATTERN = "{0}.{1}.{2}";

  private String major = "0";
  private String minor = "0";
  private String patch = "0";

  private boolean isPatch = false;

  @SuppressWarnings("unused")
  private SdkVersion() {}

  public SdkVersion(final String version) {
    Validate.notBlank(version, "Undefined version");
    Validate.matchesPattern(version, "[0-9]+(\\.[0-9]+)*(-SNAPSHOT)?", "Invalid version format");

    String[] versionParts = version.split("\\.");

    this.major = versionParts[0];

    if (versionParts.length > 1) {
      this.minor = versionParts[1];
    }

    if (versionParts.length > 2) {
      this.isPatch = true;
      this.patch = versionParts[2];
    }
  }

  public String getMajor() {
    return major;
  }

  public String getMinor() {
    return minor;
  }

  public String getPatch() {
    return patch;
  }

  public String getNextMajor() {
    return new SdkVersion(MessageFormat.format(FORMAT_PATTERN, getAsInt(major) + 1, minor, patch))
        .toString();
  }

  public String getNextMinor() {
    return new SdkVersion(MessageFormat.format(FORMAT_PATTERN, major, getAsInt(minor) + 1, patch))
        .toString();
  }

  public boolean isPatch() {
    return isPatch;
  }

  public String toNormalisedString(boolean withPatch) {
    List<String> parts = new ArrayList<>();

    parts.add(major);
    parts.add(minor);

    if (withPatch) {
      parts.add(patch);
    }

    return StringUtils.join(parts, ".");
  }

  public String toStringWithoutPatch() {
    return toNormalisedString(false);
  }

  @Override
  public String toString() {
    return toNormalisedString(true);
  }

  @Override
  public int compareTo(SdkVersion that) {
    if (that == null) {
      return 1;
    }

    if (this.equals(that)) {
      return 0;
    }

    if (getAsInt(this.getMajor()) == getAsInt(that.getMajor())) {
      if (getAsInt(this.getMinor()) == getAsInt(that.getMinor())) {
        return getAsInt(this.getPatch()) < getAsInt(that.getPatch()) ? -1 : 1;
      } else {
        return getAsInt(this.getMinor()) < getAsInt(that.getMinor()) ? -1 : 1;
      }
    } else {
      return getAsInt(this.getMajor()) < getAsInt(that.getMajor()) ? -1 : 1;
    }
  }

  private int getAsInt(String versionPart) {
    return Integer.parseInt(Optional.ofNullable(versionPart).orElse("0").replace("-SNAPSHOT", ""));
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, patch);
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
    return Objects.equals(major, other.major) && Objects.equals(minor, other.minor)
        && Objects.equals(patch, other.patch);
  }
}
