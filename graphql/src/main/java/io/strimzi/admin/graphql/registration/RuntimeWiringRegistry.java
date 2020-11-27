package io.strimzi.admin.graphql.registration;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;
import java.util.ArrayList;
import java.util.List;

public class RuntimeWiringRegistry {
    private final List<RuntimeWiring> wirings = new ArrayList<>();

    public void add(final RuntimeWiring runtimeWiring) {
        wirings.add(runtimeWiring);
    }

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
