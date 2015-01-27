package org.bshouse.wsdb.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/*
 * 
 * Provides access to the DataBase along with information and tooling methods 
 * The class also set the dynamic DB and schema for Test/Production
 * 
 */

public class HibernateUtil {

	private static SessionFactory sessionFactory;
	private static HibernateUtil instance = null;
	private static String schema = Constants.BLANK_STRING;
	
	private HibernateUtil() {
		
	}
	
	public static HibernateUtil getInstance() {
		//Load/create the static instance of HibernateUtil
		if(instance == null) {
			instance = new HibernateUtil();
		}
		return instance;
	}
	
	public static SessionFactory getSessionFactory() {
		//Load/create the static instance of SessionFactory 
		if(sessionFactory == null) {
			//Read the hibernate.cfg.xml
			Configuration config = new Configuration();
			config.configure();
			

			//Determine if the Production DB was requested. 
			if(Settings.isProduction()) {
				//Production
				config.setProperty("hibernate.connection.url", Settings.getDatabaseJdbcUrl()+Settings.getDatabaseIpAddress()+":"+Settings.getDatabasePort()+"/"+Settings.getDatabaseProduction());
				schema = Settings.getDatabaseProductionSchema();
				config.setProperty("hibernate.default_schema", schema);
				
			} else {
				//Test
				config.setProperty("hibernate.connection.url", Settings.getDatabaseJdbcUrl()+Settings.getDatabaseIpAddress()+":"+Settings.getDatabasePort()+"/"+Settings.getDatabaseTest());
				schema = Settings.getDatabaseTestSchema();
				config.setProperty("hibernate.default_schema", schema);
				
			}
			config.setProperty("hibernate.connection.username", Settings.getDatabaseUser());
			config.setProperty("hibernate.connection.password", Settings.getDatabasePassword());

			sessionFactory = config.buildSessionFactory(
					new StandardServiceRegistryBuilder().applySettings(config.getProperties()).build());
		}
		return sessionFactory;
	}
	
	public static Session getSession() {
		//Returns a Session to process DB actions
		if(sessionFactory == null) {
			return getSessionFactory().openSession();
		}
		return sessionFactory.openSession();
	}
	
	public static String getSchema() {
		//Return the active schema name
		return schema;
	}
	
	public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> untypedList) {
		//This method is used to avoid Unchecked Type Casts
		//It is not required to be used, but avoids warning when they count
		List<T> typedList = new ArrayList<T>(untypedList.size());
		for(Object o: untypedList) {
			typedList.add(clazz.cast(o));
		}
		return typedList;
	}
}
