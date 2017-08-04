package com.javapda.hqmf;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.javapda.hqmf.testsupport.TestData;

public class QualityMeasureDocumentMainTest {
	protected static Logger log = Logger
			.getLogger(QualityMeasureDocumentMainTest.class);

	@Test
	public void testEmeasureBundleZipFile() {
		String[] args = new String[] { "--emeasurebundle",
				TestData.emeasureBundleZip().getAbsolutePath() };

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// IMPORTANT: Save the old System.out!
		PrintStream originalSystemOut = System.out;
		// Tell Java to use your special stream
		System.setOut(new PrintStream(baos));
		// Print some output: goes to your special stream
		QualityMeasureDocumentMain.main(args);
		// Put things back
		System.out.flush();
		System.setOut(originalSystemOut);
		// Show what happened
		if (log.isDebugEnabled()) {
			log.debug("Here: " + baos.toString());
		}
		System.out.println(baos.toString());

	}

	@Test
	public void testEmeasureZipFile() {
		String[] args = new String[] { "--emeasurezip",
				TestData.emeasureZipCms130v3().getAbsolutePath()

		};

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// IMPORTANT: Save the old System.out!
		PrintStream originalSystemOut = System.out;
		// Tell Java to use your special stream
		System.setOut(new PrintStream(baos));
		// Print some output: goes to your special stream
		QualityMeasureDocumentMain.main(args);
		// Put things back
		System.out.flush();
		System.setOut(originalSystemOut);
		// Show what happened
		if (log.isDebugEnabled()) {
			log.debug("Here: " + baos.toString());
		}

	}

	
	
	@Test
	public void testFileProportion() {
		String[] args = new String[] { "--file",
				TestData.xmlFileCms129v4().getAbsolutePath()

		};

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// IMPORTANT: Save the old System.out!
		PrintStream originalSystemOut = System.out;
		// Tell Java to use your special stream
		System.setOut(new PrintStream(baos));
		// Print some output: goes to your special stream
		QualityMeasureDocumentMain.main(args);
		// Put things back
		System.out.flush();
		System.setOut(originalSystemOut);
		// Show what happened
		if (log.isDebugEnabled()) {
			log.debug("Here: " + baos.toString());
		}


	}

	@Test
	public void testFileContinuousVariable() {
		String[] args = new String[] { "--file",
				TestData.xmlFileCms179v3().getAbsolutePath()

		};

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// IMPORTANT: Save the old System.out!
		PrintStream originalSystemOut = System.out;
		// Tell Java to use your special stream
		System.setOut(new PrintStream(baos));
		// Print some output: goes to your special stream
		QualityMeasureDocumentMain.main(args);
		// Put things back
		System.out.flush();
		System.setOut(originalSystemOut);
		// Show what happened
		if (log.isDebugEnabled()) {
			log.debug("Here: " + baos.toString());
		}


	}

}
