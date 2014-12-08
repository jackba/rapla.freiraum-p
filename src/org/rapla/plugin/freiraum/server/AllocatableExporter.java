package org.rapla.plugin.freiraum.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.rapla.components.util.DateTools;
import org.rapla.components.util.SerializableDateTimeFormat;
import org.rapla.components.util.TimeInterval;
import org.rapla.components.xmlbundle.I18nBundle;
import org.rapla.entities.Category;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.NamedComparator;
import org.rapla.entities.User;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.entities.domain.AppointmentBlockStartComparator;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.dynamictype.Attribute;
import org.rapla.entities.dynamictype.Classification;
import org.rapla.entities.dynamictype.ClassificationFilter;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.entities.dynamictype.internal.DynamicTypeImpl;
import org.rapla.entities.dynamictype.internal.ParsedText;
import org.rapla.entities.dynamictype.internal.ParsedText.EvalContext;
import org.rapla.facade.QueryModule;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaLocale;
import org.rapla.plugin.freiraum.FreiraumPlugin;
import org.rapla.plugin.freiraum.TerminalConstants;
import org.rapla.plugin.freiraum.common.Event;
import org.rapla.plugin.freiraum.common.ResourceDescription;
import org.rapla.plugin.freiraum.common.ResourceDetail;
import org.rapla.plugin.freiraum.common.ResourceDetailRow;
import org.rapla.plugin.urlencryption.UrlEncryption;
import org.rapla.storage.StorageOperator;

public class AllocatableExporter extends RaplaComponent implements TerminalConstants {

    public SerializableDateTimeFormat dateTimeFormat;
    RaplaLocale raplaLocale;
    private DynamicType[] courseType;
    private DynamicType[] roomType;
    private DynamicType[] resourceTypes;
    private DynamicType[] eventTypes;
    private DynamicType[] externalPersonTypes;
    //User stele;
	String userid;
    Configuration config;
    private UrlEncryption encryptionservice;
    
    public AllocatableExporter(RaplaContext context, Configuration config) throws RaplaException {
    	super(context);
    	if (context.has(UrlEncryption.class))
        {
    	    encryptionservice = context.lookup(UrlEncryption.class);
        }
        
    	this.config = config;
    	raplaLocale = getRaplaLocale();
        dateTimeFormat = new SerializableDateTimeFormat();
        
        eventTypes = getDynamicTypesForKey(TerminalConstants.EVENT_TYPES_KEY);
        resourceTypes = getDynamicTypesForKey(TerminalConstants.RESOURCE_TYPES_KEY);
        roomType = getDynamicTypesForKey(TerminalConstants.ROOM_KEY);
        courseType = getDynamicTypesForKey( TerminalConstants.KURS_KEY);
        String terminalUser = config.getChild(TerminalConstants.USER_KEY).getValue(null);
        externalPersonTypes = getDynamicTypesForKey(TerminalConstants.EXTERNAL_PERSON_TYPES_KEY);
        if (terminalUser == null) {
            throw new RaplaException("Terminal User must be set to use export");
        }
        User user = getQuery().getUser(terminalUser);
        if ( user == null)
        {
        	throw new RaplaException("Terminal User " + terminalUser + " not found ");
        }
		userid = user.getId();
        
        setChildBundleName(FreiraumPlugin.RESOURCE_FILE);
    }

