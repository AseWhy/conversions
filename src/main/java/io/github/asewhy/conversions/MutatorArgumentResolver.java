package io.github.asewhy.conversions;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.support.annotations.ConvertMutator;
import io.github.asewhy.conversions.support.annotations.ConvertRequest;
import lombok.AllArgsConstructor;
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

@AllArgsConstructor
public class MutatorArgumentResolver implements HandlerMethodArgumentResolver {
    private final ConversionProvider provider;

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
        var config = provider.getConfig();
        var objectMapper = config.getObjectMapper();
        var httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        if (httpServletRequest != null) {
            var tree = objectMapper.readTree(httpServletRequest.getInputStream());

            if(parameter.getParameterAnnotation(ConvertMutator.class) != null) {
                var type = parameter.getParameterType();
                var generic = ReflectionUtils.findXGeneric(parameter.getGenericParameterType(), 0);

                var resolvedRequest = provider.createRequestResolve(tree, type, generic);

                if(resolvedRequest != null) {
                    return validate(resolvedRequest, nativeWebRequest, binderFactory, parameter);
                }
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
