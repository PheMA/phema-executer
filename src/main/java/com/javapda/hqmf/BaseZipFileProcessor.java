package com.javapda.hqmf;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

import com.javapda.util.ZipUtil;

public abstract class BaseZipFileProcessor<T> {
	protected static Logger log = Logger.getLogger(BaseZipFileProcessor.class);
	protected File zipFile;
	public BaseZipFileProcessor(File zipFile2) {
		this.zipFile = zipFile2;
		validate();
	}
	private void validate() {
		if(zipFile==null) {
			throw new IllegalArgumentException(String.format("Null not accepted for ZipFile"));
		}
		if(!zipFile.exists()) {
			throw new IllegalArgumentException(String.format("Zip File '%s' does not exist",zipFile.getAbsolutePath()));
		}
		if(!ZipUtil.isZipFile(zipFile)) {
			throw new IllegalArgumentException(String.format("File '%s' is NOT a zip file",zipFile.getAbsolutePath()));
		}
		
	}
	public abstract T process();

	protected File generateTempEmeasureXmlFileDirectory() {
		File temporaryLocationOfEmeasureXmlFiles = new File(FileUtils.getTempDirectory(),
				String.format("_zipTemp-%d-%s",System.currentTimeMillis(),fmtdatenow()));
		return temporaryLocationOfEmeasureXmlFiles;
	}
	protected void deleteDirectory(
			File temporaryLocationOfEmeasureXmlFileDirectory) {
		try {
			FileUtils.deleteDirectory(temporaryLocationOfEmeasureXmlFileDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected String fmtdatenow() {
		return new SimpleDateFormat("yyyyMMdd-HHmm-ss.SSS").format(new Date())+RandomUtils.nextInt(3400);
	}
}
