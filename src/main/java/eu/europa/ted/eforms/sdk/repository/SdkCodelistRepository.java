package eu.europa.ted.eforms.sdk.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import com.helger.genericode.v10.Row;
import com.helger.genericode.v10.SimpleCodeList;
import com.helger.genericode.v10.Value;
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

  public SdkCodelistRepository(@Nonnull final String sdkVersion, final Path codelistsDir) {
    this.sdkVersion = Validate.notBlank(sdkVersion, "Undefined SDK version");
    this.codelistsDir = Validate.notNull(codelistsDir, "Undefined codelists directory");

    Validate.isTrue(Files.isDirectory(codelistsDir),
        "Codelists directory [%s] is not found or not a directory", codelistsDir);

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
  @Nullable
  @Override
  public final SdkCodelist get(final Object codelistId) {
    if (codelistId == null) {
      return null;
    }

    return computeIfAbsent((String) codelistId, (String key) -> {
      try {
        return loadSdkCodelist(key).orElse(null);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Nullable
  @Override
  public SdkCodelist getOrDefault(final Object codelistId, final SdkCodelist defaultValue) {
    return Optional.ofNullable(get(codelistId)).orElse(defaultValue);
  }

  private Optional<SdkCodelist> loadSdkCodelist(@Nullable final String codeListId)
      throws InstantiationException {
    logger.debug("Loading SDK codelist with ID [{}] for SDK version [{}] from path [{}]",
        codeListId, sdkVersion, codelistsDir);

    // Find the SDK codelist .gc file that corresponds to the passed reference.
    // Stream the data from that file.
    final Path filepath = codelistFilesByCodelistId.get(codeListId);
    assert filepath != null : "filepath is null";

    try (InputStream is = Files.newInputStream(codelistsDir.resolve(filepath))) {
      final Optional<CodeListDocument> codelist = Optional.ofNullable(marshaller.read(is));

      if (codelist.isEmpty()) {
        return Optional.empty();
      }

      // Get all the code values in a list.
      // We assume there are no duplicate code values in the referenced
      // codelists.
      final List<String> codes = codelist
          .map(CodeListDocument::getSimpleCodeList)
          .map(SimpleCodeList::getRow)
          .map((List<Row> rows) -> rows.stream()
              .map(Row::getValue)
              .map((List<Value> rowValues) -> rowValues.stream()
                  .filter((Value rowValue) -> GenericodeTools.KEY_CODE
                      .equals(GenericodeTools.extractColRefId(rowValue)))
                  .findFirst()
                  .map((Value rowValue) -> StringUtils.strip(rowValue.getSimpleValueValue()))
                  .orElse(null))
              .filter(StringUtils::isNotBlank)
              .collect(Collectors.toList()))
          .orElseGet(ArrayList<String>::new);

      final Optional<Identification> identification =
          Optional.ofNullable(codelist.get().getIdentification());

      // Version tag of the genericode (gc) file.
      final Optional<String> codelistVersion = identification.map(Identification::getVersion);

      final Optional<String> parentId = extractParentId(identification);

      Optional<SdkCodelist> result = Optional.of(SdkEntityFactory.getSdkCodelist(sdkVersion,
          codeListId, codelistVersion.orElse(null), codes, parentId));

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
  public static final Optional<String> extractParentId(final Optional<Identification> identity) {
    return extractLongNameWithIdentifier(identity, "eFormsParentId");
  }

  /**
   * @return The extracted value as a stripped string if present, optional empty otherwise.
   */
  public static Optional<String> extractLongNameWithIdentifier(
      final Optional<Identification> identity, final String identifier) {
    return identity
        .map(Identification::getLongName)
        .map((List<LongName> longNames) -> longNames.stream()
            .filter((LongName longName) -> Objects.equals(longName.getIdentifier(), identifier))
            .findFirst()
            .map(LongName::getValue)
            .map(StringUtils::strip)
            .filter(StringUtils::isNotBlank))
        .orElse(Optional.empty());

  }

  private Map<String, Path> getCodelistPaths(final Path pathFolder) throws IOException {
    final int depth = 1; // Flat folder, not recursive for now.

    Validate.isTrue(Files.isDirectory(pathFolder),
        MessageFormat.format("Not a directory: {0}", pathFolder));

    try (Stream<Path> walk = Files.walk(pathFolder, depth)) {
      return walk
          .filter(this::isGenericodeFile)
          .map((@Nonnull Path path) -> {
            final CodeListDocument cl = marshaller.read(path);
            // We use the longName as a ID, PK in the the DB.
            // But for the filenames we do not always follow this convention.
            // So we need to map.
            final Optional<String> longName = Optional.ofNullable(cl)
                .map(CodeListDocument::getIdentification)
                .map((Identification i) -> i.getLongNameAtIndex(0))
                .map(LongName::getValue);

            return longName.isPresent() ? Pair.of(longName.get(), path) : null;
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
  }

  private boolean isGenericodeFile(@Nullable final Path path) {
    return path != null
        && Files.isRegularFile(path)
        && GenericodeTools.EXTENSION_DOT_GC
            .equals(MessageFormat.format(".{0}", FilenameUtils.getExtension(path.toString())));
  }
}
