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
import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class IdResponseFilter implements ContainerResponseFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdResponseFilter.class);
  private static final String REQUEST_ID = "Request-Id";

  @Override
  public void filter(final ContainerRequestContext request, final ContainerResponseContext response)
      throws IOException {
    final UUID id = UUID.randomUUID();
    LOGGER.info("method={} path={} request_id={} status={} bytes={}", request.getMethod(), request
        .getUriInfo().getPath(), id, response.getStatus(), response.getLength());
    response.getHeaders().add(REQUEST_ID, id);
  }
}
