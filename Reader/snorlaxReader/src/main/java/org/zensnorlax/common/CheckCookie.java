package org.zensnorlax.common;

import java.lang.annotation.*;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckCookie {
    /**
     * 是否校验Token，默认true
     */
    boolean required() default true;
}