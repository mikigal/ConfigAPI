package pl.mikigal.config.style;

import com.google.common.base.CaseFormat;

public enum NameStyle {
	CAMEL_CASE(CaseFormat.LOWER_CAMEL),
	UNDERSCORE(CaseFormat.LOWER_UNDERSCORE),
	HYPHEN(CaseFormat.LOWER_HYPHEN);

	private final CaseFormat caseFormat;

	NameStyle(CaseFormat caseFormat) {
		this.caseFormat = caseFormat;
	}

	public String format(String methodName) {
		return CaseFormat.UPPER_CAMEL.to(this.caseFormat, methodName.replace("get", "").replace("set", ""));
	}
}
