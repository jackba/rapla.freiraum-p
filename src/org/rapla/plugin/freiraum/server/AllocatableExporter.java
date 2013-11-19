package org.rapla.plugin.freiraum.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.rapla.components.util.DateTools;
import org.rapla.components.util.SerializableDateTimeFormat;
import org.rapla.components.util.xml.XMLWriter;
import org.rapla.entities.Category;
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
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaLocale;
import org.rapla.plugin.freiraum.TerminalConstants;

public class AllocatableExporter extends XMLWriter implements TerminalConstants {

    public SerializableDateTimeFormat dateTimeFormat;
    RaplaLocale raplaLocale;
    Locale locale;
    ClientFacade facade;
    Date currentTimeInGMT;
    private String terminalUser;
    private DynamicType[] courseType;
    private DynamicType[] roomType;
    private DynamicType[] resourceTypes;
    private DynamicType[] eventTypes;
    private DynamicType[] externalPersonTypes;

    public AllocatableExporter(RaplaContext context, Configuration config) throws RaplaException {
        this(context, config, context.lookup(ClientFacade.class));
    }

    public AllocatableExporter(RaplaContext context, Configuration config, ClientFacade facade) throws RaplaException {
        raplaLocale = context.lookup(RaplaLocale.class);
        this.facade = facade;
        dateTimeFormat = new SerializableDateTimeFormat();
        locale = raplaLocale.getLocale();
        currentTimeInGMT = raplaLocale.toRaplaTime(raplaLocale.getImportExportTimeZone(), new Date());
        eventTypes = getDynamicTypesForKey(config, facade, TerminalConstants.EVENT_TYPES_KEY);
        resourceTypes = getDynamicTypesForKey(config, facade, TerminalConstants.RESOURCE_TYPES_KEY);
        roomType = getDynamicTypesForKey(config, facade, TerminalConstants.ROOM_KEY);
        courseType = getDynamicTypesForKey(config, facade, TerminalConstants.KURS_KEY);
        terminalUser = config.getChild(TerminalConstants.USER_KEY).getValue(null);
        externalPersonTypes = getDynamicTypesForKey(config, facade, TerminalConstants.EXTERNAL_PERSON_TYPES_KEY);
        if (terminalUser == null) {
            throw new RaplaException("Terminal User must be set to use export");
        }
    }