    DynamicType[] getDynamicTypesForKey(String configKey) throws RaplaException {
        final List<DynamicType> result = new ArrayList<DynamicType>();
        String configValues = config.getChild(configKey).getValue(null);
        if (configValues == null) {
            throw new RaplaException("Configuration in Terminal Plugin incorrect. Please check setting for " + configKey);
        }
        try {
            final String[] dynamicTypeKeys = configValues.split(",");
            for (String dynamicTypeKey : dynamicTypeKeys) {
                if (dynamicTypeKey.trim().isEmpty())
                    continue;
                DynamicType dynamicType = getQuery().getDynamicType(dynamicTypeKey);
                if (dynamicType == null) {
                    throw new RaplaException("Configuration in Terminal Plugin incorrect. Dynamic Type '" + dynamicTypeKey + "' does not exist. Please check setting for " + configKey);
                }
                result.add(dynamicType);
            }
        } catch (RaplaException e) {
            throw new RaplaException("Configuration in Terminal Plugin incorrect. Please check setting for " + configKey, e);
        }
        // sort to use arrays binary search afterwards
        Collections.sort(result, new Comparator<DynamicType>() {
            @Override
            public int compare(DynamicType o1, DynamicType o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        return result.toArray(new DynamicType[result.size()]);
    }
    
	public List<ResourceDescription> getAllocatableList(String type,	Category category,Locale locale) throws RaplaException {
		List<ResourceDescription> result = new ArrayList<ResourceDescription>();
        List<Allocatable> allocatables = getAllAllocatables();
        for (Allocatable allocatable : allocatables) {
        	Classification classification = allocatable.getClassification();
            DynamicType dynamicType = classification.getType();
            if ( type != null && !type.trim().isEmpty())
            {
            	if ( type.equals("courses") )
            	{
            		if (!isCourse( allocatable))
            		{
            			continue;
            		}
            	}
            	else if ( type.equals("persons") )
            	{
            		if ( ! allocatable.isPerson())
            		{
            			continue;
            		}
            	}
            	else if ( type.equals("rooms") )
            	{
            		if (!isRoom( allocatable))
            		{
            			continue;
            		}
            	}
            	else
            	{
            		DynamicType dynamicType2 = getQuery().getDynamicType( type);
            		if ( !dynamicType.equals(dynamicType2 ))
            		{
            			continue;
            		}
            		
            	}
            }
            // TODO special case external persons maybe better modeled with permissions
        	if ( isExternalPerson( dynamicType))
        	{
        		Date today = getQuery().today();
        		List<AppointmentBlock> blocks = getReservationBlocks(allocatable, today);
        		//only export external persons if they have events today
        		if ( blocks.size()== 0)
        		{
        			continue;
        		}
        	}
        	if ( category != null )
        	{
        		Attribute attribute = dynamicType.getAttribute("abteilung");
        		if (attribute != null)
        		{
        			Object abteilung = classification.getValue(attribute);
        			if ( abteilung == null || !(abteilung instanceof Category))
        			{
        				continue;
        			}
        			else
        			{
        				Category allocCat = (Category) abteilung;
        				if ( !allocCat.equals( category) && !category.isAncestorOf( allocCat))
        				{
            				continue;
        				}
        			}
        		}
        		else
        		{
        			continue;
        		}
        	}
        	ResourceDescription description = getAllocatableNameIfReadable(allocatable,locale);
        	if ( description != null  ) {
        		result.add( description);
            }
        }
        return result;
	}
	
	public List<Event> getEvents(Allocatable allocatable,TimeInterval interval,Locale locale) throws RaplaException
	{
		List<Event> events = new ArrayList<>();
		List<AppointmentBlock> blocks = getReservationBlocks(allocatable, interval, false);
		for (AppointmentBlock block : blocks) 
		{
			Event event = createEvent(block, locale);
            events.add( event );
		}
		return events;
	}
	
	public List<Event> getFreeRooms(TimeInterval interval,String type,Locale locale) throws RaplaException
	{
		List<Event> result = new ArrayList<>();
		int maxFreeAllocatables = 99;
        //testbetrieb
        /* {
            String elementName = "freierRaum";
            openElement(elementName);
            printOnLine("name", "Name", "Achtung: ");
            printOnLine("freiBis", "freiBis","Daten vom 21.05.13");
            closeElement(elementName);
            maxFreeAllocatables = 2;

        }*/

        {
            List<Allocatable> allocatables = new ArrayList<Allocatable>();
            QueryModule facade = getQuery();
			for (DynamicType typeKey : roomType) {
				if ( type == null || typeKey.getKey().equals( type))
				{
	                ClassificationFilter filter = typeKey.newClassificationFilter();
	                allocatables.addAll(Arrays.asList(facade.getAllocatables(filter.toArray())));
				}
            }
            int c = 1;
            User stele = getTerminalUser();
            Date date = getQuery().today();
            for (Allocatable allocatable : allocatables) {
                if (c > maxFreeAllocatables)
                    break;
                
				if (!allocatable.canAllocate(stele, null, null, date)) {
                    continue;
                }

            	Date ende = null;
            	boolean isUsed = false;
                //Date currentTimeInGMT = raplaLocale.toRaplaTime(raplaLocale.getImportExportTimeZone(), new Date());
                Date requestedStart = interval.getStart();
                Date requestedEnd = interval.getEnd();
                if (requestedStart == null)
                {
                	// current time
                	requestedStart = raplaLocale.toRaplaTime(raplaLocale.getImportExportTimeZone(), new Date());
                }
                Date endInterval =  requestedEnd != null ? requestedEnd:DateTools.fillDate(requestedStart); 
            	TimeInterval timeInverval = new TimeInterval( requestedStart, endInterval );
				for (AppointmentBlock block : getReservationBlocks(allocatable, timeInverval, true)) {
                    Date blockStart = new Date(block.getStart());
                    Date blockEnd = new Date(block.getEnd());
                    if (blockEnd.after(requestedStart)) {
                        if (!blockStart.before( requestedEnd != null ? requestedEnd : requestedStart )) {
                        	if ( ende == null || blockStart.before( ende))
                        	{
                        		ende = blockStart;
                        	}
                        } else {
                            isUsed = true;
                            break;
                        }
                    }
                }
				if  (ende == null)
				{
					ende = endInterval;
				}
                if (!isUsed && ende.after( requestedStart)) {
                	ResourceDescription descriptor = getAllocatableNameIfReadable(allocatable, locale);
                    String startDate = raplaLocale.formatDate( requestedStart);
                    String start = raplaLocale.formatTime( requestedStart );
                    String endDate = raplaLocale.formatDate( ende );
                    String end = raplaLocale.formatTime(ende);
                	List<ResourceDescription> resourceList = Collections.singletonList( descriptor);
					Event event = new Event("free", startDate, start, endDate, end,resourceList);
					result.add( event);
                	c++;
                }
            }
        }
        return result;
	}
	
	private User getTerminalUser() throws EntityNotFoundException {
		return getClientFacade().getOperator().resolve(userid, User.class);
	}

//	 public void printFreeAllocatable(String name, Date ende) throws IOException {
//	        String elementName = "freierRaum";
//	        String endString = "19:00";
//	        if (ende != null) {
//	            endString = raplaLocale.formatTime(ende);
//	        }
//	        printOnLine("name", "Name", name);
//	        printOnLine("freiBis", "frei bis", endString);
//	        closeElement(elementName);
//	    }


	public List<Allocatable> getAllAllocatables() throws RaplaException {
		List<Allocatable> allocatables = new ArrayList<Allocatable>();


		for (DynamicType dynamicType : resourceTypes) {
		    String typeKey = dynamicType.getKey();
		    ClassificationFilter filter = null;
		    try {
		        filter = getQuery().getDynamicType(typeKey).newClassificationFilter();
		    } catch (RaplaException e) {
		        System.err.println(e.getMessage());
		        continue;
		    }
		    ArrayList<Allocatable> sortedAllocatables = new ArrayList<Allocatable>(Arrays.asList(getQuery().getAllocatables(filter.toArray())));
		    Locale locale = raplaLocale.getLocale();
		    Collections.sort(sortedAllocatables, new NamedComparator<Allocatable>(locale));
		    allocatables.addAll(sortedAllocatables);
		}
		return allocatables;
	}

	 public ResourceDescription getAllocatableNameIfReadable(Allocatable allocatable,Locale locale) throws EntityNotFoundException {
	        Classification classification = allocatable.getClassification();
	        if (classification == null)
	            return null;
	        User stele = getTerminalUser();
	        if ( !allocatable.canReadOnlyInformation( stele) )
	        {
	        	return null;
	        }

	        DynamicType dynamicType = classification.getType();
	     
            String name;
            if (isRoom(dynamicType)) { //elementName.equals(ROOM_KEY)) {
                name = getRoomName(classification, true, false,locale);
            } else if (isCourse(dynamicType)) { //elementName.equals(KURS_KEY)) {
                StringBuffer buf = new StringBuffer();
                Object titel = classification.getValue("name");
                if (titel != null) {
                    buf.append(titel);
                }
                name = buf.toString();
            } else if (allocatable.isPerson()) {
                StringBuffer buf = new StringBuffer();

                Object titel = classification.getValue("title");
                if (titel != null) {
                    buf.append(titel + " ");
                }
                Object vorname = classification.getValue("firstname");
                if (vorname != null && vorname.toString().length() > 0) {
                    buf.append(vorname.toString().substring(0, 1) + ". ");
                }
                Object surname = classification.getValue("surname");
                if (surname != null) {
                    buf.append(surname);
                }
                name = buf.toString();
            } else {
                name = classification.getName(locale);
            }
            String id = allocatable.getId();
	        String link = "getResource?resourceId="+id;
	        List<String> searchTerms = new ArrayList<String>(getSearchTerms(allocatable,locale));
			return new ResourceDescription( id, name, link,searchTerms);
	 }

	 
	private Collection<String> getSearchTerms(Allocatable allocatable,Locale locale)
	{
		Classification classification = allocatable.getClassification();
        DynamicType dynamicType = classification.getType();
        
		LinkedHashSet<String> search = new LinkedHashSet<String>();
        if (isRoom(dynamicType)) { //elementName.equals(ROOM_KEY)) {
            search.add( getRoomName(classification, true, false, locale));
        } else if (isCourse(dynamicType)) { //elementName.equals(KURS_KEY)) {
            Object titel = classification.getValue("name");
            if (titel != null) {
            	search.add( getStringValue(titel,locale));
            }
        } else if (allocatable.isPerson()) {
            Object surname = classification.getValue("surname");
            if (surname != null) {
                search.add( getStringValue( surname, locale));
            }
            if ( surname == null)
            {
	            surname = classification.getValue("lastname");
	            if (surname != null) {
	                search.add( getStringValue( surname, locale));
	            }
            }
        	Object vorname = classification.getValue("firstname");
            if (vorname != null && vorname.toString().length() > 0) {
                search.add( getStringValue( vorname,locale));
            }

        } else {
        	String name = classification.getName(locale);
        	if ( name != null)
        	{
        		search.add( name);
        	}
        }
        addSearchIfThere(classification, search, "raumart",locale);
        return search;
	}

	public ResourceDetail getAllocatable(Allocatable allocatable, String linkPrefix,Locale locale) throws RaplaException {
		User stele = getTerminalUser();
		if (!allocatable.canReadOnlyInformation(stele)) {
			return null;
		}
		boolean exportReservations = allocatable.canRead(stele);
		Map<String, ResourceDetailRow> attributes = new LinkedHashMap<String, ResourceDetailRow>();
		
		final Classification classification = allocatable.getClassification();
        DynamicType dynamicType = classification.getType();
        
        String elementName = allocatable.isPerson() ? "person" : dynamicType.getKey();

        I18nBundle i18n = getI18n();
		{
            String name = getAllocatableNameIfReadable(allocatable, locale).getName();
            final String label;
            if (isRoom(dynamicType)) { //elementName.equals(ROOM_KEY)) {
                label = dynamicType.getName(locale);
            } else if (isCourse(dynamicType)) { //elementName.equals(KURS_KEY)) {
                label = dynamicType.getName(locale);
            } else if (allocatable.isPerson()) {
                 label = i18n.getString("name", locale);
            } else if (classification.getAttribute("name") != null){
            	label = classification.getAttribute("name").getName( locale );
            }
            else
            {
                label = i18n.getString("name", locale);
            }
            attributes.put("name",printOnLine(label, name, locale));
        }

        printAttributeIfThere(attributes,classification, "jahrgang", locale);
        printAttributeIfThere(attributes, classification,  "abteilung", locale);

        printAttributeIfThere(attributes,classification,  "email", locale);
        {
            ParsedText parsedAnnotation = ((DynamicTypeImpl)dynamicType).getParsedAnnotation(TerminalConstants.IMAGE_URL_FORMAT_ANNOTATION);
            if (parsedAnnotation != null)
            {
                String label ="image";
                EvalContext evalContext = new EvalContext(locale)
                {
                    public Classification getClassification()
                    {
                        return classification;
                    }
                };
                Object value = parsedAnnotation.formatName(evalContext);
                ResourceDetailRow row  = printOnLine( label, value, locale);
                if (row != null)
                {
                    attributes.put( "bild", row);
                }
            }
            else
            {
                printAttributeIfThere(attributes,classification,  "bild", locale);
            }
        }
        {
            ParsedText parsedAnnotation = ((DynamicTypeImpl)dynamicType).getParsedAnnotation(TerminalConstants.LOCATION_URL_FORMAT_ANNOTATION);
            if (parsedAnnotation != null)
            {
                String label ="location";
                EvalContext evalContext = new EvalContext(locale)
                {
                    public Classification getClassification()
                    {
                        return classification;
                    }
                };
                Object value = parsedAnnotation.formatName(evalContext);
                ResourceDetailRow row  = printOnLine( label, value, locale);
                if (row != null)
                {
                    attributes.put( "location", row);
                }
            }
        }
        printAttributeIfThere(attributes,classification,  "telefon", locale);
        printAttributeIfThere(attributes,classification,  "raumart", locale);
        for (int i = 0; i < 10; i++)
            printAttributeIfThere(attributes,classification, "zeile" + i, locale);


        Map<String, ResourceDescription> resourceLinks = new LinkedHashMap<String,ResourceDescription>();
        {
        	Attribute attribute = classification.getAttribute("raum");
        	if ( attribute != null)
        	{
        		String roomName = getRoomName(classification, true, true,locale);
        		String roomLabel = attribute.getName(locale);
        		attributes.put("raumnr",printOnLine( roomLabel, roomName, locale));
        	}
        }
        {
            Attribute attribute = classification.getAttribute("raumnummer");
            if ( attribute != null)
            {
                Object value = classification.getValue( attribute );
                if ( value != null && value instanceof Allocatable)
                {
                    Allocatable alloc = (Allocatable) value;
                    ResourceDescription descriptor = getAllocatableNameIfReadable(alloc,locale);
                    if ( descriptor != null)
                    {
                        resourceLinks.put("raum",descriptor);
                    }
                }
            }
        }
        if (exportReservations ) 
        {
            String attributeName = "resourceURL";
            String id = allocatable.getId();
            String pageParameters = "page=calendar&user=" + stele.getUsername() + "&file=" + elementName + "&allocatable_id=" + id;
            String encryptedParamters = encryptionservice != null ?  UrlEncryption.ENCRYPTED_PARAMETER_NAME + "=" + encryptionservice.encrypt(pageParameters) : pageParameters;
            //String encryptedParamters = pageParameters;
            String url = linkPrefix + "/rapla?" + encryptedParamters;
            String label;
            if (allocatable.isPerson())
            {
            	label =i18n.getString("appointments", locale);
            } else if (isCourse(dynamicType)) {
                label = i18n.getString("reservations", locale);
            } else if (isRoom(dynamicType)) {
            	label = i18n.getString("allocation", locale);
            }  else {
                label =i18n.getString("info", locale);
            }
            try {
                attributes.put( attributeName,new ResourceDetailRow( label, new URI(url).toString()));
            } catch (URISyntaxException ex) {
                //FIXME log exceptions
            }
        }

        Attribute infoAttr = classification.getAttribute("info");
        if (infoAttr != null) {
            printAttributeIfThere(attributes,classification, "info", locale);
        } 
        
  
        ResourceDetail result = new ResourceDetail(attributes, resourceLinks);
		return result;
	}

	public Event createEvent(AppointmentBlock block,Locale locale) throws EntityNotFoundException {
		Appointment appointment = block.getAppointment();
		List<ResourceDescription> resources = getResources( appointment,locale);
		Reservation reservation = appointment.getReservation();
		String startDate = raplaLocale.formatDate(new Date(block.getStart()));
		String start = raplaLocale.formatTime(new Date(block.getStart()));
		String endDate = raplaLocale.formatDate(new Date(block.getEnd()));
		String end = raplaLocale.formatTime(new Date(block.getEnd()));

		String title = reservation.getName(locale);
		Event event = new Event(title, startDate, start,  endDate, end, resources);
		return event;
	}

	private boolean isExternalPerson(DynamicType dynamicType) {
		return Arrays.binarySearch(externalPersonTypes, dynamicType) >= 0;
	}

	public boolean isRoom(DynamicType dynamicType) {
		return Arrays.binarySearch(roomType, dynamicType) >= 0;
	}

	public boolean isRoom(Allocatable alloc) {
		DynamicType dynamicType = alloc.getClassification().getType();
		return Arrays.binarySearch(roomType, dynamicType) >= 0;
	}

	public boolean isCourse(DynamicType dynamicType) {
		return Arrays.binarySearch(courseType, dynamicType) >= 0;
	}

	public boolean isCourse(Allocatable alloc) {
		DynamicType dynamicType = alloc.getClassification().getType();
		return Arrays.binarySearch(courseType, dynamicType) >= 0;
	}

	private boolean isReservationTypeAllowed(Reservation res) throws EntityNotFoundException {
		if (Arrays.binarySearch(eventTypes, res.getClassification().getType()) <0)
			return false;
		User stele = getTerminalUser();
		return canRead(res, stele, getEntityResolver() );
	}


	private void printAttributeIfThere(Map<String, ResourceDetailRow> map,Classification classification, String attributeName,Locale locale) {
		 Attribute attribute = classification.getAttribute(attributeName);
		 if (attribute != null) {
			 Object value = classification.getValue(attribute);
			 String label = attribute.getName( locale);
			 if ( value != null)
			 {
			   ResourceDetailRow row  = printOnLine( label, value, locale);
	            if (row != null)
	            {
	            	map.put( attributeName, row);
	            }
			 }
		 }
   }

//    private void printAttributeIfThere(Map<String, ResourceDetailRow> map,Classification classification, String label, String attributeName, String tagName) throws IOException {
//        Attribute attribute = classification.getAttribute(attributeName);
//        if (attribute != null) {
//            Object value = classification.getValue(attribute);
//            if ( value != null)
//            {
//	            ResourceDetailRow row  = printOnLine( label, value);
//	            if (row != null)
//	            {
//	            	map.put( tagName, row);
//	            }
//            }
//        }
//    }
//    
    
    
    private ResourceDetailRow printOnLine(String label, Object content, Locale locale) {
    	String string = getStringValue( content, locale);
    	return new ResourceDetailRow( label, string);
    }

    public String getRoomName(Classification classification, boolean fluegel, boolean validFilename, Locale locale) {
        Category superCategory = getQuery().getSuperCategory();
        StringBuffer buf = new StringBuffer();
        if (classification.getAttribute("raum") != null) {
            Object raum = classification.getValue("raum");
            if (raum instanceof Category) {
                Category category = (Category) raum;
                Category parent = category.getParent();
                if (!fluegel || parent.getParent().equals(superCategory))
                    parent = null;
                buf.append(parent != null ? parent.getName(locale) : "").append(category.getKey().replace((parent != null ? parent.getName(locale) : ""), ""));
            } else if (raum instanceof Allocatable){
                Classification classificationRoom = ((Allocatable) raum).getClassification();
                Object value = classificationRoom.getValue("raum");
                if (value != null && value instanceof Category)
                {
                    Category category = (Category) value;
                    Category parent = category.getParent();
                    if (!fluegel || parent.getParent().equals(superCategory))
                        parent = null;
                    buf.append(parent != null ? parent.getName(locale) : "").append(category.getKey().replace((parent != null ? parent.getName(locale) : ""), ""));
                    
                }
                else
                {
                    buf.append(((Allocatable)raum).getName(locale));
                }
            } else {
                if (raum != null)
                    buf.append(raum.toString());
            }
        }
        String result = buf.toString();
        return validFilename ? result.replaceAll("[-,\\,/,\\s,\\,,:]*", "") : result;
    }

    private List<ResourceDescription> getResources(Appointment appointment,Locale locale) throws EntityNotFoundException {
        List<ResourceDescription> resources = new ArrayList<ResourceDescription>();
    	Reservation reservation = appointment.getReservation();
        for (Allocatable alloc : reservation.getAllocatablesFor(appointment)) {
            //DynamicType type = alloc.getClassification().getType();
            //String elementKey = type.getElementKey();
            //if ((!isKurs && isCourse(type)) || (!isRaum && isRoom(type)))
            {
            	ResourceDescription descriptor = getAllocatableNameIfReadable(alloc,locale);
            	if ( descriptor != null)
            	{
            		resources.add(descriptor);
            	}
            }
        }
        return resources;
    }

    private List<AppointmentBlock> getReservationBlocks(Allocatable allocatable, Date date) throws RaplaException {
        return getReservationBlocks(allocatable,  new TimeInterval( date, DateTools.addDay(date)), false );
    }

	private List<AppointmentBlock> getReservationBlocks(Allocatable allocatable,TimeInterval interval, boolean includeAllEvents) throws RaplaException {
        QueryModule facade = getQuery();
		List<AppointmentBlock> array = new ArrayList<AppointmentBlock>();
        Date start = interval.getStart();
		Date end = interval.getEnd();
		Reservation[] reservations = facade.getReservations(new Allocatable[]{allocatable}, start, end);
        for (Reservation res : reservations) {
        	if (includeAllEvents || isReservationTypeAllowed(res))
        	{
                for (Appointment app : res.getAppointmentsFor(allocatable)) {
                	Date appStart = start != null ? start : app.getStart();
                	Date appEnd = end != null ? end : app.getMaxEnd();
                	if  ( appEnd == null)
                	{
                		appEnd = DateTools.addYears( appStart, 5);
                	}
                    app.createBlocks(appStart, appEnd, array);
                }
        	}
        }
        Collections.sort(array, new AppointmentBlockStartComparator());
        return array;
	}

    private void addSearchIfThere(Classification classification,
                                  Set<String> search, String attributeName,Locale locale) {
        Attribute attribute = classification.getAttribute(attributeName);
        if (attribute != null) {
            Object value = classification.getValue(attribute);
            if (value != null) {
                String string = getStringValue(value, locale);
                search.add(string);
            }
        }
    }
   
    private String getStringValue(Object value,Locale locale) {
        if (value instanceof Category) {
            String toString = ((Category) value).getName(locale);
            return toString;
        } else if (value instanceof Date) {
            final Date date;
            if (value instanceof Date)
                date = (Date) value;
            else
                date = null;
            return dateTimeFormat.formatDate(date);
        } else {
            return value.toString();
        }
    }


}