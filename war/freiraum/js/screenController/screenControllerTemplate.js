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

var screenControllerTemplate = function (previousScreenObject, activeScreen)
{
    /**
     * Define all public functions. Every function listed here is accessible
     * from outside.
     */
    this.createAndLayoutScreen = createAndLayoutScreen;
    this.registerUserActionRecognizer = registerUserActionRecognizer;
    this.getRequestDataObject = getRequestDataObject;
    this.getTheme = getTheme;
    
    /**
     * The screenObject of the previous screen. This is needed to access
     * all relevant request data.
     */
    var previousScreenDataRequestObject = previousScreenDataRequestObject;
    var activeScreen = activeScreen;


    function getTheme ()
    {
        
    }

    /**
     * This function is called by the next screen to get all
     * collected and relevant data.
     *
     * Every screen controller has to implement getRequestRelevantData
     */
    function getRequestDataObject ()
    {
        return null;
    }

    /**
     * Creates and layouts the screen. The data needed for this screen
     * is fetched asynchronously after all recognizers have been registered.
     */
    function createAndLayoutScreen ()
    {
        registerAjaxRequestRecognizer();
        registerUserActionRecognizer();
    }

    /**
     * Register the basic recognizers needed for the ajax request.
     * All request specific success recognizers are registered just in time.
     */
    function registerAjaxRequestRecognizer ()
    {

    }

    function registerUserActionRecognizer ()
    {

    }

}