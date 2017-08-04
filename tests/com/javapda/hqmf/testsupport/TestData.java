package com.javapda.hqmf.testsupport;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import com.javapda.hqmf.QualityMeasureDocument;
import com.javapda.hqmf.QualityMeasureDocumentFactory;

public class TestData {
	public static File xmlFilesDirectory() {
		File dir = new File("tests/resources/xmlfiles");
		assertNotNull(dir);
		assertTrue(dir.exists());
		assertTrue(dir.canRead());
		return dir;
	}
	
	public static File emeasureZipCms130v3() {
		File file = new File(xmlFilesDirectory(),"EP_CMS130v3_NQF0034_Colorectal_Cancer_Screen.zip");
		assertNotNull(file);
		assertTrue(file.exists());
		return file;
	}

	
	public static File xmlFileCms179v3() {
		File file = new File(xmlFilesDirectory(),"CMS179v3.xml");
		assertNotNull(file);
		assertTrue(file.exists());
		return file;
	}
	public static File xmlFileCms129v4() {
		File file = new File(xmlFilesDirectory(),"CMS129v4.xml");
		assertNotNull(file);
		assertTrue(file.exists());
		return file;
	}
	public static File xmlFileCms69v4() {
		File file = new File(xmlFilesDirectory(),"CMS69v3.xml");
		assertNotNull(file);
		assertTrue(file.exists());
		return file;
	}

	public static File nonExistentFile() {
		return new File("non-existent-file-here");
	}

	public static File emeasureBundleZip() {
		File file = new File(xmlFilesDirectory(),"2014_eCQM_EligibleProfessional_July2014.zip");
		//File file = new File(xmlFilesDirectory(),"EMeasureZipBundle.zip");
		assertNotNull(file);
		assertTrue(file.exists());
		return file;
	}

	public static QualityMeasureDocument qualityMeasureDocument() {
		return QualityMeasureDocumentFactory.create(xmlFileCms129v4());
	}
	
}
