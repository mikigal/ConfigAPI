package pl.mikigal.config.style;

import com.google.common.base.CaseFormat;

/**
 * Allows to select how fields name's will look in config's file
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public enum NameStyle {

	/**
	 * With CAMEL_CASE fields will have camel case names
	 * For getter method getFooBar() field will look like:
	 * <code>
	 *     fooBar: "value"
	 * </code>
	 */
	CAMEL_CASE(CaseFormat.LOWER_CAMEL),

	/**
	 * With UNDERSCORE fields will have camel case names like this:
	 * For getter method getFooBar() field will look like:
	 * <code>
	 *     foo_bar: "value"
	 * </code>
	 */
	UNDERSCORE(CaseFormat.LOWER_UNDERSCORE),

	/**
	 * With UNDERSCORE fields will have camel case names like this:
	 * For getter method getFooBar() field will look like:
	 * <code>
	 *     foo-bar: "value"
	 * </code>
	 */
	HYPHEN(CaseFormat.LOWER_HYPHEN);

	/**
	 * Field's case format
	 */
	private final CaseFormat caseFormat;

	NameStyle(CaseFormat caseFormat) {
		this.caseFormat = caseFormat;
	}

	/**
	 * Format name of method to field's name in config
	 * @param methodName name of getter or setter
	 * @return name of field in config
	 */
	public String format(String methodName) {
		return CaseFormat.UPPER_CAMEL.to(this.caseFormat, methodName.replace("get", "").replace("set", ""));
	}
}
