package org.bshouse.wsdb.stripes.action;

import java.io.BufferedReader;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;

/*
 * 
 * A basis for all Stripes Action
 * It provides a foundation for Extending the application Context in a single location
 * 
 * 
 */

public class BaseAction implements ActionBean {

	ActionBeanContext cntx = null;
	
	
	@Override
	public ActionBeanContext getContext() {
		return cntx;
	}

	@Override
	public void setContext(ActionBeanContext context) {
		cntx = context;
		
	}
	
	protected String getRequestBody(){
	    StringBuffer body = new StringBuffer();
	    String line = null;
	    try {
	        BufferedReader reader = getContext().getRequest().getReader();
	        while ((line = reader.readLine()) != null) {
	            body.append(line);
	        }
	    } catch (Exception e) {
	        //e.printStackTrace();
	    }

	    return body.toString();
	}


}
