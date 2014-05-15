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
package org.rapla.plugin.freiraum.client;

import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.rapla.components.layout.TableLayout;
import org.rapla.entities.User;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.entities.dynamictype.DynamicTypeAnnotations;
import org.rapla.framework.Configuration;
import org.rapla.framework.DefaultConfiguration;
import org.rapla.framework.PluginDescriptor;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.framework.StartupEnvironment;
import org.rapla.gui.DefaultPluginOption;
import org.rapla.gui.internal.common.NamedListCellRenderer;
import org.rapla.plugin.freiraum.FreiraumPlugin;
import org.rapla.plugin.freiraum.TerminalConstants;

public class TerminalOption extends DefaultPluginOption {
    JTextField textField1;
    JTextField textField2;
    JTextField textField3;
    JList eventTypes;
    JList resourceTypes;
    JList externalPersonTypes;
    JComboBox kursTyp;
    JComboBox raumTyp;
    JComboBox steleUser;

    public TerminalOption(RaplaContext sm) throws RaplaException {
        super(sm);
    }

    @Override
    protected JPanel createPanel() throws RaplaException {
        JPanel main = super.createPanel();
        double pre = TableLayout.PREFERRED;
        double fill = TableLayout.FILL;
        JPanel panel = new JPanel();
        panel.setLayout(new TableLayout(new double[][]{{pre, 5, pre, 5, pre}, {pre, pre, pre, pre, pre, pre, pre, pre, pre, pre, pre, pre, pre, pre, pre, pre, pre,pre, pre,fill}}));

        textField1 = new JTextField();
        addCopyPaste(textField1);
        textField1.setColumns(30);
        panel.add(new JLabel("Ueberschrift der Uebersichtsseite"), "0,0");
        panel.add(textField1, "2,0");
        textField1.setEnabled(true);

        textField2 = new JTextField();
        addCopyPaste(textField2);
        textField2.setColumns(30);
        panel.add(new JLabel("Text fuer keine Veranstaltungen"), "0,2");
        panel.add(textField2, "2,2");
        textField2.setEnabled(true);

        textField3 = new JTextField();
        addCopyPaste(textField3);
        textField3.setColumns(30);
        panel.add(new JLabel("URL fuer custom css"), "0,4");
        panel.add(textField3, "2,4");
        textField3.setEnabled(true);

        resourceTypes = new JList();
        resourceTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resourceTypes.setCellRenderer(new NamedListCellRenderer(getRaplaLocale().getLocale()));
        addCopyPaste(resourceTypes);
        panel.add(new JLabel("Exportierte Ressourcen"), "0,6");
        panel.add(new JScrollPane(resourceTypes), "2,6");
        resourceTypes.setEnabled(true);

        externalPersonTypes = new JList();
        externalPersonTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        externalPersonTypes.setCellRenderer(new NamedListCellRenderer(getRaplaLocale().getLocale()));
        addCopyPaste(externalPersonTypes);
        panel.add(new JLabel("Externe Personen-Ressourcen"), "0,8");
        panel.add(new JScrollPane(externalPersonTypes), "2,8");
        externalPersonTypes.setEnabled(true);

        eventTypes = new JList();
        eventTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        eventTypes.setCellRenderer(new NamedListCellRenderer(getRaplaLocale().getLocale()));
        addCopyPaste(eventTypes);
        panel.add(new JLabel("Exportierte Veranstaltungstypen"), "0,10");
        panel.add(new JScrollPane(eventTypes), "2,10");
        eventTypes.setEnabled(true);

        kursTyp = new JComboBox();
        kursTyp.setRenderer(new NamedListCellRenderer(getRaplaLocale().getLocale()));
        addCopyPaste(kursTyp);
        panel.add(new JLabel("Kurs Ressource"), "0,12");
        panel.add(kursTyp, "2,12");
        kursTyp.setEnabled(true);

        raumTyp = new JComboBox();
        raumTyp.setRenderer(new NamedListCellRenderer(getRaplaLocale().getLocale()));
        addCopyPaste(raumTyp);
        panel.add(new JLabel("Raum Ressource"), "0,14");
        panel.add(raumTyp, "2,14");
        raumTyp.setEnabled(true);

        steleUser = new JComboBox();
        steleUser.setRenderer(new NamedListCellRenderer(getRaplaLocale().getLocale()));
        addCopyPaste(steleUser);
        panel.add(new JLabel("Stele User"), "0,16");
        panel.add(steleUser, "2,16");
        steleUser.setEnabled(true);


        JTextField textField10 = new JTextField();
        addCopyPaste(textField10);
        textField10.setEditable( false);
        textField10.setColumns(30);
        panel.add(new JLabel("Addresse fuer export"), "0,18");
        panel.add(textField10, "2,18");
        textField10.setEnabled(true);
        textField10.setText( getAddress());
        main.add(new JScrollPane(panel), BorderLayout.CENTER);
        return main;
    }
    
