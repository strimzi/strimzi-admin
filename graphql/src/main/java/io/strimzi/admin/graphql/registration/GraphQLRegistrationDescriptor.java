package io.strimzi.admin.graphql.registration;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;

public class GraphQLRegistrationDescriptor {
    private final TypeDefinitionRegistry typeDefinitionRegistry;
    private final RuntimeWiring runtimeWiring;

    private GraphQLRegistrationDescriptor(final TypeDefinitionRegistry typeDefinitionRegistry, final RuntimeWiring runtimeWiring) {
        this.typeDefinitionRegistry = typeDefinitionRegistry;
        this.runtimeWiring = runtimeWiring;
    }

    public static GraphQLRegistrationDescriptor create(final TypeDefinitionRegistry typeDefinitionRegistry, final RuntimeWiring runtimeWiring) {
        return new GraphQLRegistrationDescriptor(typeDefinitionRegistry, runtimeWiring);
    }

    public TypeDefinitionRegistry getTypeDefinitionRegistry() {
        return typeDefinitionRegistry;
    }

    public RuntimeWiring getRuntimeWiring() {
        return runtimeWiring;
    }
}
