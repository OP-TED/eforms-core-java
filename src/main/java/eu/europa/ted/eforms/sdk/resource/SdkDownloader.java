package eu.europa.ted.eforms.sdk.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.SdkVersion;
import eu.europa.ted.maven.MavenUtils;
import eu.europa.ted.maven.boot.MavenBooter;
import eu.europa.ted.util.ArchiveUtils;

public class SdkDownloader {
  private static final Logger logger = LoggerFactory.getLogger(SdkDownloader.class);

  private SdkDownloader() {}

  /**
   * Downloads a SDK version from Maven Central (or local Maven repository) and unpacks it under the
   * given root directory. - If a major version is requested (e.g., "1"), then the latest
   * minor/patch will be fetched and will be stored under a directory
   * "&lt;root_directory&gt;/&lt;minor&gt;". - If a minor version is requested (e.g., "1.1"), then
   * the latest patch for this minor will be fetched and will be stored under a directory
   * "&lt;root_directory&gt;/&lt;minor&gt;" - If a patch is request (e.g., "1.1.2"), then that patch
   * will be fetched and will be stored under a directory
   * "&lt;root_directory&gt;/&lt;minor&gt;/&lt;patch&gt;". If the requested patch is not found, then
   * an {@link IllegalArgumentException} will be thrown.
   *
   * @param sdkVersion The target SDK version (&lt;major&gt;.&lt;minor&gt;.&lt;patch&gt;)
   * @param rootDir The root directory
   * @throws IOException if the download fails
   */
  public static void downloadSdk(final SdkVersion sdkVersion, final Path rootDir)
      throws IOException {
    Path sdkDir =
        Path.of(Optional.ofNullable(rootDir).orElse(SdkConstants.DEFAULT_SDK_ROOT).toString(),
            sdkVersion.isPatch() ? sdkVersion.toString() : sdkVersion.toStringWithoutPatch());

    try {
      SdkVersion artifactVersion = getLatestSdkVersion(sdkVersion);

      if (sdkExistsAt(artifactVersion, sdkDir)) {
        logger.debug("SDK [{}] found at [{}]. No download required.", artifactVersion, sdkDir);
      } else {
        logger.info("Downloading eForms SDK version [{}]", artifactVersion);
        logger.debug("Target directory: {}", sdkDir.toAbsolutePath());

        String artifactCoords = MessageFormat.format("{0}:{1}:jar:{2}", SdkConstants.SDK_GROUP_ID,
            SdkConstants.SDK_ARTIFACT_ID, artifactVersion);
        ArchiveUtils.unzip(MavenUtils.resolve(artifactCoords), sdkDir);

        logger.debug("Successfully downloaded eForms SDK [{}] onto [{}].", artifactVersion,
            sdkDir.toAbsolutePath());

        createVersionFile(artifactVersion, sdkDir);
      }
    } catch (IOException e) {
      logger.debug("Failed to download eForms SDK with base version {}: {}", sdkVersion,
          e.getMessage());
      throw e;
    } catch (VersionRangeResolutionException | ArtifactResolutionException e) {
      logger.debug("Failed to download eForms SDK with base version {}: {}", sdkVersion,
          e.getMessage());
      throw new IOException(e);
    }
  }

  public static void downloadSdk(final String sdkVersion) throws IOException {
    downloadSdk(sdkVersion, SdkConstants.DEFAULT_SDK_ROOT);
  }

  public static void downloadSdk(final SdkVersion sdkVersion) throws IOException {
    downloadSdk(sdkVersion, SdkConstants.DEFAULT_SDK_ROOT);
  }

  public static void downloadSdk(final String sdkVersion, final Path rootDir) throws IOException {
    downloadSdk(new SdkVersion(sdkVersion), rootDir);
  }

  private static Path createVersionFile(final SdkVersion sdkVersion, final Path sdkDir)
      throws IOException {
    Path versionFilePath = Path.of(sdkDir.toString(), "VERSION");

    if (!Files.exists(versionFilePath)) {
      logger.debug("Creating version file [{}] for SDK [{}]", versionFilePath, sdkVersion);

      Files.createFile(versionFilePath);
      Files.writeString(versionFilePath, sdkVersion.toString());

      logger.debug("Successfully created version file [{}] for SDK [{}]", versionFilePath,
          sdkVersion);
    }

    return versionFilePath;
  }

  private static boolean sdkExistsAt(final SdkVersion sdkVersion, final Path sdkDir) {
    if (sdkVersion == null || sdkDir == null) {
      return false;
    }

    try {
      return sdkVersion.toString().equals(Files.readString(Path.of(sdkDir.toString(), "VERSION")));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Discovers the latest available version for a given base version. If the major of the base
   * version is zero, then the second digit is regarded as major. E.g.: - For base version 0.6, the
   * result is the largest found version number between 0.6 and 0.7 - For base version 1.0, the
   * result is the largest found version number between 1.0 and 1.1
   * 
   * @param sdkVersion The base version
   * @return
   */
  private static SdkVersion getLatestSdkVersion(final SdkVersion baseVersion)
      throws VersionRangeResolutionException {
    Validate.notNull(baseVersion, "Undefined base version");

    String minVersion = baseVersion.toString();
    String maxVersion = baseVersion.getNextMinor();
    String searchPattern = "[{0},{1})";

    if (baseVersion.isPatch()) {
      minVersion = maxVersion = baseVersion.toString();
      searchPattern = "[{0},{1}]";
    }

    Artifact artifact = new DefaultArtifact(SdkConstants.SDK_GROUP_ID, SdkConstants.SDK_ARTIFACT_ID,
        "jar", MessageFormat.format(searchPattern, minVersion, maxVersion));

    VersionRangeResult versions = MavenBooter.resolveVersionRange(artifact);
    try {
      /**
       * If the major version is "0", Maven will return all the versions up to the next major. In
       * this case we need to filter out all the discovered versions where the minor is not the same
       * as that of the base version.
       */
      if (baseVersion.getMajor().equals("0")) {
        return versions.getVersions().stream()
            .map((Version version) -> new SdkVersion(version.toString()))
            .filter((SdkVersion v) -> v.getMajor().equals(baseVersion.getMajor())
                && v.getMinor().equals(baseVersion.getMinor()))
            .max((SdkVersion i, SdkVersion j) -> i.compareTo(j)).orElseThrow();
      } else {
        Version highestVersion = versions.getHighestVersion();

        if (highestVersion == null) {
          throw new NoSuchElementException();
        }

        return new SdkVersion(highestVersion.toString());
      }
    } catch (NoSuchElementException e) {
      String snapshotVersion = MessageFormat.format("{0}-SNAPSHOT", baseVersion);
      logger.warn("No artifacts were found for SDK version [{}]. Trying with [{}]", baseVersion,
          snapshotVersion);

      artifact = new DefaultArtifact(MessageFormat.format("{0}:{1}:{2}", SdkConstants.SDK_GROUP_ID,
          SdkConstants.SDK_ARTIFACT_ID, snapshotVersion));

      versions = MavenBooter.resolveVersionRange(artifact);

      if (CollectionUtils.isEmpty(versions.getVersions())) {
        throw new IllegalArgumentException(
            MessageFormat.format("No artifacts were found for SDK version [{0}]", snapshotVersion));
      }

      return new SdkVersion(versions.getVersions().get(0).toString());
    }
  }
}
