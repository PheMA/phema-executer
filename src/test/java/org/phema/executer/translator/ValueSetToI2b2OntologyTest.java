package org.phema.executer.translator;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.phema.executer.exception.PhemaUserException;
import org.phema.executer.i2b2.OntologyService;
import org.phema.executer.models.i2b2.Concept;
import org.phema.executer.models.i2b2.I2b2TerminologyRule;
import org.phema.executer.valueSets.models.Member;
import org.phema.executer.valueSets.models.ValueSet;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Created by Luke Rasmussen on 8/14/18.
 */
@RunWith(JUnitPlatform.class)
public class ValueSetToI2b2OntologyTest {
    @Test
    void translate_EmptyRulesTest() throws PhemaUserException {
        OntologyService ontService = mock(OntologyService.class);
        ArrayList<Concept> returnedConcepts = new ArrayList<Concept>();
        when(ontService.getCodeInfo(anyString())).thenReturn(returnedConcepts);

        ValueSetToI2b2Ontology translator = new ValueSetToI2b2Ontology(null);
        translator.setOntologyService(ontService);
        ValueSet valueSet = new ValueSet();
        valueSet.addMember(new Member("1.2.3", "Test", "TESTING", "", ""));
        ValueSetToI2b2Ontology.TranslationResult result = translator.translate(valueSet);
        assertNotNull(result);
    }

    @Test
    void translate_terminologyNameMappingTest() throws PhemaUserException {
        OntologyService ontService = mock(OntologyService.class);
        ArrayList<Concept> returnedConcepts = new ArrayList<Concept>();
        returnedConcepts.add(new Concept("TEST", "Test", "I-C-D:250.0", 5, "", false, ""));
        when(ontService.getCodeInfo("I-C-D:250.0")).thenReturn(returnedConcepts);

        ValueSetToI2b2Ontology translator = new ValueSetToI2b2Ontology(null);
        translator.addRule(new I2b2TerminologyRule("ICD9", "I-C-D", ":", null, null, null));
        translator.setOntologyService(ontService);
        ValueSet valueSet = new ValueSet();
        Member member = new Member("250.0", "Test", "ICD9", "", "");
        valueSet.addMember(member);
        ValueSetToI2b2Ontology.TranslationResult result = translator.translate(valueSet);
        assertNotNull(result);
        assertEquals(1, result.MappedMembers.get(member).size());
        assertEquals("I-C-D:250.0", result.MappedMembers.get(member).get(0).getBaseCode());
    }

    @Test
    void translate_delimiterTest() throws PhemaUserException {
        OntologyService ontService = mock(OntologyService.class);
        ArrayList<Concept> returnedConcepts = new ArrayList<Concept>();
        returnedConcepts.add(new Concept("TEST", "Test", "ICD9|250.0", 5, "", false, ""));
        when(ontService.getCodeInfo("ICD9|250.0")).thenReturn(returnedConcepts);

        ValueSetToI2b2Ontology translator = new ValueSetToI2b2Ontology(null);
        translator.addRule(new I2b2TerminologyRule("ICD9", "ICD9", "|", null, null, null));
        translator.setOntologyService(ontService);
        ValueSet valueSet = new ValueSet();
        Member member = new Member("250.0", "Test", "ICD9", "", "");
        valueSet.addMember(member);
        ValueSetToI2b2Ontology.TranslationResult result = translator.translate(valueSet);
        assertNotNull(result);
        assertEquals(1, result.MappedMembers.get(member).size());
        assertEquals("ICD9|250.0", result.MappedMembers.get(member).get(0).getBaseCode());
    }

    @Test
    void translate_termTranslationTest() throws PhemaUserException {
        OntologyService ontService = mock(OntologyService.class);
        ArrayList<Concept> returnedConcepts = new ArrayList<Concept>();
        returnedConcepts.add(new Concept("TEST", "Test", "ICD:250.0-TEST", 5, "", false, ""));
        when(ontService.getCodeInfo("ICD:250.0-TEST")).thenReturn(returnedConcepts);

        ValueSetToI2b2Ontology translator = new ValueSetToI2b2Ontology(null);
        translator.addRule(new I2b2TerminologyRule("ICD9", "ICD", ":", "(.*)", "$1-TEST", null));
        translator.setOntologyService(ontService);
        ValueSet valueSet = new ValueSet();
        Member member = new Member("250.0", "Test", "ICD9", "", "");
        valueSet.addMember(member);
        ValueSetToI2b2Ontology.TranslationResult result = translator.translate(valueSet);
        assertNotNull(result);
        assertEquals(1, result.MappedMembers.get(member).size());
        assertEquals("ICD:250.0-TEST", result.MappedMembers.get(member).get(0).getBaseCode());
    }

