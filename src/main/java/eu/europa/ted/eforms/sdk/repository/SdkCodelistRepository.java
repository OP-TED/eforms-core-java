package eu.europa.ted.eforms.sdk.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.helger.genericode.Genericode10CodeListMarshaller;
import com.helger.genericode.v10.CodeListDocument;
import com.helger.genericode.v10.Identification;
import com.helger.genericode.v10.LongName;
import eu.europa.ted.eforms.sdk.entity.SdkCodelist;
import eu.europa.ted.eforms.sdk.entity.SdkEntityFactory;
import eu.europa.ted.util.GenericodeTools;

public class SdkCodelistRepository extends HashMap<String, SdkCodelist> {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(SdkCodelistRepository.class);

  private transient Path codelistsDir;
  private String sdkVersion;

  private final Map<String, Path> codelistFilesByCodelistId;
  private final Genericode10CodeListMarshaller marshaller;

  @SuppressWarnings("unused")
  private SdkCodelistRepository() {
    throw new UnsupportedOperationException();
  }

  public SdkCodelistRepository(String sdkVersion, Path codelistsDir) {
    this.sdkVersion = sdkVersion;
    this.codelistsDir = codelistsDir;

    marshaller = GenericodeTools.getMarshaller();

    try {
      this.codelistFilesByCodelistId = getCodelistPaths(codelistsDir);
    } catch (IOException e) {
      throw new RuntimeException(
          MessageFormat.format("Failed to load codelists from [{0}]", codelistsDir), e);
    }
  }

  /**
   * Builds EFX list from the passed codelist reference. This will lazily compute and cache the
   * result for reuse as the operation can be costly on some large lists.
   *
   * @param codelistId A reference to an SDK codelist.
   * @return The EFX string representation of the list of all the codes of the referenced codelist.
   */
  @Override
  public final SdkCodelist get(final Object codelistId) {
    if (codelistId == null || StringUtils.isBlank((String) codelistId)) {
      throw new RuntimeException("codelistId is null or blank.");
    }

    return computeIfAbsent((String) codelistId, key -> {
      try {
        return loadSdkCodelist(sdkVersion, key);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public SdkCodelist getOrDefault(final Object codelistId, final SdkCodelist defaultValue) {
    SdkCodelist result = get(codelistId);
    return result != null ? result : defaultValue;
  }

  private SdkCodelist loadSdkCodelist(final String sdkVersion, final String codeListId)
      throws InstantiationException {
    logger.debug("Loading SDK codelist with ID [{}] for SDK version [{}] from path [{}]",
        codeListId, sdkVersion, codelistsDir);

    // Find the SDK codelist .gc file that corresponds to the passed reference.
    // Stream the data from that file.
    final Genericode10CodeListMarshaller marshaller = GenericodeTools.getMarshaller();

    final Path filepath = codelistFilesByCodelistId.get(codeListId);
    assert filepath != null : "filepath is null";

    try (InputStream is = Files.newInputStream(codelistsDir.resolve(filepath))) {
      final CodeListDocument codelist = marshaller.read(is);

      // Get all the code values in a list.
      // We assume there are no duplicate code values in the referenced
      // codelists.
      final List<String> codes = codelist
          .getSimpleCodeList()
          .getRow().stream()
          .map(row -> row.getValue().stream()
              .filter(v -> GenericodeTools.KEY_CODE.equals(GenericodeTools.extractColRefId(v)))
              .findFirst()
              .orElseThrow(RuntimeException::new)
              .getSimpleValue()
              .getValue()
              .strip())
          .collect(Collectors.toList());

      // Version tag of the genericode (gc) file.
      final String codelistVersion = codelist.getIdentification().getVersion();

      final Optional<String> parentId = extractParentId(codelist.getIdentification());

      SdkCodelist result =
          SdkEntityFactory.getSdkCodelist(sdkVersion, codeListId, codelistVersion, codes, parentId);

      logger.debug("Finished loading SDK codelist with ID [{}] for SDK version [{}] from path [{}]",
          codeListId, sdkVersion, codelistsDir);

      return result;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get eForms parent id.
   */
  public static final Optional<String> extractParentId(final Identification ident) {
    return extractLongNameWithIdentifier(ident, "eFormsParentId");
  }

  /**
   * @return The extracted value as a stripped string if present, optional empty otherwise.
   */
  public static Optional<String> extractLongNameWithIdentifier(final Identification identity,
      final String identifierStr) {
    final Optional<LongName> valueOpt = identity.getLongName().stream()
        .filter(item -> Objects.equals(item.getIdentifier(), identifierStr)).findFirst();

    if (valueOpt.isPresent()) {
      final String parentId = valueOpt.get().getValue();
      return StringUtils.isBlank(parentId) ? Optional.empty() : Optional.of(parentId.strip());
    }

    return Optional.empty();
  }

  private Map<String, Path> getCodelistPaths(final Path pathFolder) throws IOException {
    final int depth = 1; // Flat folder, not recursive for now.

    Validate.isTrue(Files.isDirectory(pathFolder),
        MessageFormat.format("Not a directory: {0}", pathFolder));

    try (Stream<Path> walk = Files.walk(pathFolder, depth)) {
      return walk
          .filter(this::isGenericodeFile)
          .map((Path path) -> {
            final CodeListDocument cl = marshaller.read(path);
            // We use the longName as a ID, PK in the the DB.
            // But for the filenames we do not always follow this convention.
            // So we need to map.
            final String longName = cl.getIdentification().getLongNameAtIndex(0).getValue();

            return Pair.of(longName, path);
          })
          .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
  }

  private boolean isGenericodeFile(final Path path) {
    return path != null
        && Files.isRegularFile(path)
        && GenericodeTools.EXTENSION_DOT_GC
            .equals(MessageFormat.format(".{0}", FilenameUtils.getExtension(path.toString())));
  }
}
