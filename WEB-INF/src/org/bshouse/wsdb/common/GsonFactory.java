package org.bshouse.wsdb.common;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class GsonFactory {
	public static Gson getGson() {
		GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("MM/dd/yyyy");
	    gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
	        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	        @Override
	        public Date deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
	                throws JsonParseException {
	            try {
	            	
	                return df.parse(json.getAsString());
	            } catch (Exception e) {
	                return null;
	            }
	        }
	    });
	    gsonBuilder.registerTypeAdapter(Long.class, new JsonDeserializer<Long>() {
	        @Override
	        public Long deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
	                throws JsonParseException {
	        	String lVal = json.getAsString();
	        	if(StringUtils.isBlank(lVal) || !StringUtils.isNumeric(lVal)) {
	        		return -1L;
	        	}
	            try {
	                return Long.parseLong(lVal);
	            } catch (Exception e) {
	                return null;
	            }
	        }
	    });
	    return  gsonBuilder.create();
    }
}
