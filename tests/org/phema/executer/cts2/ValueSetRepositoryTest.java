package org.phema.executer.cts2;

import org.junit.jupiter.api.Test;
import org.phema.executer.valueSets.models.ValueSet;
import org.phema.executer.util.HttpHelper;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Document;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
class ValueSetRepositoryTest {

    class TestHttpHelper extends HttpHelper {
        public String nextResponse = "";

        TestHttpHelper() {
            super(false);
        }

        public Document PostXml(URI uri, Document message) throws Exception {
            return XmlHelpers.loadXMLFromString(nextResponse);
        }

        public Document GetXml(URI uri) throws Exception {
            return XmlHelpers.loadXMLFromString(nextResponse);
        }
    }

    @Test
    void initialize() throws Exception {
        TestHttpHelper httpHelper = new TestHttpHelper();
        ValueSetRepository repository = new ValueSetRepository(httpHelper);
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(ValueSetRepository.Parameters.BaseUri, "http://test");
        repository.initialize(parameters);
        assertEquals("http://test", repository.getBaseUri().toString());
    }

    ValueSetRepository initializeTestRepository(String response) throws Exception {
        TestHttpHelper httpHelper = new TestHttpHelper();
        ValueSetRepository repository = new ValueSetRepository(httpHelper);
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(ValueSetRepository.Parameters.BaseUri, "http://test");
        repository.initialize(parameters);
        httpHelper.nextResponse = response;
        return repository;
    }

    @Test
    void search_noResults() throws Exception {
        ValueSetRepository repository = initializeTestRepository("<ValueSetCatalogEntryDirectory\n" +
                "    xmlns=\"http://www.omg.org/spec/CTS2/1.1/ValueSet\"\n" +
                "    xmlns:core=\"http://www.omg.org/spec/CTS2/1.1/Core\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:schemaLocation=\"http://www.omg.org/spec/CTS2/1.1/ValueSet http://informatics.mayo.edu/cts2/spec/CTS2/1.1/valueset/ValueSet.xsd\"\n" +
                "    complete=\"COMPLETE\" numEntries=\"0\">\n" +
                "    <core:heading>\n" +
                "        <core:resourceRoot>http://phema:8080/value-sets/</core:resourceRoot>\n" +
                "        <core:resourceURI>valuesets</core:resourceURI>\n" +
                "        <core:parameter arg=\"matchvalue\">\n" +
                "            <core:val>blah</core:val>\n" +
                "        </core:parameter>\n" +
                "        <core:accessDate>2017-02-06T01:42:53.376-06:00</core:accessDate>\n" +
                "    </core:heading>\n" +
                "</ValueSetCatalogEntryDirectory>");
        ArrayList<ValueSet> result = repository.search("blah");
        assertEquals(0, result.size());
    }

    @Test
    void search() throws Exception {
        ValueSetRepository repository = initializeTestRepository("<ValueSetCatalogEntryDirectory\n" +
                "    xmlns=\"http://www.omg.org/spec/CTS2/1.1/ValueSet\"\n" +
                "    xmlns:core=\"http://www.omg.org/spec/CTS2/1.1/Core\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:schemaLocation=\"http://www.omg.org/spec/CTS2/1.1/ValueSet http://informatics.mayo.edu/cts2/spec/CTS2/1.1/valueset/ValueSet.xsd\"\n" +
                "    complete=\"COMPLETE\" numEntries=\"2\">\n" +
                "    <core:heading>\n" +
                "        <core:resourceRoot>http://phema:8080/value-sets/</core:resourceRoot>\n" +
                "        <core:resourceURI>valuesets</core:resourceURI>\n" +
                "        <core:parameter arg=\"matchvalue\">\n" +
                "            <core:val>test</core:val>\n" +
                "        </core:parameter>\n" +
                "        <core:accessDate>2017-02-06T01:35:08.208-06:00</core:accessDate>\n" +
                "    </core:heading>\n" +
                "    <entry\n" +
                "        href=\"http://phema:8080/value-sets/valueset/2.16.840.1.113883.3.1427.10000.3e006bb30cf84da1b0dc056fb0c510ae\"\n" +
                "        about=\"2.16.840.1.113883.3.1427.10000.3e006bb30cf84da1b0dc056fb0c510ae\"\n" +
                "        formalName=\"Tests\" valueSetName=\"2.16.840.1.113883.3.1427.10000.3e006bb30cf84da1b0dc056fb0c510ae\">\n" +
                "        <currentDefinition>\n" +
                "            <core:valueSetDefinition\n" +
                "                uri=\"e33213a0-e801-11e6-99ca-c3b0994d685e\" href=\"http://phema:8080/value-sets/valueset/2.16.840.1.113883.3.1427.10000.3e006bb30cf84da1b0dc056fb0c510ae/definition/e33213a0-e801-11e6-99ca-c3b0994d685e\">e33213a0-e801-11e6-99ca-c3b0994d685e</core:valueSetDefinition>\n" +
                "            <core:valueSet\n" +
                "                uri=\"2.16.840.1.113883.3.1427.10000.3e006bb30cf84da1b0dc056fb0c510ae\" href=\"http://phema:8080/value-sets/valueset/2.16.840.1.113883.3.1427.10000.3e006bb30cf84da1b0dc056fb0c510ae\">2.16.840.1.113883.3.1427.10000.3e006bb30cf84da1b0dc056fb0c510ae</core:valueSet>\n" +
                "        </currentDefinition>\n" +
                "    </entry>\n" +
                "    <entry\n" +
                "        href=\"http://phema:8080/value-sets/valueset/2.16.840.1.113883.3.1427.10000.d5158d6c27b34821943881226348b4a3\"\n" +
                "        about=\"2.16.840.1.113883.3.1427.10000.d5158d6c27b34821943881226348b4a3\"\n" +
                "        formalName=\"Test - Diabetes\" valueSetName=\"2.16.840.1.113883.3.1427.10000.d5158d6c27b34821943881226348b4a3\">\n" +
                "        <currentDefinition>\n" +
                "            <core:valueSetDefinition\n" +
                "                uri=\"f78a7990-eb4f-11e6-8328-e9aaff829943\" href=\"http://phema:8080/value-sets/valueset/2.16.840.1.113883.3.1427.10000.d5158d6c27b34821943881226348b4a3/definition/f78a7990-eb4f-11e6-8328-e9aaff829943\">f78a7990-eb4f-11e6-8328-e9aaff829943</core:valueSetDefinition>\n" +
                "            <core:valueSet\n" +
                "                uri=\"2.16.840.1.113883.3.1427.10000.d5158d6c27b34821943881226348b4a3\" href=\"http://phema:8080/value-sets/valueset/2.16.840.1.113883.3.1427.10000.d5158d6c27b34821943881226348b4a3\">2.16.840.1.113883.3.1427.10000.d5158d6c27b34821943881226348b4a3</core:valueSet>\n" +
                "        </currentDefinition>\n" +
                "    </entry>\n" +
                "</ValueSetCatalogEntryDirectory>\n");
        ArrayList<ValueSet> result = repository.search("test");
        assertEquals(2, result.size());
        assertEquals("2.16.840.1.113883.3.1427.10000.3e006bb30cf84da1b0dc056fb0c510ae", result.get(0).getOid());
        assertEquals("Tests", result.get(0).getName());
        assertEquals("2.16.840.1.113883.3.1427.10000.d5158d6c27b34821943881226348b4a3", result.get(1).getOid());
        assertEquals("Test - Diabetes", result.get(1).getName());
    }

