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
var findRoomScreenController = function(previousScreenDataRequestObject, activeScreen) {

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

	/**
	 * The screenObject of the previous screen. This is needed to access
	 * all relevant request data.
	 */
	var previousScreenDataRequestObject = previousScreenDataRequestObject;
	var activeScreen = activeScreen;

	var searchStartDate;
	var searchEndDate;
	var useShortDescriptions;

	function getTheme() {

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
		var data = {
			query : AJAX_QUERY_ROOM_FREE,
			starttime : parseInt(searchStartDate.getTime() / 1000),
			endtime : parseInt(searchEndDate.getTime() / 1000),
			elementclass : CLASS_FREE_ROOMS,
			roomCategory : $('.modifyRoomCategory option:selected').val()
		}

		return data;
	}

	/**
	 * Creates and layouts the screen. The data needed for this screen
	 * is fetched asynchronously after all recognizers have been registered.
	 */
	function createAndLayoutScreen() {
		registerAjaxRequestRecognizer();
		registerUserActionRecognizer();
		initializeAndSetDate();
		checkLayout();
		fetchData();
	}

	function reLayout() {
		checkLayout();
	}

	function fetchData() {
		$(window).one(ACTION_AJAX_ROOM_CATEGORIES, roomCategoriesLoadedAction);
		var data = {
			layout : AJAX_QUERY_ROOM_CATEGORIES
		};
		sendAjaxRequest(data, LOAD_ROOM_CATEGORIES, ACTION_AJAX_ROOM_CATEGORIES);
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

		$('.modifyDate').unbind('change');
		$('.modifyDate').bind('change', function() {
			findRoomStartDateModifiedAction($(this));
		});

		$('.modifyTime').unbind('change');
		$('.modifyTime').bind('change', function() {
			findRoomStartDateModifiedAction($(this));
		});

		$('.modifyDurationTime').unbind('change');
		$('.modifyDurationTime').bind('change', function() {
			findRoomEndDateModifiedAction($(this));
		})

		$('.searchRoomResetStarttime').unbind('click');
		$('.searchRoomResetStarttime').bind('click', initializeAndSetDate);

		$('.startRoomSearch').unbind('click');
		$('.startRoomSearch').bind('click', loadFindRoomResultAction);

	}

	function initializeAndSetDate() {
		searchStartDate = new Date();
		searchStartDate.setMinutes((Math.round(searchStartDate.getMinutes() / 5)) * 5);
		searchStartDate.setSeconds(0);
		searchEndDate = calculateEndDate(searchStartDate);

		findRoomUpdateDateTimePicker();
	}

	function createStartDateOptionList() {
		$('select.startDays').children().remove();
		$('select.startMonths').children().remove();
		$('select.startYears').children().remove();

		

// 		Today's date is start date for the list
		var startDate = (new Date()).getDate();
		var tempDate = new Date();

		if (searchStartDate.getMonth() != (new Date()).getMonth() || searchStartDate.getFullYear() != (new Date()).getFullYear()) {
// 			If the month or year is different to today's month and year set startDate to 1
			startDate = 1;
			tempDate = new Date(searchStartDate.getFullYear(), searchStartDate.getMonth(), 1)
		}
		
// 		The 0th day of the next month is the amount of days of the previous month
		var daysOfMonth = (new Date(searchStartDate.getFullYear(), searchStartDate.getMonth() + 1, 0)).getDate();

		var weekdayNames = WEEKDAY;

		if (useShortDescriptions) {
			weekdayNames = WEEKDAY_SHORT;
		}

		for (var i = startDate; i <= daysOfMonth; i++) {

			var selectField = $('<option>');
			selectField.append(i + ' ' + weekdayNames[tempDate.getDay()]);
			selectField.attr({
				value : i
			});

			$('select.startDays').append(selectField);

// 			Increment tempDate to get the next day
			tempDate.setDate(tempDate.getDate() + 1);
		}

		$('select.startDays').selectmenu('refresh');

		tempDate = new Date(searchStartDate);

		var startMonth = (new Date()).getMonth();

		if (searchStartDate.getFullYear() != (new Date).getFullYear()) {
			startMonth = 0;
		}

		var monthNames = MONTH;

		if (useShortDescriptions) {
			monthNames = MONTH_SHORT;
		}

		for (var i = startMonth; i <= 11; i++) {

			var selectField = $('<option>');
			selectField.append(monthNames[i]);
			selectField.attr({
				value : i
			});

			$('select.startMonths').append(selectField);
		}

		$('select.startMonths').selectmenu('refresh');

		var startYear = (new Date()).getFullYear();

		for (var i = startYear; i <= startYear + 1; i++) {
			var selectField = $('<option>');

			var year = i;

			if (useShortDescriptions) {
				year = i.toString().substring(2, 4);
			}

			selectField.append(year);
			selectField.attr({
				value : i
			});

			$('select.startYears').append(selectField);
		}

		$('select.startYears').selectmenu('refresh');

	}

	function checkLayout() {
		var selectWidth = $('select.startDays').outerWidth(true) + $('select.startMonths').outerWidth(true) + $('select.startYears').outerWidth(true);

		useShortDescriptions = false;

		if ($(window).width() < (selectWidth * 1.2)) {
			useShortDescriptions = true;
		}

		createStartDateOptionList();

	}

	function findRoomUpdateDateTimePicker() {

		createStartDateOptionList(false);

		hideStartDateOptions();
		hideEndDateOptions();

		var startDays = searchStartDate.getDate();
		var startMonths = searchStartDate.getMonth();
		var startYears = searchStartDate.getFullYear();
		var startHours = searchStartDate.getHours();
		var startMinutes = searchStartDate.getMinutes();

		var endHours = searchEndDate.getHours();
		var endMinutes = searchEndDate.getMinutes();

		$("select.startDays option[value='" + startDays + "']").attr('selected', true);
		$('select.startDays').selectmenu('refresh');
		$("select.startMonths option[value='" + startMonths + "']").attr('selected', true);
		$('select.startMonths').selectmenu('refresh');
		$('select.startYears').prop('selectedIndex', searchStartDate.getFullYear() - (new Date()).getFullYear()).selectmenu('refresh');
		$('select.startMinutes').prop('selectedIndex', Math.round(startMinutes / 5)).selectmenu('refresh');
		$("select.startHours option[value='" + startHours + "']").attr('selected', true);
		$("select.startHours").selectmenu('refresh');

		var selectedIndexEndHours = $('select.endHours option[value="' + endHours + '"]').index();

		$('select.endMinutes').prop('selectedIndex', Math.round(endMinutes / 5)).selectmenu('refresh');
		$('select.endHours').prop('selectedIndex', selectedIndexEndHours).selectmenu('refresh');

	}

	function roomCategoriesLoadedAction(event, data) {
		$('.modifyRoomCategory').children().remove();
		$('select.modifyRoomCategory').append(data).selectmenu('refresh');
	}

	function findRoomStartDateModifiedAction(buttonClicked) {
		var subject = buttonClicked.data('modify-subject');

		switch (subject) {
			case CASE_MINUTES:
				searchStartDate.setMinutes(parseInt(buttonClicked.val()));
				break;
			case CASE_HOURS:
				searchStartDate.setHours(parseInt(buttonClicked.val()));
				break;
			case CASE_DAY:
				var day = searchStartDate.setDate(parseInt(buttonClicked.val()));
				break;
			case CASE_MONTH:
				searchStartDate.setMonth(parseInt(buttonClicked.val()));
				break;
			case CASE_YEAR:
				searchStartDate.setFullYear(parseInt(buttonClicked.val()));
				if (parseInt(buttonClicked.val()) == (new Date()).getFullYear()) {
					searchStartDate.setMonth((new Date()).getMonth());
					searchStartDate.setDate((new Date()).getDate());
				}
				break;
		}

		searchEndDate = calculateEndDate(searchStartDate);

		findRoomUpdateDateTimePicker();
	}

	function findRoomEndDateModifiedAction(buttonClicked) {
		var subject = buttonClicked.data('modify-subject');

		switch (subject) {
			case CASE_MINUTES:
				searchEndDate.setMinutes(parseInt(buttonClicked.val()));
				break;
			case CASE_HOURS:
				searchEndDate.setHours(parseInt(buttonClicked.val()));
				break;
		}

		hideEndDateOptions();

		findRoomUpdateDateTimePicker();
	}

	function loadFindRoomResultAction() {
		searchDuration = parseInt($('.durationHours').attr('value'));
		$.mobile.changePage($('#findRoomResultScreen'), {
			transition : 'fade'
		});
	}

	function loadHomeScreenAction() {
		$.mobile.changePage($('#homeScreen'), {
			transition : 'fade'
		});
	}

	function calculateEndDate(date) {
		var searchEndDate = new Date(date.getTime() + 45 * 60000);

		return searchEndDate;
	}

	function hideStartDateOptions() {
		$('.startHours').children().remove();
		var today = new Date();

		var startHours = 0;

		if (today.getDate() == searchStartDate.getDate() && today.getMonth() == searchStartDate.getMonth() && today.getFullYear() == searchStartDate.getFullYear()) {
			startHours = today.getHours();
		}

		for (var i = startHours; i <= 22; i++) {
			var optionElement = $('<option>');
			optionElement.attr('value', i);
			optionElement.html(i);

			$('.startHours').append(optionElement);
		}
	}

	function hideEndDateOptions() {
		$('.endHours').children().remove();
		for (var i = searchStartDate.getHours(); i <= 23; i++) {
			var optionElement = $('<option>');
			optionElement.attr('value', i);
			optionElement.html(i);

			$('.endHours').append(optionElement);
		}

		if ((searchEndDate.getHours() == searchStartDate.getHours()) && searchEndDate.getMinutes() < searchStartDate.getMinutes()) {
			searchEndDate.setMinutes(searchStartDate.getMinutes());
		}

	}

}