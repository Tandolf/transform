package se.andolf.transform.config;

import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.spring.web.reactive.ExecutionInputCustomizer;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import se.andolf.transform.FileUtils;
import se.andolf.transform.graphql.mutations.CategoryMutationDataFetcher;
import se.andolf.transform.graphql.mutations.ExerciseMutationDataFetcher;
import se.andolf.transform.graphql.queries.CategoryDataFetcher;
import se.andolf.transform.graphql.queries.ExerciseDataFetcher;
import se.andolf.transform.graphql.queries.UserDataFetcher;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Configuration
@EnableAspectJAutoProxy
@AllArgsConstructor
public class GraphQLConfig {

    private final UserDataFetcher userDataFetcher;
    private final ExerciseDataFetcher exerciseDataFetcher;
    private final CategoryDataFetcher categoryDataFetcher;
    private final ExerciseMutationDataFetcher exerciseMutationDataFetcher;
    private final CategoryMutationDataFetcher categoryMutationDataFetcher;

    @Bean
    public GraphQL graphQL() {
        final String content = FileUtils.readFile("schema.graphqls");
        final GraphQLSchema graphQLSchema = buildSchema(content);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    @Bean
    @Primary
    public ExecutionInputCustomizer customExecutionInputCustomizer() {
        return (ExecutionInput executionInput, ServerWebExchange serverWebExchange) -> ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> Mono.just(executionInput.transform(builder -> builder
                        .context(Context.of(SecurityContext.class, securityContext)))))
                .switchIfEmpty(Mono.just(executionInput));
    }

    private GraphQLSchema buildSchema(String sdl) {
        final TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        final RuntimeWiring runtimeWiring = buildWiring();
        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("user", userDataFetcher))
                .type(newTypeWiring("Query")
                        .dataFetcher("exercises", exerciseDataFetcher))
                .type(newTypeWiring("Query")
                        .dataFetcher("categories", categoryDataFetcher))
                .type(newTypeWiring("Mutation")
                        .dataFetcher("createExercise", exerciseMutationDataFetcher))
                .type(newTypeWiring("Mutation")
                        .dataFetcher("createCategory", categoryMutationDataFetcher))
                .build();
    }
}
