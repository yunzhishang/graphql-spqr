package io.leangen.graphql.generator.mapping.common;

import java.lang.reflect.AnnotatedType;
import java.util.Map;

import io.leangen.geantyref.GenericTypeReflector;

/**
 * @author Bojan Tomic (kaqqao)
 */
public class MapScalarAdapter extends ObjectScalarAdapter {

    @Override
    public boolean supports(AnnotatedType type) {
        return GenericTypeReflector.isSuperType(Map.class, type.getType());
    }
}
