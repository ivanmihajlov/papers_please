//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.31 at 12:39:22 AM CEST 
//


package com.ftn.papers_please.model.evaluation_form;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{https://github.com/ivanmihajlov/papers_please}reviewer"/&gt;
 *         &lt;element name="scientific_paper_title"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;minLength value="0"/&gt;
 *               &lt;maxLength value="100"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="scientific_paper_summary"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;minLength value="0"/&gt;
 *               &lt;maxLength value="400"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{https://github.com/ivanmihajlov/papers_please}suggestions"/&gt;
 *         &lt;element ref="{https://github.com/ivanmihajlov/papers_please}overall_recommendation"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="reviewer_id" type="{http://www.w3.org/2001/XMLSchema}date" /&gt;
 *       &lt;attribute name="scientific_paper_id" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "reviewer",
    "scientificPaperTitle",
    "scientificPaperSummary",
    "suggestions",
    "overallRecommendation"
})
@XmlRootElement(name = "evaluation_form")
public class EvaluationForm {

    @XmlElement(required = true)
    protected Reviewer reviewer;
    @XmlElement(name = "scientific_paper_title", required = true)
    protected String scientificPaperTitle;
    @XmlElement(name = "scientific_paper_summary", required = true)
    protected String scientificPaperSummary;
    @XmlElement(required = true)
    protected Suggestions suggestions;
    @XmlElement(name = "overall_recommendation", required = true)
    protected OverallRecommendation overallRecommendation;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "reviewer_id")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar reviewerId;
    @XmlAttribute(name = "scientific_paper_id")
    protected String scientificPaperId;

    /**
     * Gets the value of the reviewer property.
     * 
     * @return
     *     possible object is
     *     {@link Reviewer }
     *     
     */
    public Reviewer getReviewer() {
        return reviewer;
    }

    /**
     * Sets the value of the reviewer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Reviewer }
     *     
     */
    public void setReviewer(Reviewer value) {
        this.reviewer = value;
    }

    /**
     * Gets the value of the scientificPaperTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScientificPaperTitle() {
        return scientificPaperTitle;
    }

    /**
     * Sets the value of the scientificPaperTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScientificPaperTitle(String value) {
        this.scientificPaperTitle = value;
    }

    /**
     * Gets the value of the scientificPaperSummary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScientificPaperSummary() {
        return scientificPaperSummary;
    }

    /**
     * Sets the value of the scientificPaperSummary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScientificPaperSummary(String value) {
        this.scientificPaperSummary = value;
    }

    /**
     * Gets the value of the suggestions property.
     * 
     * @return
     *     possible object is
     *     {@link Suggestions }
     *     
     */
    public Suggestions getSuggestions() {
        return suggestions;
    }

    /**
     * Sets the value of the suggestions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Suggestions }
     *     
     */
    public void setSuggestions(Suggestions value) {
        this.suggestions = value;
    }

    /**
     * Gets the value of the overallRecommendation property.
     * 
     * @return
     *     possible object is
     *     {@link OverallRecommendation }
     *     
     */
    public OverallRecommendation getOverallRecommendation() {
        return overallRecommendation;
    }

    /**
     * Sets the value of the overallRecommendation property.
     * 
     * @param value
     *     allowed object is
     *     {@link OverallRecommendation }
     *     
     */
    public void setOverallRecommendation(OverallRecommendation value) {
        this.overallRecommendation = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the reviewerId property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getReviewerId() {
        return reviewerId;
    }

    /**
     * Sets the value of the reviewerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setReviewerId(XMLGregorianCalendar value) {
        this.reviewerId = value;
    }

    /**
     * Gets the value of the scientificPaperId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScientificPaperId() {
        return scientificPaperId;
    }

    /**
     * Sets the value of the scientificPaperId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScientificPaperId(String value) {
        this.scientificPaperId = value;
    }

}