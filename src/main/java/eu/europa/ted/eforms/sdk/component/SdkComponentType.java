package eu.europa.ted.eforms.sdk.component;

/**
 * Enumeration of component types that can be registered with the SDK component
 * factory.
 */
public enum SdkComponentType {
  FIELD, NODE, CODELIST, NOTICE_TYPE, EFX_EXPRESSION_TRANSLATOR, EFX_TEMPLATE_TRANSLATOR, EFX_RULES_TRANSLATOR,
  EFX_COMPUTE_DEPENDENCY_EXTRACTOR, EFX_VALIDATION_DEPENDENCY_EXTRACTOR,
  SYMBOL_RESOLVER, SCRIPT_GENERATOR, MARKUP_GENERATOR, VALIDATOR_GENERATOR;
}
