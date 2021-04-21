package com.epherical.fortune.impl.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringValue {
    String value();
    String configPath() default "";
    String comment() default "";

    /**
     * This should be used if you want to migrate your variable names to another system. If they are nested, separate them with /
     * ex: jim/jones/real-key
     * @return an array of old variable names.
     */
    String[] oldVars() default "";
}
