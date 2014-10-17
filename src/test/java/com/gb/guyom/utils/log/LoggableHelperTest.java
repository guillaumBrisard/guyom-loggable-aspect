package com.gb.guyom.utils.log;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class LoggableHelperTest {

	private LoggableHelper loggableHelper;

	@Before
	public void setup() {
		loggableHelper = new LoggableHelper();
	}

	/**
	 * LoggableHelper can build a string from an object.
	 * 
	 * @throws Exception
	 *             If something goes wrong
	 */
	@Test
	public void buildsTextFromObject() throws Exception {
		final Object[][] pairs = new Object[][] { new Object[] { 1, "1" }, new Object[] { 1.43f, "1.43" },
				new Object[] { "\u20ac-plain", "'\u20ac-plain'" }, new Object[] { "test ", "'test '" },
				new Object[] { null, "NULL" }, new Object[] { new String[0], "[]" },
				new Object[] { new String[] { "abc", "x" }, "['abc', 'x']" },
				new Object[] { new Object[] { null, 5 }, "[NULL, 5]" }, };
		for (final Object[] pair : pairs) {
			MatcherAssert.assertThat(loggableHelper.toText(pair[0], -1, false), Matchers.equalTo(pair[1].toString()));
		}
	}

	/**
	 * LoggableHelper can handle toxic objects gracefully.
	 * 
	 * @throws Exception
	 *             If something goes wrong
	 */
	@Test
	public void handlesToxicObjectsGracefully() throws Exception {
		MatcherAssert.assertThat(
				loggableHelper.toText(new Object() {
					@Override
					public String toString() {
						throw new IllegalArgumentException("boom");
					}
				}, 100, false),
				Matchers.equalTo("[com.gb.guyom.utils.log.LoggableHelperTest$1 thrown java.lang.IllegalArgumentException(boom)]"));
	}
}
