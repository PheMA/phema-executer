package com.javapda.hqmf;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.javapda.hqmf.testsupport.TestData;

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
