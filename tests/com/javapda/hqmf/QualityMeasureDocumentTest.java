package com.javapda.hqmf;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.javapda.hqmf.testsupport.TestData;

public class QualityMeasureDocumentTest {
	protected static Logger log = Logger
			.getLogger(QualityMeasureDocumentTest.class);	

	private QualityMeasureDocument qualityMeasureDocument;
	@Before public void setup() {
		initializeVariables();
	}
	

	@Test
	public void test() {
		if(log.isDebugEnabled()) {
			log.debug(qualityMeasureDocument);
		}
	}

	private void initializeVariables() {
		qualityMeasureDocument = QualityMeasureDocumentFactory.create(TestData.xmlFileCms129v4());
		assertNotNull(qualityMeasureDocument);
		
	}
	
}
