package org.bshouse.wsdb.stripes.action;

import java.util.Date;
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
 * This action class provides the RESTful Ski Event API
 * 
 */


@UrlBinding("/api/1.0/skievent/{id}")
@HttpCache(allow=false)
public class SkiEventApiAction extends BaseAction {

	private Session db = HibernateUtil.getSession(); //Database Access
	private Gson g = new GsonBuilder().setDateFormat("MM/dd/yyyy").create(); //Java Object to JSON converter
	private Map<String,Object> json = new HashMap<String,Object>(); //HashMap to be converted to JSON
	private String id = Constants.BLANK_STRING; //Stripes put the ID from the URL here
	
	
	@DefaultHandler
	public Resolution rest() {
		//When a users requests the UrlBinding, Stripes loads this action
		//Call the method that fits the need of the request
		String method = getContext().getRequest().getMethod().toUpperCase();
		if("GET".equals(method)) { //Lookup Ski Event
			list();
		} else if("POST".equals(method)) { //Add a new Ski Event
			add();
		} else if("PUT".equals(method)) { //Update an existing Ski Event
			update();
		} else if("DELETE".equals(method)) { //Delete an existing Ski Event
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
	 * Handles requests to list 1 or all Ski Events
	 * 
	 */
	private void list() {		
		//Create a Hibernate Criteria Query for a "Ski Event" in the database
		Criteria c = db.createCriteria(SkiEvent.class);
		if(StringUtils.isNotBlank(id)) {
			//Load the requested Ski Event
			c.add(Restrictions.eq("id", Long.parseLong(id)));
		}
        else {
            c.add(Restrictions.ge("skiday", new Date()));
        }
		//Based on the Criteria, List all matches and cast it to a typed list
		List<SkiEvent> cl = HibernateUtil.castList(SkiEvent.class, c.list());
		
		json.put("success",true); //Tell the JavaScript we were successful
		json.put("data", cl); //Add the List of Ski Events
	}
	
	/*
	 * 
	 * Handles request to add new Ski Events
	 * 
	 */
	public void add() {
		SkiEvent skievent = g.fromJson(getRequestBody(), SkiEvent.class);
		//Add a Ski Event
		if(skievent != null) {
			//Stripes put something in the Ski Event object
			if(skievent.valid().length() == 0) {
				//Validation returns no errors
				if(skievent.getId() == -1L) {
					//The default ID is set
					db.save(skievent); //Add the Ski Event
					json.put("success",true);
					json.put("data", skievent); //Return the now saved Ski Event
				} else {
					//Attempted to Add a Ski Event that already exists
					json.put("success",false);
					json.put("message", "Ski Event already exists");
				}
			} else {
				//The provided information did not fit the model
				json.put("success",false);
				json.put("message", skievent.valid()); //Return validation failure messages
			}
			
		} else {
			//No form data was submitted
			json.put("success",false);
			json.put("message", "Add failed because required data is missing");
		}
	}
	
	/*
	 * Handles requests to update an existing Ski Event
	 */
	public void update() {
		SkiEvent skievent = g.fromJson(getRequestBody(), SkiEvent.class);
		
		if(skievent != null && id != null && skievent.getId().equals(Long.parseLong(id))) {
			//We got a Ski Event, that was PUT to a URL with an ID number and the Ski Event.id matches the URL ID
			if(skievent.valid().length() == 0) {
				//No validation errors
				if(skievent.getId() > -1L) {
					//The ID is not the default value
					db.update(skievent); //Update DB
					db.flush(); //Commit change
					json.put("success",true);
					json.put("data", skievent); //return update Ski Event
				} else {
					//Attempted to Update a new Ski Event (should be added instead)
					json.put("success",false);
					json.put("message", "Ski Event does not exist");
				}
			} else {
				//Validation problem
				json.put("success",false);
				json.put("message", skievent.valid()); //Include validation errors
			}
			
		} else {
			//No form data or URL ID & Ski Event.id do not match
			json.put("success",false);
			json.put("message", "Update failed because required data is missing");
		}
	}
	
	
	/*
	 * 
	 * Handles requests to delete a Ski Event
	 * 
	 */
	public void delete() {
		if(StringUtils.isNotBlank(id) && StringUtils.isNumeric(id)) {
			//The URL ID is present and is a number
			Long cid = Long.parseLong(id); //Convert URL ID to a Long
			Criteria c = db.createCriteria(SkiEvent.class); //Create a Criteria Query on Ski Event
			c.add(Restrictions.eq("id", cid)); //Restrict the Query to the requested ID
			List<SkiEvent> cl = HibernateUtil.castList(SkiEvent.class, c.list()); //List Ski Event
			if(cl.size() == 1) {
				//List returned the 1 expected Ski Event
				db.delete(cl.get(0)); //Delete Ski Event
				db.flush(); //Commit delete
				json.put("success",true);
				json.put("message", "Ski Event deleted"); //Include message
			} else {
				//Attempted to delete a Ski Event that does not exist
				json.put("success",false);
				json.put("message", "Ski Event does not exist"); //Include message
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


}
