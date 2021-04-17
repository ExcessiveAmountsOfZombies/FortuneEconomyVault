package com.epherical.fortune.impl.config.annotation;

import com.epherical.fortune.impl.config.FortuneConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StorageValue {

    FortuneConfig.StorageType value();
}
