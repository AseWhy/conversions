package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.annotations.ConvertMutator;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
public record MutatorArgumentResolver(
    ConversionProvider provider
) implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(ConvertMutator.class) != null && provider.getFactory().getStore().isPresentMutator(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest nativeWebRequest,
        WebDataBinderFactory binderFactory
    ) throws Exception {
        var factory = provider.getFactory();
        var objectMapper = factory.getObjectMapper();
        var httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        if (httpServletRequest != null) {
            var tree = objectMapper.readTree(httpServletRequest.getInputStream());
            var parsed = objectMapper.treeToValue(tree, Map.class);
            var result = (ConversionMutator<?>) objectMapper.treeToValue(tree, parameter.getParameterType());

            provider.createMutator(result, parsed);

            return result;
        }

        return Objects.requireNonNull(parameter.getConstructor()).newInstance();
    }
}
