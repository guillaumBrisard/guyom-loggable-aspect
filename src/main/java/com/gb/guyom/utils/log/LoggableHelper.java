package com.gb.guyom.utils.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggableHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggableHelper.class);

	private static final String COMMA = ", ";
	private static final String DOTS = "...";

	/**
	 * Log one line.
	 * 
	 * @param level
	 *            Level of logging
	 * @param source
	 *            Source of the logging operation
	 * @param format
	 *            The format string
	 * @param arguments
	 *            arguments - a list of 3 or more arguments
	 */
	void log(final Loggable.Level level, final Object source, final String format, final Object... arguments) {
		final Logger logger = getLogger(source);
		switch (level) {
		case TRACE:
			if (logger.isTraceEnabled()) {
				logger.trace(format, arguments);
			}
			break;
		case DEBUG:
			if (logger.isDebugEnabled()) {
				logger.debug(format, arguments);
			}
			break;
		case INFO:
			if (logger.isInfoEnabled()) {
				logger.debug(format, arguments);
			}
			break;
		case WARN:
			if (logger.isWarnEnabled()) {
				logger.warn(format, arguments);
			}
			break;
		case ERROR:
			logger.error(format, arguments);
			break;
		default:
			LOGGER.error("This level of log is not handled : {}", level);
		}
	}

	/**
	 * Log level is enabled?
	 *
	 * @param level
	 *            Level of logging
	 * @param source
	 *            Source of the logging operation
	 * @return TRUE if enabled
	 */
	boolean enabled(final Loggable.Level level, final Object source) {
		final Logger logger = getLogger(source);
		boolean enabled = false;
		switch (level) {
		case TRACE:
			enabled = logger.isTraceEnabled();
			break;
		case DEBUG:
			enabled = logger.isDebugEnabled();
			break;
		case INFO:
			enabled = logger.isInfoEnabled();
			break;
		case WARN:
			enabled = logger.isWarnEnabled();
			break;
		case ERROR:
			enabled = logger.isErrorEnabled();
			break;
		default:
			LOGGER.error("This level of log is not handled : {}", level);
		}
		return enabled;
	}

	/**
	 * Get the instance of the logger for this particular caller.
	 * 
	 * @param source
	 *            Source of the logging operation
	 * @return The instance of {@code Logger} class
	 */
	private Logger getLogger(final Object source) {
		final Logger srcLogger;
		if (source instanceof Class) {
			srcLogger = LoggerFactory.getLogger((Class<?>) source);
		}
		else if (source instanceof String) {
			srcLogger = LoggerFactory.getLogger(String.class.cast(source));
		}
		else {
			srcLogger = LoggerFactory.getLogger(source.getClass());
		}
		return srcLogger;
	}

	/**
	 * Make a string out of method.
	 * 
	 * @param thiz
	 *            this
	 * @param method
	 *            The method
	 * @param args
	 *            Actual arguments of the method
	 * @param trim
	 *            Shall we trim long texts?
	 * @param skip
	 *            Shall we skip details and output just dots?
	 * @param logthis
	 *            Shall we add toString result to log?
	 * @return Text representation of it
	 */
	String toText(final Object thiz, final String methodName, final Object[] args, final int trim, final boolean skip,
			final boolean logThis) {
		final StringBuilder log = new StringBuilder();

		if (logThis && (thiz != null)) {
			log.append(thiz.toString());
		}

		log.append('#').append(methodName).append('(');

		if (skip) {
			log.append(LoggableHelper.DOTS);
		}
		else {
			for (int pos = 0; pos < args.length; ++pos) {
				if (pos > 0) {
					log.append(LoggableHelper.COMMA);
				}
				log.append(this.toText(args[pos], trim, false));
			}
		}

		return log.append(')').toString();
	}

	/**
	 * Make a string out of an exception.
	 * 
	 * @param exp
	 *            The exception
	 * @return Text representation of it
	 */
	String toText(final Throwable exp) {
		final StringBuilder text = new StringBuilder();
		text.append(exp.getClass().getName());
		final String msg = exp.getMessage();
		if (msg != null) {
			text.append('(').append(msg).append(')');
		}
		return text.toString();
	}

	/**
	 * Make a string out of an object.
	 * 
	 * @param arg
	 *            The argument
	 * @param trim
	 *            Shall we trim long texts?
	 * @param skip
	 *            Shall we skip it with dots?
	 * @return Text representation of it
	 */
	String toText(final Object arg, final int trim, final boolean skip) {
		final StringBuilder text = new StringBuilder();
		if (arg == null) {
			text.append("NULL");
		}
		else if (skip) {
			text.append(LoggableHelper.DOTS);
		}
		else {
			try {
				text.append(trimText(this.toText(arg), trim));
			}
			catch (final Throwable ex) {
				text.append(String.format("[%s thrown %s]", arg.getClass().getName(), this.toText(ex)));
			}
		}
		return text.toString();
	}

	/**
	 * Create text.
	 * 
	 * @param nano
	 *            period to convert in ns
	 * @return The text
	 */
	String toText(final double nano, int precision) {
		final double number;
		final String title;
		if (nano < 1000L) {
			number = nano;
			title = "ns";
		}
		else if (nano < 1000L * 1000) {
			number = nano / 1000L;
			title = "µs";
		}
		else if (nano < 1000L * 1000 * 1000) {
			number = nano / (1000L * 1000);
			title = "ms";
		}
		else { // if (nano < 1000L * 1000 * 1000 * 60) {
			number = nano / (1000L * 1000 * 1000);
			title = "s";
		}

		final String format;
		if (precision >= 0) {
			format = String.format("%%.%df%%s", precision);
		}
		else {
			format = "%.0f%s";
		}
		return String.format(format, number, title);
	}

	/**
	 * Make a string out of an object.
	 * 
	 * @param arg
	 *            The argument
	 * @return Text representation of it
	 */
	private String toText(final Object arg) {
		String text;
		if (arg.getClass().isArray()) {
			final StringBuilder bldr = new StringBuilder();
			bldr.append('[');
			for (final Object item : (Object[]) arg) {
				if (bldr.length() > 1) {
					bldr.append(LoggableHelper.COMMA);
				}
				bldr.append(this.toText(item, -1, false));
			}
			text = bldr.append(']').toString();
		}
		else {
			final String origin = arg.toString();
			if (arg instanceof String || origin.contains(" ") || origin.isEmpty()) {
				text = String.format("'%s'", origin);
			}
			else {
				text = origin;
			}
		}
		return text;
	}

	/**
	 * trim the text
	 * 
	 * @param text
	 *            The text to trim
	 * @return The result
	 */
	private String trimText(final String text, final int maxLength) {
		final String result;
		if (maxLength < 0 || text.length() < maxLength) {
			result = text;
		}
		else {
			final int skip = text.length() - maxLength;
			final StringBuilder output = new StringBuilder().append(text.substring(0, maxLength / 2)) //
			.append("..").append(skip).append("..");
			int beginIndex = text.length() - maxLength + output.length();
			if (beginIndex < text.length()) {
				output.append(text.substring(text.length() - maxLength + output.length()));
			}
			result = output.toString();
		}
		return result.replace("\n", "\\n");
	}

}
