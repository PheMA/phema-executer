package com.javapda.util;

import com.javapda.hqmf.testsupport.TestData;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ZipUtilTest {
	protected static Logger log = Logger.getLogger(ZipUtilTest.class);
	
	@Test
	public void test() {
		assertNotNull(ZipUtil.getFileNames(TestData.emeasureBundleZip()));
		assertEquals(64,ZipUtil.getFileNames(TestData.emeasureBundleZip()).size());
		if(log.isDebugEnabled()) {
			int counter=1;
			for(String f : ZipUtil.getFileNames(TestData.emeasureBundleZip())) {
				System.out.println(String.format("%2d. %s", counter++,f));
			}
		}
	}

}
