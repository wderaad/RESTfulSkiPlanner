package org.bshouse.wsdb.common;

import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

/*
 * 
 * This class provide static Resolutions to common pages within the application.
 * It provide a single source of URIs/URLs so that file system changes do not generate
 * massive change sets   
 * 
 */

public class ResolutionUrl {

	public static final Resolution INDEX = new RedirectResolution("/index.jsp");
	public static final Resolution INDEX_JSP = new ForwardResolution("/WEB-INF/jsp/index.jsp");
	
}
