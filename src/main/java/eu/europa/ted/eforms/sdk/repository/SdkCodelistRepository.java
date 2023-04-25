package eu.europa.ted.eforms.sdk.repository;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private final Map<String, CodeListDocument> codelistContentsByCodelistId;

  @SuppressWarnings("unused")
  private SdkCodelistRepository() {
    throw new UnsupportedOperationException();
  }

  public SdkCodelistRepository(@Nonnull final String sdkVersion, final Path codelistsDir) {
    this.sdkVersion = Validate.notBlank(sdkVersion, "Undefined SDK version");
    this.codelistsDir = Validate.notNull(codelistsDir, "Undefined codelists directory");

    Validate.isTrue(Files.isDirectory(codelistsDir),
        "Codelists directory [%s] is not found or not a directory", codelistsDir);

    try {
      this.codelistContentsByCodelistId = getCodelistContentsByCodelistIds(codelistsDir);
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
    final Optional<CodeListDocument> codelist =
        Optional.ofNullable(codelistContentsByCodelistId.get(codeListId));

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

    final Optional<SdkCodelist> result = Optional.of(SdkEntityFactory.getSdkCodelist(sdkVersion,
        codeListId, codelistVersion.orElse(null), codes, parentId));

    logger.debug("Finished loading SDK codelist with ID [{}] for SDK version [{}] from path [{}]",
        codeListId, sdkVersion, codelistsDir);

    return result;
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

  /**
   * Loads all of the codelists by reading codelist files under a given a folder.
   *
   * @param codelistsDir The folder containing all codelist files
   * @return A map of codelist IDs to codelist contents
   * @throws IOException If there are failures when discovering and parsing the files
   */
  private Map<String, CodeListDocument> getCodelistContentsByCodelistIds(final Path codelistsDir)
      throws IOException {
    Validate.notNull(codelistsDir, "Undefined codelists directory");
    Validate.isTrue(Files.isDirectory(codelistsDir),
        MessageFormat.format("Not a directory: {0}", codelistsDir));

    logger.debug("Getting codelist file paths from directory [{}]", codelistsDir);

    final int depth = 1; // Flat folder, not recursive for now.

    try (Stream<Path> walk = Files.walk(codelistsDir, depth)) {
      return walk
          .filter(this::isGenericodeFile)
          .map((@Nonnull Path path) -> {
            try {
              return getCodelistIdAndContents(path);
            } catch (FileNotFoundException e) {
              throw new RuntimeException(e);
            }
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
  }

  /**
   * Gets the codelist ID from a codelist file.
   *
   * @param codelistPath The codelist file's path
   * @return The codelist ID and the codelist file's contents as a key/value pair
   * @throws FileNotFoundException If the codelist file's path is undefined or not an existing file
   */
  @Nullable
  private Pair<String, CodeListDocument> getCodelistIdAndContents(@Nonnull Path codelistPath)
      throws FileNotFoundException {
    Validate.notNull(codelistPath, "Undefined codelist path");

    if (!Files.isRegularFile(codelistPath)) {
      throw new FileNotFoundException(codelistPath.toString());
    }

    final Optional<CodeListDocument> codelist =
        Optional.ofNullable(GenericodeTools.getMarshaller().read(codelistPath));

    if (codelist.isPresent()) {
      // We use the longName as a ID, PK in the the DB.
      // But for the filenames we do not always follow this convention.
      // So we need to map.
      return codelist
          .map(CodeListDocument::getIdentification)
          .map((Identification identification) -> identification.getLongNameAtIndex(0))
          .map(LongName::getValue)
          .map((String longName) -> Pair.of(longName, codelist.get()))
          .orElse(null);
    }

    return null;
  }

  private boolean isGenericodeFile(@Nullable final Path path) {
    return path != null
        && Files.isRegularFile(path)
        && GenericodeTools.EXTENSION_DOT_GC
            .equals(MessageFormat.format(".{0}", FilenameUtils.getExtension(path.toString())));
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}
