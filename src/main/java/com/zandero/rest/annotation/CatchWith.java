package com.zandero.rest.annotation;

import com.zandero.rest.exception.ExceptionHandler;
import com.zandero.rest.exception.GenericExceptionHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CatchWith {

	/**
	 * One or more exception handler to handle given exception types.
	 * List handlers in order they should be considered, first match is used
	 *
	 * @return list of exception handlers, or default exception handler if none associated
	 */
	Class<? extends ExceptionHandler>[] value() default GenericExceptionHandler.class;
}
