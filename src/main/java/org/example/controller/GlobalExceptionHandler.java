package org.example.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle IllegalArgumentException
     * This method will be invoked when IllegalArgumentException is thrown in any controller method.
     *
     * @param e The exception caught.
     * @param request The current web request.
     * @return A ResponseEntity<JSONObject> object that contains error information. The error information includes HTTP status code 500 and an error message "Server Error".
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JSONObject> handleIllegalArgumentException(IllegalArgumentException e, WebRequest request) {
        HandlerMethod handlerMethod = (HandlerMethod) request.getAttribute(
                HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);

        assert handlerMethod != null;
        log.error("Illegal argument exception in controller " + handlerMethod.getBeanType().getName() +
                " method " + handlerMethod.getMethod().getName(), e);

        JSONObject response = new JSONObject();
        response.put("code", 400);
        response.put("msg", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Global exception handler.
     *
     * @param e The exception caught.
     * @param request The current web request.
     * @return A ResponseEntity<JSONObject> object that contains error information. The error information includes HTTP status code 500 and an error message "Server Error".
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<JSONObject> handleAllExceptions(Exception e, WebRequest request) {
        HandlerMethod handlerMethod = (HandlerMethod) request.getAttribute(
                HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);

        assert handlerMethod != null;
        log.error("Unexpected exception in controller " + handlerMethod.getBeanType().getName() +
                " method " + handlerMethod.getMethod().getName(), e);

        JSONObject response = new JSONObject();
        response.put("code", 500);
        response.put("msg", "服务器错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}