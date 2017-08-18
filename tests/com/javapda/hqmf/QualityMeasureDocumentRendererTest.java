package com.javapda.hqmf;

import com.javapda.hqmf.testsupport.TestData;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class QualityMeasureDocumentRendererTest {
	protected static Logger log = Logger.getLogger(QualityMeasureDocumentRendererTest.class);
	
	@Test
	public void test() {
		assertNotNull(QualityMeasureDocumentRenderer.renderHeader());
		if(log.isDebugEnabled()) {
			log.debug(QualityMeasureDocumentRenderer.renderHeader());
			log.debug(new QualityMeasureDocumentRenderer(TestData.qualityMeasureDocument()).render());
		}
	}

}
