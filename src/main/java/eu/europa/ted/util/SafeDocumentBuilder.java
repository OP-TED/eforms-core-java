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
package eu.europa.ted.util;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for the creation of {@link DocumentBuilder} instances for XML parsing, using XXE
 * prevention techniques as recommended by OWASP.
 *
 * @see <a href=
 *      "https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java">OWASP
 *      XXE Prevention Cheat Sheet</a>
 */
public class SafeDocumentBuilder {

  private static final Logger logger = LoggerFactory.getLogger(SafeDocumentBuilder.class);

  private SafeDocumentBuilder() {
    throw new AssertionError("Utility class.");
  }

  /**
   * Creates a {@link DocumentBuilder} using XXE prevention techniques. Allows DOCTYPE declarations.
   *
   * @return A {@link DocumentBuilder} instance
   * @throws ParserConfigurationException when the builder is configured with a feature that is
   *         unsupported by the XML processor
   */
  public static DocumentBuilder buildSafeDocumentBuilderAllowDoctype()
      throws ParserConfigurationException {
    return buildSafeDocumentBuilder(false);
  }

  /**
   * Creates a {@link DocumentBuilder} using XXE prevention techniques. Raises a fatal error when a
   * DOCTYPE declaration is found.
   *
   * @return A {@link DocumentBuilder} instance
   * @throws ParserConfigurationException when the builder is configured with a feature that is
   *         unsupported by the XML processor
   */
  public static DocumentBuilder buildSafeDocumentBuilderStrict()
      throws ParserConfigurationException {
    return buildSafeDocumentBuilder(true);
  }

  private static DocumentBuilder buildSafeDocumentBuilder(final boolean disallowDoctypeDecl)
      throws ParserConfigurationException {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
    String feature = null;
    try {
      feature = "http://apache.org/xml/features/disallow-doctype-decl";
      dbf.setFeature(feature, disallowDoctypeDecl);

      feature = "http://xml.org/sax/features/external-general-entities";
      dbf.setFeature(feature, false);

      feature = "http://xml.org/sax/features/external-parameter-entities";
      dbf.setFeature(feature, false);

      feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
      dbf.setFeature(feature, false);

      dbf.setXIncludeAware(false);
      dbf.setExpandEntityReferences(false);
      dbf.setValidating(false);
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);

      return dbf.newDocumentBuilder();
    } catch (final ParserConfigurationException e) {
      logger.info("Error: The feature '{}' is probably not supported by your XML processor.",
          feature);
      logger.debug("ParserConfigurationException was thrown:", e);
      throw e;
    }
  }
}
