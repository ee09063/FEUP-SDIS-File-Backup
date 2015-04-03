package Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class LoadProperties {
	
	public Properties prop;
	
	public LoadProperties() {
		File file = new File("config.properties");
		FileInputStream fis;
		prop = new Properties();
		try {
			fis = new FileInputStream(file);
			prop.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
