package com.javapda.hqmf;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.javapda.hqmf.testsupport.TestData;


public class QualityMeasureDocumentFactoryTest {

	@Test public void test() {
		assertNotNull(QualityMeasureDocumentFactory.create(TestData.xmlFileCms129v4()));
	}
	@Test(expected=RuntimeException.class) 
	public void testBadFile() {
		assertNotNull(QualityMeasureDocumentFactory.create(TestData.nonExistentFile()));
	}
	@Test(expected=RuntimeException.class) 
	public void testNullFile() {
		assertNotNull(QualityMeasureDocumentFactory.create((File)null));
	}
	@Test(expected=RuntimeException.class) 
	public void testNullStringXmlText() {
		assertNotNull(QualityMeasureDocumentFactory.create((String)null));
	}
}
