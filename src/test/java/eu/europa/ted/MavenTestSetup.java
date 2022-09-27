package eu.europa.ted;

import java.net.URISyntaxException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;

public class MavenTestSetup {
  @BeforeAll
  public static void setUserHome() throws URISyntaxException {
    System.setProperty("M2_SETTINGS",
        Path.of(MavenTestSetup.class.getClassLoader().getResource("dummy-m2/settings.xml").toURI())
            .toString());
  }
}
