package io.strimzi.admin.graphql.registration;

import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;

/**
 * Contains a schema definition and runtime implementation of the schema which can be merged with
 * each other to form a GraphQL executable schema.
 */
public class GraphQLRegistrationDescriptor {
    private final TypeDefinitionRegistry typeDefinitionRegistry;
    private final RuntimeWiring runtimeWiring;

    private GraphQLRegistrationDescriptor(final TypeDefinitionRegistry typeDefinitionRegistry, final RuntimeWiring runtimeWiring) {
        this.typeDefinitionRegistry = typeDefinitionRegistry;
        this.runtimeWiring = runtimeWiring;
    }

    /**
     * Factory class to create a GraphQLRegistrationDescriptor
     * @param typeDefinitionRegistry a GraphQL schema definition associated with the runtimeWiring
     * @param runtimeWiring a GraphQL runtime implementation associated with the schema definition
     * @return a GraphQLRegistrationDescriptor containing the schema definition and the runtime implementation
     */
    public static GraphQLRegistrationDescriptor create(final TypeDefinitionRegistry typeDefinitionRegistry, final RuntimeWiring runtimeWiring) {
        return new GraphQLRegistrationDescriptor(typeDefinitionRegistry, runtimeWiring);
    }

    /**
     * Retrieve the schema definition
     * @return a GraphQL schema definition
     */
    public TypeDefinitionRegistry getTypeDefinitionRegistry() {
        return typeDefinitionRegistry;
    }

    /**
     * Retrieve a runtime implementation associated with a schema
     * @return a GraphQL runtime implementation
     */
    public RuntimeWiring getRuntimeWiring() {
        return runtimeWiring;
    }
}
