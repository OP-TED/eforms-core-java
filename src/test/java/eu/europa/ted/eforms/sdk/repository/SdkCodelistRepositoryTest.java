package eu.europa.ted.eforms.sdk.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.helger.genericode.v10.Identification;
import com.helger.genericode.v10.LongName;
import eu.europa.ted.eforms.sdk.entity.SdkCodelist;

class SdkCodelistRepositoryTest {
  private SdkCodelistRepository repository;
  private Identification identification;

  @BeforeEach
  public void setUp() {
    repository =
        new SdkCodelistRepository("999.0", Path.of("src", "test", "resources", "codelists", "/"));

    identification = new Identification();

    final LongName longName1 = new LongName("test-codelist-parentId");
    longName1.setIdentifier("eFormsParentId");
    final LongName longName2 = new LongName("test-codelist-dummy");
    longName2.setIdentifier("dummy");

    identification.setLongName(Arrays.asList(longName1, longName2));
  }

  @Test
  void testGetObject() {
    Optional<SdkCodelist> codelist = Optional.ofNullable(repository.get("test-codelist"));

    assertEquals("test-codelist", codelist.map(SdkCodelist::getCodelistId).orElse(null));
    assertEquals(Arrays.asList("inc"), codelist.map(SdkCodelist::getCodes).orElse(null));
  }

  @Test
  void testGetOrDefaultObjectSdkCodelist() {
    SdkCodelist defaultCodelist =
        new DummySdkCodelist("default-codelist", "1", Arrays.asList("code1", "code2"), null);

    Optional<SdkCodelist> codelist =
        Optional.ofNullable(repository.getOrDefault("test-codelist", defaultCodelist));
    assertEquals("test-codelist", codelist.map(SdkCodelist::getCodelistId).orElse(null));

    codelist =
        Optional.ofNullable(repository.getOrDefault("nonexisting-codelist", defaultCodelist));
    assertEquals("default-codelist", codelist.map(SdkCodelist::getCodelistId).orElse(null));
  }

  @Test
  void testExtractParentId() {
    assertEquals("test-codelist-parentId",
        SdkCodelistRepository.extractParentId(Optional.of(identification)).get());
  }

  @Test
  void testExtractLongNameWithIdentifier() {
    assertEquals("test-codelist-dummy", SdkCodelistRepository
        .extractLongNameWithIdentifier(Optional.of(identification), "dummy").get());
  }
}
