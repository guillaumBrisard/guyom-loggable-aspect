package com.gb.guyom.utils.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Makes a method loggable.
 *
 * <p>
 * For example, this {@code load()} method produces a log line on every call:
 *
 * <pre>
 * &#064;Loggable
 * String load(String resource) throws IOException {
 * 	return &quot;something&quot;;
 * }
 * </pre>
 *
 * <p>
 * You can configure the level of logging:
 *
 * <pre>
 * &#064;Loggable(Loggable.Level.DEBUG)
 * void save(String resource) throws IOException {
 * 	// do something
 * }
 * </pre>
 *
 * <p>
 * You can specify a maximum execution time limit for a method. If such a limit is reached a logging message will be
 * issued with a {@code WARN} priority. It is a very convenient mechanism for profiling applications in production.
 * Default value of a limit is 1 second.
 *
 * <pre>
 * &#064;Loggable(limit = 2)
 * void save(String resource) throws IOException {
 * 	// do something, potentially slow
 * }
 * </pre>
 *
 * <p>
 * You can change the time unit for the "limit" parameter. Default unit of measurement is a second:
 *
 * <pre>
 * &#064;Loggable(limit = 200, unit = TimeUnit.MILLISECONDS)
 * void save(String resource) throws IOException {
 * 	// do something, potentially slow
 * }
 * </pre>
 *
 * <p>
 * You can ignore certain exception types, and they won't be logged when thrown. It is very useful when exceptions are
 * used to control flow (which is not a good practice, but is still used in some frameworks, for example in JAX-RS):
 *
 * <pre>
 * &#064;Loggable(ignore = WebApplicationException.class)
 * String get() {
 * 	if (not_logged_in()) {
 * 		throw new WebApplicationException(forward_to_login_page());
 * 	}
 * }
 * </pre>
 *
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Loggable {

	public enum Level {
		TRACE, DEBUG, INFO, WARN, ERROR
	}

	/**
	 * Level of logging.
	 */
	Level value() default Level.INFO;

	/**
	 * Maximum amount allowed for this method (a warning will be issued if it takes longer).
	 */
	int limit() default 10000;

	/**
	 * Time unit for the limit.
	 */
	TimeUnit unit() default TimeUnit.MILLISECONDS;

	/**
	 * Shall we trim long texts in order to make log lines more readable?
	 * -1 or Integer.MAX_VALUE disable trimming
	 */
	int trim() default 100;

	/**
	 * Method entry moment should be reported as well (by default only an exit moment is reported).
	 */
	boolean prepend() default false;

	/**
	 * List of exception types, which should not be logged if thrown.
	 */
	Class<? extends Throwable>[] ignore() default {};

	/**
	 * Skip logging of result, replacing it with dots?
	 */
	boolean skipResult() default false;

	/**
	 * Skip logging of arguments, replacing them all with dots?
	 */
	boolean skipArgs() default false;

	/**
	 * Add toString() result to log line.
	 */
	boolean logThis() default false;

	/**
	 * The precision (number of fractional digits) to be used when displaying the measured execution time.
	 */
	int precision() default 2;

	/**
	 * The name of the logger to be used. If not specified, defaults to the class name of the annotated class or method.
	 */
	String name() default "";

}
