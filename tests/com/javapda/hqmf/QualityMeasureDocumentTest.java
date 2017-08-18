package com.javapda.hqmf;

import com.javapda.hqmf.testsupport.TestData;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

//import org.junit.Before;

public class QualityMeasureDocumentTest {
	protected static Logger log = Logger
			.getLogger(QualityMeasureDocumentTest.class);	

	private QualityMeasureDocument qualityMeasureDocument;

	@BeforeEach
	public void setup() {
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
