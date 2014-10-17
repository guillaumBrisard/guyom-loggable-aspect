package com.gb.guyom.utils.log;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.Aspects;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;

@RunWith(MockitoJUnitRunner.class)
public final class LoggableTest {

	/**
	 * Foo toString result.
	 */
	private static final transient String RESULT = "some text";

	@Mock
	private Appender<ILoggingEvent> mockAppender;

	// Captor is genericised with ch.qos.logback.classic.spi.LoggingEvent
	@Captor
	private ArgumentCaptor<ILoggingEvent> captorLoggingEvent;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		LoggableHelper loggableHelper = new LoggableHelper();
		LoggableAspectHelper loggableAspectHelper = new LoggableAspectHelper();
		loggableAspectHelper.setLoggableHelper(loggableHelper);
		LoggableAspect loggableAspect = Aspects.aspectOf(LoggableAspect.class);
		loggableAspect.setLoggableAspectHelper(loggableAspectHelper);

		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.addAppender(mockAppender);
		when(mockAppender.getName()).thenReturn("MOCK");
	}

	@After
	public void teardown() {
		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.detachAppender(mockAppender);
	}

	/**
	 * Loggable can log simple calls.
	 * 
	 * @throws Exception
	 *             If something goes wrong
	 */
	@Test
	public void logsSimpleCall() throws Exception {
		new LoggableTest.Foo().revert("hello");
	}

	/**
	 * Loggable can ignore toString() methods.
	 * 
	 * @throws Exception
	 *             If something goes wrong
	 */
	@Test
	public void ignoresToStringMethods() throws Exception {
		new LoggableTest.Foo().self();
	}

	/**
	 * Loggable can log static methods.
	 * 
	 * @throws Exception
	 *             If something goes wrong
	 */
	@Test
	public void logsStaticMethods() throws Exception {
		LoggableTest.Foo.text();
	}

	/**
	 * Loggable can ignore inherited methods.
	 * 
	 * @throws Exception
	 *             If something goes wrong
	 */
	@Test
	public void doesntLogInheritedMethods() throws Exception {
		new LoggableTest.Foo().parentText();
	}

	/**
	 * Loggable can ignore some exceptions.
	 * 
	 * @throws Exception
	 *             If something goes wrong
	 */
	@Test(expected = IllegalStateException.class)
	public void ignoresSomeExceptions() throws Exception {
		new LoggableTest.Foo().doThrow();
	}

	/**
	 * Loggable can log duration with a specific time unit.
	 * 
	 * @throws Exception
	 *             If something goes wrong
	 */
	@Test
	public void logsDurationWithSpecifiedTimeUnit() throws Exception {
		LoggableTest.Foo.logsDurationInSeconds();

		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final ILoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertTrue(loggingEvent.getFormattedMessage().matches(".* in \\d.\\d{3}.*"));
	}

	/**
	 * Loggable can log toString method.
	 * 
	 * @throws Exception
	 *             If something goes wrong
	 */
	@Test
	public void logsToStringResult() throws Exception {
		new LoggableTest.Foo().last("TEST");

		verify(mockAppender).doAppend(argThat(new ArgumentMatcher<ILoggingEvent>() {
			@Override
			public boolean matches(Object argument) {
				return ((ILoggingEvent) argument).getFormattedMessage().contains(LoggableTest.RESULT);
			}
		}));
	}

	/**
	 * Loggable can log methods that specify their own logger name.
	 * 
	 * @throws Exception
	 *             If something goes wrong
	 */
	@Test
	public void logsWithExplicitLoggerName() throws Exception {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder ple = new PatternLayoutEncoder();
		ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
		ple.setContext(lc);
		ple.start();

		OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<ILoggingEvent>();
		appender.setName("REAL");
		appender.setContext(lc);
		appender.setEncoder(ple);
		appender.setOutputStream(new ByteArrayOutputStream());
		appender.start();

		logger.addAppender(appender);

		LoggableTest.Foo.explicitLoggerName();

		String logString = appender.getOutputStream().toString();
		System.out.println("logstring = " + logString);
		Assert.assertTrue(logString.contains("test-logger"));

		appender.stop();
		ple.stop();
		logger.detachAppender(appender);
	}

	/**
	 * Parent class, without logging.
	 */
	private static class Parent {
		/**
		 * Get some text.
		 * 
		 * @return The text
		 */
		public String parentText() {
			return "some parent text";
		}
	}

	/**
	 * Dummy class, for tests above.
	 */
	@Loggable(value = Loggable.Level.DEBUG, prepend = true, limit = 1, unit = TimeUnit.MILLISECONDS)
	private static final class Foo extends LoggableTest.Parent {
		@Override
		public String toString() {
			return LoggableTest.RESULT;
		}

		/**
		 * Get self instance.
		 * 
		 * @return Self
		 */
		@Loggable(Loggable.Level.INFO)
		public Foo self() {
			return this;
		}

		/**
		 * Static method.
		 * 
		 * @return Some text
		 * @throws Exception
		 *             If terminated
		 */
		public static String text() throws Exception {
			TimeUnit.SECONDS.sleep(2L);
			return LoggableTest.Foo.hiddenText();
		}

		/**
		 * Method annotated with Loggable specifying explicit logger name.
		 * 
		 * @return A String
		 * @throws Exception
		 *             If terminated
		 */
		@Loggable(value = Loggable.Level.DEBUG, name = "test-logger", prepend = true)
		public static String explicitLoggerName() throws Exception {
			return LoggableTest.Foo.hiddenText();
		}

		/**
		 * Revert string.
		 * 
		 * @param text
		 *            Some text
		 * @return Reverted text
		 */
		@Loggable(value = Loggable.Level.INFO, trim = -1)
		public String revert(final String text) {
			return new StringBuffer(text).reverse().toString();
		}

		/**
		 * Method with different time unit specificaiton.
		 * 
		 * @return Some text
		 * @throws Exception
		 *             If terminated
		 */
		@Loggable(precision = 3)
		public static String logsDurationInSeconds() throws Exception {
			TimeUnit.SECONDS.sleep(2);
			return LoggableTest.Foo.hiddenText();
		}

		/**
		 * Get last char.
		 * 
		 * @param text
		 *            Text to get last char from.
		 * @return Last char.
		 */
		@Loggable(value = Loggable.Level.INFO, logThis = true)
		public String last(final String text) {
			return text.substring(text.length() - 1);
		}

		/**
		 * Private static method.
		 * 
		 * @return Some text
		 */
		private static String hiddenText() {
			return "some static text";
		}

		/**
		 * Always throw.
		 */
		@Loggable(ignore = { IOException.class, RuntimeException.class })
		private void doThrow() {
			throw new IllegalStateException();
		}
	}
}
