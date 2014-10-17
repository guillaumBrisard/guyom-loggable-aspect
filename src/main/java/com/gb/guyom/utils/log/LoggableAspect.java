package com.gb.guyom.utils.log;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Logs method calls.
 *
 * <p>
 * It is an AspectJ aspect and you are not supposed to use it directly. It is instantiated by AspectJ runtime framework
 * when your code is annotated with {@link Loggable} annotation.
 *
 * @version $Id$
 */
@Aspect
public class LoggableAspect {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggableAspect.class);

	@Autowired
	private LoggableAspectHelper logAspectHelper;

	void setLoggableAspectHelper(LoggableAspectHelper loggableAspectHelper) {
		this.logAspectHelper = loggableAspectHelper;
	}

	/**
	 * Log methods in a class.
	 *
	 * @param point
	 *            Joint point
	 * @return The result of call
	 * @throws Throwable
	 *             If something goes wrong inside
	 */
	@Around("execution(public * (@com.gb.guyom.utils.log.Loggable *).*(..))" //
			+ " && !execution(String *.toString())" //
			+ " && !execution(int *.hashCode())" //
			+ " && !execution(boolean *.canEqual(Object))" //
			+ " && !execution(boolean *.equals(Object))" //
			+ " && !cflow(call(com.gb.guyom.utils.log.LoggableAspect.new()))")
	public Object wrapClass(final ProceedingJoinPoint point) throws Throwable {
		final Method method = MethodSignature.class.cast(point.getSignature()).getMethod();
		Object output;
		if (method.isAnnotationPresent(Loggable.class)) {
			output = point.proceed();
		}
		else {
			output = logAspectHelper.wrap(point, method, method.getDeclaringClass().getAnnotation(Loggable.class));
		}
		return output;
	}

	/**
	 * Log individual methods.
	 *
	 * @param point
	 *            Joint point
	 * @return The result of call
	 * @throws Throwable
	 *             If something goes wrong inside
	 */
	@Around("(execution(* *(..)) || initialization(*.new(..)))" + " && @annotation(com.gb.guyom.utils.log.Loggable)")
	public Object wrapMethod(final ProceedingJoinPoint point) throws Throwable {
		final Method method = MethodSignature.class.cast(point.getSignature()).getMethod();
		return logAspectHelper.wrap(point, method, method.getAnnotation(Loggable.class));
	}

}
