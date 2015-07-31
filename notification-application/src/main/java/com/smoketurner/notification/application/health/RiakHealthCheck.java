/**
 * Copyright 2015 Smoke Turner, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smoketurner.notification.application.health;

import java.util.List;
import javax.annotation.Nonnull;
import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakNode;
import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Preconditions;

public class RiakHealthCheck extends HealthCheck {

    private final RiakClient client;

    /**
     * Constructor
     *
     * @param client
     *            Riak client
     */
    public RiakHealthCheck(@Nonnull final RiakClient client) {
        this.client = Preconditions.checkNotNull(client);
    }

    @Override
    protected Result check() throws Exception {
        final List<RiakNode> nodes = client.getRiakCluster().getNodes();
        if (nodes.size() > 0) {
            return Result.healthy();
        }
        return Result.unhealthy("No available Riak nodes");
    }
}
