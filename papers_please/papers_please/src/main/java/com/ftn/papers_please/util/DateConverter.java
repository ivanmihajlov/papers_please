package com.ftn.papers_please.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * Converter class for java.util.Date
 * parse and print functions must be static
 *
 */
public class DateConverter {

	private static final SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy");

	/**
	 * Parses text value of a date.
	 * 
	 * @param value
	 *     date text format
	 * @return
	 *     date in java.util.Date format
	 */
	public static Date parseDate(String value) {
		try {
			return dateFormat1.parse(value);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Prints SimpleDateFormat date.
	 * 
	 * @param value
	 *     date in java.util.Date format
	 * @return
	 *    date text format
	 */
	public static String printDate(Date value) {
		if (value != null)
			return dateFormat1.format(value);
		else
			return null;
	}

	/**
	 * Parses text value of a date.
	 * 
	 * @param value
	 *     date text format
	 * @return
	 *     date in java.util.Date format
	 */
	public static Date parseYear(String value) {
		try {
			return dateFormat2.parse(value);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Prints SimpleDateFormat date.
	 * 
	 * @param value
	 *     date in java.util.Date format
	 * @return
	 *    date text format
	 */
	public static String printYear(Date value) {
		if (value != null)
			return dateFormat2.format(value);
		else
			return null;
	}
}