    static DynamicType[] getDynamicTypesForKey(Configuration config, ClientFacade facade, String configKey) throws RaplaException {
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
                DynamicType dynamicType = facade.getDynamicType(dynamicTypeKey);
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
                return o1.getElementKey().compareTo(o2.getElementKey());
            }
        });
        return result.toArray(new DynamicType[result.size()]);
    }

    public void printFreeAllocatable(String name, Date ende) throws IOException {
        String elementName = "freierRaum";
        openElement(elementName);
        String endString = "19:00";
        if (ende != null) {
            endString = raplaLocale.formatTime(ende);
        }
        printOnLine("name", "Name", name);
        printOnLine("freiBis", "frei bis", endString);
        closeElement(elementName);
    }


    /**
     * creates the export xml-file for the terminal
     */
    public void export(BufferedWriter buf, String linkPrefix) throws RaplaException, IOException {
        String encoding = "utf-8";
        buf.append("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
        buf.append("\n<RaplaImport version=\"0.9\">\n");
        setWriter(buf);
        setIndentLevel(1);
        User stele = facade.getUser(terminalUser);
        {
            List<Allocatable> allocatables = getAllAllocatables();
            for (Allocatable alloc : allocatables) {
                if (alloc.canReadOnlyInformation(stele)) {
                    boolean exportReservations = alloc.canRead(stele);
                    printAllocatable(alloc, linkPrefix, exportReservations);
                }
            }
        }

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
            for (DynamicType typeKey : roomType) {
                ClassificationFilter filter = typeKey.newClassificationFilter();
                allocatables.addAll(Arrays.asList(facade.getAllocatables(filter.toArray())));
            }
            Date today = facade.today();
            int c = 1;
            for (Allocatable allocatable : allocatables) {
                if (c > maxFreeAllocatables)
                    break;
                if (!allocatable.canAllocate(stele, null, null, today)) {
                    continue;
                }

                Date ende = null;
                //Date start = null;
                boolean isUsed = false;
                for (AppointmentBlock block : getReservationBlocksToday(allocatable)) {
                    Date blockStart = new Date(block.getStart());
                    Date blockEnd = new Date(block.getEnd());
                    if (blockEnd.after(currentTimeInGMT)) {
                        if (!blockStart.before(currentTimeInGMT)) {
                            ende = blockStart;
                        } else {
                            isUsed = true;
                            break;
                        }
                    }
                }
                if (!isUsed) {
                    String name = getRoomName(allocatable.getClassification(), true, false);
                    printFreeAllocatable(name, ende);
                    c++;
                }
            }
        }

        buf.append("</RaplaImport>");
    }

	public List<Allocatable> getAllAllocatables() throws RaplaException {
		List<Allocatable> allocatables = new ArrayList<Allocatable>();


		for (DynamicType dynamicType : resourceTypes) {
		    String typeKey = dynamicType.getElementKey();
		    ClassificationFilter filter = null;
		    try {
		        filter = facade.getDynamicType(typeKey).newClassificationFilter();
		    } catch (RaplaException e) {
		        System.err.println(e.getMessage());
		        continue;
		    }
		    ArrayList<Allocatable> sortedAllocatables = new ArrayList<Allocatable>(Arrays.asList(facade.getAllocatables(filter.toArray())));
		    Collections.sort(sortedAllocatables, new NamedComparator<Allocatable>(locale));
		    allocatables.addAll(sortedAllocatables);
		}
		return allocatables;
	}

	 public String getAllocatableNameIfReadable(Allocatable allocatable) throws IOException, RaplaException {
	        Classification classification = allocatable.getClassification();
	        
	        if (classification == null)
	            return null;

	        DynamicType dynamicType = classification.getType();
	        // only export resource if it matches external persons
	        // because they have lecture today
	        if (allocatable.isPerson() && isExternalPerson(dynamicType))
	            return null;
	     
	     
            String name;
            if (isRoom(dynamicType)) { //elementName.equals(ROOM_KEY)) {
                name = getRoomName(classification, true, false);
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

	        return name;
	 }

	 
    private void printAllocatable(Allocatable allocatable, String linkPrefix, boolean exportReservations) throws IOException, RaplaException {
        Classification classification = allocatable.getClassification();
        LinkedHashSet<String> search = new LinkedHashSet<String>();

        if (classification == null)
            return;
        List<AppointmentBlock> blocks = getReservationBlocksToday(allocatable);


        DynamicType dynamicType = classification.getType();
        // only export resource if it matches external persons
        // because they have lecture today
        if (allocatable.isPerson() && isExternalPerson(dynamicType) && blocks.size() == 0)
            return;
        String elementName = allocatable.isPerson() ? "person" : dynamicType.getElementKey();

        openTag(elementName);
        att("typ", dynamicType.getElementKey());
        closeTag();
        {
            String name;
            String searchTerm = null;
            final String label;
            if (isRoom(dynamicType)) { //elementName.equals(ROOM_KEY)) {
                name = getRoomName(classification, true, false);
                label = "Raum";
                searchTerm = name;
            } else if (isCourse(dynamicType)) { //elementName.equals(KURS_KEY)) {
                StringBuffer buf = new StringBuffer();
                Object titel = classification.getValue("name");
                if (titel != null) {
                    buf.append(titel);
                }
                name = buf.toString();
                searchTerm = name;
                label = "Kurs";
            } else if (allocatable.isPerson()) {
                StringBuffer buf = new StringBuffer();

                Object titel = classification.getValue("title");
                if (titel != null) {
                    buf.append(titel + " ");
                }
                Object vorname = classification.getValue("firstname");
                if (vorname != null && vorname.toString().length() > 0) {
                    buf.append(vorname.toString().substring(0, 1) + ". ");
                    searchTerm = vorname.toString();
                }
                Object surname = classification.getValue("surname");
                if (surname != null) {
                    buf.append(surname);
                    searchTerm = surname.toString();
                }
                name = buf.toString();
                label = "Name";
            } else {
                name = classification.getName(locale);
                searchTerm = name;
                label = "Bezeichnung";
            }

            printOnLine("name", label, name);
            if (searchTerm != null)
                search.add(searchTerm);
        }


        printAttributeIfThere(classification, "Jahrgang", "jahrgang");
        printAttributeIfThere(classification, "Studiengang", "abteilung");
        printAttributeIfThere(classification, "Studiengang", "abteilung", "studiengang");
        //addSearchIfThere(classification, search, "abteilung");

        printAttributeIfThere(classification, "E-Mail", "email");
        printAttributeIfThere(classification, "Bild", "bild");
        printAttributeIfThere(classification, "Telefon", "telefon");
        printAttributeIfThere(classification, "Raumart", "raumart");
        for (int i = 0; i < 10; i++)
            printAttributeIfThereWithElementAsLabel(classification, "zeile" + i, "zeile" + i);

        addSearchIfThere(classification, search, "raumart");
        String roomName = getRoomName(classification, true, true);
        printOnLine("raumnr", "Raum", roomName);


        if (exportReservations && blocks.size() > 0) {
            {
                String attributeName = "resourceURL";
                @SuppressWarnings("unchecked")
                Object id = ((RefEntity<Allocatable>) allocatable).getId();
                SimpleIdentifier localname = (org.rapla.entities.storage.internal.SimpleIdentifier) id;
                String key = /*allocatable.getRaplaType().getLocalName() + "_" + */ "" + localname.getKey();
                String pageParameters = "page=calendar&user=" + terminalUser + "&file=" + elementName + "&allocatable_id=" + key;
//                String encryptedParamters = encryptionservice != null ?  UrlEncryption.ENCRYPTED_PARAMETER_NAME + "=" + encryptionservice.encrypt(pageParameters) : pageParameters;
                String encryptedParamters = pageParameters;
                String url = linkPrefix + "/rapla?" + encryptedParamters;
                //todo: activate encryption
                try {
                    printOnLine(attributeName, "Link", new URI(url));
                } catch (URISyntaxException ex) {
                    //FIXME log exceptions
                }
            }
            {
                String attributeName = "linkTitel";
                openElementOnLine(attributeName);
                if (allocatable.isPerson())
                    printEncode(LINK_TITEL_PERSON);
                else if (isCourse(dynamicType))
                    printEncode(LINK_TITEL_KURS);
                else if (isRoom(dynamicType))
                    printEncode(LINK_TITEL_RAUM);
                else
                    printEncode(LINK_TITEL_DEFAULT);
                closeElementOnLine(attributeName);
                println();
            }
        }

        Attribute infoAttr = classification.getAttribute("info");
        if (infoAttr != null) {
            printAttributeIfThere(classification, "Info", "info");
        } else if (blocks.size() > 0 && exportReservations) {
            String attributeName = "info";
            openTag(attributeName);
            att("label", "Info");
            closeTag();
            print("<![CDATA[");
            println();
            for (AppointmentBlock block : blocks) {
                Appointment appointment = block.getAppointment();

                String resources = getResourceString(dynamicType,
                        appointment);
                Reservation reservation = appointment.getReservation();
                Date beginn = new Date(block.getStart());
                Date end = new Date(block.getEnd());
                String title = reservation.getName(locale);
                printInfo(title, resources, beginn, end);
            }
            print("]]>");
            println();
            closeElement(attributeName);
        } else {
            // if there are no blocks and no info attribute just add an empty attribute
            /*String attributeName = "info";
            openTag(attributeName);
            att("label", "Info");
            closeTag();
            closeElement(attributeName);*/
        }

        for (String tag : search) {
            openElementOnLine("suchbegriff");
            printEncode(tag);
            closeElementOnLine("suchbegriff");
            println();
        }
        closeElement(elementName);
    }

	public boolean isExternalPerson(DynamicType dynamicType) {
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

	public boolean isReservationTypeAllowed(Reservation res) {
		return Arrays.binarySearch(eventTypes, res.getClassification().getType()) >= 0;
	}


    private void printAttributeIfThere(Classification classification, String label, String attributeName, String tagName) throws IOException {
        Attribute attribute = classification.getAttribute(attributeName);
        if (attribute != null) {
            Object value = classification.getValue(attribute);
            printOnLineIfNotNull(tagName, label, value);
        }

    }

    public String getRoomName(Classification classification, boolean fluegel, boolean validFilename) {
        Category superCategory = facade.getSuperCategory();
        StringBuffer buf = new StringBuffer();
        if (classification.getAttribute("raum") != null) {
            Object raum = classification.getValue("raum");
            if (raum instanceof Category) {
                Category category = (Category) raum;
                Category parent = category.getParent();
                if (!fluegel || parent.getParent().equals(superCategory))
                    parent = null;
                buf.append(parent != null ? parent.getName(locale) : "").append(category.getKey().replace((parent != null ? parent.getName(locale) : ""), ""));
            } else {
                if (raum != null)
                    buf.append(raum.toString());
            }
        }
        String result = buf.toString();
        return validFilename ? result.replaceAll("[-,\\,/,\\s,\\,,:]*", "") : result;
    }


    private String getResourceString(DynamicType dynamicType,
                                     Appointment appointment) {
        Reservation reservation = appointment.getReservation();
        boolean isKurs = isCourse(dynamicType);
        boolean isRaum = isRoom(dynamicType);
        List<String> allocatableName = new ArrayList<String>();
        for (Allocatable alloc : reservation.getAllocatablesFor(appointment)) {
            DynamicType type = alloc.getClassification().getType();
            //String elementKey = type.getElementKey();
            if (!isKurs && isCourse(type)) { //elementKey.equals(KURS_KEY)) {
                allocatableName.add(alloc.getName(locale));
            }
            if (!isRaum && isRoom(type)) { //elementKey.equals(ROOM_KEY)) {
                allocatableName.add(alloc.getName(locale));
            }
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < allocatableName.size(); i++) {
            String allocName = allocatableName.get(i);
            if (i == 0) {
                buf.append("(");
            }
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(allocName);
            if (i == allocatableName.size() - 1) {
                buf.append(")");
            }
        }
        String resources = buf.toString();
        return resources;
    }

    private List<AppointmentBlock> getReservationBlocksToday(
            Allocatable allocatable) throws RaplaException {
        Date start = facade.today();
        Date end = DateTools.addDay(start);
        List<AppointmentBlock> array = new ArrayList<AppointmentBlock>();
        Reservation[] reservations = facade.getReservations(new Allocatable[]{allocatable}, start, end);
        for (Reservation res : reservations) {
            if (isReservationTypeAllowed(res))
                for (Appointment app : res.getAppointmentsFor(allocatable)) {
                    app.createBlocks(start, end, array);
                }
        }
        Collections.sort(array, new AppointmentBlockStartComparator());
        return array;
    }



    private void printOnLineIfNotNull(String tagName, String label, Object content) throws IOException {
        if (content != null) {
            printOnLine(tagName, label, content);
        }
    }

    private void printOnLine(String tagName, String label, Object content) throws IOException {
        openTag(tagName);
        if (label != null) {
            att("label", label);
        }
        closeTagOnLine();
        printValue(content);
        closeElementOnLine(tagName);
        println();
    }


    private void printInfo(String title, String resources, Date beginn, Date end) throws IOException {
        openElement("div");
        openElementOnLine("span");
        String beginnString = raplaLocale.formatTime(beginn);
        print(beginnString);
        closeElementOnLine("span");
        if (end != null) {
            print(" - ");
            openElementOnLine("span");
            String endString = raplaLocale.formatTime(end);
            print(endString);
            closeElementOnLine("span");
        }
        println();
        openElementOnLine("span");
        printEncode(title);
        closeElementOnLine("span");
        println();
        openElementOnLine("span");
        print(resources);
        closeElementOnLine("span");
        println();
        closeElement("div");
    }

    private void printAttributeIfThere(Classification classification,
                                       String label, String attributeName) throws IOException {
        printAttributeIfThere(classification, label, attributeName, attributeName);
    }

    private void printAttributeIfThereWithElementAsLabel(Classification classification, String attributeName, String tagName) throws IOException {
        Attribute attribute = classification.getAttribute(attributeName);
        if (attribute != null) {
            Object value = classification.getValue(attribute);
            printOnLineIfNotNull(tagName, attribute.getName().toString(), value);
        }
    }

    private void addSearchIfThere(Classification classification,
                                  Set<String> search, String attributeName) {
        Attribute attribute = classification.getAttribute(attributeName);
        if (attribute != null) {
            Object value = classification.getValue(attribute);
            if (value != null) {
                String string = getStringValue(value);
                search.add(string);
            }
        }
    }

    private void printValue(Object value) throws IOException {
        if (value == null) {
            return;
        }
        if (value instanceof URI) {
            print("<![CDATA[");
            print(value.toString());
            print("]]>");
        } else {
            String string = getStringValue(value);
            printEncode(string.replace("\n", "<br/>"));
        }
    }

    private String getStringValue(Object value) {
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