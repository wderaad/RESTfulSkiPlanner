package org.bshouse.wsdb.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.bshouse.wsdb.beans.SkiEvent;
import org.bshouse.wsdb.common.HibernateUtil;
import org.bshouse.wsdb.common.Settings;
import org.bshouse.wsdb.server.Servers;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SkiEventApiTest {

	@SuppressWarnings("unused")
	private Settings s = new Settings(); //Init file based configuration
	private Session db = HibernateUtil.getSession(); //Init DB connection
	private static Servers wsdb; //DB & WebServer
	private Gson g = new GsonBuilder().setDateFormat("MM/dd/yyyy").create(); //Java Object to JSON converter
	//Build the skievent API base URL
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
		//A common method for deleting all skievent from the DB
		if(StringUtils.isNotBlank(HibernateUtil.getSchema())) {
			db.createSQLQuery("delete from "+HibernateUtil.getSchema()+".eventinfo").executeUpdate();
		} else {
			db.createSQLQuery("delete from eventinfo").executeUpdate();
		}
		db.flush();
	}
	
	private void addskievent(String fname) {
		//A simple way to add dummy skievents for testing
		SkiEvent c = new SkiEvent();
		c.setNameFirst(fname);
		db.save(c);
	}
	
	
	/*
	 * 
	 * The the skievent API listing function
	 * 
	 */
	@Test
	public void testskieventList() throws ClientProtocolException, IOException {
		
		cleanDb(); //Empty skievent table
		HttpClient httpclient = HttpClients.createDefault(); //Create a HTTP client
		HttpGet get = new HttpGet(baseUrl+"/api/1.0/skievent"); //Setup a GET request with the skievent API
		HttpResponse hr = httpclient.execute(get); //Run the request
		String content = IOUtils.toString(hr.getEntity().getContent()); //Get the response body
		System.out.println("1. testskieventList: "+content); //Output the body for debugging
		assertTrue(content.equals("{\"data\":[],\"success\":true}")); //Assert we got a blank list
		
		
		addskievent("Bill"); //Add skievent Bill
		addskievent("Bob"); //Add skievent Bob
		db.flush(); //Commit
		get = new HttpGet(baseUrl+"/api/1.0/skievent"); //Prepare GET request to list skievents
		hr = httpclient.execute(get); //Execute the Request
		content = IOUtils.toString(hr.getEntity().getContent()); //Get the response body
		System.out.println("2. testskieventList: "+content); //Output the body for debugging
		assertTrue(content.endsWith(",\"success\":true}")); //Assert we got a success
	}
	
	/*
	 * 
	 * Test adding a skievent via the skievent API
	 * 
	 */
	@Test
	public void testskieventAdd() throws ClientProtocolException, IOException {
		cleanDb(); //Empty DB
		HttpClient httpclient = HttpClients.createDefault(); //Create HTTP Client
		HttpPost post = new HttpPost(baseUrl+"/api/1.0/skievent"); //Create POST request

		//Build JSON request data
		SkiEvent se = new SkiEvent();
		se.setNameFirst("Joe");
		se.setNameLast("Doe");
		se.setEmail("Joe.Doe@Inter.Net");
		se.setNumberCell("303-333-3333");
		se.setPref("Snowboard");
		se.setResort("Copper");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, 1);
		se.setSkiday(c.getTime());
		se.setSkill("Hack");
		
		StringEntity input = new StringEntity(g.toJson(se));
		input.setContentType("application/json");
		post.setEntity(input);

		HttpResponse hr = httpclient.execute(post); //Execute the POST
		String content = IOUtils.toString(hr.getEntity().getContent()); //Get Response body
		System.out.println("testAddskievent: "+content); //Output for debugging
		assertTrue(content.endsWith(",\"success\":true}")); //Assert True response
	}
	
	
	/*
	 * 
	 * Test skievent addition with a validation failure
	 * 
	 */
	@Test
	public void testskieventAddOverflow() throws ClientProtocolException, IOException {
		cleanDb(); //Empty DB
		HttpClient httpclient = HttpClients.createDefault(); //Create HTTP Client
		HttpPost post = new HttpPost(baseUrl+"/api/1.0/skievent"); //Create POST request

		//Create POST data
		List<NameValuePair> nvpl = new ArrayList<NameValuePair>(5);
		nvpl.add(new BasicNameValuePair("skievent.id","-1"));
		nvpl.add(new BasicNameValuePair("skievent.nameFirst","First"));
		nvpl.add(new BasicNameValuePair("skievent.nameLast","Last"));
		//Overflow Phone Number
		nvpl.add(new BasicNameValuePair("skievent.numberCell","1-303-555-121234sdfgsdfg5wegsdgf434534534534534"));
		nvpl.add(new BasicNameValuePair("skievent.email","user@mail.com"));
		
		post.setEntity(new UrlEncodedFormEntity(nvpl)); //Add POST data to request
		HttpResponse hr = httpclient.execute(post); //Execute request
		String content = IOUtils.toString(hr.getEntity().getContent()); //Get response bodu
		System.out.println("testAddOverflowskievent: "+content); //Output for debugging
		//Assert we got the expected error message
		assertTrue(content.equals("{\"message\":\"\\nPhone number must not exceed 30 characters.\",\"success\":false}"));
	}
	
	/*
	 * 
	 * Test adding a Contat with a bad date format
	 * 
	 * 
	 */
	
	@Test
	public void testskieventAddBadDateFormat() throws ClientProtocolException, IOException {
		
		//Test Invalid date format 
		HttpClient httpclient = HttpClients.createDefault(); //Create HTTP Client
		HttpPost post = new HttpPost(baseUrl+"/api/1.0/skievent"); //Creat POST Request

		//Populate POST data
		List<NameValuePair> nvpl = new ArrayList<NameValuePair>(5);
		nvpl.add(new BasicNameValuePair("skievent.id","-1"));
		nvpl.add(new BasicNameValuePair("skievent.nameFirst","First"));
		nvpl.add(new BasicNameValuePair("skievent.nameLast","Last"));
		nvpl.add(new BasicNameValuePair("skievent.numberCell","1-303-555-1212"));
		nvpl.add(new BasicNameValuePair("skievent.email","user@mail.com"));
		nvpl.add(new BasicNameValuePair("skievent.bday","2000/10/31"));
		
		post.setEntity(new UrlEncodedFormEntity(nvpl)); //Attach POST data to request
		HttpResponse hr = httpclient.execute(post); //Execute POST request
		String content = IOUtils.toString(hr.getEntity().getContent()); //Get response body
		System.out.println("testAddBadDateskievent: "+content); //Output for debugging
		assertTrue(content.endsWith("{\"message\":\"\\nBirthday must be a valid date formatted like MM/DD/YYYY.\",\"success\":false}"));
		
		
	}
	
	
	/*
	 * 
	 * Test adding a skievent with a bad date
	 * October 32, 2000 
	 * 
	 */
	@Test
	public void testskieventAddBadDate() throws ClientProtocolException, IOException {
		
		//Invalid date format
		HttpClient httpclient = HttpClients.createDefault(); //Create HTTP client
		HttpPost post = new HttpPost(baseUrl+"/api/1.0/skievent"); //Create POST request

		//Populate POST data with Invalid Date
		List<NameValuePair> nvpl = new ArrayList<NameValuePair>(5);
		nvpl.add(new BasicNameValuePair("skievent.id","-1"));
		nvpl.add(new BasicNameValuePair("skievent.nameFirst","First"));
		nvpl.add(new BasicNameValuePair("skievent.nameLast","Last"));
		nvpl.add(new BasicNameValuePair("skievent.numberCell","1-303-555-1212"));
		nvpl.add(new BasicNameValuePair("skievent.email","user@mail.com"));
		nvpl.add(new BasicNameValuePair("skievent.bday","10/32/2000"));
		
		post.setEntity(new UrlEncodedFormEntity(nvpl)); //Add POST data to the request
		HttpResponse hr = httpclient.execute(post); //Execute the POST request
		String content = IOUtils.toString(hr.getEntity().getContent()); //Get the POST response body
		System.out.println("testAddBadDateskievent: "+content); //Output for debugging
		//Ensure we got the expected response
		assertTrue(content.endsWith("{\"message\":\"\\nBirthday must be a valid date formatted like MM/DD/YYYY.\",\"success\":false}"));
	}

	
	
	/*
	 * 
	 * Test Updating an existing skievent
	 * 
	 */
	@Test
	public void testskieventUpdate() throws ClientProtocolException, IOException, URISyntaxException {
		cleanDb(); //Empty table
		
		//Add a skievent to edit
		HttpClient httpclient = HttpClients.createDefault(); //Create HTTP Client
		HttpPost post = new HttpPost(baseUrl+"/api/1.0/skievent"); //Create POST request

		//Populate POST data
		List<NameValuePair> nvpl = new ArrayList<NameValuePair>(5);
		nvpl.add(new BasicNameValuePair("skievent.id","-1"));
		nvpl.add(new BasicNameValuePair("skievent.nameFirst","First"));
		nvpl.add(new BasicNameValuePair("skievent.nameLast","Last"));
		nvpl.add(new BasicNameValuePair("skievent.numberCell","1-303-555-1212"));
		nvpl.add(new BasicNameValuePair("skievent.email","user@mail.com"));
		
		post.setEntity(new UrlEncodedFormEntity(nvpl)); //Add POST data to request
		HttpResponse hr = httpclient.execute(post); //Execute POST request
		String content = IOUtils.toString(hr.getEntity().getContent()); //Get response body
		System.out.println("testUpdateskievent(Add): "+content); //Output for debugging
		assertTrue(content.endsWith(",\"success\":true}")); //Assert skievent was added
		
		//Parse the ID of the newly added skievent
		String id = StringUtils.substringBetween(content,"\"id\":", ",");

		System.out.println("ID: "+id); //Output for debugging
		
		//Edit skievent
		HttpPut put = new HttpPut(baseUrl+"/api/1.0/skievent/"+id); //Create a PUT request
		//Populate PUT data
		List<NameValuePair> unvpl = new ArrayList<NameValuePair>(5);
		unvpl.add(new BasicNameValuePair("skievent.id",id));
		unvpl.add(new BasicNameValuePair("skievent.nameFirst","First"));
		unvpl.add(new BasicNameValuePair("skievent.nameLast","Last"));
		unvpl.add(new BasicNameValuePair("skievent.numberCell","1-303-555-1212"));
		unvpl.add(new BasicNameValuePair("skievent.email","email@mail.com"));
		put.setEntity(new UrlEncodedFormEntity(unvpl)); //Attach PUT data to request
		hr = httpclient.execute(put); //Execute PUT request
		content = IOUtils.toString(hr.getEntity().getContent()); //Get response body
		System.out.println("testUpdateskievent: "+content); //Output for debugging
		//Ensure it was successful and the email address changed as expected
		assertTrue(content.endsWith(",\"success\":true}") && content.indexOf("email@mail.com") > 0);
	}
	
	
	/*
	 * 
	 * Test skievent delete
	 * 
	 */
	@Test
	public void testskieventDelete() throws ClientProtocolException, IOException {
		cleanDb(); //Empty skievent table
		
		//Add a skievent to delete
		HttpClient httpclient = HttpClients.createDefault(); //Create an HTTP client
		HttpPost post = new HttpPost(baseUrl+"/api/1.0/skievent"); //Create a POST request

		//Populate POST data
		List<NameValuePair> nvpl = new ArrayList<NameValuePair>(5);
		nvpl.add(new BasicNameValuePair("skievent.id","-1"));
		nvpl.add(new BasicNameValuePair("skievent.nameFirst","First"));
		nvpl.add(new BasicNameValuePair("skievent.nameLast","Last"));
		nvpl.add(new BasicNameValuePair("skievent.numberCell","1-303-555-1212"));
		nvpl.add(new BasicNameValuePair("skievent.email","user@mail.com"));
		
		post.setEntity(new UrlEncodedFormEntity(nvpl)); //Attache POST date to request
		HttpResponse hr = httpclient.execute(post); //Execute the POST request
		String content = IOUtils.toString(hr.getEntity().getContent()); //Get response body
		System.out.println("testDeleteskievent(Add): "+content); //Output for debugging
		assertTrue(content.endsWith(",\"success\":true}")); //Ensure success
		
		//Parse the ID of the newly added skievent
		String id = StringUtils.substringBetween(content,"\"id\":", ",");

		System.out.println("ID: "+id); //Output for debugging
		
		//Delete skievent
		HttpDelete del = new HttpDelete(baseUrl+"/api/1.0/skievent/"+id); //Create DELETE request
		hr = httpclient.execute(del); //Execute DELETE request
		content = IOUtils.toString(hr.getEntity().getContent()); //Get response body
		System.out.println("testDeleteskievent: "+content); //Output for debugging
		assertTrue(content.endsWith(",\"success\":true}")); //Ensure success
	}

	/*
	 * 
	 * Test delete of an invalid (non-existant) skievent
	 * 
	 */
	
	@Test
	public void testskieventDeleteInvalid() throws ClientProtocolException, IOException {
		cleanDb(); //Empty skievent table
		HttpClient httpclient = HttpClients.createDefault(); //Create HTTP Client
		//Delete skievent
		HttpDelete del = new HttpDelete(baseUrl+"/api/1.0/skievent/abc123"); //Create DELETE request
		HttpResponse hr = httpclient.execute(del); //Execute DELETE request
		String content = IOUtils.toString(hr.getEntity().getContent()); //Get response body
		System.out.println("testDeleteInvalidskievent: "+content); //Output for debugging
		//Ensure the proper error message was returned
		assertTrue(content.equals("{\"message\":\"Delete failed because required data is missing\",\"success\":false}"));
	}
	
}
