package com.veraxsystems.vxipmi.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesManager {

    private static final Logger logger = Logger.getLogger(PropertiesManager.class);

    private static class PropertiesManagerHolder {		
		private final static PropertiesManager instance = new PropertiesManager();
	}

    private final Map<String, String> properties = new HashMap<String, String>();

    private PropertiesManager() {
        loadProperties("/connection.properties");
        loadProperties("/vxipmi.properties");
    }

    public static PropertiesManager getInstance() {
        return PropertiesManagerHolder.instance;
    }

    private void loadProperties(String name) {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream(name));

            for (Object key : properties.keySet()) {
                properties.put(key.toString(), properties.getProperty(key.toString()));
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getProperty(String key) {
    	if(logger.isDebugEnabled()) {
            logger.debug("Getting " + key + ": " + properties.get(key));    		
    	}
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
}
