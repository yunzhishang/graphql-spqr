package io.leangen.graphql.metadata.strategy.query;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.RelayId;
import io.leangen.graphql.metadata.OperationArgument;
import io.leangen.graphql.metadata.OperationArgumentDefaultValue;
import io.leangen.graphql.util.ClassUtils;

public class AnnotatedArgumentBuilder implements ResolverArgumentBuilder {

    @Override
    public List<OperationArgument> buildResolverArguments(Method resolverMethod, AnnotatedType enclosingType) {
        List<OperationArgument> operationArguments = new ArrayList<>(resolverMethod.getParameterCount());
        AnnotatedType[] parameterTypes = ClassUtils.getParameterTypes(resolverMethod, enclosingType);
        for (int i = 0; i < resolverMethod.getParameterCount(); i++) {
            Parameter parameter = resolverMethod.getParameters()[i];
            ClassUtils.checkIfResolvable(parameterTypes[i], resolverMethod); //checks if the type is resolvable
            AnnotatedType parameterType = ClassUtils.stripBounds(parameterTypes[i]);
            parameterType = ClassUtils.addAnnotations(parameterType, parameter.getAnnotations());
            operationArguments.add(new OperationArgument(
                    parameterType,
                    getArgumentName(parameter, parameterType),
                    getArgumentDescription(parameter, parameterType),
                    defaultValue(parameter, parameterType),
                    parameter.isAnnotationPresent(GraphQLContext.class),
                    isMappable(parameter)
            ));
        }
        return operationArguments;
    }

    protected String getArgumentName(Parameter parameter, AnnotatedType parameterType) {
        if (parameterType.isAnnotationPresent(RelayId.class)) {
            return RelayId.FIELD_NAME;
        }
        GraphQLArgument meta = parameter.getAnnotation(GraphQLArgument.class);
        return meta != null && !meta.name().isEmpty() ? meta.name() : parameter.getName();
    }

    protected String getArgumentDescription(Parameter parameter, AnnotatedType parameterType) {
        GraphQLArgument meta = parameter.getAnnotation(GraphQLArgument.class);
        return meta != null ? meta.description() : null;
    }

    protected OperationArgumentDefaultValue defaultValue(Parameter parameter, AnnotatedType parameterType) {

        GraphQLArgument meta = parameter.getAnnotation(GraphQLArgument.class);
        if (meta == null) return OperationArgumentDefaultValue.EMPTY;
        try {
            return meta.defaultValueProvider().newInstance().getDefaultValue(parameter, parameterType, defaultValue(meta.defaultValue()));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(
                    meta.defaultValueProvider().getName() + " must expose a public default constructor", e);
        }
    }

    private OperationArgumentDefaultValue defaultValue(String value) {
        if (GraphQLArgument.NONE.equals(value)) {
            return OperationArgumentDefaultValue.EMPTY;
        } else if (GraphQLArgument.NULL.equals(value)) {
            return OperationArgumentDefaultValue.NULL;
        }
        return new OperationArgumentDefaultValue(value);
    }

    private boolean isMappable(Parameter parameter) {
        return Arrays.stream(parameter.getAnnotations())
                .noneMatch(ann -> ann.annotationType().isAnnotationPresent(GraphQLIgnore.class));
    }
}
