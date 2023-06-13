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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helger.genericode.v10.CodeListDocument;
import com.helger.genericode.v10.Identification;
import com.helger.genericode.v10.LongName;
import com.helger.genericode.v10.Row;
import com.helger.genericode.v10.SimpleCodeList;
import com.helger.genericode.v10.Value;
import eu.europa.ted.eforms.sdk.domain.codelist.CodelistForIndex;
import eu.europa.ted.eforms.sdk.domain.codelist.CodelistsIndex;
import eu.europa.ted.eforms.sdk.entity.SdkCodelist;
import eu.europa.ted.eforms.sdk.entity.SdkEntityFactory;
import eu.europa.ted.util.GenericodeTools;

public class SdkCodelistRepository extends HashMap<String, SdkCodelist> {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(SdkCodelistRepository.class);

  private transient Path codelistsDir;
  private String sdkVersion;

  private final Map<String, Pair<Path, CodeListDocument>> codelistInfoByCodelistIds;

  @SuppressWarnings("unused")
  private SdkCodelistRepository() {
    throw new UnsupportedOperationException();
  }

  public SdkCodelistRepository(final String sdkVersion, final Path codelistsDir) {
    this.sdkVersion = Validate.notBlank(sdkVersion, "Undefined SDK version");
    this.codelistsDir = Validate.notNull(codelistsDir, "Undefined codelists directory");

    Validate.isTrue(Files.isDirectory(codelistsDir),
        "Codelists directory [%s] is not found or not a directory", codelistsDir);

    try {
      this.codelistInfoByCodelistIds = getCodelistInfoByCodelistIds(codelistsDir);
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
    if (codelistId == null) {
      return null;
    }

    return computeIfAbsent((String) codelistId,
        Unchecked.function((String key) -> loadSdkCodelist(key).orElse(null)));
  }

  @Override
  public SdkCodelist getOrDefault(final Object codelistId, final SdkCodelist defaultValue) {
    return Optional.ofNullable(get(codelistId)).orElse(defaultValue);
  }

  private Optional<SdkCodelist> loadSdkCodelist(final String codeListId)
      throws InstantiationException {
    logger.debug("Loading SDK codelist with ID [{}] for SDK version [{}]", codeListId, sdkVersion);

    // Find the SDK codelist .gc file that corresponds to the passed reference.
    // Stream the data from that file.
    final Optional<CodeListDocument> codelist =
        Optional.ofNullable(codelistInfoByCodelistIds.get(codeListId))
            .map(Unchecked.function((Pair<Path, CodeListDocument> codelistInfo) -> Optional
                .ofNullable(codelistInfo.getValue())
                .orElse(getCodelistContents(codelistInfo.getKey()))));

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

    logger.debug("Finished loading SDK codelist with ID [{}] for SDK version [{}]", codeListId,
        sdkVersion);

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
   * Loads the paths of all of the codelists by looking for and reading the codelists index.
   * <p>
   * The result is a map which associates information (file path and a placeholder for contents) for
   * each codelist with its ID.
   *
   * @param codelistsDir The folder containing the codelists index and files
   * @return A map of codelist IDs to pairs of codelist file paths and contents
   * @throws IOException If there are failures when discovering and parsing the files
   */
  private Map<String, Pair<Path, CodeListDocument>> getCodelistInfoByCodelistIds(
      final Path codelistsDir) throws IOException {
    Validate.notNull(codelistsDir, "Undefined codelists directory");
    Validate.isTrue(Files.isDirectory(codelistsDir),
        MessageFormat.format("Not a directory: {0}", codelistsDir));

    final Path indexFile = Path.of(codelistsDir.toString(), "codelists.json");

    logger.debug("Loading codelists index from [{}]", indexFile);
    CodelistsIndex codelistsIndex =
        createObjectMapper().readValue(indexFile.toFile(), CodelistsIndex.class);

    final Map<String, Pair<Path, CodeListDocument>> result = new HashMap<>();
    codelistsIndex.getCodelists().stream().forEach((CodelistForIndex codelist) -> {
      final String codelistId = codelist.getId();
      final Path codelistPath = Path.of(codelistsDir.toString(), codelist.getFilename());

      logger.trace("Adding path [{}] for codelist [{}]", codelistPath, codelistId);
      if (!Files.isRegularFile(codelistPath)) {
        logger.warn("Codelist file [{}] not found. Codelist [{}] will be skipped", codelistPath,
            codelistId);
      } else {
        // We're only interested in populating the codelist filepaths for now.
        // The contents of each document will be populated for each codelist later, on demand.
        result.put(codelistId, Pair.of(codelistPath, null));
      }
    });

    return result;

  }

  /**
   * Gets the codelist contents from a codelist file.
   *
   * @param codelistPath The codelist file's path
   * @return The codelist file's contents
   * @throws FileNotFoundException If the codelist file's path is undefined or not an existing file
   */
  private CodeListDocument getCodelistContents(Path codelistPath) throws FileNotFoundException {
    Validate.notNull(codelistPath, "Undefined codelist path");

    if (!Files.isRegularFile(codelistPath)) {
      throw new FileNotFoundException(codelistPath.toString());
    }

    logger.debug("Reading from file [{}]", codelistPath);
    return Optional.ofNullable(GenericodeTools.getMarshaller().read(codelistPath)).orElse(null);
  }

  private ObjectMapper createObjectMapper() {
    return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
