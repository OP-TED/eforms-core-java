package eu.europa.ted.maven.boot;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import eu.europa.ted.MavenTestSetup;
import eu.europa.ted.eforms.sdk.SdkConstants;

class MavenBooterTest extends MavenTestSetup {
  private static final List<String> availableVersions = Arrays.asList("0.6.0", "0.6.1", "0.6.2",
      "0.7.0", "0.7.3", "1.0.0", "1.0.2", "1.1.0", "1.1.3");

  @Test
  void testResolveVersionRange()
      throws ArtifactResolutionException, VersionRangeResolutionException {
    Artifact artifact = new DefaultArtifact(SdkConstants.SDK_GROUP_ID, SdkConstants.SDK_ARTIFACT_ID,
        "jar", "[0.0.0,2.0.0]");
    VersionRangeResult versionRange = MavenBooter.resolveVersionRange(artifact);
    Assertions.assertTrue(CollectionUtils.isEqualCollection(availableVersions,
        versionRange.getVersions().stream().map(Version::toString).collect(Collectors.toList())));
  }

  @Test
  void testResolveArtifact() throws ArtifactResolutionException {
    Artifact artifact = new DefaultArtifact(SdkConstants.SDK_GROUP_ID, SdkConstants.SDK_ARTIFACT_ID,
        "jar", "1.1.3");
    File artifactFile = MavenBooter.resolveArtifact(artifact);
    Assertions.assertTrue(Files.exists(artifactFile.toPath()));
  }
}