    @Test
    void getByOID() throws Exception {
        ValueSetRepository repository = initializeTestRepository("<ValueSetCatalogEntryMsg\n" +
                "    xmlns=\"http://www.omg.org/spec/CTS2/1.1/ValueSet\"\n" +
                "    xmlns:core=\"http://www.omg.org/spec/CTS2/1.1/Core\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.omg.org/spec/CTS2/1.1/ValueSet http://informatics.mayo.edu/cts2/spec/CTS2/1.1/valueset/ValueSet.xsd\">\n" +
                "    <core:heading>\n" +
                "        <core:resourceRoot>http://phema:8080/value-sets/</core:resourceRoot>\n" +
                "        <core:resourceURI>valueset/2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd</core:resourceURI>\n" +
                "        <core:accessDate>2017-02-06T02:00:45.039-06:00</core:accessDate>\n" +
                "    </core:heading>\n" +
                "    <valueSetCatalogEntry entryState=\"ACTIVE\"\n" +
                "        about=\"2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd\"\n" +
                "        formalName=\"My test value set\" valueSetName=\"2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd\">\n" +
                "        <core:status>active</core:status>\n" +
                "        <core:changeDescription changeType=\"CREATE\"\n" +
                "            committed=\"COMMITTED\"\n" +
                "            containingChangeSet=\"fb48359e-fbd5-4b3b-bfd3-5e162e1573f1\" changeDate=\"2017-01-31T16:31:55.000-06:00\">\n" +
                "            <core:changeNotes>\n" +
                "                <core:value/>\n" +
                "            </core:changeNotes>\n" +
                "        </core:changeDescription>\n" +
                "        <core:sourceAndRole>\n" +
                "            <core:source>PhEMA Authoring Tool</core:source>\n" +
                "            <core:role uri=\"http://purl.org/dc/elements/1.1/creator\">creator</core:role>\n" +
                "        </core:sourceAndRole>\n" +
                "        <core:alternateID>2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd</core:alternateID>\n" +
                "        <definitions>http://phema:8080/value-sets/valueset/2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd/definitions</definitions>\n" +
                "        <currentDefinition>\n" +
                "            <core:valueSetDefinition\n" +
                "                uri=\"1204be00-e805-11e6-96c9-c5c7a053f3e9\" href=\"http://phema:8080/value-sets/valueset/2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd/definition/1204be00-e805-11e6-96c9-c5c7a053f3e9\">1204be00-e805-11e6-96c9-c5c7a053f3e9</core:valueSetDefinition>\n" +
                "            <core:valueSet\n" +
                "                uri=\"2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd\" href=\"http://phema:8080/value-sets/valueset/2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd\">2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd</core:valueSet>\n" +
                "        </currentDefinition>\n" +
                "    </valueSetCatalogEntry>\n" +
                "</ValueSetCatalogEntryMsg>\n");

        ValueSet result = repository.getByOID("2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd");
        assertNotNull(result);
        assertEquals("2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67cd", result.getOid());
        assertEquals("My test value set", result.getName());
    }

    @Test
    void getByOID_noResult() throws Exception {
        ValueSetRepository repository = initializeTestRepository("<UnknownValueSet xmlns=\"http://www.omg.org/spec/CTS2/1.1/Exceptions\"\n" +
                "    xmlns:core=\"http://www.omg.org/spec/CTS2/1.1/Core\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.omg.org/spec/CTS2/1.1/Exceptions http://informatics.mayo.edu/cts2/spec/CTS2/1.1/core/Exceptions.xsd\">\n" +
                "    <message>\n" +
                "        <core:value>Resource with Identifier: Name: '2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67ce' not found.</core:value>\n" +
                "    </message>\n" +
                "    <severity>ERROR</severity>\n" +
                "</UnknownValueSet>");

        ValueSet result = repository.getByOID("2.16.840.1.113883.3.1427.10000.1777f99bae7e4f4c9e4eeb4eb87b67ce");
        assertNull(result);
    }
}