    public String getAddress() 
	{
		final URL codeBase;
        try 
        {
            StartupEnvironment env = getService( StartupEnvironment.class );
            codeBase = env.getDownloadURL();
        }
        catch (Exception ex)
        {
        	return "Not in webstart mode. Exportname is "  ;
        }
	
            
        try 
        {
            // In case of enabled and activated URL encryption:
            String pageParameters = "page=" + "freiraum";
            final String urlExtension;

//            if( getContext().has( UrlEncryption.class))
//            {
//            	UrlEncryption webservice = getService(UrlEncryption.class);
//				String encryptedParamters = webservice.encrypt(pageParameters);
//				urlExtension = UrlEncryption.ENCRYPTED_PARAMETER_NAME+"="+encryptedParamters;
//            }
//            else
            {
                urlExtension = pageParameters;
            }
            return new URL( codeBase,"rapla?" + urlExtension).toExternalForm();
        } 
//        catch (RaplaException ex)
//        {
//        	getLogger().error(ex.getMessage(), ex);
//        	return "Exportname is invalid ";
//        } 
        catch (MalformedURLException e) 
        {
        	return "Malformed url. " + e.getMessage() + ". Exportname is invalid " ;
		} 
	}


    public String getName(Locale locale) {
        return "Freiraum Einstellungen";
    }

    protected void addChildren(DefaultConfiguration newConfig) {
        {
            DefaultConfiguration conf = new DefaultConfiguration("ueberschrift");
            conf.setValue(textField1.getText());
            newConfig.addChild(conf);
        }
        {
            DefaultConfiguration conf = new DefaultConfiguration("keinekurse");
            conf.setValue(textField2.getText());
            newConfig.addChild(conf);
        }
        {
            DefaultConfiguration conf = new DefaultConfiguration("cssurl");
            conf.setValue(textField3.getText());
            newConfig.addChild(conf);
        }
        // TODO Replace with RaplaMap and store in preferences to save real references instead of keys (in case the keys or username changes)
        {
            DefaultConfiguration conf = new DefaultConfiguration(TerminalConstants.EVENT_TYPES_KEY);
            String value = getDynamicTypeKeysFromListSelection(Arrays.asList(eventTypes.getSelectedValues()));
            conf.setValue(value);
            newConfig.addChild(conf);
        }
        {
            DefaultConfiguration conf = new DefaultConfiguration(TerminalConstants.EXTERNAL_PERSON_TYPES_KEY);
            String value = getDynamicTypeKeysFromListSelection(Arrays.asList(externalPersonTypes.getSelectedValues()));
            conf.setValue(value);
            newConfig.addChild(conf);
        }


        {
            DefaultConfiguration conf = new DefaultConfiguration(TerminalConstants.RESOURCE_TYPES_KEY);
            String value = getDynamicTypeKeysFromListSelection(Arrays.asList(resourceTypes.getSelectedValues()));
            conf.setValue(value);
            newConfig.addChild(conf);
        }
        {
            DefaultConfiguration conf = new DefaultConfiguration(TerminalConstants.ROOM_KEY);
            conf.setValue(((DynamicType) raumTyp.getSelectedItem()).getKey());
            newConfig.addChild(conf);
        }
        {
            DefaultConfiguration conf = new DefaultConfiguration(TerminalConstants.KURS_KEY);
            conf.setValue(((DynamicType) kursTyp.getSelectedItem()).getKey());
            newConfig.addChild(conf);
        }
        {
            DefaultConfiguration conf = new DefaultConfiguration(TerminalConstants.USER_KEY);
            conf.setValue(((User) steleUser.getSelectedItem()).getUsername());
            newConfig.addChild(conf);
        }


    }

    private String getDynamicTypeKeysFromListSelection(List selectedValuesList) {
        StringBuilder b = new StringBuilder();
        for (int i = 0, selectedValuesListSize = selectedValuesList.size(); i < selectedValuesListSize; i++) {
            Object o = selectedValuesList.get(i);
            b.append(((DynamicType) o).getKey());
            if (i < selectedValuesListSize - 1)
                b.append(",");
        }

        return b.toString();
    }

