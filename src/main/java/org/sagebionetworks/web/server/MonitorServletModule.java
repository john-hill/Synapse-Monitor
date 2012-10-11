package org.sagebionetworks.web.server;

import java.util.Properties;

import org.sagebionetworks.web.server.servlet.AddServlet;
import org.sagebionetworks.web.server.servlet.AmazonClientFactoryImpl;
import org.sagebionetworks.web.server.servlet.LoginServlet;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.SynapseProviderImpl;
import org.sagebionetworks.web.server.servlet.UserDataStoreImpl;

import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

/**
 * Binds the service servlets to their paths and any other 
 * Guice binding required on the server side.
 * 
 * @author John
 *
 */
public class MonitorServletModule extends ServletModule {

	@Override
	protected void configureServlets() {
		// Setup the Search service
		bind(SynapseClientImpl.class).in(Singleton.class);
		serve("/monitor/synapse").with(SynapseClientImpl.class);
		
		// Storage of user data.
		bind(UserDataStoreImpl.class).in(Singleton.class);
		serve("/monitor/datastore").with(UserDataStoreImpl.class);
		
		// Synapse Provider
		bind(SynapseProviderImpl.class).in(Singleton.class);
		bind(SynapseProvider.class).to(SynapseProviderImpl.class);
		
		// setup Login
		bind(LoginServlet.class).in(Singleton.class);
		serve("/monitor/service/login").with(LoginServlet.class);
		
		// Add
		bind(AddServlet.class).in(Singleton.class);
		serve("/monitor/service/add").with(AddServlet.class);
		
		// Bind the environment variables
		Properties props = new Properties();
		// Get the required properties.
		props.setProperty(AmazonClientFactoryImpl.AWS_ACCESS_KEY_ID, getSystemPropertyOrEnvironmentVar(AmazonClientFactoryImpl.AWS_ACCESS_KEY_ID));
		props.setProperty(AmazonClientFactoryImpl.AWS_SECRET_KEY, getSystemPropertyOrEnvironmentVar(AmazonClientFactoryImpl.AWS_SECRET_KEY));
		// Make these properties available to for IoC.
		Names.bindProperties(binder(),props);
	}
	
	/**
	 * Extract the key from either a system property or environment variable.
	 * @param key
	 * @return
	 */
	public static String getSystemPropertyOrEnvironmentVar(String key){
		String value = System.getenv(key);
		if(value == null){
			value = System.getProperty(key);
		}
		if(value == null) throw new IllegalArgumentException("Failed to find: "+key+" as a property or environment variable");
		return value;
	}
}
