package com.javapda.hqmf;

import com.javapda.hqmf.testsupport.TestData;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class QualityMeasureDocumentFactoryTest {

	@Test public void test() {
		assertNotNull(QualityMeasureDocumentFactory.create(TestData.xmlFileCms129v4()));
	}

	@Test
	public void testBadFile() {
		assertThrows(RuntimeException.class, () -> {
			assertNotNull(QualityMeasureDocumentFactory.create(TestData.nonExistentFile()));
		});
	}
	@Test
	public void testNullFile() {
		assertThrows(RuntimeException.class, () -> {
			assertNotNull(QualityMeasureDocumentFactory.create((File)null));
		});
	}
	@Test
	public void testNullStringXmlText() {
		assertThrows(RuntimeException.class, () -> {
			assertNotNull(QualityMeasureDocumentFactory.create((String)null));
		});
	}
}
