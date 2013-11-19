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
var loadedScreen = null;
var previousScreen = null;

/**
 * This method is needed by jquery mobile to execute all methods needed on the
 * page initialization
 */
$(document).bind('pageinit', function(event) {
	if (navigator.userAgent.match(/MSIE/i) || navigator.userAgent.match(/Opera/i)) {
		registerDeviceActionRecognizer();
	}
});

/**
 * This method is needed by jquery
 */
$(document).ready(function() {

	if (navigator.userAgent.match(/Android/i)) {
		window.scrollTo(0, 1);
	}

	getAndSetIdentifier();

	checkForPrivateBrowsing();
	registerDeviceActionRecognizer();

});

/******************************************************************************************************
 *
 * Recognizer. These functions registers all recognizers needed to handle device
 * actions correctly.
 *
 *****************************************************************************************************/

/*
 * Registers all device action recognizer. These function is just called one time
 * in $(document).ready
 */
function registerDeviceActionRecognizer() {
	$(window).unbind('resize');
	$(window).bind('resize', function(eventObject) {
		layoutContentArea();
		reLayoutScreen();
	});

	$(window).unbind('pageshow');
	$(window).bind('pageshow', function() {
		layoutContentArea();
		identifyAndLayoutScreen($.mobile.activePage);
	});

	$(window).unbind(ACTION_RE_LAYOUT);
	$(window).bind(ACTION_RE_LAYOUT, function() {
		reLayoutScreen();
	});

	/**
	 * This event copies the value from loadedScreen to previousScreen as soon as
	 * the page changes.
	 * previousScreen in turn is then used by identifyAndLayoutScreen to load the
	 * new screen properly.
	 * This is needed to prevent malfunctions due to the browser caching
	 * mechanism.
	 */
	$(window).unbind('pagebeforechange');
	$(window).bind('pagebeforechange', pageChanges);

}

function identifyAndLayoutScreen(activeScreen) {
	var pageID = $(activeScreen).attr('id');

	var screenObject = null;

	if (previousScreen != null) {
		previousScreen.unregisterRecognizerAndServices();

		if (previousScreen.getRequestDataObject() != null) {
			sessionStorage.requestDataObject = JSON.stringify(previousScreen.getRequestDataObject());
		}

		if (previousScreen.getTheme() != undefined) {
			sessionStorage.previousTheme = previousScreen.getTheme();
		}
	}

	if (sessionStorage.requestDataObject != undefined) {
		var previousScreenRequestDataObject = JSON.parse(sessionStorage.requestDataObject);
	}

	if (sessionStorage.previousTheme != undefined) {
		var previousTheme = sessionStorage.previousTheme;
	}

	switch (pageID) {
		case 'homeScreen':
			screenObject = new homeScreenController(previousScreenRequestDataObject, activeScreen);
			break;
		case 'configStudentScreen':
			screenObject = new elementsListScreenController(previousScreenRequestDataObject, activeScreen);
			break;
		case 'configProfScreen':
			screenObject = new elementsListScreenController(previousScreenRequestDataObject, activeScreen);
			break;
		case 'configRoomScreen':
			screenObject = new elementsListScreenController(previousScreenRequestDataObject, activeScreen);
			break;
		case 'findRoomScreen':
			screenObject = new findRoomScreenController(previousScreenRequestDataObject, activeScreen);
			break;
		case 'timetableScreen':
			screenObject = new timetableScreenController(previousScreenRequestDataObject, previousTheme, activeScreen);
			break;
		case 'findRoomResultScreen':
			screenObject = new findRoomResultScreenController(previousScreenRequestDataObject, activeScreen);
			break;
		case 'calendarScreen':
			screenObject = new calendarScreenController(previousScreenRequestDataObject, previousTheme, activeScreen);
			break;
	}

	loadedScreen = screenObject;

	screenObject.createAndLayoutScreen();

}

function reLayoutScreen() {
	if (loadedScreen != null) {
		loadedScreen.reLayout();
	}
}

function setDocumentTitle() {
	document.title = APP_NAME + ' :: ' + $($.mobile.activePage).find('.ui-title').text();
}

function checkForPrivateBrowsing() {
	try {
		sessionStorage.safariCheck = 'test';
	} catch (error) {
		$('#privateBrowsingError').css({
			display : 'table'
		});
		$('.homeButtonGroup').hide();
		$.mobile.changePage($('#homeScreen'), {
			transition : 'flip'
		});
	}
}

function pageChanges() {
	previousScreen = loadedScreen;
}