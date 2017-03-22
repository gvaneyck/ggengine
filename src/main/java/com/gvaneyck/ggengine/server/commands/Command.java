package com.gvaneyck.ggengine.server.commands;

import com.gvaneyck.ggengine.server.dto.client.ClientCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    ClientCommand value() default ClientCommand.NONE;
}