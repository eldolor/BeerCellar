package com.cm.beer.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;

public class Logger {

	public static final String LOG_LEVEL_LOG = "LOG";
	public static final String LOG_LEVEL_WARN = "WARN";
	public static final String LOG_LEVEL_ERROR = "ERROR";
	// default to WARN
	public static String CURRENT_LOG_LEVEL = LOG_LEVEL_WARN;

	private static boolean IS_INITIALIZED;

	/**
	 * @param logMsg
	 */
	public static void log(String logMsg) {
		if (!IS_INITIALIZED) {
			setupDefaultConfiguration();
		}

		if (CURRENT_LOG_LEVEL.equals(LOG_LEVEL_LOG)) {
			Thread current = Thread.currentThread();
			long tid = current.getId();
			StackTraceElement[] stack = current.getStackTrace();
			String tag = stack[3].getClassName();
			String methodName = stack[3].getMethodName();
			// Prepend current thread ID and name of calling method to the
			// message.
			logMsg = "[" + tid + "] [" + tag + "] [" + methodName + "] "
					+ logMsg;
			if (Logger.isLogEnabled())  Logger.log(logMsg);
		}
	}

	/**
	 * @param logMsg
	 */
	public static void warn(String logMsg) {
		if (!IS_INITIALIZED) {
			setupDefaultConfiguration();
		}
		if (CURRENT_LOG_LEVEL.equals(LOG_LEVEL_LOG)
				|| CURRENT_LOG_LEVEL.equals(LOG_LEVEL_WARN)) {
			Thread current = Thread.currentThread();
			long tid = current.getId();
			StackTraceElement[] stack = current.getStackTrace();
			String tag = stack[3].getClassName();
			String methodName = stack[3].getMethodName();
			// Prepend current thread ID and name of calling method to the
			// message.
			logMsg = "[" + tid + "] [" + tag + "] [" + methodName + "] "
					+ logMsg;
			Log.w(tag, logMsg);
		}
	}

	/**
	 * @param logMsg
	 */
	public static void error(String logMsg) {
		if (!IS_INITIALIZED) {
			setupDefaultConfiguration();
		}
		Thread current = Thread.currentThread();
		long tid = current.getId();
		StackTraceElement[] stack = current.getStackTrace();
		String tag = stack[3].getClassName();
		String methodName = stack[3].getMethodName();
		// Prepend current thread ID and name of calling method to the
		// message.
		logMsg = "[" + tid + "] [" + tag + "] [" + methodName + "] " + logMsg;
		Log.e(tag, logMsg);
	}

	/**
	 * @param logMsg
	 */
	public static void error(String logMsg, Throwable t) {
		if (!IS_INITIALIZED) {
			setupDefaultConfiguration();
		}
		Thread current = Thread.currentThread();
		long tid = current.getId();
		StackTraceElement[] stack = current.getStackTrace();
		String tag = stack[3].getClassName();
		String methodName = stack[3].getMethodName();
		// Prepend current thread ID and name of calling method to the message.
		logMsg = "[" + tid + "] [" + tag + "] [" + methodName + "] " + logMsg;
		Log.e(tag, logMsg, t);
	}

	/**
	 * @return
	 */
	public static boolean isLogEnabled() {
		return (CURRENT_LOG_LEVEL.equals(LOG_LEVEL_LOG)) ? true : false;
	}

	/**
	 * @return
	 */
	public static boolean isWarnEnabled() {
		return (CURRENT_LOG_LEVEL.equals(LOG_LEVEL_LOG) || CURRENT_LOG_LEVEL
				.equals(LOG_LEVEL_WARN)) ? true : false;
	}

	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static synchronized void setupDefaultConfiguration() {
		// double lock
		if (!IS_INITIALIZED) {
			IS_INITIALIZED = true;
			// Yes, we currently don't do anything meaningful here
		}
	}

	private static void init() {
		if (!IS_INITIALIZED) {
			setupDefaultConfiguration();
		}
	}

	// tag is in String[0]; log message is in String[1]
	private static String[] generateLogMessageAndTag(String formatter,
			Object... args) {
		String logMsg = String.format(formatter, args);
		Thread current = Thread.currentThread();
		long tid = current.getId();
		StackTraceElement[] stack = current.getStackTrace();
		String tag = stack[4].getClassName();
		String methodName = stack[4].getMethodName();
		// Prepend current thread ID and name of calling method to the message.
		logMsg = "[" + tid + "] [" + tag + "] [" + methodName + "] " + logMsg;

		String[] returnArray = new String[2];
		returnArray[0] = tag;
		returnArray[1] = logMsg;
		return returnArray;
	}

	// If you're wondering, it's short for FormattedLog
	public static void flog(String formatter, Object... args) {
		init();
		if (CURRENT_LOG_LEVEL.equals(LOG_LEVEL_LOG)) {
			String[] logMsg = generateLogMessageAndTag(formatter, args);
			Log.d(logMsg[0], logMsg[1]);
		}
	}

	// If you're wondering, it's short for FormattedWarning
	public static void fwarn(String formatter, Object... args) {
		init();
		if (CURRENT_LOG_LEVEL.equals(LOG_LEVEL_LOG)
				|| CURRENT_LOG_LEVEL.equals(LOG_LEVEL_WARN)) {
			String[] logMsg = generateLogMessageAndTag(formatter, args);
			Log.w(logMsg[0], logMsg[1]);
		}
	}

	// If you're wondering, it's short for FormattedError
	public static void ferror(String formatter, Object... args) {
		init();
		String[] logMsg = generateLogMessageAndTag(formatter, args);
		Log.e(logMsg[0], logMsg[1]);
	}

}