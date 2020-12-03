/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.admin.graphql.registration;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;
import java.util.ArrayList;
import java.util.List;

/**
 * The runtime of a graphql schema is implemented in a set of data fetchers, type resolvers and
 * custom scalars. This class allows different sets to be added as individual sets and then returned
 * as a single consolidated set which is created by merging the fetchers, resolvers and scalars.
 */
public class RuntimeWiringRegistry {
    private final List<RuntimeWiring> wirings = new ArrayList<>();

    /**
     * Add a schema implementation
     * @param runtimeWiring the schema implementation to add
     */
    public void add(final RuntimeWiring runtimeWiring) {
        wirings.add(runtimeWiring);
    }

    /**
     * Merges all the individual schema implementations.
     * @return a consolidated merged schema implementation.
     */
    public RuntimeWiring getRuntimeWiring() {
        final RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();

        wirings.forEach(wiring -> {
            wiring.getDirectiveWiring().forEach(runtimeWiring::directiveWiring);
            wiring.getRegisteredDirectiveWiring().forEach(runtimeWiring::directive);
            wiring.getScalars().forEach((k, v) -> runtimeWiring.scalar(v));
            wiring.getTypeResolvers().forEach((s, typeResolver) -> runtimeWiring.type(TypeRuntimeWiring.newTypeWiring(s).typeResolver(typeResolver)));
            wiring.getDataFetchers().forEach((s, dataFetcherMap) -> runtimeWiring.type(TypeRuntimeWiring.newTypeWiring(s).dataFetchers(dataFetcherMap)));
            wiring.getEnumValuesProviders().forEach((s, enumValueProvider) -> runtimeWiring.type(TypeRuntimeWiring.newTypeWiring(s).enumValues(enumValueProvider)));
        });

        return runtimeWiring.build();
    }
}
