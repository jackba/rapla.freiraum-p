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
 * 
 * @param {Object} errorData errorData Assoziatives Array, das auch zum Übergeben von Daten zwischen GUI und Server genutzt wird.
 * Es wird unterschieden, ob es genau dieses Array oder ein beliebiges Assoziatives Array ist. Bei einem beliebigen Array werden einfach alle Key-Value Paare an den Server gesendet 
 * @param {Object} isString Boolean, der angibt, ob die Fehlermeldung nur ein reiner String ist. Ein reiner String sollte nicht als Array interpretiert werden
 */
function proceedError(errorData,isString){
	//Prüfe, ob der Error aus dem local Storage kommt und ein String ist;Dann wird der String später nicht als Array interpretiert
	if(typeof errorData == "string" || isString == true){
		n =-1;
	}else{n = 1;}

	
	if(errorData.indexOf('Browser') == -1){
	//Sammle alle Client-Informationen, um sie später anzuhängen (nur, wenn sie nicht bereits vorhanden sind):
	var clientInfo = "\nBrowser:" +  navigator.appName;
	clientInfo = clientInfo + "\nBrowser-Version:" +navigator.appVersion;
	clientInfo = clientInfo + "\nOS:" +navigator.platform;
	}
	else{
		clientInfo = "";
	}
	//Spezielles Übergabe-Array, bei dem ein Fehler aufgetreten ist
	var data = null;
	var error = null;
	//Zugriffe auf Array-Elemente müssen gefangen sein, weil wenn undefiniert eine Exception auftritt
	try{data = errorData["data"];}catch (e){}
	
	try{error = errorData["error"];}catch(e){}
	
	
	if(data == null && error != null){
		data =
        {
            logging : 'mobile',
            error: clientInfo + error
        }
		
		//Sende die Fehlermeldung an den Server
		$.ajax({type: "GET",url : '../php/ajaxresponse.php',async : true,data: data, error : function(data)
        {
            //Edit von Martin
           localStorage.setItem("errorMessage", errorMessage);
        }});	
		
		alert(error);
	}
	//Beliebiges Array -> sende alle Key-Value Paare
	else {
		var errorMessage = clientInfo;
		
		//schreibe alle key value paare in einen string. Aber NUR, wenn es auch ein assoziativer Array und nicht der String aus local Storage ist
		if (n > 0){
			
			for(var index in errorData) {
				errorMessage =  errorMessage + "\n" + index + ": " +errorData[index] ;
			}
		}
		else{
			errorMessage =  errorData;
		}
		data =
        {
            logging : 'mobile',
            error: errorMessage
        }
				
		$.ajax({type: "GET",url : '../php/ajaxresponse.php',async : true,data: data, error : function(data)
        {
           localStorage.setItem("errorMessage", errorMessage);
        }});	
	}
	
}
