package mork.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Properties;

public class JMorkProperties {

	Properties properties;
	
	public JMorkProperties() {

		File propertiesFile = new File("jmork.properties");
		properties = new Properties();
		InputStream inStream;
		try {
			inStream = new FileInputStream(propertiesFile);
			properties.load(inStream);
		} catch (FileNotFoundException e) {
			// Ignore this error.  We simply start with no properties set.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void putProperty(String key, String value) {
		properties.put(key, value);
		
		try {
			Writer out = new FileWriter("jmork.properties");
			properties.store(out, "Persist settings for JMork UI");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
