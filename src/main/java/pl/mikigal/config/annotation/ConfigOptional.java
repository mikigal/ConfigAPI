package pl.mikigal.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allow to mark field in config as nullable
 * Only fields with this annotation can return null or does not exists in config's file
 * @since 1.0
 * @author Mikołaj Gałązka
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ConfigOptional {
}
