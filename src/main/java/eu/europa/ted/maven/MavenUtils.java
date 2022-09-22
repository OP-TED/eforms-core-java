package eu.europa.ted.maven;

import java.io.File;
import org.apache.commons.lang3.Validate;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.maven.boot.MavenBooter;

public class MavenUtils {
  private static final Logger logger = LoggerFactory.getLogger(MavenUtils.class);

  private MavenUtils() {}

  public static File resolve(String artifactCoords) throws ArtifactResolutionException {
    Validate.notBlank(artifactCoords, "Undefined artifact coordinates.");

    logger.debug("Resolving artifact with coordinates [{}]", artifactCoords);

    Artifact artifact = new DefaultArtifact(artifactCoords);

    File artifactFile = MavenBooter.resolveArtifact(artifact);

    logger.debug("Resolved [{}] as [{}].", artifact, artifactFile);

    return artifactFile;
  }
}
