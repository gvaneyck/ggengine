package com.gvaneyck.ggengine.server.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gvaneyck.ggengine.server.domain.User;
import lombok.AllArgsConstructor;

import java.lang.reflect.Method;
import java.util.Map;

@AllArgsConstructor
public class CommandRef {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String name;
    private Object instance;
    private Method method;
    private Class argType;

    public void invoke(User user, Map<String, Object> args) {
        try {
            Object realArgs = objectMapper.convertValue(args, argType);
            method.invoke(instance, user, realArgs);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking command: " + name, e);
        }
    }
}
