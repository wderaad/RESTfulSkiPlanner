package org.bshouse.wsdb.test;

import static org.junit.Assert.*;

import org.bshouse.wsdb.common.HibernateUtil;
import org.bshouse.wsdb.common.Settings;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Test;

public class DBTest {

	@SuppressWarnings("unused")
	private Settings s = new Settings(); //Init file based configuration
	
	@Test
	public void testActiveDb() {
		Session db = HibernateUtil.getSession(); //Get a DB connection
		//Create a SQL query
		Query q = db.createSQLQuery("SELECT TOP 1 current_timestamp FROM INFORMATION_SCHEMA.SYSTEM_TABLES");
		assertEquals(q.list().size(), 1); //Ensure it return 1 item	
	}

}
