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
@Table(name="eventinfo")
public class SkiEvent extends BaseBean {

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
	
	@Column(name="skiday", nullable=true)
	@Temporal(TemporalType.DATE)
	private Date skiday = null;
	
	@Column(name="resort", nullable=true, length=254)
	private String resort = Constants.BLANK_STRING;
	
	@Column(name="pref", nullable=true, length=254)
	private String pref = Constants.BLANK_STRING;
	
	@Column(name="skill", nullable=true, length=254)
	private String skill = Constants.BLANK_STRING;
	
	
	
	/*
	 * Stripes has its own Error Handling (that I chose not to override)
	 * This is a hack to let invalid dates be passed as strings so that 
	 * java code can validate and send JSON errors.
	 * 
	 */
	@Transient
	private transient String sday = Constants.BLANK_STRING;
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
		
		if(StringUtils.isNotBlank(sday)) {
			//This code is part of the Stripes validation avoidance
			//When Stripes sets a bday, this code converts it into a Date for DB storage  
			try {
				sdf.setLenient(false);
				skiday = sdf.parse(sday);
			} catch(Exception e) {
				sb.append("\nSki day must be a valid date formatted like MM/DD/YYYY.");
			}
		}
		
		if(StringUtils.isNotBlank(resort) && resort.length() > 254) {
			sb.append("\nResort must not exceed 254 characters.");
		}
		
		if(StringUtils.isNotBlank(pref) && pref.length() > 254) {
			sb.append("\nEquipment preference must not exceed 254 characters.");
		}		
		
		if(StringUtils.isNotBlank(skill) && skill.length() > 254) {
			sb.append("\nSkill level must not exceed 254 characters.");
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

	public Date getSkiday() {
		return skiday;
	}

	public void setSkiday(Date skiday) {
		//This extra code is part of the Stripes validation avoidance
		//It simply set the string version (sday) to the date represented by skihday
		//This is used when a eventinfo is loaded from the DB by Hibernate
		if(skiday != null) {
			this.skiday = skiday;
			sday = sdf.format(skiday);
		}
	}

	public String getSday() {
		return sday;
	}

	public void setSday(String sday) {
		if(StringUtils.isNotBlank(sday)) {
			this.sday = sday;
		}
	}
		
    public String getResort() {
		return resort;
	}

	public void setResort(String resort) {
		this.resort = resort;
	}
	
	public String getPref() {
		return pref;
	}

	public void setPref(String pref) {
		this.pref = pref;
	}
	
	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
	}
}
