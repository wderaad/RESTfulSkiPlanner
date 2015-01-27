package org.bshouse.wsdb.server;

import java.io.File;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.bshouse.wsdb.common.Settings;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.hsqldb.persist.HsqlProperties;

public class Servers {

	private HsqlProperties hp;
	private org.hsqldb.Server server = null;
	private Server s = null;

	
	public void start() {
		
		//Load file based application settings
		Settings.loadAppProperties(getClass().getClassLoader().getResourceAsStream("app.properties"));
		
		//Start the DB
		startDb();
		
		//Create a new WebServer
		s = new Server();

		
		//Create a connector on the requested IP/Port or default values
		ServerConnector sc = new ServerConnector(s);
		if(StringUtils.isNotBlank(Settings.getWebserverIpAddress())) {
			sc.setHost(Settings.getWebserverIpAddress());
		}
		if(StringUtils.isNotBlank(Settings.getWebserverPortHttp())) {
			sc.setPort(Integer.parseInt(Settings.getWebserverPortHttp()));
		}
		s.setConnectors(new Connector[] { sc });
		
		
		//Add a WebApp
		WebAppContext wac = new WebAppContext();
		if(!Settings.isProduction()) {
			//Enable automatic reloading in Test
			wac.setInitParameter("Extension.Packages", "org.stripesbook.reload.extensions");
		}
		wac.setDescriptor("WEB-INF/web.xml");
		wac.setResourceBase(".");
		wac.setContextPath("/");
		wac.setParentLoaderPriority(true);
		
		//Add the WebAppContext to the web server
		s.setHandler(wac);
		
		try {
			//Start the web server
			s.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void join() {
		if(s == null) {
			return;
		}
		//Join the web server thread
		try {
			s.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		//Stop the Web Server & DB
		try {
			if(s != null) {
				s.stop();
			}
			if(server != null) {
				server.stop();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void startDb() {
		//Set path to DB
		File dbDir = new File("WEB-INF/db/dummy");
		
		//Configure DB settings
		Properties p = new Properties();
		p.put("server.address", Settings.getDatabaseIpAddress());
		p.put("server.port", Settings.getDatabasePort());
		p.put("server.database.0","file:"+dbDir.getAbsolutePath()+
				";hsqldb.sqllog=3;sql.enforce_names=true;user="+Settings.getDatabaseUser()+";password="+Settings.getDatabasePassword());
		p.put("server.dbname.0", "dummy");
		p.put("server.database.1","file:"+dbDir.getAbsolutePath()+
				";hsqldb.sqllog=3;sql.enforce_names=true;user="+Settings.getDatabaseUser()+";password="+Settings.getDatabasePassword());
		p.put("server.dbname.1", "prod");
		p.put("server.silent","false");
		p.put("server.trace", "true");
		p.put("server.no_system_exit", "true");
		p.put("server.remote_open", "false");
		p.put("server.acl", "WEB-INF/src/properties/server.acl");
		
		hp = new HsqlProperties(p);
		
		//Start the database server with the settings from above
		server = new org.hsqldb.Server();
		try {
			server.setProperties(hp);
			server.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		//Command line startup
		Servers s = new Servers();
		s.start();
		s.join();
	}


}
