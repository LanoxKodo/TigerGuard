package lanoxkododev.tigerguard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;

public class Utils {

	TigerLogs logger = new TigerLogs();
	private HashMap<String, String> configMap;
	
	/**
	 * Check if the input String is a url or not
	 * 
	 * @param item - The String to check
	 * @return
	 */
	public boolean isUrl(String item)
	{
		try
		{
			new URI(item);
			return true;
		}
		catch (URISyntaxException e)
		{
			return false;
		}
	}
	
	private File getConfigFile(String filename)
	{
		return new File(System.getProperty("user.dir") + File.separator + filename);
	}
	
	/**
	 * Important method, creates the config file in the event one does not exist. Admin's must enter the new credentials as needed.
	 * @param filename - the config file the bot searches for
	 */
	private void createConfigFile(String filename)
	{
		try
		{
			new File(filename).createNewFile();
			FileWriter writer = new FileWriter(filename);
			writer.write("#TigerGuard Config File");
			writer.write("\n#Edit the below values so they fit your usecase");
			writer.write("\naddress: ");
			writer.write("\nport: ");
			writer.write("\nbotToken: ");
			writer.write("\ndatabaseName: ");
			writer.write("\ndatabaseUsername: ");
			writer.write("\ndatabasePassword: ");
			writer.close();
			logger.log(LogType.INFO, "New TigerGuardConfig.txt file created. Please edit this file to permit the bot to sign in and work");
		}
		catch (IOException e)
		{
			logger.logErr(LogType.ERROR, "Failure writing config file", null, e);
		}
	}
	
	/**
	 * Important method, verifies the config file and it's status and data.
	 * 
	 * @param filename - the config file the bot references
	 */
	public boolean verifyConfig(String filename)
	{
		if (!getConfigFile(filename).exists()) createConfigFile(filename);
		else
		{
			try (BufferedReader reader = new BufferedReader(new FileReader(getConfigFile(filename))))
			{
				if (reader != null)
				{
					configMap = new HashMap<>();
					String line;
					while ((line = reader.readLine()) != null)
					{
						if (!line.startsWith("#"))
						{
							String[] parts = line.split(": ", 2);
							if (parts.length == 2)
							{
								String key = parts[0].trim();
								String value = parts[1].trim();
								
								if (!value.isEmpty()) configMap.put(key, value);
								else return false; //Do not permit the bot to continue if any value is not filled in.
							}
						}
					}
					
					reader.close();
					return true;
				}
			}
			catch (IOException e)
			{
				logger.logErr(LogType.ERROR, "Failure finding config file", null, e);
			}
		}
		
		return false;
	}
	
	public String getValue(String key)
	{
		return configMap.get(key);
	}
}
