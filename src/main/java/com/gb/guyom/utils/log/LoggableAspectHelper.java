package com.gb.guyom.utils.log;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggableAspectHelper {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggableAspectHelper.class);

	private LoggableHelper loggableHelper;

	public LoggableAspectHelper() {
		loggableHelper = new LoggableHelper();
	}

	public void setLoggableHelper(LoggableHelper loggableHelper) {
		this.loggableHelper = loggableHelper;
	}

	/**
	 * Catch exception and re-call the method.
	 * 
	 * @param point
	 *            Joint point
	 * @param method
	 *            The method
	 * @param annotation
	 *            The annotation
	 * @return The result of call
	 * @throws Throwable
	 *             If something goes wrong inside
	 */
	public Object wrap(final ProceedingJoinPoint point, final Method method, final Loggable annotation)
			throws Throwable {

		final Object source = getSourceForLogger(method, annotation.name());
		Loggable.Level level = annotation.value();

		if (annotation.prepend()) {
			loggableHelper.log(level, source, new StringBuilder(loggableHelper.toText(//
					point.getThis(), //
					method.getName(), //
					point.getArgs(), //
					annotation.trim(), //
					annotation.skipArgs(), //
					annotation.logThis())).append(": entered").toString());
		}

		StringBuilder msg;
		final Object result;
		final long nano;
		final long start = System.nanoTime();
		try {
			result = point.proceed();
		}
		catch (final Throwable ex) {
			nano = System.nanoTime() - start;
			if (!this.contains(annotation.ignore(), ex)) {
				final StackTraceElement trace = ex.getStackTrace()[0];

				msg = new StringBuilder();
				msg.append(String.format("%s: thrown %s out of %s#%s[%d] in ", //
						loggableHelper.toText(//
								point.getThis(), //
								method.getName(), //
								point.getArgs(), //
								annotation.trim(), //
								annotation.skipArgs(), //
								annotation.logThis()), //
						loggableHelper.toText(ex), //
						trace.getClassName(), //
						trace.getMethodName(), //
						trace.getLineNumber()));
				msg.append(loggableHelper.toText(nano, annotation.precision()));
				loggableHelper.log(Loggable.Level.ERROR, source, msg.toString());
			}
			throw ex;
		}

		nano = System.nanoTime() - start;
		final boolean over = nano > annotation.unit().toNanos(annotation.limit());
		if (loggableHelper.enabled(level, source) || over) {
			msg = new StringBuilder();
			msg.append(
					loggableHelper.toText(point.getThis(), method.getName(), point.getArgs(), annotation.trim(),
							annotation.skipArgs(), annotation.logThis())).append(':');

			if (!method.getReturnType().equals(Void.TYPE)) {
				msg.append(' ').append(loggableHelper.toText(result, annotation.trim(), annotation.skipResult()));
			}
			msg.append(" in ").append(loggableHelper.toText(nano, annotation.precision()));
			if (over) {
				level = Loggable.Level.WARN;
				msg.append(" (too slow!)");
			}
			loggableHelper.log(level, source, msg.toString());
		}

		return result;
	}

	/**
	 * Checks whether array of types contains given type.
	 * 
	 * @param array
	 *            Array of them
	 * @param exp
	 *            The exception to find
	 * @return TRUE if it's there
	 */
	private boolean contains(final Class<? extends Throwable>[] array, final Throwable exp) {
		if (array == null)
			return false;

		boolean contains = false;
		for (final Class<? extends Throwable> type : array) {
			if (this.instanceOf(exp.getClass(), type)) {
				contains = true;
				break;
			}
		}
		return contains;
	}

	/**
	 * The type is an instance of another type?
	 * 
	 * @param child
	 *            The child type
	 * @param parent
	 *            Parent type
	 * @return TRUE if child is really a child of a parent
	 */
	private boolean instanceOf(final Class<?> child, final Class<?> parent) {
		boolean instance = child.equals(parent)
				|| (child.getSuperclass() != null && this.instanceOf(child.getSuperclass(), parent));
		if (!instance) {
			for (final Class<?> iface : child.getInterfaces()) {
				instance = this.instanceOf(iface, parent);
				if (instance) {
					break;
				}
			}
		}
		return instance;
	}

	/**
	 * Get the source for the logger for this method.
	 * 
	 * @param method
	 *            The method
	 * @param name
	 *            The Loggable annotation
	 * @return The source object that logger will use
	 */
	private Object getSourceForLogger(final Method method, final String name) {
		final Object source;
		if (name.isEmpty()) {
			source = method.getDeclaringClass();
		}
		else {
			source = name;
		}
		return source;
	}

}
