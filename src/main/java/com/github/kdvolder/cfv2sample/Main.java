package com.github.kdvolder.cfv2sample;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

	public static void main(String[] args) {
		String s = datestamp();
		System.out.println(s);
	}

	private static String datestamp() {
		Date d = new Date();
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
		return f.format(d);
	}

}
