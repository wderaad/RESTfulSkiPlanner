package org.bshouse.wsdb.beans;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.bshouse.wsdb.common.Constants;

/*
 * 
 * This is the object use by Hibernate for DB persistance
 * 
 */

@Entity
@Table(name="contact")
public class Contact extends BaseBean {

	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id = -1L;
	
	@Column(name="first", nullable=false, length=254)
	private String nameFirst = Constants.BLANK_STRING;
	
	@Column(name="last", nullable=true, length=254)
	private String nameLast = Constants.BLANK_STRING;
	
	@Column(name="cell", nullable=true, length=30)
	private String numberCell = Constants.BLANK_STRING;
	
	@Column(name="email", nullable=true, length=254)
	private String email = Constants.BLANK_STRING;
	
	@Column(name="birthday", nullable=true)
	@Temporal(TemporalType.DATE)
	private Date birthday = null;
	
	
	
	/*
	 * Stripes has its own Error Handling (that I chose not to override)
	 * This is a hack to let invalid dates be passed as strings so that 
	 * java code can validate and send JSON errors.
	 * 
	 */
	@Transient
	private transient String bday = Constants.BLANK_STRING;
	@Transient
	private transient SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

	public String valid() {
		//Validation code that ensures the incoming data conforms to the model
		
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isBlank(nameFirst)) {
			sb.append("\nFirst Name is a required field.");
		} else if(nameFirst.length() > 254) {
			sb.append("\nFirst Name must not exceed 254 characters.");
		}
		
		if(StringUtils.isNotBlank(nameLast) && nameLast.length() > 254) {
			sb.append("\nLast Name must not exceed 254 characters.");
		}
		
		if(StringUtils.isNotBlank(numberCell) && numberCell.length() > 30) {
			sb.append("\nPhone number must not exceed 30 characters.");
		}
		
		if(StringUtils.isNotBlank(email) && email.length() > 254) {
			sb.append("\nEmail address must not exceed 254 characters.");
		}
		
		if(StringUtils.isNotBlank(bday)) {
			//This code is part of the Stripes validation avoidance
			//When Stripes sets a bday, this code converts it into a Date for DB storage  
			try {
				sdf.setLenient(false);
				birthday = sdf.parse(bday);
			} catch(Exception e) {
				sb.append("\nBirthday must be a valid date formatted like MM/DD/YYYY.");
			}
		}
		return sb.toString();
	}
	
	
	/*
	 * Getters & Setters
	 * 
	 */
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNameFirst() {
		return nameFirst;
	}

	public void setNameFirst(String nameFirst) {
		this.nameFirst = nameFirst;
	}

	public String getNameLast() {
		return nameLast;
	}

	public void setNameLast(String nameLast) {
		this.nameLast = nameLast;
	}

	public String getNumberCell() {
		return numberCell;
	}

	public void setNumberCell(String numberCell) {
		this.numberCell = numberCell;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		//This extra code is part of the Stripes validation avoidance
		//It simply set the string version (bday) to the date represented by birthday
		//This is used when a contact is loaded from the DB by Hibernate
		if(birthday != null) {
			this.birthday = birthday;
			bday = sdf.format(birthday);
		}
	}

	public String getBday() {
		return bday;
	}

	public void setBday(String bday) {
		if(StringUtils.isNotBlank(bday)) {
			this.bday = bday;
		}
	}
}
