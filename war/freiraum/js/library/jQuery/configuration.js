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

/**
 * This method executes all configurational stuff
 */
$(document).bind("mobileinit", function(){
  $.mobile.loadingMessageTextVisible = true;
  $.mobile.activeBtnClass = true;
  //$.event.special.swipe.horizontalDistanceThreshold = 100;
  $.event.special.swipe.horizontalDistanceThreshold = 50;
});