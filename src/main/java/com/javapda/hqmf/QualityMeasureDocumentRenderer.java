package com.javapda.hqmf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class QualityMeasureDocumentRenderer {
	protected static Logger log = Logger.getLogger(QualityMeasureDocumentRenderer.class);
	private QualityMeasureDocument qualityMeasureDocument;
	
	static final String[] renderOrder = new String[] {
		"measureType",
		"measureIdentifier",
		"nqf",
		"versionNumber",
		"versionSpecificIdentifier",
		"versionNeutralIdentifier",
		"initialPatientPopulationGuid",
		"numeratorGuid",
		"denominatorGuid",
		"denominatorExceptionsGuid",
		"denominatorExclusionsGuid",
		"raceGuid",
		"sexGuid",
		"payerGuid",
		"ethnicityGuid",
		"supplementalFields",
		"description",
	};

	public static String renderHeader() {
		return StringUtils.join(renderOrder,"|");
	}
	public String render() {
		List<String> data = new ArrayList<>();
		for(String attribute : renderOrder) {
			data.add(getAttribute(attribute)!=null?getAttribute(attribute).toString():null);

			
		}
		return StringUtils.join(data, "|");
	}
	
	public Object getAttribute(String field)
	{
		
	    // MZ: Find the correct method
	    for (Method method : qualityMeasureDocument.getClass().getMethods())
	    {
	        if ((method.getName().startsWith("get")) && (method.getName().length() == (field.length() + 3)))
	        {
	            if (method.getName().toLowerCase().endsWith(field.toLowerCase()))
	            {
	                // MZ: Method found, run it
	                try
	                {
	                    return method.invoke(qualityMeasureDocument);
	                }
	                catch (IllegalAccessException e)
	                {
	                    log.fatal("Could not determine method: " + method.getName());
	                }
	                catch (InvocationTargetException e)
	                {
	                    log.fatal("Could not determine method: " + method.getName());
	                }

	            }
	        }
	    }


	    return null;
	}	
	public QualityMeasureDocumentRenderer(QualityMeasureDocument qualityMeasureDocument2) {
		this.qualityMeasureDocument = qualityMeasureDocument2;
	}
}
