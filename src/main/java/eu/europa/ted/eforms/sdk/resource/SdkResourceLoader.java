package eu.europa.ted.eforms.sdk.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.SdkVersion;

public class SdkResourceLoader {
  private SdkResourceLoader() {}

  /**
   * Returns the path to a resource under the given subfolder. The subfolder is meant to exist under
   * the defined root path.
   *
   * @param sdkVersion The target SDK version
   * @param resourceType The resource type
   * @param filename The target filename
   * @param sdkRootPath Path of the root SDK folder
   * @return The target resource's path
   */
  public static Path getResourceAsPath(final SdkVersion sdkVersion,
      final PathResource resourceType, String filename, Path sdkRootPath) {
    Validate.notNull(sdkVersion, "Undefined SDK version");

    final String sdkDir =
        sdkVersion.isPatch() ? sdkVersion.toString() : sdkVersion.toStringWithoutPatch();

    final String resourcePath = Optional.ofNullable(resourceType)
        .map(PathResource::getPath)
        .map(Path::toString)
        .orElse(StringUtils.EMPTY);

    sdkRootPath = ObjectUtils.defaultIfNull(sdkRootPath, SdkConstants.DEFAULT_SDK_ROOT);
    filename = ObjectUtils.defaultIfNull(filename, StringUtils.EMPTY);

    final Path result =
        Path.of(sdkRootPath.toString(), sdkDir, resourcePath, filename).toAbsolutePath();

    Validate.isTrue(Files.exists(result),
        MessageFormat.format("Resource [{0}] does not exist", result));

    return result;
  }

  public static Path getResourceAsPath(final SdkVersion sdkVersion,
      final PathResource resourceType,
      Path sdkRootPath) {
    return getResourceAsPath(sdkVersion, resourceType, null, sdkRootPath);
  }

  public static Path getResourceAsPath(final String sdkVersion,
      final PathResource resourceType, String filename, Path sdkRootPath) {
    return getResourceAsPath(new SdkVersion(sdkVersion), resourceType, filename, sdkRootPath);
  }

  public static Path getResourceAsPath(final String sdkVersion,
      final PathResource resourceType, Path sdkRootPath) {
    return getResourceAsPath(new SdkVersion(sdkVersion), resourceType, sdkRootPath);
  }

  /**
   * Returns a resource of the given SDK version as an input stream.
   *
   * @param sdkVersion The target SDK version
   * @param resourceType The resource type
   * @param filename The target filename
   * @param sdkRootPath Path of the root SDK folder
   * @return The target resource as a stream
   * @throws IOException if the resource cannot be retrieved
   */
  public static InputStream getResourceAsStream(final SdkVersion sdkVersion,
      final PathResource resourceType, final String filename, Path sdkRootPath) throws IOException {
    return Files.newInputStream(getResourceAsPath(sdkVersion, resourceType, filename, sdkRootPath));
  }

  public static InputStream getResourceAsStream(final String sdkVersion,
      final PathResource resourceType, final String filename, Path sdkRootPath) throws IOException {
    return getResourceAsStream(new SdkVersion(sdkVersion), resourceType, filename, sdkRootPath);
  }
}
