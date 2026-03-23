/*
 * Copyright 2022 European Union
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European
 * Commission – subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.europa.ted.eforms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import eu.europa.ted.util.SafeDocumentBuilder;

/**
 * A class representing a Notice document with accessor methods for its XML contents and metadata.
 */
public class NoticeDocument {

  private static final String TAG_PRIMARY_LANGUAGE = "cbc:NoticeLanguageCode";
  private static final String TAG_SDK_VERSION = "cbc:CustomizationID";
  private static final String TAG_SUBTYPE_CODE = "cbc:SubTypeCode";
  private static final String XPATH_ADDITIONAL_LANGUAGE =
      "/*/AdditionalNoticeLanguage/ID/text()";

  private static final XPath xpath = XPathFactory.newInstance().newXPath();

  private final Element root;
  private final String xmlContents;

  public NoticeDocument(final Path noticeXmlPath)
      throws ParserConfigurationException, SAXException, IOException {
    Validate.notNull(noticeXmlPath, "Undefined Notice XML file path");

    if (!Files.isRegularFile(noticeXmlPath)) {
      throw new FileNotFoundException(noticeXmlPath.toString());
    }

    this.xmlContents = Files.readString(noticeXmlPath, StandardCharsets.UTF_8);
    this.root = parseXmlRoot(this.xmlContents);
  }

  public NoticeDocument(final InputStream noticeXmlInput)
      throws ParserConfigurationException, SAXException, IOException {
    Validate.notNull(noticeXmlInput, "Undefined Notice XML input");

    this.xmlContents = new String(noticeXmlInput.readAllBytes(), StandardCharsets.UTF_8);
    this.root = parseXmlRoot(this.xmlContents);
  }

  public NoticeDocument(final String noticeXmlContents)
      throws ParserConfigurationException, SAXException, IOException {
    Validate.notBlank(noticeXmlContents, "Invalid Notice XML contents");

    this.xmlContents = noticeXmlContents;
    this.root = parseXmlRoot(this.xmlContents);
  }

  private static Element parseXmlRoot(final String xmlContents)
      throws ParserConfigurationException, SAXException, IOException {
    try (InputStream input =
        new java.io.ByteArrayInputStream(xmlContents.getBytes(StandardCharsets.UTF_8))) {
      final Element root =
          SafeDocumentBuilder.buildSafeDocumentBuilderAllowDoctype().parse(input)
              .getDocumentElement();
      Validate.notNull(root, "No XML root found");
      return root;
    }
  }

  /**
   * Gets the notice sub type from the notice XML.
   *
   * @return The notice sub type as found in the notice XML
   */
  public String getNoticeSubType() {
    return Optional.ofNullable(this.root.getElementsByTagName(TAG_SUBTYPE_CODE))
        .map((final NodeList subTypeCodes) -> {
          Optional<String> result = Optional.empty();
          for (int i = 0; i < subTypeCodes.getLength(); i++) {
            result = Optional.ofNullable(subTypeCodes.item(i))
                .filter((final Node node) -> node.getAttributes() != null)
                .map(Node::getTextContent)
                .map(StringUtils::strip);
          }
          return result.orElse(null);
        })
        .filter(StringUtils::isNotBlank)
        .orElseThrow(() -> new RuntimeException("SubTypeCode not found in notice XML"));
  }

  /**
   * Gets the eForms SDK version from the notice XML.
   *
   * @return The eForms SDK version as found in the notice XML
   */
  public String getEformsSdkVersion() {
    return Optional.ofNullable(this.root.getElementsByTagName(TAG_SDK_VERSION))
        .filter((final NodeList nodes) -> nodes.getLength() == 1)
        .map((final NodeList nodes) -> Optional.ofNullable(nodes.item(0))
            .map(Node::getTextContent)
            .map(StringUtils::strip)
            .map((final String str) -> str.startsWith("eforms-sdk-")
                ? str.substring("eforms-sdk-".length()) : str)
            .orElse(null))
        .filter(StringUtils::isNotBlank)
        .orElseThrow(() -> new RuntimeException("eForms SDK version not found in notice XML"));
  }

  /**
   * Gets the primary language from the notice XML.
   *
   * @return The primary language
   */
  public String getPrimaryLanguage() {
    return Optional
        .ofNullable(this.root.getElementsByTagName(TAG_PRIMARY_LANGUAGE))
        .map((final NodeList nodes) -> nodes.item(0))
        .map(Node::getTextContent)
        .orElse(null);
  }

  /**
   * Gets the list of other languages from the notice XML.
   *
   * @return A list of other languages
   * @throws XPathExpressionException If an error occurs evaluating the XPath expression
   */
  public List<String> getOtherLanguages() throws XPathExpressionException {
    return Optional
        .ofNullable(xpath.evaluateExpression(XPATH_ADDITIONAL_LANGUAGE,
            this.root.getOwnerDocument(), XPathNodes.class))
        .map((final XPathNodes nodes) -> {
          final List<String> languages = new ArrayList<>();
          nodes.forEach((final Node node) -> {
            if (StringUtils.isNotBlank(node.getTextContent())) {
              languages.add(node.getTextContent());
            }
          });
          return languages;
        })
        .orElseGet(ArrayList::new);
  }

  /**
   * Gets the notice XML contents.
   *
   * @return The notice XML
   */
  public String getXmlContents() {
    return this.xmlContents;
  }
}
