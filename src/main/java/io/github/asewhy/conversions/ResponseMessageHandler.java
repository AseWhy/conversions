package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.annotations.ConvertResponse;
import io.github.asewhy.conversions.support.annotations.ShiftController;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Log4j2
public final class ResponseMessageHandler extends RequestResponseBodyMethodProcessor {
    private final ConversionProvider provider;

    public ResponseMessageHandler(List<HttpMessageConverter<?>> converters, ConversionProvider provider) {
        super(converters);

        this.provider = provider;
    }

    @Override
    public boolean supportsReturnType(@NotNull MethodParameter parameter) {
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
        @NotNull NativeWebRequest webRequest
    ) throws HttpMediaTypeNotAcceptableException, IOException {
        var httpServletRequest = webRequest.getNativeResponse(HttpServletResponse.class);

        if(httpServletRequest != null) {
            var converted = returnValue;
            var mapping = getMappingName(returnType);

            if(canProcess(returnType, mapping)) {
                converted = provider.createResponseResolve(returnValue, mapping);
            } else {
                log.warn("IS NOT A CONVERTIBLE ENTITY " + returnType.getParameterType());
            }

            super.handleReturnValue(converted, returnType, mavContainer, webRequest);
        }
    }

    /**
     * ???????????????? ???????????????? ???????????????? ?????? ?????????????? ???????????????????????????? ????????????????
     *
     * @param parameter ???????????????????????????? ????????????????
     * @return ??????????????
     */
    private String getMappingName(@NotNull MethodParameter parameter) {
        var method = parameter.getMethod();

        if(method == null) {
            return ConversionUtils.COMMON_MAPPING;
        }

        var annotation = AnnotationUtils.findAnnotation(method, ConvertResponse.class);
        var annotatedClass = parameter.getContainingClass();

        if(annotation != null) {
            return annotation.mapping();
        }

        annotation = AnnotationUtils.findAnnotation(parameter.getDeclaringClass() , ConvertResponse.class);

        if(annotation != null) {
            return annotation.mapping();
        }

        return ConversionUtils.COMMON_MAPPING;
    }

    /**
     * ???????????? true ???????? ???????? ???????????????????? ?????????? ???????????????????? ???????? ??????
     *
     * @param parameter ???????????????? ?????? ??????????????????
     * @return true ???????? ???????? ???????????????????? ?????????? ???????????????????? ???????? ??????
     */
    private boolean canProcess(@NotNull MethodParameter parameter, String mapping) {
        var result = parameter.getParameterType();
        var store = provider.getFactory().getStore();

        if(store.isPresentResponse(result)) {
            return true;
        } else {
            return provider.canResolveResponse(result, parameter.getGenericParameterType(), mapping);
        }
    }
}
