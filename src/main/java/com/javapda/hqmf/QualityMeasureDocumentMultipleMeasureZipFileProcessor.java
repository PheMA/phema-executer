package com.javapda.hqmf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.javapda.util.ZipUtil;

public class QualityMeasureDocumentMultipleMeasureZipFileProcessor extends BaseZipFileProcessor<List<QualityMeasureDocument>>{
	public QualityMeasureDocumentMultipleMeasureZipFileProcessor(File zipFile2) {
		super(zipFile2);
	}

	@Override
	public List<QualityMeasureDocument> process() {
		List<QualityMeasureDocument> qualityMeasureDocuments = new ArrayList<>();
		List<String> filenames = extracted();
		for (String zipFilenameInZipFile : filenames) {
			File temporaryLocationOfEmeasureXmlFileDirectory = generateTempEmeasureXmlFileDirectory();
			temporaryLocationOfEmeasureXmlFileDirectory.mkdirs();
			File destinationFile= new File(temporaryLocationOfEmeasureXmlFileDirectory,truncate(zipFilenameInZipFile));
			ZipUtil.getFileFromZip(zipFile, zipFilenameInZipFile, destinationFile);
			qualityMeasureDocuments.add(new QualityMeasureDocumentSingleMeasureZipFileProcessor(destinationFile).process());
			deleteDirectory(temporaryLocationOfEmeasureXmlFileDirectory);
		}
		return qualityMeasureDocuments;
	}

	private String truncate(String filename) {
		if(filename.lastIndexOf("/")>0) {
			filename = filename.substring(filename.lastIndexOf("/")+1);
		}
		return filename;
	}

	private List<String> extracted() {
		List<String> filenamesInZip = ZipUtil.getFileNames(zipFile);
		List<String> filenames = new ArrayList<>();
		for (String candidate : filenamesInZip) {
			if (candidate.endsWith(".zip")) {
				filenames.add(candidate);
			}
		}
		if(filenames.isEmpty()) {
			throw new IllegalStateException(String.format("File '%s' had %d .zip files, expected 1 or more",zipFile.getAbsolutePath(),filenames.size()));
		}
		return filenames;
	}
}
