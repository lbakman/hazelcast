/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.client.impl.client;

import com.hazelcast.client.ClientEndpoint;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.spi.InternalCompletableFuture;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.Operation;

public abstract class TargetClientRequest extends ClientRequest implements ExecutionCallback {

    private static final int TRY_COUNT = 100;

    @Override
    public final void process() throws Exception {
        final ClientEndpoint endpoint = getEndpoint();
        Operation op = prepareOperation();
        op.setCallerUuid(endpoint.getUuid());

        InvocationBuilder builder = getInvocationBuilder(op)
                .setTryCount(TRY_COUNT)
                .setResultDeserialized(false);
        InternalCompletableFuture f = builder.invoke();
        f.andThen(this);
    }

    protected abstract InvocationBuilder getInvocationBuilder(Operation op);

    protected abstract Operation prepareOperation();

    @Override
    public final void onResponse(Object response) {
        endpoint.sendResponse(filter(response), getCallId());
    }

    @Override
    public final void onFailure(Throwable t) {
        onResponse(t);
    }

    protected Object filter(Object response) {
        return response;
    }
}