    protected void readConfig(Configuration config) {
        {
            String value = config.getChild("ueberschrift").getValue(TerminalConstants.KURS_UEBERSCHRIFT);
            textField1.setText(value);
        }
        {
            String value = config.getChild("keinekurse").getValue(TerminalConstants.NO_COURSES);
            textField2.setText(value);
        }
        {
            String value = config.getChild("cssurl").getValue("");
            textField3.setText(value);
        }

        DynamicType[] resources = new DynamicType[0];
        try {
            resources = getAllRessources();
        } catch (RaplaException e) {
            getLogger().error(e.getMessage(), e);
        }


        {
            String value = config.getChild(TerminalConstants.KURS_KEY).getValue("");
            kursTyp.setModel(new DefaultComboBoxModel(resources));
            updateSelectionModel(value, kursTyp);
        }

        {
            String value = config.getChild(TerminalConstants.ROOM_KEY).getValue("");
            raumTyp.setModel(new DefaultComboBoxModel(resources));

            updateSelectionModel(value, raumTyp);
        }

        {
            String value = config.getChild(TerminalConstants.USER_KEY).getValue("");
            try {
                steleUser.setModel(new DefaultComboBoxModel(getClientFacade().getUsers()));
            } catch (RaplaException e) {
                getLogger().error(e.getMessage(), e);
            }
            updateUserSelectionModel(value, steleUser);
        }


        {
            try {
                DefaultListModel model = new DefaultListModel();
                DynamicType[] dynamicTypes = getClientFacade().getDynamicTypes(DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_RESERVATION);
                for (DynamicType dynamicType : dynamicTypes) {
                    model.addElement(dynamicType);
                }
                eventTypes.setModel(model);
            } catch (RaplaException e) {
                getLogger().error(e.getMessage(), e);
            }

            String value = config.getChild(TerminalConstants.EVENT_TYPES_KEY).getValue("");
            String[] keys = value.split(",");
            updateSelectionModel(keys, eventTypes);
        }

        {
            DefaultListModel model = new DefaultListModel();
            for (DynamicType dynamicType : resources) {
                model.addElement(dynamicType);
            }
            resourceTypes.setModel(model);

            String value = config.getChild(TerminalConstants.RESOURCE_TYPES_KEY).getValue("");
            String[] keys = value.split(",");
            updateSelectionModel(keys, resourceTypes);
        }

        {
            DefaultListModel model = new DefaultListModel();
            for (DynamicType dynamicType : resources) {
                String annotation = dynamicType.getAnnotation(DynamicTypeAnnotations.KEY_CLASSIFICATION_TYPE);
                if ( annotation != null && annotation.equals( DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_PERSON))
                    model.addElement(dynamicType);
            }
            externalPersonTypes.setModel(model);

            String value = config.getChild(TerminalConstants.EXTERNAL_PERSON_TYPES_KEY).getValue("");
            String[] keys = value.split(",");
            updateSelectionModel(keys, externalPersonTypes);
        }


    }

    private DynamicType[] getAllRessources() throws RaplaException {
        DynamicType[] resources = getClientFacade().getDynamicTypes(DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_RESOURCE);
        DynamicType[] persons = getClientFacade().getDynamicTypes(DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_PERSON);
        //DynamicType [] all = new DynamicType[resources.length + persons.length];
        resources = Arrays.copyOf(resources, resources.length + persons.length);
        System.arraycopy(persons, 0, resources, resources.length - persons.length, persons.length);
        return resources;
    }

    private void updateSelectionModel(String value, JComboBox comboBox) {
        ComboBoxModel model = comboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            DynamicType d = (DynamicType) comboBox.getModel().getElementAt(i);
            if (d.getKey().equals(value)) {
                comboBox.setSelectedItem(d);
                break;
            }
        }
    }

    private void updateUserSelectionModel(String value, JComboBox comboBox) {
        ComboBoxModel model = comboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            User d = (User) comboBox.getModel().getElementAt(i);
            if (d.getUsername().equals(value)) {
                comboBox.setSelectedItem(d);
                break;
            }
        }
    }


    private void updateSelectionModel(String[] keys, JList list) {
        Arrays.sort(keys);
        final ListModel model = list.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            DynamicType d = (DynamicType) model.getElementAt(i);
            if (Arrays.binarySearch(keys, d.getKey()) >= 0) {
                list.getSelectionModel().addSelectionInterval(i, i);
            }
        }
    }


    @Override
    public Class<? extends PluginDescriptor<?>> getPluginClass() {
        return FreiraumPlugin.class;
    }

    @Override
    public String getName(Object object) {
        return "Freiraum Info";
    }


}
