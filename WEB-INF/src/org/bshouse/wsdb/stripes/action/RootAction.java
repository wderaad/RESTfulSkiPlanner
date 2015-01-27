package org.bshouse.wsdb.stripes.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.bshouse.wsdb.common.ResolutionUrl;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.HttpCache;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

/*
 * 
 * This Action functions as the general purpose web server
 * It serves known content and 404s everything else
 * 
 */

@UrlBinding("/")
@HttpCache(allow=false)
public class RootAction extends BaseAction {

	@DefaultHandler
	public Resolution home() {
		final String URI = getContext().getRequest().getRequestURI(); //Reference requested "file"
		
		if(URI.endsWith(".js")) {
			//Serve JavaScript files
			try {
				return new StreamingResolution("text/javascript",new FileReader("./"+URI));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else if(URI.endsWith(".css")) {
			//Serve Cascading Style Sheet (CSS) files
			try {
				return new StreamingResolution("text/css",new FileReader("./"+URI));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else if(URI.endsWith(".jsp")) {
			//Serve dynamic JSP pages
			return new StreamingResolution(URI);

		} else if(URI.endsWith(".png")) {
			//Serve PNG Images
			try {
				return new StreamingResolution("image/png") {
					protected void stream(HttpServletResponse response) throws Exception {
						FileInputStream fr = new FileInputStream(new File("./"+URI));
						OutputStream os = response.getOutputStream();
						IOUtils.copy(fr,os);
						IOUtils.closeQuietly(fr);
						IOUtils.closeQuietly(os);
				     }
				};
						
						//,new FileReader("./"+URI));
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if(URI.equals("/")) {
			//Serve Welcome (defualt) Page
			return ResolutionUrl.INDEX_JSP;
			
		}
		
		return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND); //404 Error

		
	}
}
