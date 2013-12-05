package org.rapla.plugin.freiraum.server;

import java.util.ArrayList;
import java.util.List;

import org.rapla.entities.Category;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.storage.EntityResolver;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.freiraum.common.CategoryDescription;
import org.rapla.plugin.freiraum.common.RaplaJsonService;
import org.rapla.plugin.freiraum.common.ResourceDescriptor;
import org.rapla.plugin.freiraum.common.ResourceDetail;
import org.rapla.server.RemoteJsonFactory;
import org.rapla.server.RemoteSession;
import org.rapla.storage.LocalCache;
import org.rapla.storage.RaplaSecurityException;
import org.rapla.storage.StorageOperator;

import com.google.gwtjsonrpc.common.AsyncCallback;

public class RaplaJsonServiceFactory extends RaplaComponent implements RemoteJsonFactory<RaplaJsonService>
{
//	ResourceDescriptor test1 = new ResourceDescriptor("1","Room1","resource");
//	ResourceDescriptor test2 = new ResourceDescriptor("2","[Room2","resource");
//	List<ResourceDescriptor> testList = new ArrayList<ResourceDescriptor>();
//	{
//		testList.add( test1 );
//		testList.add( test2 );
//	}
//	ResourceDetail detail;
//	{
//		LinkedHashMap<String,  ResourceDetailRow> details = new LinkedHashMap<String, ResourceDetailRow>();
//		List<Event> events = new ArrayList<Event>();
//		events.add(new Event("Vorlesung","10:00", "11:00",testList));
//		details.put( "name", new ResourceDetailRow("Name","resource1"));
//		details.put( "bild", new ResourceDetailRow("Bild", "bildlink"));
//		detail = new ResourceDetail( details, events);
//	}
//	
	AllocatableExporter exporter;
	EntityResolver resolver;
	
	public RaplaJsonServiceFactory(RaplaContext context, Configuration config) throws RaplaException {
		super( context);
		exporter = new AllocatableExporter(context, config);
		resolver = getContext().lookup(StorageOperator.class);
	}
	
	@Override
	public RaplaJsonService createService(RemoteSession remoteSession)
			throws RaplaContextException {
		return new RaplaJsonService() {
			
			@Override
			public void getResources(String type, String categoryId,AsyncCallback<List<ResourceDescriptor>> callback) {
				try
				{
					Category category = getCategoryForId(categoryId);
					List<ResourceDescriptor> result = exporter.getAllocatableList(type, category);
					callback.onSuccess(  result);
				}
				catch (Exception ex)
				{
					callback.onFailure( ex);
				}
			}

			private Category getCategoryForId(String categoryId)
					throws EntityNotFoundException, RaplaException {
				Category category = null;
				if ( categoryId != null && !categoryId.trim().isEmpty())
				{
					category = (Category)resolver.resolve( LocalCache.getId(categoryId));
				}
				return category;
			}

			@Override
			public void getResource(String id,AsyncCallback<ResourceDetail> callback) 
			{
				// Todo replace with correct link
//				StringBuffer a = request.getRequestURL();
//				int indexOf = a.lastIndexOf("/rapla");
//				String linkPrefix = a.substring(0, indexOf);
				String linkPrefix = "http://localhost:8051/rapla";
				try
				{
					Comparable id2 = LocalCache.getId(id);
					Allocatable allocatable = (Allocatable)resolver.resolve( id2);
					ResourceDetail detail = exporter.getAllocatable(allocatable, linkPrefix);
					if ( detail != null)
					{
						callback.onSuccess( detail);
					}
					else
					{
						callback.onFailure( new RaplaSecurityException("No permission to read resource"));
					}
				}
				catch (Exception ex)
				{
					callback.onFailure( ex);
				}
				
			}

			@Override
			public void getOrganigram(String categoryId,AsyncCallback<List<CategoryDescription>> callback) {
				Category category;
				try
				{
					category = getCategoryForId( categoryId);
				}
				catch (Exception ex)
				{
					callback.onFailure( ex);
					return;
				}
				if ( category == null)
				{
					 Category root = getQuery().getSuperCategory();
					//TODO need to replace with correct category key
					category = root.getCategory("c2");
					if ( category == null)
					{
						callback.onFailure( new EntityNotFoundException("Category with key c2 needed"));
						return;
					}
				}
				List<CategoryDescription> result = get( category);
				callback.onSuccess( result);
			}
			
			List<CategoryDescription> get( Category cat)
			{
				List<CategoryDescription> children  =new ArrayList<CategoryDescription>();
				for (Category child : cat.getCategories())
				{
					String id = ((RefEntity<?>)child).getId().toString();
					String name = child.getName( getLocale());
					CategoryDescription childDescription = new CategoryDescription(id, name);
					children.add( childDescription);
				}
				return children;
			}
			

			
		};
	}

}
