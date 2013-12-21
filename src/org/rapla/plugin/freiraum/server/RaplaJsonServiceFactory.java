package org.rapla.plugin.freiraum.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.rapla.entities.Category;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.MultiLanguageName;
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
			public void getResources(String type, String categoryId, String language,AsyncCallback<List<ResourceDescriptor>> callback) {
				try
				{
					Category category = getCategoryForId(categoryId);
					Locale locale = getLocale( language);
					List<ResourceDescriptor> result = exporter.getAllocatableList(type, category, locale);
					callback.onSuccess(  result);
				}
				catch (Exception ex)
				{
					getLogger().error(ex.getMessage(), ex);
					callback.onFailure( ex);
				}
			}

			@Override
			public void getResource(String id, String language,AsyncCallback<ResourceDetail> callback) 
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
					Locale locale = getLocale(language); 
					ResourceDetail detail = exporter.getAllocatable(allocatable, linkPrefix, locale);
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
					getLogger().error(ex.getMessage(), ex);
					callback.onFailure( ex);
				}
				
			}

			public Locale getLocale(String language) {
				return language != null && language.trim().toLowerCase().equals("en") ? Locale.ENGLISH: Locale.GERMAN;
			}

			@Override
			public void getOrganigram(String categoryId, String language,AsyncCallback<List<CategoryDescription>> callback) {
				Category category;
				if ( categoryId == null)
				{
					category = getOrganigram();
					if ( category == null)
					{
						callback.onFailure( new EntityNotFoundException("Category with name studiengang needed"));
						return;
					}
				}
				else
				{
					try
					{
						category = getCategoryForId( categoryId);
					}
					catch (Exception ex)
					{
						getLogger().error(ex.getMessage(), ex);
						callback.onFailure( ex);
						return;
					}
				}
				Locale locale = getLocale( language);
				List<CategoryDescription> result = get( category, locale);
				callback.onSuccess( result);
			}

			private Category getOrganigram() {
				Category root = getQuery().getSuperCategory();
				// meanwhile we use a fallback
				for (Category cat: root.getCategories())
				{
					MultiLanguageName name = cat.getName();
					Collection<String> availableLanguages = name.getAvailableLanguages();
					for ( String language: availableLanguages)
					{
						String translation = name.getName(language);
						// this captures all root categories with name studiengang or studienga"nge
						if (translation != null && translation.toLowerCase().indexOf("studieng")>= 0)
						{
							return cat;
						}
					}
				}
				return null;
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
			
			private List<CategoryDescription> get( Category cat, Locale locale)
			{
				List<CategoryDescription> children  =new ArrayList<CategoryDescription>();
				for (Category child : cat.getCategories())
				{
					String id = ((RefEntity<?>)child).getId().toString();
					String name = child.getName( locale);
					CategoryDescription childDescription = new CategoryDescription(id, name);
					children.add( childDescription);
				}
				return children;
			}
			

			
		};
	}

}
