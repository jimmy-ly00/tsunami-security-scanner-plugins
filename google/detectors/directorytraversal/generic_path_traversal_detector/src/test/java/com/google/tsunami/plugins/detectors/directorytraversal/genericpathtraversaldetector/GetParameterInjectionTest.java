/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.tsunami.plugins.detectors.directorytraversal.genericpathtraversaldetector;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.tsunami.common.net.http.HttpRequest;
import com.google.tsunami.proto.NetworkService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link GetParameterInjection}. */
@RunWith(JUnit4.class)
public final class GetParameterInjectionTest {
  private static final GetParameterInjection INJECTION_POINT = new GetParameterInjection();

  private static final HttpRequest REQUEST_WITHOUT_GET_PARAMETERS =
      HttpRequest.get("https://google.com").withEmptyHeaders().build();
  private static final HttpRequest REQUEST_WITH_GET_PARAMETERS =
      HttpRequest.get("https://google.com?key=value&other=test").withEmptyHeaders().build();
  private static final NetworkService MINIMAL_NETWORK_SERVICE =
      NetworkService.newBuilder().setServiceName("http").build();
  private static final String PAYLOAD = "../../../../etc/passwd";

  @Test
  public void injectPayload_onRelativePathTraversalPayloadWithGetParameters_generatesExploits() {
    ImmutableSet<PotentialExploit> exploitsWithPayloadInGetParameters =
        ImmutableSet.of(
            PotentialExploit.create(
                MINIMAL_NETWORK_SERVICE,
                HttpRequest.get("https://google.com?key=../../../../etc/passwd&other=test")
                    .withEmptyHeaders()
                    .build(),
                PAYLOAD,
                PotentialExploit.Priority.LOW),
            PotentialExploit.create(
                MINIMAL_NETWORK_SERVICE,
                HttpRequest.get("https://google.com?key=value&other=../../../../etc/passwd")
                    .withEmptyHeaders()
                    .build(),
                PAYLOAD,
                PotentialExploit.Priority.LOW));

    assertThat(
            INJECTION_POINT.injectPayload(
                MINIMAL_NETWORK_SERVICE, REQUEST_WITH_GET_PARAMETERS, PAYLOAD))
        .containsAtLeastElementsIn(exploitsWithPayloadInGetParameters);
  }

  @Test
  public void
      injectPayload_onRelativePathTraversalPayloadWithoutGetParameters_generatesNoExploits() {
    assertThat(
            INJECTION_POINT.injectPayload(
                MINIMAL_NETWORK_SERVICE, REQUEST_WITHOUT_GET_PARAMETERS, PAYLOAD))
        .isEmpty();
  }
}