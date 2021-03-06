//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.31 at 12:51:52 AM CEST 
//


package com.ftn.papers_please.model.scientific_paper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;element name="paper_id" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="reference_author" type="{https://github.com/ivanmihajlov/papers_please}TPerson" maxOccurs="unbounded"/&gt;
 *         &lt;element name="publisher" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="year_of_publication" type="{http://www.w3.org/2001/XMLSchema}gYear"/&gt;
 *         &lt;element name="pages" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "paperId",
    "title",
    "referenceAuthor",
    "publisher",
    "yearOfPublication",
    "pages"
})
@XmlRootElement(name = "reference")
public class Reference {

    @XmlElement(name = "paper_id", required = true)
    protected String paperId;
    @XmlElement(required = true)
    protected String title;
    @XmlElement(name = "reference_author", required = true)
    protected List<TPerson> referenceAuthor;
    @XmlElement(required = true)
    protected String publisher;
    @XmlElement(name = "year_of_publication", required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter2 .class)
    @XmlSchemaType(name = "gYear")
    protected Date yearOfPublication;
    @XmlElement(required = true)
    protected String pages;

    /**
     * Gets the value of the paperId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaperId() {
        return paperId;
    }

    /**
     * Sets the value of the paperId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaperId(String value) {
        this.paperId = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the referenceAuthor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceAuthor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceAuthor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TPerson }
     * 
     * 
     */
    public List<TPerson> getReferenceAuthor() {
        if (referenceAuthor == null) {
            referenceAuthor = new ArrayList<TPerson>();
        }
        return this.referenceAuthor;
    }

    /**
     * Gets the value of the publisher property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * Sets the value of the publisher property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublisher(String value) {
        this.publisher = value;
    }

    /**
     * Gets the value of the yearOfPublication property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getYearOfPublication() {
        return yearOfPublication;
    }

    /**
     * Sets the value of the yearOfPublication property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYearOfPublication(Date value) {
        this.yearOfPublication = value;
    }

    /**
     * Gets the value of the pages property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPages() {
        return pages;
    }

    /**
     * Sets the value of the pages property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPages(String value) {
        this.pages = value;
    }

}
