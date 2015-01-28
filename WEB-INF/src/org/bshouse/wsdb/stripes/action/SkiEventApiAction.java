package org.bshouse.wsdb.stripes.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bshouse.wsdb.beans.SkiEvent;
import org.bshouse.wsdb.common.Constants;
import org.bshouse.wsdb.common.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HttpCache;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

/*
 * 
 * This action class provides the RESTful Contact API
 * 
 */


@UrlBinding("/api/1.0/contact/{id}")
@HttpCache(allow=false)
public class SkiEventApiAction extends BaseAction {

	private Session db = HibernateUtil.getSession(); //Database Access
	private Gson g = new GsonBuilder().setDateFormat("MM/dd/yyyy").create(); //Java Object to JSON converter
	private Map<String,Object> json = new HashMap<String,Object>(); //HashMap to be converted to JSON
	private String id = Constants.BLANK_STRING; //Stripes put the ID from the URL here
	private SkiEvent contact; //Stripes puts the form data here
	
	
	@DefaultHandler
	public Resolution rest() {
		//When a users requests the UrlBinding, Stripes loads this action
		//Call the method that fits the need of the request
		String method = getContext().getRequest().getMethod().toUpperCase();
		if("GET".equals(method)) { //Lookup Contact
			list();
		} else if("POST".equals(method)) { //Add a new contact
			add();
		} else if("PUT".equals(method)) { //Update an existing contact
			update();
		} else if("DELETE".equals(method)) { //Delete an existing contact
			delete();
		} else {
			json.put("success",false);
			json.put("message", "Unsupported method requested: "+method);
		}
		db.close(); //Close the Database
		//Return anything loaded into the "json" HashMap as JSON text
		return new StreamingResolution("application/json",g.toJson(json));
	}
	
	/*
	 * 
	 * Handles requests to list 1 or all Contacts
	 * 
	 */
	private void list() {		
		//Create a Hibernate Criteria Query for a "Contact" in the database
		Criteria c = db.createCriteria(SkiEvent.class);
		if(StringUtils.isNotBlank(id)) {
			//Load the requested contact
			c.add(Restrictions.eq("id", Long.parseLong(id)));
		}
		//Based on the Criteria, List all matches and cast it to a typed list
		List<SkiEvent> cl = HibernateUtil.castList(SkiEvent.class, c.list());
		
		json.put("success",true); //Tell the JavaScript we were successful
		json.put("data", cl); //Add the List of Contacts
	}
	
	/*
	 * 
	 * Handles request to add new Contacts
	 * 
	 */
	public void add() {
		//Add a Contact
		if(contact != null) {
			//Stripes put something in the Contact object
			if(contact.valid().length() == 0) {
				//Validation returns no errors
				if(contact.getId() == -1L) {
					//The default ID is set
					db.save(contact); //Add the contact
					json.put("success",true);
					json.put("data", contact); //Return the now saved contact
				} else {
					//Attempted to Add a contact that already exists
					json.put("success",false);
					json.put("message", "Contact already exists");
				}
			} else {
				//The provided information did not fit the model
				json.put("success",false);
				json.put("message", contact.valid()); //Return validation failure messages
			}
			
		} else {
			//No form data was submitted
			json.put("success",false);
			json.put("message", "Add failed because required data is missing");
		}
	}
	
	/*
	 * Handles requests to update an existing Contact
	 */
	public void update() {
		
		
		if(contact != null && id != null && contact.getId().equals(Long.parseLong(id))) {
			//We got a Contact, that was PUT to a URL with an ID number and the Contact.id matches the URL ID
			if(contact.valid().length() == 0) {
				//No validation errors
				if(contact.getId() > -1L) {
					//The ID is not the default value
					db.update(contact); //Update DB
					db.flush(); //Commit change
					json.put("success",true);
					json.put("data", contact); //return update Contact
				} else {
					//Attempted to Update a new Contact (should be added instead)
					json.put("success",false);
					json.put("message", "Contact does not exist");
				}
			} else {
				//Validation problem
				json.put("success",false);
				json.put("message", contact.valid()); //Include validation errors
			}
			
		} else {
			//No form data or URL ID & Contact.id do not match
			json.put("success",false);
			json.put("message", "Update failed because required data is missing");
		}
	}
	
	
	/*
	 * 
	 * Handles requests to delete a Contact
	 * 
	 */
	public void delete() {
		if(StringUtils.isNotBlank(id) && StringUtils.isNumeric(id)) {
			//The URL ID is present and is a number
			Long cid = Long.parseLong(id); //Convert URL ID to a Long
			Criteria c = db.createCriteria(SkiEvent.class); //Create a Criteria Query on Contact
			c.add(Restrictions.eq("id", cid)); //Restrict the Query to the requested ID
			List<SkiEvent> cl = HibernateUtil.castList(SkiEvent.class, c.list()); //List Contact
			if(cl.size() == 1) {
				//List returned the 1 expected Contact
				db.delete(cl.get(0)); //Delete Contact
				db.flush(); //Commit delete
				json.put("success",true);
				json.put("message", "Contact deleted"); //Include message
			} else {
				//Attempted to delete a Contact that does not exist
				json.put("success",false);
				json.put("message", "Contact does not exist"); //Include message
			}
			
		} else {
			//The URL ID was blank or not a number
			json.put("success",false);
			json.put("message", "Delete failed because required data is missing");
		}
	}

	
	/*
	 * 
	 * Getters and Setters for request data (Called by Stripes Framework)
	 * 
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SkiEvent getSkiEvent() {
		return contact;
	}

	public void setSkiEvent(SkiEvent contact) {
		this.contact = contact;
	}

}
