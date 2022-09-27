package eu.europa.ted.maven.boot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.DefaultProxySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.maven.listener.ConsoleRepositoryListener;
import eu.europa.ted.maven.listener.ConsoleTransferListener;

public class MavenBooter {
  private static final Logger logger = LoggerFactory.getLogger(MavenBooter.class);

  private static final String SETTINGS_FILE_NAME = "settings.xml";

  private static final MavenBooter INSTANCE = new MavenBooter();

  private final RepositorySystem repositorySystem;
  private final RepositorySystemSession repositorySession;
  private final Settings settings;
  private final List<RemoteRepository> repositories;

  private MavenBooter() {
    repositorySystem = ManualRepositorySystemFactory.newRepositorySystem();

    try {
      settings = getSettings();
      repositorySession = newRepositorySystemSession(repositorySystem, settings);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to configure Maven", e);
    }
    repositories = newRepositories(repositorySession, settings);
  }

  public static VersionRangeResult resolveVersionRange(Artifact artifact)
      throws VersionRangeResolutionException {
    VersionRangeRequest rangeRequest = new VersionRangeRequest().setArtifact(artifact)
        .setRepositories(MavenBooter.INSTANCE.repositories);

    return MavenBooter.INSTANCE.repositorySystem
        .resolveVersionRange(MavenBooter.INSTANCE.repositorySession, rangeRequest);
  }

  public static File resolveArtifact(Artifact artifact) throws ArtifactResolutionException {
    ArtifactRequest artifactRequest = new ArtifactRequest().setArtifact(artifact)
        .setRepositories(MavenBooter.INSTANCE.repositories);

    return MavenBooter.INSTANCE.repositorySystem
        .resolveArtifact(MavenBooter.INSTANCE.repositorySession, artifactRequest).getArtifact()
        .getFile();

  }

