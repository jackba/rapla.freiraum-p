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
function layoutContentArea ()
{
    setDocumentTitle();

    $.mobile.activePage.css(
    {
        minHeight : $(window).height() + 'px'
    });

    var content = $($.mobile.activePage).children('.ui-content');

    var height = calculateContentHeight();

    // Erkenne den Browser und überprüfe ob es sich um ein iPhone handelt
    if (navigator.userAgent.match(/iPhone/) == 'iPhone')
    {
        // Addiere die URL-Bar des Browsers hinzu diese ist exakt 60px hoch
        height += 60;
    }

    content.css(
    {
        minHeight : height
    });

    var innerContentHeight = content.first().outerHeight(true);

    content.css(
    {
        height : height
    });

    if ($($.mobile.activePage).data('role') == 'dialog')
    {
        content.css(
        {
            maxHeight : height * 0.5
        });
    }
}

function calculateContentHeight ()
{
    var currentPage = $.mobile.activePage;
    var height = 0;
    if (currentPage != undefined)
    {
        var content = currentPage.children('.ui-content');

        height = $(window).height();

        var hHeight = currentPage.children('.header').outerHeight(true);
        var fHeight = currentPage.children('.footer').outerHeight(true);

        height -= hHeight + fHeight + calculatePaddingBorderOfElement(content)['height'];
    }

    return height;
}

function calculatePaddingBorderOfElement (element)
{
    var paddingTop = parseInt(element.css('padding-top'));
    var paddingBottom = parseInt(element.css('padding-bottom'));

    var borderTop = parseInt(element.css('border-top-width'));
    var borderBottom = parseInt(element.css('border-bottom-width'));

    var height = paddingTop + paddingBottom + borderTop + borderBottom;

    var paddingLeft = parseInt(element.css('padding-left'));
    var paddingRight = parseInt(element.css('padding-right'));

    var borderLeft = parseInt(element.css('border-left-width'));
    var borderRight = parseInt(element.css('border-right-width'));

    var width = paddingLeft + paddingRight + borderLeft + borderRight;

    var measures = new Array ();

    measures['height'] = height;
    measures['width'] = width;

    return measures;
}

function setDocumentTitle ()
{
    document.title = APP_NAME + ' :: ' + $($.mobile.activePage).find('.ui-title').text();
}

