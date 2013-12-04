package org.rapla.plugin.freiraum.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.rapla.framework.RaplaContextException;
import org.rapla.plugin.freiraum.common.CategoryDescription;
import org.rapla.plugin.freiraum.common.RaplaJsonService;
import org.rapla.plugin.freiraum.common.ResourceDescriptor;
import org.rapla.plugin.freiraum.common.ResourceDetail;
import org.rapla.plugin.freiraum.common.ResourceDetailRow;
import org.rapla.server.RemoteJsonFactory;
import org.rapla.server.RemoteSession;

import com.google.gwtjsonrpc.common.AsyncCallback;

public class RaplaJsonServiceFactory implements RemoteJsonFactory<RaplaJsonService>
{
	ResourceDescriptor test1 = new ResourceDescriptor("Room1","resource");
	ResourceDescriptor test2 = new ResourceDescriptor("Room2","resource");
	LinkedHashMap<String,  ResourceDetailRow> test = new LinkedHashMap<String, ResourceDetailRow>();
	{
		test.put( "name", new ResourceDetailRow("Name","resource1"));
		test.put( "bild", new ResourceDetailRow("Bild", "bildlink"));
	}
	ResourceDetail detail = new ResourceDetail( test);
	
	List<ResourceDescriptor> testList = new ArrayList<ResourceDescriptor>();
	{
		testList.add( test1 );
		testList.add( test2 );
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
			public void getResource(String id,AsyncCallback<ResourceDetail> callback) {
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
