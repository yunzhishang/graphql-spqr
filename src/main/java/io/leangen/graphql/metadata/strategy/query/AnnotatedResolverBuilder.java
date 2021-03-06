package io.leangen.graphql.metadata.strategy.query;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.metadata.Resolver;
import io.leangen.graphql.metadata.execution.FieldAccessor;
import io.leangen.graphql.metadata.execution.MethodInvoker;
import io.leangen.graphql.metadata.execution.SingletonMethodInvoker;
import io.leangen.graphql.util.ClassUtils;

public class AnnotatedResolverBuilder extends FilteredResolverBuilder {

    public AnnotatedResolverBuilder() {
        this.operationNameGenerator = new DelegatingOperationNameGenerator(new AnnotatedOperationNameGenerator(), new MethodOperationNameGenerator());
        this.argumentExtractor = new AnnotatedArgumentBuilder();
    }

    @Override
    public Collection<Resolver> buildQueryResolvers(Object querySourceBean, AnnotatedType beanType) {
        return buildQueryResolvers(querySourceBean, beanType, getFilters());
    }

    @Override
    public Collection<Resolver> buildMutationResolvers(Object querySourceBean, AnnotatedType beanType) {
        return buildMutationResolvers(querySourceBean, beanType, getFilters());
    }

    private Collection<Resolver> buildQueryResolvers(Object querySourceBean, AnnotatedType beanType, List<Predicate<Member>> filters) {
        Stream<Resolver> methodInvokers = ClassUtils.getAnnotatedMethods(ClassUtils.getRawType(beanType.getType()), GraphQLQuery.class).stream()
                .filter(filters.stream().reduce(Predicate::and).orElse(acceptAll))
                .map(method -> new Resolver(
                        operationNameGenerator.generateQueryName(method, beanType),
                        method.getAnnotation(GraphQLQuery.class).description(),
                        querySourceBean == null ? new MethodInvoker(method, beanType) : new SingletonMethodInvoker(querySourceBean, method, beanType),
                        argumentExtractor.buildResolverArguments(method, beanType)
                ));
        Stream<Resolver> fieldAccessors = ClassUtils.getAnnotatedFields(ClassUtils.getRawType(beanType.getType()), GraphQLQuery.class).stream()
                .filter(filters.stream().reduce(Predicate::and).orElse(acceptAll))
                .map(field -> new Resolver(
                        operationNameGenerator.generateQueryName(field, beanType),
                        field.getAnnotation(GraphQLQuery.class).description(),
                        new FieldAccessor(field, beanType),
                        Collections.emptyList()
                ));
        return Stream.concat(methodInvokers, fieldAccessors).collect(Collectors.toSet());

    }

    private Collection<Resolver> buildMutationResolvers(Object querySourceBean, AnnotatedType beanType, List<Predicate<Member>> filters) {
        return ClassUtils.getAnnotatedMethods(ClassUtils.getRawType(beanType.getType()), GraphQLMutation.class).stream()
                .filter(filters.stream().reduce(Predicate::and).orElse(acceptAll))
                .map(method -> new Resolver(
                        operationNameGenerator.generateMutationName(method, beanType),
                        method.getAnnotation(GraphQLMutation.class).description(),
                        querySourceBean == null ? new MethodInvoker(method, beanType) : new SingletonMethodInvoker(querySourceBean, method, beanType),
                        argumentExtractor.buildResolverArguments(method, beanType)
                )).collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        return operationNameGenerator.getClass().hashCode() + argumentExtractor.getClass().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AnnotatedResolverBuilder)) return false;
        AnnotatedResolverBuilder that = (AnnotatedResolverBuilder) other;
        return this.operationNameGenerator.getClass().equals(that.operationNameGenerator.getClass())
                && this.argumentExtractor.getClass().equals(that.argumentExtractor.getClass());
    }
}
