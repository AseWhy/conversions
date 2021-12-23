package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.annotations.ShiftController;
import io.github.asewhy.conversions.support.annotations.ConvertResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public final class ResponseMessageHandler extends RequestResponseBodyMethodProcessor {
    private final ConversionProvider provider;

    public ResponseMessageHandler(List<HttpMessageConverter<?>> converters, ConversionProvider provider) {
        super(converters);

        this.provider = provider;
    }

    @Override
    public boolean supportsReturnType(MethodParameter parameter) {
        var annotation = parameter.getParameterAnnotation(ConvertResponse.class) != null;
        var annotatedClass = parameter.getContainingClass();

        if (!annotation) {
            annotation = annotatedClass.getAnnotation(ConvertResponse.class) != null;
        }

        if(!annotation) {
            annotation = annotatedClass.getAnnotation(ShiftController.class) != null;
        }

        return annotation;
    }

    @Override
    public void handleReturnValue(
        Object returnValue,
        @NotNull MethodParameter returnType,
        @NotNull ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest
    ) throws HttpMediaTypeNotAcceptableException, IOException {
        var httpServletRequest = webRequest.getNativeResponse(HttpServletResponse.class);

        if(httpServletRequest != null) {
            var converted = (Object) null;
            var mapping = getMappingName(returnType);

            if(canProcess(returnType)) {
                if (returnValue instanceof Collection<?> collection) {
                    converted = provider.createResponse(collection, mapping);
                } else {
                    converted = provider.createResponse(returnValue, mapping);
                }
            } else {
                converted = returnValue;
            }

            super.handleReturnValue(converted, returnType, mavContainer, webRequest);
        }
    }

    /**
     * Получить название маппинга для текущей конвертируемой сущности
     *
     * @param parameter конвертируемый параметр
     * @return маппинг
     */
    private String getMappingName(MethodParameter parameter) {
        var annotation = parameter.getParameterAnnotation(ConvertResponse.class);
        var annotatedClass = parameter.getContainingClass();

        if(annotation != null) {
            return annotation.mapping();
        }

        annotation = annotatedClass.getAnnotation(ConvertResponse.class);

        if(annotation != null) {
            return annotation.mapping();
        }

        var controller = annotatedClass.getAnnotation(ShiftController.class);

        if(controller != null) {
            return controller.mapping();
        }

        return ConversionUtils.COMMON_MAPPING;
    }

    /**
     * Вернет true если этот обработчик может обработать этот тип
     *
     * @param parameter параметр для обработки
     * @return true если этот обработчик может обработать этот тип
     */
    private Boolean canProcess(MethodParameter parameter) {
        var result = parameter.getParameterType();

        if(Collection.class.isAssignableFrom(result)) {
            var type = parameter.getGenericParameterType();

            if(type instanceof Class<?> typeClass) {
                result = typeClass;
            }
        }

        return provider.getFactory().getStore().isPresentResponse(result);
    }
}
