package org.sagebionetworks.web.client.transform;

import javax.management.RuntimeErrorException;

import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

import com.google.inject.Inject;

/**
 * The basic implementation of the JSONEntityFactory.
 * 
 * @author John
 *
 */
public class JSONEntityFactoryImpl implements JSONEntityFactory {
	// This factory allows the creation of new class instances using only the class name.
	// Since reflection is not allowed in GWT, it uses an auto-generated swtich.
	AutoGenFactory internalFactory;
	// This factory creates any needed adapters
	AdapterFactory adapterFactory;
	
	@Inject
	public JSONEntityFactoryImpl(AdapterFactory adapterFactory){
		internalFactory = new AutoGenFactory();
		this.adapterFactory = adapterFactory;
	}

	/**
	 * Create a new entity from a JSON String.
	 */
	@Override
	public <T extends JSONEntity> T createEntity(String json, Class<? extends T> clazz) {
		if(json == null) throw new IllegalArgumentException("Json string cannot be null");
		if(clazz == null) throw new IllegalArgumentException("Clazz cannot be null");
		// Use the factory to create a new instance from the class name
		return (T) createEntity(json, clazz.getName());
	}
	
	/**
	 * Create a new entity for the passed JSON and class name.
	 * @param json
	 * @param className
	 * @return
	 * @throws JSONObjectAdapterException 
	 */
	@Override
	public JSONEntity createEntity(String json, String className) {
		if(json == null) throw new IllegalArgumentException("Json string cannot be null");
		if(className == null) throw new IllegalArgumentException("Classname cannot be null");
		// Use the factory to create the instance
		JSONEntity entity = this.internalFactory.newInstance(className);
		return initializeEntity(json, entity);
	}
	
	@Override
	public JSONEntity newInstance(String className) {
		return this.internalFactory.newInstance(className);
	}

	/**
	 * Initialize the passed new JSONEntity using the provided JSON string
	 */
	@Override
	public <T extends JSONEntity> T initializeEntity(String json, T newEntity) {
		if(json == null) throw new IllegalArgumentException("Json string cannot be null");
		if(newEntity == null) throw new IllegalArgumentException("NewEntity cannot be null");
		// The adapter factory creates our adapter
		JSONObjectAdapter adapter;
		try {
			adapter = this.adapterFactory.createNew(json);
			newEntity.initializeFromJSONObject(adapter);
			return newEntity;
		} catch (JSONObjectAdapterException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public String createJsonStringForEntity(JSONEntity entity){
		if(entity == null) throw new IllegalArgumentException("Entity cannot be null");
		// Get a new adapter
		JSONObjectAdapter adapter = this.adapterFactory.createNew();
		try {
			entity.writeToJSONObject(adapter);
			return adapter.toJSONString(); 
		} catch (JSONObjectAdapterException e) {
			throw new RuntimeException(e);
		}

	}


	@Override
	public Entity createEntity(String json){
		// first we must parse the json to determine the type.
		JSONObjectAdapter adapter;
		try {
			adapter = this.adapterFactory.createNew(json);
			if(!adapter.has("entityType")) throw new IllegalArgumentException("Cannot determine the entity type because the 'entityType' is null");
			String entityType = adapter.getString("entityType");
			// create a new isntance
			Entity entity = (Entity) internalFactory.newInstance(entityType);
			entity.initializeFromJSONObject(adapter);
			return entity;
		} catch (JSONObjectAdapterException e) {
			throw new RuntimeException(e);
		}

	}

}
