package eu.europa.ted.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import eu.europa.ted.eforms.sdk.SdkConstants;

public class ResourceLoader {
  private String root = SdkConstants.DEFAULT_SDK_ROOT;

  public static final ResourceLoader INSTANCE = new ResourceLoader();

  private ResourceLoader() {}

  public ResourceLoader setRoot(String root) {
    Optional.ofNullable(root).ifPresent((String s) -> this.root = s);

    return this;
  }

  public String getRoot() {
    return root;
  }

  public Path getResourceAsPath(final PathResource resource, final String sdkVersion) {
    return getResourceAsPath(resource, sdkVersion, null);
  }

  public Path getResourceAsPath(final PathResource resource, String sdkVersion, String filename) {
    Validate.notEmpty(sdkVersion, "Undefined SDK resources version");

    sdkVersion = Optional.ofNullable(sdkVersion).orElse(StringUtils.EMPTY);

    final String resourcePath = Optional.ofNullable(resource).map(PathResource::getPath)
        .orElse(Path.of(StringUtils.EMPTY)).toString();
    filename = Optional.ofNullable(filename).orElse(StringUtils.EMPTY);

    Path result = Path.of(root, sdkVersion, resourcePath, filename).toAbsolutePath();

    Validate.isTrue(Files.exists(result),
        MessageFormat.format("Resource [{0}] does not exist", result));

    return result;
  }

  public InputStream getResourceAsStream(final PathResource resource, String sdkVersion,
      final String filename) throws IOException {
    return Files.newInputStream(getResourceAsPath(resource, sdkVersion, filename));
  }
}
