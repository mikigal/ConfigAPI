package pl.mikigal.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allow to specify path in config for field.
 * Without this annotation field's names will be generated with selected name style
 * @see pl.mikigal.config.style.NameStyle
 * @since 1.0
 * @author Mikołaj Gałązka
 */
@Target({ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ConfigPath {
	String value();
}
