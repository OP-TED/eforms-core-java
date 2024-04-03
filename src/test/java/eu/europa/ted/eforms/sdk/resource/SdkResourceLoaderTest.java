package eu.europa.ted.eforms.sdk.resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import eu.europa.ted.MavenTestSetup;
import eu.europa.ted.eforms.sdk.SdkConstants;

public class SdkResourceLoaderTest extends MavenTestSetup {
  private static final Path SDK_ROOT_DIR = Path.of("target/eforms-sdk");

  @BeforeAll
  static void downloadSdk() throws IOException {
    SdkDownloader.downloadSdk("1.1", SDK_ROOT_DIR);
  }

  @Test
  void testGetResourceAsPath() {
    Path path = SdkResourceLoader.getResourceAsPath("1.1.3",
        SdkConstants.SdkResource.FIELDS_JSON, SDK_ROOT_DIR);

    assertTrue(path.endsWith("fields/fields.json"));
  }

  @Test
  void testGetResourceAsStream() throws IOException {
    InputStream is = SdkResourceLoader.getResourceAsStream("1.1.3",
        SdkConstants.SdkResource.FIELDS, "fields.json", SDK_ROOT_DIR);

    assertTrue(is.read() >= 0);
    is.close();
  }
}