    @Test
    void translate_filterPath_includeTest() throws PhemaUserException {
        OntologyService ontService = mock(OntologyService.class);
        ArrayList<Concept> returnedConcepts = new ArrayList<Concept>();
        returnedConcepts.add(new Concept("\\\\TEST\\Test\\", "Test", "ICD:250.0-TEST", 5, "", false, ""));
        when(ontService.getCodeInfo("ICD:250.0-TEST")).thenReturn(returnedConcepts);

        ValueSetToI2b2Ontology translator = new ValueSetToI2b2Ontology(null);
        translator.addRule(new I2b2TerminologyRule("ICD9", "ICD", ":", "(.*)", "$1-TEST", "\\\\TEST"));
        translator.setOntologyService(ontService);
        ValueSet valueSet = new ValueSet();
        Member member = new Member("250.0", "Test", "ICD9", "", "");
        valueSet.addMember(member);
        ValueSetToI2b2Ontology.TranslationResult result = translator.translate(valueSet);
        assertNotNull(result);
        assertEquals(1, result.MappedMembers.get(member).size());
        assertEquals("ICD:250.0-TEST", result.MappedMembers.get(member).get(0).getBaseCode());
        assertEquals(0, result.FilteredOutMembers.size());
    }

    @Test
    void translate_filterPath_excludeTest() throws PhemaUserException {
        OntologyService ontService = mock(OntologyService.class);
        ArrayList<Concept> returnedConcepts = new ArrayList<Concept>();
        returnedConcepts.add(new Concept("\\\\TEST\\Test\\", "Test", "ICD:250.0-TEST", 5, "", false, ""));
        when(ontService.getCodeInfo("ICD:250.0-TEST")).thenReturn(returnedConcepts);

        ValueSetToI2b2Ontology translator = new ValueSetToI2b2Ontology(null);
        translator.addRule(new I2b2TerminologyRule("ICD9", "ICD", ":", "(.*)", "$1-TEST", "\\\\NOT-TEST"));
        translator.setOntologyService(ontService);
        ValueSet valueSet = new ValueSet();
        Member member = new Member("250.0", "Test", "ICD9", "", "");
        valueSet.addMember(member);
        ValueSetToI2b2Ontology.TranslationResult result = translator.translate(valueSet);
        assertNotNull(result);
        assertEquals(0, result.MappedMembers.size());
        assertEquals(1, result.FilteredOutMembers.size());
        assertEquals("ICD:250.0-TEST", result.FilteredOutMembers.get(member).get(0).getBaseCode());
    }

    @Test
    void translate_termTranslation_MultipleMatchingRules() throws PhemaUserException {
        OntologyService ontService = mock(OntologyService.class);
        ArrayList<Concept> returnedConcepts = new ArrayList<Concept>();
        returnedConcepts.add(new Concept("TEST", "Test 1", "ICD:250.0-TEST1", 5, "", false, ""));
        returnedConcepts.add(new Concept("TEST", "Test 2", "I|250.0-TEST2", 5, "", false, ""));
        when(ontService.getCodeInfo("ICD:250.0-TEST1")).thenReturn(new ArrayList<Concept>(returnedConcepts.subList(0,1)));
        when(ontService.getCodeInfo("I|250.0-TEST2")).thenReturn(new ArrayList<Concept>(returnedConcepts.subList(1,2)));

        // Here we have multiple matching rules.  This lets us verify that we can have
        // several rules that do some type of translation (e.g., add a suffix) and return
        // multiple results
        ValueSetToI2b2Ontology translator = new ValueSetToI2b2Ontology(null);
        translator.addRule(new I2b2TerminologyRule("ICD9", "ICD", ":", "(.*)", "$1-TEST1", null));
        translator.addRule(new I2b2TerminologyRule("ICD9", "I", "|", "(.*)", "$1-TEST2", null));
        translator.setOntologyService(ontService);
        ValueSet valueSet = new ValueSet();
        Member member = new Member("250.0", "Test", "ICD9", "", "");
        valueSet.addMember(member);
        ValueSetToI2b2Ontology.TranslationResult result = translator.translate(valueSet);
        assertNotNull(result);
        assertEquals(2, result.MappedMembers.get(member).size());
        assertEquals(2, result.DistinctMappedConcepts.size());
        assertEquals("ICD:250.0-TEST1", result.MappedMembers.get(member).get(0).getBaseCode());
        assertEquals("I|250.0-TEST2", result.MappedMembers.get(member).get(1).getBaseCode());
        assertEquals("ICD:250.0-TEST1", result.DistinctMappedConcepts.get(0).getBaseCode());
        assertEquals("I|250.0-TEST2", result.DistinctMappedConcepts.get(1).getBaseCode());
    }

