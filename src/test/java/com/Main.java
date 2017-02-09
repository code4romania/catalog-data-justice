package com;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	public static void main(String[] args) {
		String text = "oviata in 2 ani, 4 luni si 4 zile, nu-i asa?";
		
		Pattern pattern = Pattern.compile(
				"(?:(?:(?:(?:\\d)+ (?:ani|an))|(?:(?:\\d)+ (?:luna|luni))|(?:(?:\\d)+ (?:zile|zi)))(?:(?:\\s?si\\s?)|(?:\\s?,\\s))?)+");
		Matcher matcher = pattern.matcher(text);
		if (matcher.find())
			System.out.println(matcher.group(0));
	}
}
