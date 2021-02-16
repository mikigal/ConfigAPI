package pl.mikigal.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to create comment for field in config
 * New lines can be made with <code>\n</code> if annotation is used for class or CommentStyle is ABOVE_CONTENT
 * @see pl.mikigal.config.style.CommentStyle
 * @since 1.0
 * @author Mikołaj Gałązka
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Comment {
	String value();
}
