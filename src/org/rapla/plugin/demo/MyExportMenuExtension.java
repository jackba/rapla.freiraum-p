/*--------------------------------------------------------------------------*
 | Copyright (C) 2006 Christopher Kohlhaas                                  |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.plugin.demo;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.rapla.components.iolayer.IOInterface;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.dynamictype.Attribute;
import org.rapla.entities.dynamictype.Classification;
import org.rapla.facade.CalendarModel;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.toolkit.IdentifiableMenuEntry;


public class MyExportMenuExtension extends RaplaGUIComponent implements IdentifiableMenuEntry,ActionListener
{
	String id = "dummy export";
	private JMenuItem item;

    public MyExportMenuExtension(RaplaContext sm)  {
        super(sm);
		item = new JMenuItem( id );
        item.setIcon( getIcon("icon.export") );
        item.addActionListener(this);
    }
    
    public String getId() {
		return id;
	}

	public JMenuItem getMenuElement() {
		return item;
	}
	
	 public void actionPerformed(ActionEvent evt) {
         try {
             CalendarModel model = getService(CalendarModel.class);
             export( model);
         } catch (Exception ex) {
             showException( ex, getMainComponent() );
         }
	 }
    
    public void export(final CalendarModel model) throws Exception
    {
        final Reservation[] events = model.getReservations();
        // generates a text file from all filtered events;
        StringBuffer buf = new StringBuffer();
        buf.append( "List of all events:");
        buf.append('\n');
        for (int i=0;i< events.length;i++)
        {
            buf.append(getClassificationLine(events[i].getClassification()));
            buf.append('\n');
        }
        saveFile( buf.toString().getBytes(), "list.txt","txt");
    }

    private String getClassificationLine( Classification classification) {
        Attribute[] attributes = classification.getAttributes();
        StringBuffer buf = new StringBuffer();
        for ( int i= 0;i < attributes.length; i++ )
        {
            Object value = classification.getValue( attributes[i]);
            if ( value != null )
            {
                buf.append( getName( value) );
            }
            buf.append(';');
        }
        return buf.toString();
    }

    public void saveFile(byte[] content,String filename, String extension) throws RaplaException {
        final Frame frame = (Frame) SwingUtilities.getRoot(getMainComponent());
        IOInterface io = getService( IOInterface.class);
        try {
            io.saveFile( frame, null, new String[] {extension}, filename, content);
        } catch (IOException e) {
            throw new RaplaException("Cant export file!", e);
        }
    }




}

