/**
 * Copyright 2015 Smoke Turner, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.smoketurner.notification.application.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@Provider
public class CharsetResponseFilter implements ContainerResponseFilter {

  @Override
  public void filter(final ContainerRequestContext request, final ContainerResponseContext response)
      throws IOException {
    final MediaType type = response.getMediaType();
    if (type != null) {
      if (!type.getParameters().containsKey(MediaType.CHARSET_PARAMETER)) {
        final MediaType typeWithCharset =
            type.withCharset(StandardCharsets.UTF_8.displayName(Locale.ENGLISH));
        response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, typeWithCharset);
      }
    }
  }
}
