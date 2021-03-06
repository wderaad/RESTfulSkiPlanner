package org.bshouse.wsdb.test;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bshouse.wsdb.beans.SkiEvent;
import org.bshouse.wsdb.common.HibernateUtil;
import org.bshouse.wsdb.common.Settings;
import org.bshouse.wsdb.server.Servers;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class SkiPlannerApiTest {

	@SuppressWarnings("unused")
	private Settings s = new Settings(); //Init file based configuration
	private Session db = HibernateUtil.getSession(); //Init DB connection
	private static Servers wsdb; //DB & WebServer
	//Build the Contact API base URL
	private final String baseUrl = new String("http://"+Settings.getWebserverIpAddress()+":"+Settings.getWebserverPortHttp());
	
	@BeforeClass
	public static void startServers() {
		//Setup the Web & DB server before any tests run
		wsdb = new Servers();
		wsdb.start();
	}
	
	@AfterClass
	public static void stopServers() {
		//Stop the DB & WebServer after all the tests have run
		wsdb.stop();
	}
	
	private void cleanDb() {
		//A common method for deleting all Contact from the DB
		if(StringUtils.isNotBlank(HibernateUtil.getSchema())) {
			db.createSQLQuery("delete from "+HibernateUtil.getSchema()+".eventinfo").executeUpdate();
		} else {
			db.createSQLQuery("delete from eventinfo").executeUpdate();
		}
		db.flush();
	}
	
    @Test
	public void addEvent() {
		//A simple way to add dummy contacts for testing
		cleanDb();
		SkiEvent c = new SkiEvent();
		String fname = "Will";
		c.setNameFirst(fname);
		db.save(c);
		
		Criteria crit = db.createCriteria(SkiEvent.class);
		List<SkiEvent> sel = crit.list();
		assertTrue(sel.size() == 1);
		assertTrue("Will".equals(sel.get(0).getNameFirst()));

	}
	
}