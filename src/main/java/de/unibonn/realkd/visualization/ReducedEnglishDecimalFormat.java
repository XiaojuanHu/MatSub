package de.unibonn.realkd.visualization;

import java.text.DecimalFormat;
import java.text.FieldPosition;

public class ReducedEnglishDecimalFormat extends DecimalFormat {

	private static final long serialVersionUID = 4234581408883643635L;

	private static String[] suffix = new String[] { "", "K", "M", "B", "T" };
	private static int MAX_LENGTH = 4;
	private static int FIRST_MIN_EXPONENT = 3;

	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo,
			FieldPosition pos) {
		return toAppendTo.append(formatTheNumber(number));
	}

	public String formatTheNumber(double number) {
		if (number < 0) {
			return "-" + format(-number);
		}

		if (doesNotNeedExponentAndFirstDigit(number)) {
			return new DecimalFormat(".##").format(number);
		}

		if (betweenZeroAndOne(number)) {
			return new DecimalFormat("0.##E0").format(number).replace("E", "e");
		}

		String r = new DecimalFormat("##0E0").format(number);
		r = r.replaceAll("E[0-9]",
				suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
		while (r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[A-Z]")) {
			r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
		}

		return r;
	}

	private boolean doesNotNeedExponentAndFirstDigit(double number) {
		String r = new DecimalFormat("0.##E0").format(number);
		return Integer.valueOf("" + r.charAt(r.length() - 1)) < FIRST_MIN_EXPONENT;
	}

	private boolean betweenZeroAndOne(double number) {
		return number != 0 && number < 1;
	}
}
