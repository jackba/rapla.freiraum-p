/*--------------------------------------------------------------------------*
 | Copyright (c) 2013 Fabian Luft, Martin Wilhelm			    			|
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
var elementsListScreenController = function(previousScreenDataRequestObject, activeScreen) {
	/**
	 * Define all public functions. Every function listed here is accessible
	 * from outside.
	 */
	this.createAndLayoutScreen = createAndLayoutScreen;
	this.reLayout = reLayout;
	this.registerUserActionRecognizer = registerUserActionRecognizer;
	this.getRequestDataObject = getRequestDataObject;
	this.getTheme = getTheme;
	this.unregisterRecognizerAndServices = unregisterRecognizerAndServices;

	var theme;

	/**
	 * The screenObject of the previous screen. This is needed to access
	 * all relevant request data.
	 */
	var previousScreenDataRequestObject = previousScreenDataRequestObject;
	var activeScreen = activeScreen;

	/**
	 * These variables are needed for the timetable request.
	 * selectedProf - stores the prof id which identifies a professor
	 * selectedProfDisplayName - stores the name which will be displayed in the
	 * timetable header
	 */
	var selectedElementId = null;
	var displayName = null;

	function getTheme() {
		return theme;
	}

	function unregisterRecognizerAndServices() {

	}

	/**
	 * This function is called by the next screen to get all
	 * collected and relevant data.
	 *
	 * Every screen controller has to implement getRequestRelevantData
	 */
	function getRequestDataObject() {
		var query;
		var resourceId = selectedElementId;
		switch (activeScreen.attr('id')) {
			case 'configProfScreen':
				query = AJAX_QUERY_TIMETABLE_PROF;
				break;
			case 'configStudentScreen':
				query = AJAX_QUERY_TIMETABLE_COURSE;
				break;
			case 'configRoomScreen':
				query = AJAX_QUERY_TIMETABLE_ROOM;
				break;
		}

		var data = {
			'query' : query,
			'resourceId' : resourceId,
			'displayName' : displayName,
			elementclass : CLASS_EVENT
		};

		return data;
	}

	/**
	 * Creates and layouts the screen. The data needed for this screen
	 * is fetched asynchronously after all recognizers have been registered.
	 */
	function createAndLayoutScreen() {
		theme = activeScreen.data('theme');
		registerAjaxRequestRecognizer();
		registerUserActionRecognizer();
		if (activeScreen.find('.elementsListview').children().length == 0) {
			fetchData();
		}
	}

	function reLayout() {

	}

	/**
	 * Register the basic recognizers needed for the ajax request.
	 * All request specific success recognizers are registered just in time.
	 */
	function registerAjaxRequestRecognizer() {

	}

	function registerUserActionRecognizer() {
		activeScreen.find('.home').unbind('click');
		activeScreen.find('.home').bind('click', loadHomeScreenAction);

		activeScreen.find('.' + CLASS_ELEMENTS).unbind('click');
		activeScreen.find('.' + CLASS_ELEMENTS).bind('click', loadTimetableScreenAction);
	}

	function fetchData() {

		var loadMessage;
		switch (activeScreen.attr('id')) {
			case 'configProfScreen':
				loadMessage = LOAD_PROF_LIST;
				break;
			case 'configStudentScreen':
				loadMessage = LOAD_COURSE_LIST;
				break;
			case 'configRoomScreen':
				loadMessage = LOAD_ROOM_LIST;
				break;
		}

		
		var data = previousScreenDataRequestObject;
		
		if (loadMessage === LOAD_COURSE_LIST)
		{
			$(window).one(ACTION_AJAX_ELEMENTS_LIST, elementsLoadedActionDATAJS);
			sendDataJSRequest(data, loadMessage, ACTION_AJAX_ELEMENTS_LIST);
		}
		else {
			$(window).one(ACTION_AJAX_ELEMENTS_LIST, elementsLoadedAction);
			sendAjaxRequest(data, loadMessage, ACTION_AJAX_ELEMENTS_LIST);
		}
		

	}
	
	function elementsLoadedActionDATAJS(event) {
		activeScreen.find('.elementsListview').children().remove();
		
		$(event.elements).each(function (index, element) {
			var listElement = $('<li/>');
			var linkElement = $('<a/>');
			linkElement.addClass(previousScreenDataRequestObject.elementclass);
			linkElement.data( element );
			linkElement.append(document.createTextNode( element.name ));
			
			listElement.append(linkElement);
			
			activeScreen.find('.elementsListview').append(listElement);
		});
		
		activeScreen.find('.elementsListview').listview('refresh');
		registerUserActionRecognizer();
	}

	function elementsLoadedAction(event, data) {
		var width = activeScreen.find('.listviewCourses').width();
		activeScreen.find('.elementsListview').children().remove();
		activeScreen.find('.elementsListview').append(data);
		activeScreen.find('.elementsListview').listview('refresh');
		activeScreen.find('.elementsListview').width(width);
		registerUserActionRecognizer();
	}

	function loadTimetableScreenAction() {
		
		
		switch (activeScreen.attr('id')) {
			case 'configProfScreen':
				selectedElementId = $(this).data('resource-id');
				var selectedProfTitle = "";
				if ($(this).data('prof-title') != "") {
					selectedProfTitle = $(this).data('prof-title') + ' ';
				}
				selectedProfDisplayName = selectedProfTitle + $(this).data('prof-surname');
				displayName = selectedProfDisplayName;
				break;
			case 'configStudentScreen':
				selectedElementId = $(this).data().id;
				displayName = $(this).data().name;
				break;
			case 'configRoomScreen':
				selectedElementId = $(this).data('resource-id');
				displayName = $(this).data('room');
				break;
		}

		$.mobile.changePage($('#timetableScreen'), {
			transition : 'fade'
		});
	}

	function loadHomeScreenAction() {
		$.mobile.changePage($('#homeScreen'), {
			transition : 'fade'
		});
	}

}