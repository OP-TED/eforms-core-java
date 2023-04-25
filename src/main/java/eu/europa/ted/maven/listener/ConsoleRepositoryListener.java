package eu.europa.ted.maven.listener;

import static java.util.Objects.requireNonNull;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simplistic repository listener that logs events to the console.
 */
public class ConsoleRepositoryListener extends AbstractRepositoryListener {
  private static final Logger logger = LoggerFactory.getLogger(ConsoleRepositoryListener.class);

  private static final String EVENT_CANNOT_BE_NULL = "event cannot be null";

  public ConsoleRepositoryListener() {
    // Default Constructor
  }

  @Override
  public void artifactDeployed(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Deployed {} to {}", event.getArtifact(), event.getRepository());
  }

  @Override
  public void artifactDeploying(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Deploying {} to {}", event.getArtifact(), event.getRepository());
  }

  @Override
  public void artifactDescriptorInvalid(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Invalid artifact descriptor for {}: {}", event.getArtifact(),
        event.getException().getMessage());
  }

  @Override
  public void artifactDescriptorMissing(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Missing artifact descriptor for {}", event.getArtifact());
  }

  @Override
  public void artifactInstalled(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Installed {} to {}", event.getArtifact(), event.getFile());
  }

  @Override
  public void artifactInstalling(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Installing {} to {}", event.getArtifact(), event.getFile());
  }

  @Override
  public void artifactResolved(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Resolved artifact {} from {}", event.getArtifact(), event.getRepository());
  }

  @Override
  public void artifactDownloading(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Downloading artifact {} from {}", event.getArtifact(), event.getRepository());
  }

  @Override
  public void artifactDownloaded(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Downloaded artifact {} from {}", event.getArtifact(), event.getRepository());
  }

  @Override
  public void artifactResolving(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Resolving artifact {}", event.getArtifact());
  }

  @Override
  public void metadataDeployed(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Deployed {} to {}", event.getMetadata(), event.getRepository());
  }

  @Override
  public void metadataDeploying(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Deploying {} to {}", event.getMetadata(), event.getRepository());
  }

  @Override
  public void metadataInstalled(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Installed {} to {}", event.getMetadata(), event.getFile());
  }

  @Override
  public void metadataInstalling(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Installing {} to {}", event.getMetadata(), event.getFile());
  }

  @Override
  public void metadataInvalid(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Invalid metadata {}", event.getMetadata());
  }

  @Override
  public void metadataResolved(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Resolved metadata {} from {}", event.getMetadata(), event.getRepository());
  }

  @Override
  public void metadataResolving(RepositoryEvent event) {
    requireNonNull(event, EVENT_CANNOT_BE_NULL);
    logger.debug("Resolving metadata {} from {}", event.getMetadata(), event.getRepository());
  }

}
