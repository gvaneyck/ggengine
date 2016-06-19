package com.gvaneyck.ggengine.game.actions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Method or Field as a callable action.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Action {
    /**
     * The name used to call this action.
     * Defaults to Class.Method or Class.Field
     */
    String value() default "";
}