    @Test
    void translate_termTranslation_MultipleMatchingRules_WithExclusions() throws PhemaUserException {
        OntologyService ontService = mock(OntologyService.class);
        ArrayList<Concept> returnedConcepts = new ArrayList<Concept>();
        returnedConcepts.add(new Concept("\\\\TEST\\Test1\\", "Test 1", "ICD:250.0-TEST1", 5, "", false, ""));
        returnedConcepts.add(new Concept("\\\\TEST\\Test1\\", "Test 1", "ICD:250.0-TEST1", 5, "", true, ""));
        returnedConcepts.add(new Concept("\\\\TEST\\Test2\\", "Test 2", "I|250.0-TEST2", 5, "", false, ""));
        returnedConcepts.add(new Concept("\\\\TEST\\Test2\\", "Test 2", "I|250.0-TEST2", 5, "", true, ""));
        returnedConcepts.add(new Concept("\\\\OTHER\\Test2\\", "Test 3", "I|250.0-TEST2", 5, "", false, ""));
        returnedConcepts.add(new Concept("\\\\OTHER\\Test2\\", "Test 3", "I|250.0-TEST2", 5, "", true, ""));
        when(ontService.getCodeInfo("ICD:250.0-TEST1")).thenReturn(new ArrayList<>(returnedConcepts.subList(0,2)));
        when(ontService.getCodeInfo("I|250.0-TEST2")).thenReturn(new ArrayList<>(returnedConcepts.subList(2,6)));

        // Here we have multiple matching rules.  This lets us verify that we can have
        // several rules that do some type of translation (e.g., add a suffix) and return
        // multiple results.  This differs from the previous test in that we have
        // duplicate codes present (making sure we get distinct back), and we filter out
        // results based on the path.
        ValueSetToI2b2Ontology translator = new ValueSetToI2b2Ontology(null);
        translator.addRule(new I2b2TerminologyRule("ICD9", "ICD", ":", "(.*)", "$1-TEST1", "\\\\TEST\\"));
        translator.addRule(new I2b2TerminologyRule("ICD9", "I", "|", "(.*)", "$1-TEST2", "\\\\TEST\\"));
        translator.setOntologyService(ontService);
        ValueSet valueSet = new ValueSet();
        Member member = new Member("250.0", "Test", "ICD9", "", "");
        valueSet.addMember(member);
        ValueSetToI2b2Ontology.TranslationResult result = translator.translate(valueSet);
        assertNotNull(result);
        assertEquals(2, result.MappedMembers.get(member).size());
        assertEquals(2, result.DistinctMappedConcepts.size());
        assertEquals(1, result.FilteredOutMembers.size());
        assertEquals("ICD:250.0-TEST1", result.MappedMembers.get(member).get(0).getBaseCode());
        assertEquals("I|250.0-TEST2", result.MappedMembers.get(member).get(1).getBaseCode());
        assertEquals("ICD:250.0-TEST1", result.DistinctMappedConcepts.get(0).getBaseCode());
        assertEquals("I|250.0-TEST2", result.DistinctMappedConcepts.get(1).getBaseCode());
        assertEquals("I|250.0-TEST2", result.FilteredOutMembers.get(member).get(0).getBaseCode());
    }
}