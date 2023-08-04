package eu.europa.ted.eforms.sdk.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import eu.europa.ted.MavenTestSetup;

class SdkDownloaderTest extends MavenTestSetup {
  private static final Path SDK_ROOT_DIR = Path.of("target/eforms-sdk");

  private void checkVersions(String sdkBaseVersion, String sdkFullVersion, String sdkDirName)
      throws IOException {
    Path versionFilePath = Path.of(SDK_ROOT_DIR.toString(), sdkDirName, "VERSION");
    Assertions.assertEquals(sdkFullVersion, Files.readString(versionFilePath));

    Path fieldsJsonPath = Path.of(SDK_ROOT_DIR.toString(), sdkDirName, "fields", "fields.json");
    String sdkVersionOnFieldsJson = StringUtils.EMPTY;

    try (JsonParser jsonParser = new JsonFactory().createParser(fieldsJsonPath.toFile())) {
      while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
        if ("sdkVersion".equals(jsonParser.getCurrentName())) {
          jsonParser.nextToken();
          sdkVersionOnFieldsJson =
              StringUtils.removeStart(jsonParser.getValueAsString(), "eforms-sdk-");
          break;
        }
      }
    }

    Assertions.assertEquals(sdkBaseVersion, sdkVersionOnFieldsJson);
  }

  @Test
  void testDownloadSdkMinor() throws IOException {
    SdkDownloader.downloadSdk("0.6", SDK_ROOT_DIR);
    checkVersions("0.6.0", "0.6.2", "0.6");

    SdkDownloader.downloadSdk("0.7", SDK_ROOT_DIR);
    checkVersions("0.7.0", "0.7.3", "0.7");

    SdkDownloader.downloadSdk("1.0", SDK_ROOT_DIR);
    checkVersions("1.0.0", "1.0.2", "1.0");

    SdkDownloader.downloadSdk("1.1", SDK_ROOT_DIR, false);
    checkVersions("1.1.0", "1.1.3", "1.1");
    
    SdkDownloader.downloadSdk("1.1", SDK_ROOT_DIR, true);
    checkVersions("1.1.4", "1.1.4-SNAPSHOT", "1.1");
    
    SdkDownloader.downloadSdk("2.0", SDK_ROOT_DIR, false);
    checkVersions("2.0.0", "2.0.0-rc.1", "2.0");
  }

  @Test
  void testDownloadSdkPatch() throws IOException {
    SdkDownloader.downloadSdk("0.6.2", SDK_ROOT_DIR);
    checkVersions("0.6.0", "0.6.2", "0.6.2");

    SdkDownloader.downloadSdk("0.7.3", SDK_ROOT_DIR);
    checkVersions("0.7.0", "0.7.3", "0.7.3");

    SdkDownloader.downloadSdk("1.0.2", SDK_ROOT_DIR);
    checkVersions("1.0.0", "1.0.2", "1.0.2");

    SdkDownloader.downloadSdk("1.1.3", SDK_ROOT_DIR);
    checkVersions("1.1.0", "1.1.3", "1.1.3");
  }
}
