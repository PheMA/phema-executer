package com.javapda.hqmf;

import com.javapda.hqmf.parser.groovy.QualityMeasureDocumentImpl;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;


public class QualityMeasureDocumentFactory {
	public static QualityMeasureDocument create(String xml) {
		return new QualityMeasureDocumentImpl(xml);
	}
	
	public static QualityMeasureDocument create(File xmlFile) {
		if(xmlFile==null) throw new IllegalArgumentException("Must provide file (not null)");
		if(!xmlFile.exists()) throw new IllegalArgumentException(String.format("File '%s' does not exist.",xmlFile.getAbsolutePath()));
		return buildQualityMeasureDocument(xmlFile);
	}
	
	private static QualityMeasureDocument buildQualityMeasureDocument(
			File xmlFile) {
		QualityMeasureDocument qualityMeasureDocument = null;
		try {
			qualityMeasureDocument = create(FileUtils.readFileToString(xmlFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return qualityMeasureDocument;
	}
}
