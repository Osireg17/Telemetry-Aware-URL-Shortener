package com.urlshortener.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Import the standard JUnit 5 assertions
import static org.junit.jupiter.api.Assertions.assertEquals;


class Base62ServiceTest {

	private Base62Service base62Service;

	@BeforeEach
	void setUp() {
		base62Service = new Base62Service();
	}

	@Test
	void testEncodeZero() {
		// Given the number 0
		long number = 0L;

		// When the encode method is called
		String encoded = base62Service.encode(number);

		// Then the result should be "0"
		// assertEquals uses the format: assertEquals(expected, actual)
		assertEquals("0", encoded);
	}

	@Test
	void testEncodeSingleCharacter() {
		// Given a number that maps to a single Base62 character
		long number = 10L;

		// When the encode method is called
		String encoded = base62Service.encode(number);

		// Then the result should be "a"
		assertEquals("a", encoded);
	}

	@Test
	void testEncodeMultiCharacter() {
		// Given a number that results in a multi-character string
		long number = 62L;

		// When the encode method is called
		String encoded = base62Service.encode(number);

		// Then the result should be "10"
		assertEquals("10", encoded);
	}

	@Test
	void testEncodeLargeNumber() {
		// Given a large number
		long number = 123456789L;

		// When the encode method is called
		String encoded = base62Service.encode(number);

		// Then the result should be "1ly7vk"
		assertEquals("8m0Kx", encoded);
	}

	@Test
	void testEncodeMaxValue() {
		// Given the maximum long value
		long number = Long.MAX_VALUE;

		// When the encode method is called
		String encoded = base62Service.encode(number);

		// Then the result should be "AzL8n0Y58m7"
		assertEquals("aZl8N0y58M7", encoded);
	}
}