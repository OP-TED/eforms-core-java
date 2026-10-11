/*
 * Copyright 2026 European Union
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
package eu.europa.ted.eforms.sdk.repository;

import java.nio.file.Path;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentFactory;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;

/**
 * Gives the repositories of an SDK version. Each SDK version keeps its metadata in its own files,
 * so each has its own repositories, registered with {@link SdkComponent} for its major version.
 */
public class SdkRepositoryFactory extends SdkComponentFactory {
  public static final SdkRepositoryFactory INSTANCE = new SdkRepositoryFactory();

  private SdkRepositoryFactory() {
    super();
  }

  /**
   * @param sdkVersion the version of the SDK
   * @param sdkPath the folder of the SDK
   * @return the nodes of that SDK, by identifier
   * @throws InstantiationException if the nodes cannot be read for this SDK version
   */
  public static SdkNodeRepository getSdkNodeRepository(final String sdkVersion,
      final Path sdkPath) throws InstantiationException {
    return SdkRepositoryFactory.INSTANCE.getComponentImpl(sdkVersion,
        SdkComponentType.NODE_REPOSITORY, SdkNodeRepository.class, sdkVersion, sdkPath);
  }

  /**
   * @param sdkVersion the version of the SDK
   * @param sdkPath the folder of the SDK
   * @param nodeRepository the nodes of the same SDK, to link each field to its parent node; may be
   *        null to leave the parent nodes unlinked
   * @return the fields of that SDK, by identifier
   * @throws InstantiationException if the fields cannot be read for this SDK version
   */
  public static SdkFieldRepository getSdkFieldRepository(final String sdkVersion,
      final Path sdkPath, final SdkNodeRepository nodeRepository) throws InstantiationException {
    return SdkRepositoryFactory.INSTANCE.getComponentImpl(sdkVersion,
        SdkComponentType.FIELD_REPOSITORY, SdkFieldRepository.class, sdkVersion, sdkPath,
        nodeRepository);
  }
}
