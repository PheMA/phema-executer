package com.javapda.hqmf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.javapda.util.ZipUtil;

public class QualityMeasureDocumentSingleMeasureZipFileProcessor extends BaseZipFileProcessor<QualityMeasureDocument>{
	public QualityMeasureDocumentSingleMeasureZipFileProcessor(File zipFile2) {
		super(zipFile2);
	}
	public QualityMeasureDocument process() {
		String filename = extracted();
		File temporaryLocationOfEmeasureXmlFileDirectory = generateTempEmeasureXmlFileDirectory();
		temporaryLocationOfEmeasureXmlFileDirectory.mkdirs();
		File destinationFile= new File(temporaryLocationOfEmeasureXmlFileDirectory,filename);
		ZipUtil.getFileFromZip(zipFile, filename,destinationFile);
		if(log.isDebugEnabled()) {
			log.debug("destinationFile: " + destinationFile);
		}
		QualityMeasureDocument qualityMeasureDocument = QualityMeasureDocumentFactory.create(destinationFile);
		deleteDirectory(temporaryLocationOfEmeasureXmlFileDirectory);
		return qualityMeasureDocument;
	}
	private String extracted() {
		List<String> filenamesInZip = ZipUtil.getFileNames(zipFile);
		List<String> filenames = new ArrayList<>();
		for (String candidate : filenamesInZip) {
			if (candidate.endsWith(".xml")) {
				filenames.add(candidate);
			}
		}
		if(filenames.size()!=1) {
			throw new IllegalStateException(String.format("File '%s' had %d .xml files, expected only 1",zipFile.getAbsolutePath(),filenames.size()));
		}
		return filenames.iterator().next();
	}
}
