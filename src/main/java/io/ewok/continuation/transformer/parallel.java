package io.ewok.continuation.transformer;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target(ElementType.TYPE_USE)
public @interface parallel {

}
