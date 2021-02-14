package pl.mikigal.config.style;

/**
 * Allows to select how comments will look in config's file
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public enum CommentStyle {
	/**
	 * With INLINE comments will look like this:
	 * <code>
	 *     foo: "bar" // It's example comment
	 * </code>
	 */
	INLINE,

	/**
	 * With ABOVE_CONTENT comments will look like this:
	 * <code>
	 *     // It's example comment
	 *     foo: "bar"
	 * </code>
	 */
	ABOVE_CONTENT
}