  private static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system,
      Settings settings) {
    DefaultRepositorySystemSession session =
        MavenRepositorySystemUtils.newSession().setProxySelector(getProxySelector(settings));

    session.setOffline(settings.isOffline());
    session.setLocalRepositoryManager(
        system.newLocalRepositoryManager(session, getLocalRepository(settings)));
    session.setTransferListener(new ConsoleTransferListener());
    session.setRepositoryListener(new ConsoleRepositoryListener());

    return session;
  }

  private static LocalRepository getLocalRepository(Settings settings) {
    List<Function<Void, Path>> localRepoDiscoveryFunctions = Arrays.asList((Void v) -> {
      logger.debug("Looking for local repository path using system property M2_LOCAL_REPO.");
      String mavenLocalRepo = System.getProperty("M2_LOCAL_REPO");

      return StringUtils.isNotBlank(mavenLocalRepo) ? Path.of(mavenLocalRepo) : null;
    }, (Void v) -> {
      logger.debug("Looking for local repository path in Maven settings.");
      String localRepoInSettings = settings.getLocalRepository();
      return StringUtils.isNotBlank(localRepoInSettings) ? Path.of(localRepoInSettings) : null;
    });

    Path localRepositoryPath = null;

    for (Function<Void, Path> f : localRepoDiscoveryFunctions) {
      localRepositoryPath = f.apply(null);
      if (localRepositoryPath != null) {
        break;
      }
    }

    if (localRepositoryPath == null) {
      logger.debug("Looking for local repository path under user's home.");
      localRepositoryPath = Path.of(System.getProperty("user.home"), ".m2", "repository");
    }

    LocalRepository localRepository = new LocalRepository(localRepositoryPath.toString());

    logger.debug("Local repository: {}", localRepository.getBasedir());

    return localRepository;
  }

  private static File getSettingsFile() {
    List<Function<Void, Path>> settingsDiscoveryFunctions = Arrays.asList((Void v) -> {
      logger.debug("Looking for settings file using system property M2_SETTINGS.");
      String mavenSettings = System.getProperty("M2_SETTINGS");
      return StringUtils.isNotBlank(mavenSettings) ? Path.of(mavenSettings) : null;
    }, (Void v) -> {
      logger.debug("Looking for settings file using environment variable MAVEN_OPTS.");

      String mavenOpts = System.getenv("MAVEN_OPTS");
      if (StringUtils.isBlank(mavenOpts)) {
        return null;
      }

      String mavenOptsUserHome = Arrays.asList(mavenOpts.split("\\s")).stream()
          .filter((String s) -> s.startsWith("-Duser.home="))
          .map((String s) -> s.replaceAll("-Duser.home=", StringUtils.EMPTY)).findFirst()
          .orElse(StringUtils.EMPTY);

      return StringUtils.isNotBlank(mavenOptsUserHome)
          ? Path.of(mavenOptsUserHome, ".m2", SETTINGS_FILE_NAME)
          : null;
    }, (Void v) -> {
      logger.debug("Looking for settings file under user's home folder.");
      return Path.of(System.getProperty("user.home"), ".m2", SETTINGS_FILE_NAME);
    }, (Void v) -> {
      logger.debug(
          "Looking for settings file under Maven Home, as set by environment variale M2_HOME.");

      String mavenHome = Optional.ofNullable(System.getenv("M2_HOME")).orElse(StringUtils.EMPTY);
      return StringUtils.isNotBlank(mavenHome) ? Path.of(mavenHome, "conf", SETTINGS_FILE_NAME)
          : null;
    });

    Path settingsPath = null;

    for (Function<Void, Path> f : settingsDiscoveryFunctions) {
      settingsPath = f.apply(null);
      if (settingsPath != null && Files.exists(settingsPath)) {
        break;
      }
    }

    if (settingsPath == null || !Files.exists(settingsPath)) {
      logger.debug("No settings file found");
      return null;
    } else {
      logger.debug("Settings path: {}", settingsPath);
      return settingsPath.toFile();
    }
  }

  private static Settings getSettings() throws SettingsBuildingException {
    File settingsFile = getSettingsFile();

    if (settingsFile == null) {
      return new Settings();
    }

    DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest()
        .setGlobalSettingsFile(settingsFile).setUserSettingsFile(settingsFile);

    return new DefaultSettingsBuilderFactory().newInstance().build(request).getEffectiveSettings();
  }

  private static List<RemoteRepository> newRepositories(RepositorySystemSession session,
      Settings settings) {
    List<RemoteRepository> remoteRepositories = new ArrayList<>();

    if (settings.getProfiles() != null) {
      List<String> activeProfiles = settings.getActiveProfiles();

      settings.getProfiles().stream().forEach((Profile profile) -> {
        if (StringUtils.isNotBlank(profile.getId()) && activeProfiles.contains(profile.getId())) {
          Optional.ofNullable(profile.getRepositories()).orElse(Collections.emptyList()).stream()
              .forEach((Repository repository) -> remoteRepositories
                  .add(toRemoteRepository(repository, session)));
        }
      });

      if (remoteRepositories.isEmpty() && !settings.isOffline()) {
        remoteRepositories.addAll(getDefaultRepositories(session));
      }
    }

    return remoteRepositories;
  }

  private static RemoteRepository toRemoteRepository(Repository repository,
      RepositorySystemSession session) {
    // need a temp repo to lookup proxy
    RemoteRepository tempRemoteRepository = toRemoteRepositoryBuilder(repository).build();
    org.eclipse.aether.repository.Proxy proxy =
        session.getProxySelector().getProxy(tempRemoteRepository);

    // now build the actual repo and attach proxy
    return toRemoteRepositoryBuilder(repository).setProxy(proxy).build();
  }

  private static RemoteRepository.Builder toRemoteRepositoryBuilder(Repository repository) {
    return new RemoteRepository.Builder(repository.getId(), "default", repository.getUrl());
  }

  private static List<RemoteRepository> getDefaultRepositories(RepositorySystemSession session) {
    RemoteRepository.Builder builder =
        new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/");

    // need a temp repo to lookup proxy
    RemoteRepository tempDefaultRepository = builder.build();
    org.eclipse.aether.repository.Proxy proxy =
        session.getProxySelector().getProxy(tempDefaultRepository);

    // now build the actual repo and attach proxy
    RemoteRepository defaultRepository = builder.setProxy(proxy).build();

    return Arrays.asList(defaultRepository);
  }

  private static ProxySelector getProxySelector(Settings settings) {
    DefaultProxySelector selector = new DefaultProxySelector();

    Optional.ofNullable(settings.getProxies()).orElse(Collections.emptyList())
        .forEach((Proxy proxy) -> selector.add(convertProxy(proxy), proxy.getNonProxyHosts()));

    return selector;
  }

  private static org.eclipse.aether.repository.Proxy convertProxy(Proxy settingsProxy) {
    AuthenticationBuilder auth = new AuthenticationBuilder()
        .addUsername(settingsProxy.getUsername()).addPassword(settingsProxy.getPassword());

    return new org.eclipse.aether.repository.Proxy(settingsProxy.getProtocol(),
        settingsProxy.getHost(), settingsProxy.getPort(), auth.build());
  }
}
