package com.Egret.Utils;

import java.util.Calendar;

public class Log {

	public static void append(String s) {
		printHeader();
		System.out.println(s);
	}
	
	public static void error(String s) {
		printHeader();
		System.out.println("ERROR --- " + s);
	}
	
	private static void printHeader() {
		
		Calendar cal = Calendar.getInstance();
		System.out.print(
				"[" +
				cal.getTime() +
				"] "
		);
	}
}
