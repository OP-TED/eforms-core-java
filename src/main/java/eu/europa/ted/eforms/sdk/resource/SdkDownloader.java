package eu.europa.ted.eforms.sdk.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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

  private SdkDownloader() {
  }

  /**
   * Downloads a SDK version from Maven Central (or local Maven repository) and
   * unpacks it in a subfolder under the given root directory.
   * 
   * If the given version is not specific enough (e.g. 1.0), its latest
   * patch version will be downloaded.
   *
   * @param sdkVersion       The SDK version to download.
   * @param rootDir          The root directory where the SDK will be downloaded
   * @param includeSnapshots If true, the latest snapshot version will be
   *                         downloaded if the given version is not found
   * @throws IOException If the download fails.
   */
  public static void downloadSdk(final SdkVersion sdkVersion, final Path rootDir, boolean includeSnapshots)
      throws IOException {
    Path sdkDir = Path.of(Optional.ofNullable(rootDir).orElse(SdkConstants.DEFAULT_SDK_ROOT).toString(),
        sdkVersion.isPatch() ? sdkVersion.toString() : sdkVersion.toStringWithoutPatch());

    try {
      SdkVersion artifactVersion = getLatestSdkVersion(sdkVersion, includeSnapshots);

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

  /**
   * Overload of {@link #downloadSdk(SdkVersion, Path, boolean)} that excludes snapshot versions.
   * 
   * @param sdkVersion    The SDK version to download.
   * @param rootDir       The root directory where the SDK will be downloaded.
   * @throws IOException  If the download fails.
   */
  public static void downloadSdk(final SdkVersion sdkVersion, final Path rootDir)
      throws IOException {
    downloadSdk(sdkVersion, rootDir, false);
  }

  /**
   * Overload of {@link #downloadSdk(SdkVersion, Path, boolean)}.
   * 
   * @param sdkVersion The SDK version to download.
   * @throws IOException If the download fails.
   */
  public static void downloadSdk(final String sdkVersion) throws IOException {
    downloadSdk(sdkVersion, false);
  }

  /**
   * Overload of {@link #downloadSdk(SdkVersion, Path, boolean)}.
   * 
   * @param sdkVersion       The SDK version to download.
   * @param includeSnapshots If true, then SNAPSHOT versions will be downloaded if
   *                         no other version is available.
   * @throws IOException If the download fails.
   */
  public static void downloadSdk(final String sdkVersion, boolean includeSnapshots) throws IOException {
    downloadSdk(sdkVersion, SdkConstants.DEFAULT_SDK_ROOT, includeSnapshots);
  }

  /**
   * Overload of {@link #downloadSdk(SdkVersion, Path, boolean)}.
   * 
   * @param sdkVersion The SDK version to download.
   * @throws IOException If the download fails.
   */
  public static void downloadSdk(final SdkVersion sdkVersion) throws IOException {
    downloadSdk(sdkVersion, false);
  }

  /**
   * Overload of {@link #downloadSdk(SdkVersion, Path, boolean)}.
   * 
   * @param sdkVersion       The SDK version to download.
   * @param includeSnapshots If true, then SNAPSHOT versions will be downloaded if
   *                         no other version is available.
   * @throws IOException If the download fails.
   */
  public static void downloadSdk(final SdkVersion sdkVersion, boolean includeSnapshots) throws IOException {
    downloadSdk(sdkVersion, SdkConstants.DEFAULT_SDK_ROOT, includeSnapshots);
  }

  /**
   * Overload of {@link #downloadSdk(SdkVersion, Path, boolean)}.
   * 
   * @param sdkVersion The SDK version to download.
   * @param rootDir    The root directory to download the SDK to.
   * @throws IOException If the download fails.
   */
  public static void downloadSdk(final String sdkVersion, final Path rootDir) throws IOException {
    downloadSdk(sdkVersion, rootDir, false);
  }

  /**
   * Overload of {@link #downloadSdk(SdkVersion, Path, boolean)}.
   * 
   * If the version is not specific enough (i.e. does not include a patch
   * version), then the latest available version will be downloaded.
   * 
   * @param sdkVersion       The SDK version to download.
   * @param rootDir          The root directory to download the SDK to.
   * @param includeSnapshots If true, then SNAPSHOT versions will be downloaded if
   *                         no other version is available.
   * @throws IOException If the download fails.
   */
  public static void downloadSdk(final String sdkVersion, final Path rootDir, boolean includeSnapshots)
      throws IOException {
    downloadSdk(new SdkVersion(sdkVersion), rootDir, includeSnapshots);
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
   * Discovers the latest available version for a given base version. 
   * 
   * @param sdkVersion       The base version to look for.
   * @param includeSnapshots Whether to resolve SNAPSHOT versions if needed (e.g.,
   *                         1.0.0-SNAPSHOT)
   * @return The latest available version.
   */
  private static SdkVersion getLatestSdkVersion(final SdkVersion baseVersion, final boolean includeSnapshots)
      throws VersionRangeResolutionException {
    Validate.notNull(baseVersion, "Undefined base version");

    try {
      return getHighestVersion(resolveVersionRange(getSearchPattern(baseVersion)), includeSnapshots);
    } catch (NoSuchElementException e1) {
      throw new IllegalArgumentException(
          MessageFormat.format("No artifacts found for SDK {0}.", baseVersion));
    }
  }

  /**
   * Defines the search pattern for release versions of a given SDK version.
   * 
   * @param sdkVersion The base version to construct the search pattern for.
   * @return A search pattern for release versions of the given base version.
   */
  private static String getSearchPattern(SdkVersion sdkVersion) {
    if (sdkVersion.isPatch()) {
      return MessageFormat.format("[{0}]", sdkVersion.toString());
    }
    return MessageFormat.format("[{0}.*]", sdkVersion.toString());
  }

  /**
   * Uses Maven to resolve a version range from a given search pattern.
   * 
   * @param searchPattern The search pattern to resolve.
   * @return A version range result.
   * @throws VersionRangeResolutionException If the version range could not be
   *                                         resolved.
   */
  private static VersionRangeResult resolveVersionRange(String searchPattern) throws VersionRangeResolutionException {
    Artifact artifact = new DefaultArtifact(SdkConstants.SDK_GROUP_ID, SdkConstants.SDK_ARTIFACT_ID, "jar",
        searchPattern);
    return MavenBooter.resolveVersionRange(artifact);
  }

  /**
   * Gets the highest version from a version range.
   * A pre-release version will be returned only if there is no released version in the range.
   * 
   * @param versionRange The version range to search.
   * @param includeSnapshots Whether to include SNAPSHOT versions.
   * @return The highest version found.
   * @throws NoSuchElementException If the version range is empty.
   */
  private static SdkVersion getHighestVersion(VersionRangeResult versionRange, boolean includeSnapshots) throws NoSuchElementException {
  
    List<Version> rangeVersions = versionRange.getVersions();

    if (CollectionUtils.isEmpty(rangeVersions)) {
      throw new NoSuchElementException();
    }

    // Get all the versions in the range into a list of SdkVersion objects.
    // Exclude snapshots if requested.
    List<SdkVersion> allVersions = rangeVersions.stream()
        .map(Object::toString)
        .map(SdkVersion::new)
        .filter(v -> includeSnapshots || !v.isSnapshot())
        .collect(Collectors.toList());

    // Get all the release versions into a separate list.
    // Make sure to include snapshots if requested.
    List<SdkVersion> releaseVersions = allVersions.stream()
        .filter(v -> !v.isPreRelease() || (v.isSnapshot() && includeSnapshots))
        .collect(Collectors.toList());

    // We will only consider pre-release versions if there are no release versions.
    List<SdkVersion> eligibleVersions = CollectionUtils.isEmpty(releaseVersions) ? allVersions : releaseVersions;

    // If there are no eligible versions, then throw an exception.
    if (CollectionUtils.isEmpty(eligibleVersions)) {
      throw new NoSuchElementException();
    }

    // We can simply return the last version from the list, as Maven sorts versions as we need.
    return eligibleVersions.get(eligibleVersions.size() -1);
  }
}
