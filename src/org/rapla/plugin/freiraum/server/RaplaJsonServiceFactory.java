package org.rapla.plugin.freiraum.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.rapla.components.util.ParseDateException;
import org.rapla.components.util.TimeInterval;
import org.rapla.entities.Category;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.MultiLanguageName;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.storage.EntityResolver;
import org.rapla.entities.storage.RefEntity;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaLocale;
import org.rapla.plugin.freiraum.common.CategoryDescription;
import org.rapla.plugin.freiraum.common.Event;
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
			public void getResource(String resourceId, String language,AsyncCallback<ResourceDetail> callback) 
			{
				if ( resourceId == null)
				{
					throw new IllegalArgumentException("resourceId must be set");
				}
				// FIXME replace with correct link
//				StringBuffer a = request.getRequestURL();
//				int indexOf = a.lastIndexOf("/rapla");
//				String linkPrefix = a.substring(0, indexOf);
				String linkPrefix = "http://localhost:8051/rapla";
				try
				{
					Comparable id2 = LocalCache.getId(resourceId);
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

			@Override
			public void getFreeResources(String start, String end,String resourceType, String language,	AsyncCallback<List<Event>> callback) {
				try
				{
					TimeInterval interval = createInterval(start, end);
					Locale locale = getLocale(language); 
					List<Event> result = exporter.getFreeRooms(interval, resourceType, locale);
					callback.onSuccess( result );
				}
				catch (Exception ex)
				{
					getLogger().error(ex.getMessage(), ex);
					callback.onFailure( ex);
				}
			}

			public TimeInterval createInterval(String start, String end)
					throws ParseDateException {
				Date startDate = null;
				Date endDate = null;
				if ( start != null)
				{
					startDate = getRaplaLocale().getSerializableFormat().parseTimestamp( start );
				}
				if ( end != null)
				{
					endDate = getRaplaLocale().getSerializableFormat().parseTimestamp( end );
				}
				TimeInterval interval = new TimeInterval(startDate, endDate);
				return interval;
			}
			
			@Override
			public void getEvents(String start, String end, String resourceId, String language,AsyncCallback<List<Event>> callback)
			{
				try
				{
					if ( resourceId == null)
					{
						throw new IllegalArgumentException("resourceId must be set");
					}
					Locale locale = getLocale(language); 
					TimeInterval interval = createInterval(start, end);
					Comparable id2 = LocalCache.getId(resourceId);
					Allocatable allocatable = (Allocatable)resolver.resolve( id2);
					List<Event> result = exporter.getEvents(allocatable, interval,  locale);
					callback.onSuccess( result );
				}
				catch (Exception ex)
				{
					getLogger().error(ex.getMessage(), ex);
					callback.onFailure( ex);
				}
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
				// there is no given key for the organigram, so we use a fallback
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

			private Locale getLocale(String language) {
				RaplaLocale raplaLocale = getRaplaLocale();
				if ( language == null  || language.trim().length() == 0)
				{
					return raplaLocale.getLocale();
				}
				return new Locale(language);
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
