package eu.europa.ted.eforms.sdk.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.europa.ted.eforms.sdk.SdkConstants;

public abstract class MapFromJson<T> extends HashMap<String, T> {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(MapFromJson.class);

  protected final String sdkVersion;

  @SuppressWarnings("unused")
  private MapFromJson() {
    throw new UnsupportedOperationException();
  }

  protected MapFromJson(final String sdkVersion, final Path jsonPath)
      throws InstantiationException {
    this.sdkVersion = sdkVersion;

    try {
      populateMap(jsonPath);
    } catch (IOException e) {
      throw new RuntimeException(MessageFormat
          .format("Failed to set resource filepath to [{0}]. Error was: {1}", jsonPath, e));
    }
  }

  protected MapFromJson(final String sdkVersion, final Path jsonPath, final Object... context)
      throws InstantiationException {
    this.sdkVersion = sdkVersion;

    try {
      populateMap(jsonPath, context);
    } catch (IOException e) {
      throw new RuntimeException(MessageFormat
          .format("Failed to set resource filepath to [{0}]. Error was: {1}", jsonPath, e));
    }
  }

  private final void populateMap(final Path jsonPath, final Object... context)
      throws IOException, InstantiationException {
    logger.debug("Populating maps for context, jsonPath={}", jsonPath);

    final ObjectMapper mapper = buildStandardJacksonObjectMapper();

    try (InputStream fieldsJsonInputStream = Files.newInputStream(jsonPath)) {
      if (fieldsJsonInputStream == null) {
        throw new RuntimeException(String.format("File not found: %s", jsonPath));
      }

      if (fieldsJsonInputStream.available() == 0) {
        throw new RuntimeException(String.format("File is empty: %s", jsonPath));
      }

      final JsonNode json = mapper.readTree(fieldsJsonInputStream);
      populateMap(json, context);
    }
  }

  /**
   * Abstract method for populating the map from JSON. Existing subclasses implement this.
   */
  protected abstract void populateMap(final JsonNode json) throws InstantiationException;

  /**
   * Context-aware population method. Default implementation delegates to the abstract method,
   * ignoring context. Subclasses that need context should override this method.
   */
  protected void populateMap(final JsonNode json, final Object... context)
      throws InstantiationException {
    populateMap(json);
  }

  /**
   * Reads the map of stable identifiers ({@link SdkConstants#STABLE_IDS_KEY}), which
   * fields/fwd/fields.json and fields/fwd/nodes.json of SDK 2 each start with.
   *
   * <p>
   * Applications use the stable identifier, such as "disclosureDate", instead of hard-coding the
   * identifier of the field or the node.
   * </p>
   *
   * @param json the root of a file that may start with a map of stable identifiers
   * @return the identifier of each field or node, by its stable identifier, in the order of the
   *         file, or an empty map for the SDK versions whose files carry no such map
   */
  protected static Map<String, String> stableIdsOf(final JsonNode json) {
    final JsonNode stableIds = json.get(SdkConstants.STABLE_IDS_KEY);
    if (stableIds == null || !stableIds.isObject()) {
      return Collections.emptyMap();
    }

    // Keep the order of the file, so that what is read back is what the SDK gives.
    final Map<String, String> idByStableId = new LinkedHashMap<>();
    final Iterator<String> names = stableIds.fieldNames();
    while (names.hasNext()) {
      final String stableId = names.next();
      if (stableIds.hasNonNull(stableId)) {
        idByStableId.put(stableId, stableIds.get(stableId).asText(null));
      }
    }
    return Collections.unmodifiableMap(idByStableId);
  }

  /**
   * @return A reusable Jackson object mapper instance.
   */
  private static ObjectMapper buildStandardJacksonObjectMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    // https://fasterxml.github.io/jackson-annotations/javadoc/2.7/com/fasterxml/jackson/annotation/JsonInclude.Include.html
    // Value that indicates that only properties with non-null values are to be
    // included.
    mapper.setSerializationInclusion(Include.NON_NULL);
    // Value that indicates that only properties with null value,
    // or what is considered empty, are not to be included.
    mapper.setSerializationInclusion(Include.NON_EMPTY);
    return mapper;
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
