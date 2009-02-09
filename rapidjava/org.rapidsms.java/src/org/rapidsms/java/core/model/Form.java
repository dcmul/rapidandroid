/**
 * 
 */
package org.rapidsms.java.core.model;

import org.rapidsms.java.core.parser.service.ParsingService.ParserType;

/**
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 16, 2009
 * 
 *          Main class for form definition.
 * 
 */
public class Form {

	private int formId;
	private String formName;
	private String prefix;
	private String description;
	private ParserType parser;
	private Field[] fields;

	public Form() {
		formId = -1;
	}

	public Form(int id, String name, String prefix, String desc, Field[] fields, ParserType parse) {
		this.formId = id;
		formName = name;
		this.prefix = prefix;
		this.description = desc;
		this.fields = fields;
		this.parser = parse;

		// Log.d.out.println("Created new form: " + formName + " prefix: " +
		// prefix);
	}

	/**
	 * @return the fields
	 */
	public Field[] getFields() {
		return fields;
	}

	/**
	 * @return the formId
	 */
	public int getFormId() {
		return formId;
	}

	/**
	 * @param formId
	 *            the formId to set
	 */

	/**
	 * @return the formName
	 */
	public String getFormName() {
		return formName;
	}

	/**
	 * @param formName
	 *            the formName to set
	 */
	public void setFormName(String formName) {
		this.formName = formName;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param prefix
	 *            the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param parser
	 *            the parser to set
	 */
	public void setParserType(ParserType parser) {
		this.parser = parser;
	}

	/**
	 * @return the parser
	 */
	public ParserType getParserType() {
		return parser;
	}

	/**
	 * @param formId
	 *            the formId to set
	 */
	public void setFormId(int formId) {
		this.formId = formId;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(Field[] fields) {
		this.fields = fields;
	}
}
