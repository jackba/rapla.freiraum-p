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
var homeScreenController = function (previousScreenObject, activeScreen)
{

    this.createAndLayoutScreen = createAndLayoutScreen;
    this.reLayout = reLayout;
    this.registerUserActionRecognizer = registerUserActionRecognizer;
    this.getRequestDataObject = getRequestDataObject;
    this.getTheme = getTheme;
    this.unregisterRecognizerAndServices = unregisterRecognizerAndServices;

    var previousScreenObject = previousScreenObject;
    var activeScreen = activeScreen;

    var requestDataObject = null;

    function getTheme ()
    {

    }
    
    function unregisterRecognizerAndServices ()
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
        return requestDataObject;
    }

    function createAndLayoutScreen ()
    {
        registerUserActionRecognizer();
        layoutHomeScreen();
    }

    function reLayout ()
    {
        layoutHomeScreen();
    }

    function registerUserActionRecognizer ()
    {
        $('#configStudent').unbind('click');
        $('#configStudent').bind('click', loadConfigStudentAction);

        $('#configProf').unbind('click');
        $('#configProf').bind('click', loadConfigProfAction);

        $('#configRoom').unbind('click');
        $('#configRoom').bind('click', loadConfigRoomAction);

        $('#findRoom').unbind('click');
        $('#findRoom').bind('click', loadFindRoomAction);

    }

    function loadConfigStudentAction ()
    {
        requestDataObject =
        {
            layout : 'courses',
            elementclass : CLASS_ELEMENTS
        }
        $.mobile.changePage($('#configStudentScreen'),
        {
            transition : 'fade'
        });
    }

    function loadConfigProfAction ()
    {
        requestDataObject =
        {
            layout : 'professors',
            elementclass : CLASS_ELEMENTS
        }
        $.mobile.changePage($('#configProfScreen'),
        {
            transition : 'fade'
        });
    }

    function loadConfigRoomAction ()
    {
        requestDataObject =
        {
            layout : 'rooms',
            elementclass : CLASS_ELEMENTS
        }
        $.mobile.changePage($('#configRoomScreen'),
        {
            transition : 'fade'
        })
    }

    function loadFindRoomAction ()
    {
        $.mobile.changePage($('#findRoomScreen'),
        {
            transition : 'fade'
        });
    }

    function layoutHomeScreen ()
    {
        var width = $(document).width();

        $('.homeButtonGroup').width(Math.round(0.9 * width));

        $('.homeButtonGroup').css(
        {
            position : 'relative',
            left : ($('.contentArea').width() - $('.homeButtonGroup').outerWidth()) / 2
        });

        var heightFactorized = parseInt($('div.image').css('max-height'));
        var widthFactorized = parseInt($('div.image').css('max-width'));

        if ($('.contentArea').height() < ($('.homeButtonGroup').outerHeight() * 1.1))
        {
            var scaleFactor = $('.homeButtonGroup').outerHeight() / ($('.contentArea').height() * 1.5);

            var heightFactorized = Math.floor(heightFactorized * scaleFactor);
            var widthFactorized = Math.floor(widthFactorized * scaleFactor);

            $('div.image').css(
            {
                height : heightFactorized + 'px',
                width : widthFactorized + 'px'
            });
        }

        if ($('.contentArea').height() > ($('.homeButtonGroup').outerHeight() * 1.1))
        {
            $('.homeButtonGroup').css(
            {
                top : ($('.contentArea').height() - $('.homeButtonGroup').outerHeight()) / 2
            });
        }

        $('.imageWrapper').each(function ()
        {
            var parentWidth = $(this).closest('span.ui-btn-inner').width() - calculatePaddingBorderOfElement($(this))['width'];
            $(this).width(parentWidth);
        });

        $('.contentArea').css(
        {
            height : ($('.homeButtonGroup').outerHeight(true) + (parseInt($('.homeButtonGroup').css('top')) * 2))
        });
       
        

        hideDetailInfo();
    }
    
    function hideDetailInfo ()
    {
        $('.homeButtonDetailInfo').show();
        $('.homeButtonInfo').each(function ()
        {
            if ($(this).width() > $(this).closest('span.ui-btn-inner').width())
            {
                $('.homeButtonDetailInfo').hide();
            }
        });

    }

}
