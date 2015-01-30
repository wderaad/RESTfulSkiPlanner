package org.bshouse.wsdb.stripes.action;

// Importing all the packages
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

//Binding the url with the class.
@UrlBinding("/api/1.0/skievent/{id}")
@HttpCache(allow=false)

public class SkiEventApiAction extends BaseAction {
    //Database Access
	private Session db = HibernateUtil.getSession();
    //Java Object to JSON converter
	private Gson g = new GsonBuilder().setDateFormat("MM/dd/yyyy").create();
    //HashMap to be converted to JSON
	private Map<String,Object> json = new HashMap<String,Object>();
    //Stripes put the ID from the URL here
	private String id = Constants.BLANK_STRING;


	@DefaultHandler
	public Resolution rest() {

		//When a users requests the UrlBinding, Stripes loads this action
		//Call the method that fits the need of the request
		String method = getContext().getRequest().getMethod().toUpperCase();
		if("GET".equals(method)) {
		//Lookup Ski Event
			list();
		} else if("POST".equals(method)) {
		//Add a new Ski Event
			add();
		} else if("PUT".equals(method)) {
		//Update an existing Ski Event
			update();
		} else if("DELETE".equals(method)) {
		//Delete an existing Ski Event
			delete();
		} else {
            //flash the message
			json.put("success",false);
			json.put("message", "Unsupported method requested: "+method);
		}
        //Close the Database
		db.close();
		//Return anything loaded into the "json" HashMap as JSON text
		return new StreamingResolution("application/json",g.toJson(json));
	}
	
	/*
	 * 
	 * This class Handles requests to list 1 or all Ski Events
	 * 
	 */
	private void list() {		
		//Create a Hibernate Criteria Query for a "Ski Event" in the database
		Criteria c = db.createCriteria(SkiEvent.class);
        //loop to take the id and the date function
		if(StringUtils.isNotBlank(id)) {
			//Load the requested Ski Event
            //restricted to use the id equal
            // to the parsed id
			c.add(Restrictions.eq("id", Long.parseLong(id)));
		}
        else {
            //restricted to show only present day
            //and future days ski events.
            //ge() will show dates
            //greater or equal to the date when the server will start running
            c.add(Restrictions.ge("skiday", new Date()));
        }
		//Based on the above restriction Criteria,
		// List all matches and cast it to a typed list
		List<SkiEvent> cl = HibernateUtil.castList(SkiEvent.class, c.list());
        //Tell the JavaScript we were successful
		json.put("success",true);
        //Add the List of Ski Events
		json.put("data", cl);
	}
	
	/*
	 * 
	 * Handles request to add new Ski Events
	 *
	 *add () will add the new event
	 *or update if user is trying to add
	 *already existing event.
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
                    //Add the Ski Event
					db.save(skievent);
                    //Tell the JavaScript we were successful
					json.put("success",true);
                    //Return the now saved Ski Event
					json.put("data", skievent);
				} else {
					//Attempted to Add a Ski Event that already exists
                    //rather update the already existing record
					//json.put("success",false);
					json.put("message", "Ski Event already exists. Updating the Ski Event.");
                    //update here
                    //Update DB
                    db.update(skievent);
                    //Commit change
                    db.flush();
                    //Tell the JavaScript we were successful
                    json.put("success",true);
                    //return update Ski Event
                    json.put("data", skievent);
				}
			} else {
				//The provided information did not fit the model
				json.put("success",false);
                //Return validation failure messages
				json.put("message", skievent.valid());
			}
			
		} else {
			//No form data was submitted
			json.put("success",false);
            //flashing of the message when adding fails
			json.put("message", "Add failed because required data is missing");
		}
	}
	
	/*
	 * Handles requests to update an existing Ski Event
	 * update() will update the already existing ski event
	 * if user tries to update the non-existing record
	 * the function will add it as a new record.
	 */
	public void update() {
		SkiEvent skievent = g.fromJson(getRequestBody(), SkiEvent.class);
		
		if(skievent != null && id != null && skievent.getId().equals(Long.parseLong(id))) {
			//We got a Ski Event, that was PUT to a URL with an ID number and the Ski Event.id matches the URL ID
			if(skievent.valid().length() == 0) {
				//No validation errors
				if(skievent.getId() > -1L) {
					//The ID is not the default value
                    //Update DB
					db.update(skievent);
                    //Commit change
					db.flush();
                    //Tell javascript that we are successful
					json.put("success",true);
                    //return update Ski Event
					json.put("data", skievent);
				} else {
					//Attempted to Update a new Ski Event (should be added instead)
                    //as the event entered is not available
                    //to be updated, we will add the event
					//json.put("success",false);
					json.put("message", "Ski Event does not exist. Adding the new Ski Event. ");
                    //Add the Ski Event
                    db.save(skievent);
                    //Tell Javascript that we are successful
                    json.put("success",true);
                    //Return the now saved Ski Event
                    json.put("data", skievent);
				}
			} else {
				//Validation problem
				json.put("success",false);
                //Include validation errors
				json.put("message", skievent.valid());
			}
			
		} else {
			//No form data or URL ID & Ski Event.id do not match
			json.put("success",false);
            //show error message when the updation fails
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
            //Convert URL ID to a Long
			Long cid = Long.parseLong(id);
            //Create a Criteria Query on Ski Event
			Criteria c = db.createCriteria(SkiEvent.class);
            //Restrict the Query to the requested ID
			c.add(Restrictions.eq("id", cid));
            //List Ski Event
			List<SkiEvent> cl = HibernateUtil.castList(SkiEvent.class, c.list());
			if(cl.size() == 1) {
				//List returned the 1 expected Ski Event
                //Delete Ski Event
				db.delete(cl.get(0));
                //Commit delete
				db.flush();
                //Tell Javascript that we are successful
				json.put("success",true);
                //Include message to flash
				json.put("message", "Ski Event deleted");
			} else {
				//Attempted to delete a Ski Event that does not exist
				json.put("success",false);
                //Include message
				json.put("message", "Ski Event does not exist");
			}
			
		} else {
			//The URL ID was blank or not a number
			json.put("success",false);
            //Show error message
			json.put("message", "Delete failed because required data is missing");
		}
	}

	
	/*
	 * 
	 * Getters and Setters for request data (Called by Stripes Framework)
	 * 
	 */
    //to return the id
	public String getId() {
		return id;
	}
    //to set the referred id
	public void setId(String id) {
		this.id = id;
	}


}
