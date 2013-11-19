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

function sendAjaxRequest(data, message, successEvent) {
	$.ajax({
		url : URL_AJAX_REQUEST,
		async : true,
		data : data,
		cache : false,
		beforeSend : function() {
			//Edit von Martin: Nur Status anzeigen, wenn message gefüllt ist
			if (message != "" && message != null) {
				$.mobile.loading('show', {
					theme : 'a',
					text : message
				});
			}

		},
		success : function(data) {
			// Logging von Fehlern
			//Edit von Martin:
			errorData = localStorage.getItem("errorMessage");
			if (errorData != null) {
				proceedError(errorData, true);
				localStorage.removeItem("errorMessage");
			}
			//Edit Ende

			var jsonData = requestCheckForError(data);
			$.mobile.loading('hide');
			if (jsonData != null) {
				var dataToSend = jsonData.toString()
				if ( typeof (jsonData) == 'object') {
					dataToSend = jsonData;
				}
				$(window).trigger(successEvent, dataToSend);
			} else {
				//Edit von Martin
				proceedError(jsonData, false);
				//$(window).trigger(AJAX_ERROR_EVENT);
			}

		},
		error : function(data) {
			//Edit von Martin
			var jsonData = requestCheckForError(data);
			proceedError(jsonData, false);
			//$(window).trigger(AJAX_ERROR_EVENT);
			//$(window).trigger(AJAX_ERROR_EVENT);
			//ajaxErrorMessage();
		}
	});
}

function sendDataJSRequest(data, message, successEvent) {

	$.ajax({
		url : URL_AJAX_REQUEST,
		async : true,
		data : data,
		cache : false,
		beforeSend : function() {
			//Edit von Martin: Nur Status anzeigen, wenn message gefüllt ist
			if (message != "" && message != null) {
				$.mobile.loading('show', {
					theme : 'a',
					text : message
				});
			}

		},
		success : function(data) {
			// Logging von Fehlern
			//Edit von Martin:
			errorData = localStorage.getItem("errorMessage");
			if (errorData != null) {
				proceedError(errorData, true);
				localStorage.removeItem("errorMessage");
			}
			//Edit Ende

			var jsonData = requestCheckForError(data);
			$.mobile.loading('hide');
			if (jsonData != null) {
				// var dataToSend = jsonData.toString()
				
				var jQEvent = $.Event(successEvent);
				
				jQEvent.elements = jsonData;				

				$(window).trigger( jQEvent );
			} else {
				//Edit von Martin
				proceedError(jsonData, false);
				//$(window).trigger(AJAX_ERROR_EVENT);
			}

		},
		error : function(data) {
			//Edit von Martin
			var jsonData = requestCheckForError(data);
			proceedError(jsonData, false);
			//$(window).trigger(AJAX_ERROR_EVENT);
			//$(window).trigger(AJAX_ERROR_EVENT);
			//ajaxErrorMessage();
		}
	});
}

function ajaxErrorMessage() {
	alert('An error occurred during request.');
}

function requestCheckForError(data) {
	if (data != '') {
		var json = jQuery.parseJSON(data);

		if (json.error != "" && json.error != null) {
			alert(json.error);
		} else {
			return json.data;
		}
	}

	return null;
}

