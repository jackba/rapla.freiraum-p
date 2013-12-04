package org.rapla.plugin.freiraum.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.rapla.framework.RaplaContextException;
import org.rapla.plugin.freiraum.common.CategoryDescription;
import org.rapla.plugin.freiraum.common.Event;
import org.rapla.plugin.freiraum.common.RaplaJsonService;
import org.rapla.plugin.freiraum.common.ResourceDescriptor;
import org.rapla.plugin.freiraum.common.ResourceDetail;
import org.rapla.plugin.freiraum.common.ResourceDetailRow;
import org.rapla.server.RemoteJsonFactory;
import org.rapla.server.RemoteSession;

import com.google.gwtjsonrpc.common.AsyncCallback;

public class RaplaJsonServiceFactory implements RemoteJsonFactory<RaplaJsonService>
{
	ResourceDescriptor test1 = new ResourceDescriptor(1,"Room1","resource");
	ResourceDescriptor test2 = new ResourceDescriptor(2,"Room2","resource");
	List<ResourceDescriptor> testList = new ArrayList<ResourceDescriptor>();
	{
		testList.add( test1 );
		testList.add( test2 );
	}
	ResourceDetail detail;
	{
		LinkedHashMap<String,  ResourceDetailRow> details = new LinkedHashMap<String, ResourceDetailRow>();
		List<Event> events = new ArrayList<Event>();
		events.add(new Event("Vorlesung","10:00", "11:00",testList));
		details.put( "name", new ResourceDetailRow("Name","resource1"));
		details.put( "bild", new ResourceDetailRow("Bild", "bildlink"));
		detail = new ResourceDetail( details, events);
	}
	
	
	@Override
	public RaplaJsonService createService(RemoteSession remoteSession)
			throws RaplaContextException {
		return new RaplaJsonService() {
			
			@Override
			public void getResources(String type, String searchTerm,
					AsyncCallback<List<ResourceDescriptor>> callback) {
				callback.onSuccess(  testList);
			}

			@Override
			public void getResource(String id,AsyncCallback<ResourceDetail> callback) 
			{
				callback.onSuccess( detail);
			}

			@Override
			public void getOrganigram(AsyncCallback<CategoryDescription> callback) {
				
			}

			@Override
			public void getResourcesFromCategory(String type,
					CategoryDescription category,
					AsyncCallback<List<ResourceDescriptor>> callback) {
				callback.onSuccess(  testList);
			}
		};
	}

}
