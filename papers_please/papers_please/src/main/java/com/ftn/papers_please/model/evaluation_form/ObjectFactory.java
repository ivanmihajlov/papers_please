//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.31 at 12:39:22 AM CEST 
//


package com.ftn.papers_please.model.evaluation_form;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.ftn.papers_please.model.evaluation_form package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.ftn.papers_please.model.evaluation_form
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EvaluationForm }
     * 
     */
    public EvaluationForm createEvaluationForm() {
        return new EvaluationForm();
    }

    /**
     * Create an instance of {@link Reviewer }
     * 
     */
    public Reviewer createReviewer() {
        return new Reviewer();
    }

    /**
     * Create an instance of {@link TPerson }
     * 
     */
    public TPerson createTPerson() {
        return new TPerson();
    }

    /**
     * Create an instance of {@link Suggestions }
     * 
     */
    public Suggestions createSuggestions() {
        return new Suggestions();
    }

    /**
     * Create an instance of {@link Suggestion }
     * 
     */
    public Suggestion createSuggestion() {
        return new Suggestion();
    }

    /**
     * Create an instance of {@link OverallRecommendation }
     * 
     */
    public OverallRecommendation createOverallRecommendation() {
        return new OverallRecommendation();
    }

    /**
     * Create an instance of {@link TScore }
     * 
     */
    public TScore createTScore() {
        return new TScore();
    }

}
