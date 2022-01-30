package io.github.asewhy.conversions;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.support.annotations.ConvertMutator;
import io.github.asewhy.conversions.support.annotations.ConvertRequest;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

@SuppressWarnings("unchecked")
public record MutatorArgumentResolver(
    ConversionProvider provider
) implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(@NotNull MethodParameter parameter) {
        return
            parameter.getParameterAnnotation(ConvertMutator.class) != null ||
            parameter.getParameterAnnotation(ConvertRequest.class) != null;
    }

    @Override
    public Object resolveArgument(
        @NotNull MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        @NotNull NativeWebRequest nativeWebRequest,
        WebDataBinderFactory binderFactory
    ) throws Exception {
        var factory = provider.getFactory();
        var objectMapper = factory.getObjectMapper();
        var httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        if (httpServletRequest != null) {
            var tree = objectMapper.readTree(httpServletRequest.getInputStream());

            if(parameter.getParameterAnnotation(ConvertMutator.class) != null) {
                var type = parameter.getParameterType();
                var result = objectMapper.treeToValue(tree, type);

                if(result instanceof Collection<?> collection) {
                    var generic = ReflectionUtils.findXGeneric(parameter.getGenericParameterType(), 0);

                    if(generic != null && factory.getStore().isPresentMutator(generic)) {
                        var parsedGeneric = (Class<? extends ConversionMutator<?>>) generic;
                        var ghosts = ReflectionUtils.makeCollectionInstance(type);

                        for (var current : collection) {
                            if (current instanceof HashMap<?, ?> context) {
                                var mutator = objectMapper.convertValue(context, parsedGeneric);
                                var castedContext = (Map<String, Object>) context;

                                provider.createMutator(mutator, castedContext);

                                ghosts.add(mutator);
                            }
                        }

                        result = ghosts;
                    }
                } else {
                    var parsed = objectMapper.treeToValue(tree, Map.class);

                    if (
                        factory.getStore().isPresentMutator(parameter.getParameterType()) &&
                        result instanceof ConversionMutator<?> mutator
                    ) {
                        provider.createMutator(mutator, parsed);
                    }
                }

                return validate(result, nativeWebRequest, binderFactory, parameter);
            } else {
                return validate(objectMapper.treeToValue(tree, parameter.getParameterType()), nativeWebRequest, binderFactory, parameter);
            }
        }

        return validate(Objects.requireNonNull(parameter.getConstructor()).newInstance(), nativeWebRequest, binderFactory, parameter);
    }

    @Contract("_, _, _, _ -> param1")
    public Object validate(Object result, NativeWebRequest nativeWebRequest, WebDataBinderFactory binderFactory, @NotNull MethodParameter parameter) throws Exception {
        if(parameter.hasParameterAnnotation(Valid.class)) {
            var binder = binderFactory.createBinder(nativeWebRequest, result, "resolvedObjectLogicalName");

            binder.validate();

            var bindingResult = binder.getBindingResult();

            if (bindingResult.getErrorCount ()> 0) {
                throw new MethodArgumentNotValidException(parameter, bindingResult);
            }
        }

        return result;
    }
}
