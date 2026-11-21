package eu.europa.ted.maven.boot;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.settings.Activation;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.AuthenticationContext;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.util.repository.DefaultProxySelector;
import org.eclipse.aether.version.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import eu.europa.ted.MavenTestSetup;
import eu.europa.ted.eforms.sdk.SdkConstants;

class MavenBooterTest extends MavenTestSetup {
  private static final List<String> availableVersions = Arrays.asList("0.6.0", "0.6.1", "0.6.2",
      "0.7.0", "0.7.3", "1.0.0-beta.1", "1.0.0", "1.0.2", "1.1.0", "1.1.3", "1.1.4-SNAPSHOT", "2.0.0-rc.1", "2.0.0-SNAPSHOT");

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

  @Test
  void testGetActiveProfiles() {
    Settings settings = new Settings();
    settings.addProfile(newProfile("listed", null));
    settings.addProfile(newProfile("listed-and-by-default", true));
    settings.addProfile(newProfile("by-default", true));
    settings.addProfile(newProfile("not-by-default", false));
    settings.addProfile(newProfile("no-activation", null));
    settings.addActiveProfile("listed");
    settings.addActiveProfile("listed-and-by-default");

    List<String> activeProfileIds = MavenBooter.getActiveProfiles(settings).stream()
        .map(Profile::getId).collect(Collectors.toList());

    Assertions.assertEquals(Arrays.asList("listed", "listed-and-by-default", "by-default"),
        activeProfileIds);
  }

  @Test
  void testNewRepositories_LogInWithTheServerOfTheSameId() {
    Settings settings = new Settings();
    Profile profile = newProfile("repositories", true);
    profile.addRepository(newRepository("private", "https://private.example.com/maven"));
    profile.addRepository(newRepository("public", "https://public.example.com/maven"));
    settings.addProfile(profile);
    settings.addServer(newServer("private", "user", "server-secret"));

    DefaultRepositorySystemSession session = newSession();
    List<RemoteRepository> repositories = MavenBooter.newRepositories(session, settings);

    Assertions.assertEquals(Arrays.asList("user", "server-secret"),
        loginOf(session, repositories.get(0)));
    Assertions.assertNull(repositories.get(1).getAuthentication());
  }

  @Test
  void testNewRepositories_LogInToCentralWithItsServer() {
    // Without repositories in the settings, Maven Central is used, with its server if there is one.
    Settings settings = new Settings();
    settings.addProfile(newProfile("no-repositories", true));
    settings.addServer(newServer("central", "user", "server-secret"));

    DefaultRepositorySystemSession session = newSession();
    List<RemoteRepository> repositories = MavenBooter.newRepositories(session, settings);

    Assertions.assertEquals("central", repositories.get(0).getId());
    Assertions.assertEquals(Arrays.asList("user", "server-secret"),
        loginOf(session, repositories.get(0)));
  }

  @Test
  void testDecrypt_DecryptsTheEncryptedPasswords() throws URISyntaxException {
    Settings settings = new Settings();
    settings.addServer(newServer("encrypted", "user", "{dSycwJ4MEPoHWfzx6UndIE4o959VjcGYc9kW8/1WIkI=}"));
    settings.addServer(newServer("plain", "user", "plain-secret"));
    Proxy proxy = new Proxy();
    proxy.setId("proxy");
    proxy.setPassword("{j/sU9FMXHLQHsSLiFxjO2uWHqElytXh2An3W4pg+Cb8=}");
    settings.addProxy(proxy);

    // The system property would name another file than the one of the test, as for Maven.
    String securityLocation =
        System.clearProperty(DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION);
    try {
      MavenBooter.decrypt(settings, Path.of(MavenBooterTest.class.getClassLoader()
          .getResource("dummy-m2/settings-security.xml").toURI()));
    } finally {
      if (securityLocation != null) {
        System.setProperty(DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, securityLocation);
      }
    }

    Assertions.assertEquals("server-secret", settings.getServer("encrypted").getPassword());
    Assertions.assertEquals("plain-secret", settings.getServer("plain").getPassword());
    Assertions.assertEquals("proxy-secret", settings.getProxies().get(0).getPassword());
  }

  @Test
  void testGetSettingsSecurityFile_IsInTheUserHomeWhereverTheSettingsAre() {
    // The tests read their settings from dummy-m2 (see MavenTestSetup), and Maven still looks for
    // the master password in the .m2 folder of the user's home.
    Assertions.assertEquals(
        Path.of(System.getProperty("user.home"), ".m2", "settings-security.xml"),
        MavenBooter.getSettingsSecurityFile());
  }

  private static DefaultRepositorySystemSession newSession() {
    return new DefaultRepositorySystemSession().setProxySelector(new DefaultProxySelector());
  }

  /**
   * @return The username and the password that the repository logs in with.
   */
  private static List<String> loginOf(DefaultRepositorySystemSession session,
      RemoteRepository repository) {
    try (AuthenticationContext context = AuthenticationContext.forRepository(session, repository)) {
      return Arrays.asList(context.get(AuthenticationContext.USERNAME),
          context.get(AuthenticationContext.PASSWORD));
    }
  }

  private static Repository newRepository(String id, String url) {
    Repository repository = new Repository();
    repository.setId(id);
    repository.setUrl(url);
    return repository;
  }

  private static Server newServer(String id, String username, String password) {
    Server server = new Server();
    server.setId(id);
    server.setUsername(username);
    server.setPassword(password);
    return server;
  }

  private static Profile newProfile(String id, Boolean activeByDefault) {
    Profile profile = new Profile();
    profile.setId(id);

    if (activeByDefault != null) {
      Activation activation = new Activation();
      activation.setActiveByDefault(activeByDefault);
      profile.setActivation(activation);
    }

    return profile;
  }
}
