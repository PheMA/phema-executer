package org.phema.executer.hqmf;

import org.phema.executer.hqmf.v2.Document;

/**
 * Created by Luke Rasmussen on 8/19/17.
 */
public class Parser {
    public static final String HQMF_VERSION_1 = "1.0";
    public static final String HQMF_VERSION_2 = "2.0";

    public IDocument parse(String xml) throws Exception {
        IDocument document = new Document(xml, false);
        return document;
    }
}
