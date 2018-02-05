<%
/*
This file is part of "Ηλεκτρονική Διακίνηση Εγγράφων Δήμου Αθηναίων(ΗΔΕ) 
Documents' Digital Handling/Digital Signature of Municipality of Athens(DDHDS)".

Complementary service to allow digital signing from browser

Copyright (C) <2018> Municipality Of Athens
Service Author Panagiotis Skarvelis p.skarvelis@athens.gr
 
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

<authors' emails : v.kaliakouda@athens.gr, a.koutsaftis@athens.gr, p.skarvelis@athens.gr>
<authors' address : Λιοσίων 22, Αθήνα - Ελλάδα, ΤΚ:10438 ---- Liosion 22, Athens - Greece, PC:10438>
*/

%>
<%@ page language="java" session="true" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="moa.ds.service.athens.gr.radweriel"%>
<%@page import="moa.ds.service.athens.gr.thales"%>      
<%! static int myStatic = 0; %> 
<% 
//Auto pou tha kanei upload tha prepei na einai ena PDF xoris ypografes
String filename = null;
String initstate="false";

try {
		if (request.getParameter("cms")!=null)radweriel.alf_server=request.getParameter("cms");
	} catch (Exception e) {
		 response.getWriter().println("set cms");
	}

try {
	   if (request.getParameter("bpm")!=null) thales.bos_server =request.getParameter("bpm");
	} catch (Exception e) {
		 response.getWriter().println("set bpm");
	}

try{ 
filename = radweriel.getFilenameByID(request.getParameter("uuid"));
      } catch (Exception e) {
 		 //Tha prepei na exei valei filenamepath
 		 if (request.getParameter("init")==null) { response.getWriter().println("set path");
 		 return;
 		   } else {
 			  radweriel.deleteChildsOnFolderByUUID(request.getParameter("init"));   
 			   initstate="true";
 		    }
 		   }
         
pageContext.setAttribute("filename", filename);
pageContext.setAttribute("initstate", initstate);
%>
<!DOCTYPE html PUBLIC "-fpost//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" href="css/bootstrap.min.css" />
<link rel="stylesheet" href="css/bootstrap-theme.min.css" />
<link rel="stylesheet" href="css/font-awesome.min.css" />
<link rel="stylesheet" href="css/moass.css" />

<script src="js/jquery.js"></script>
<script src="js/hwcrypto-legacy.js"></script>
<script src="js/hwcrypto.js"></script>
<script src="js/hex2base.js"></script>
<script src="js/sign.js"></script> 
<script src="js/bootstrap.min.js"></script>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Municipality Of Athens Signing Service</title>

</head>
<body>
  <noscript>
    <center>
    <h1>JavaScript is disabled</h1>
    </center>
  </noscript>
<div id="cover"></div>

<script>
    $(document).ready(function() {
   	
    	
    var doc_uuid = "${param.uuid}";
    	
//////PARENT MESSAGING///
    	function getSgnFrameHeight(){
    		return Math.max(
    		        $("html").height(),
    		        $("body").height(),
    		        $("#certsModal .modal-dialog").height(),
    		        $("#pdfModal .modal-dialog").height()
    		    )+30;
     }
    	setInterval(function() { 
    		var iheight = getSgnFrameHeight();
    		parent.postMessage({"sgnFrameHeight":iheight},'*');
    	},500);   	
    	
    	
////FUNCTIONS/////////////////////////////////////////   	
 window.getCookie = function(name) {
  match = document.cookie.match(new RegExp(name + '=([^;]+)'));
  if (match) return match[1];
}
        // Returns "true" if a plugin is present for the MIME
        function hasPluginFor(mime) {
            return navigator.mimeTypes && mime in navigator.mimeTypes;
        }
        // Checks if a function is present (used for Chrome)
        function hasExtensionFor(cls) {
            return typeof window[cls] === 'function';
        }

        function pluginInstalled(){
        //TODO check for hwcrypto legacy also!
        //	if (hasPluginFor("application/x-digidoc")==true||hasExtensionFor("TokenSigning")==true) return true;
        //    return false;
        return true;
        }

        function log_text(s) {
    	    $("#log").append('<div class="alert alert-warning alert-dismissible fade in" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">×</span></button>'+s+'</div>');    
    	}


        function sign(sbtn,uuid){
    	  		
        var btn = $(sbtn);
        var spinner = btn.children("i");
        var oldclass = spinner.attr("class");
        btn.prop('disabled', true);	
        spinner.attr("class","fa fa-spinner fa-pulse fa-fw");
              	    
    	var hashtype = "SHA-256";
    	//var backend = "chrome";
    	var lang = "en";


    	//if (!window.hwcrypto.use(backend)) {
    	//         log_text("Use chrome!"); //??
    	//  }	

    	// Sign
    	window.hwcrypto.getCertificate({lang: lang}).then(function(cert) {
    	   var sreason= $('#reason').val();                                                                            // e1c8c837-3ed0-43d6-ba0b-0f45bd135855  c912db1f-7a56-41e7-9f99-474b35d0f2e4  taskUUID KHE_user_registration--3.0--8--identification--it32faa1be-2318-4e60-935b-0cd69f0fa64e--mainActivityInstance--noLoop
    	    $.getJSON("preSign",{cer:hexToPem(cert.hex),signers:"${param.signers}",reason:sreason,uuid:uuid,loggedUser:"${param.loggedUser}",taskUUID:"${param.taskUUID}"},function(result){
    	 
    	    	if (result.error) {
        	    	log_text("Λάθος: " + result.error);
  	              spinner.attr("class",oldclass);
	              btn.prop('disabled', false);
    	    	} else {
 
    	        var hash=result.HASH2SIGN; //TODO check if HASH2SIGN exist
    	        //log_text("Signing " + hashtype + ": " + hash);
    	        
    	        window.hwcrypto.sign(cert, {type: hashtype, hex: hash}, {lang: lang}).then(function(response) {
    	        // log_text("Generated signature:\n" + response.hex.match(/.{1,64}/g).join("\n"));

    	            //send back
    	            $.post("postSign",{sign: response.hex },function(data, status){
    	            	 log_text("Το σχέδιο υπογράφηκε");
                         console.log("ypografh",data);
    	                 spinner.attr("class",oldclass);
    	                 btn.prop('disabled', false);
    	                 doc_uuid=(JSON.parse(data)).uuid;
    	                 $("#pdf").click();      
                         $("#fpdf").removeClass('hidden');
    	            }); 
    	           }, function(err) {
    	            log_text("Αποτυχημένη προσπάθεια υπογραφής: " + err);
    	            
    	              spinner.attr("class",oldclass);
    	              btn.prop('disabled', false);
    	        });
    	    	}
    	    });

    	}, function(err) {
    	    log_text("Αδυναμία λήψης πιστοποιητικού : " + err);
            spinner.attr("class",oldclass);
            btn.prop('disabled', false);
    	    
    	});
    	
    	}       
        
////////////////////////////////////////////////////////////////       

if (pluginInstalled()){$("#sign").removeClass('hidden');} else {log_text("Δέν έχετε εγκατεστημένο το προσθετο για την δυνατότητα ψηφιακής υπογραφής απευθείας απο το πρόγραμμα περιήγησης. Επικοινωνείστε με το aitimata@athens.gr για εγκατάσταση του πρόσθετου.")}; 

if ("${initstate}"=="true") { $("#initstate").removeClass('hidden'); } else { $("#signstate").removeClass('hidden'); }

$("#fget").click(function() {
	    //TODO set cookie on server and catch to prevent multiclicks
	    // // hope the server sets Content-Disposition: attachment!
	    btn = $(this);
        spinner = btn.children("i");
        oldclass = spinner.attr("class");
        btn.prop('disabled', true);
   	    spinner.attr("class","fa fa-spinner fa-pulse fa-fw");
   	    unsense = Math.random().toString(36).substring(7);
	    window.location = 'getPDF?uuid=${param.uuid}&loggedUser=${param.loggedUser}&taskUUID=${param.taskUUID}&unsense='+unsense;
	    event.stopPropagation();
	    attempts = 60;
	    downloadTimer = window.setInterval( function() {
	        var token = getCookie( "unsense" );
	        if( (token == unsense) || (attempts <= 0) ) {
	            window.clearInterval(downloadTimer);
	            spinner.attr("class",oldclass);
	            btn.prop('disabled', false);
	        }
	        attempts--;
	    }, 1000 );//1000 
 
 });
 
 
$("#fpost").click(function(event){
	$("#file").val("");
	  $("#file").click(); 
   });
 


$("#file").change(function(event){

	  // if ($("#file").val().replace(/.*(\/|\\)/, '')=="${filename}"){
	    	var btn = $("#fpost");
	        var spinner = btn.children("i");
	        var oldclass = spinner.attr("class");
	            btn.prop('disabled', true);	
	       	    spinner.attr("class","fa fa-spinner fa-pulse fa-fw");
	       	    
		   file = event.target.files[0];
		    var data = new FormData();
		    data.append('file',file);
		    data.append('parentUUID', '${param.init}');
		    data.append('loggedUser', '${param.loggedUser}');
		    data.append('taskUUID', '${param.taskUUID}');
		    data.append('description', 'Σχέδιο προς υπογραφή'); 

		    
//first clean folder 		    
  // Introduce a timeout before we make a search request 
  // This is added in the event that this function is called when
  // the page is ready and your user is using Chrome. When in Chrome
  // and a search request is sent it will always return a 403.
  setTimeout(function () {
$.ajax({
    url: 'cleanParent',
    type: 'DELETE',
    data: data,
    cache: false,
    processData: false,
    contentType: false,
    success: function(result) {

    	
    	 if(typeof result.error === 'undefined')
         {
    		 
           $.ajax({
			   url: 'postPDF',
			   type: 'POST',
			   data: data,
			  // dataType: 'json',
			  //async: false,
			  cache: false,
		       processData: false, // Don't process the files
		       contentType: false, // Set content type to false as jQuery will tell the server its a query string request
		       success: function(data)
		        {
		            if(typeof data.error === 'undefined')
		            {
		            	log_text("Το σχέδιο φορτώθηκε"); 
		             //Call sign   
		              spinner.attr("class",oldclass);
		              uuid=data.uuid;
		              sign($('#fpost')[ 0 ],data.uuid);
		            }
		            else
		            {filename
		                // Handle errors here
		                console.log('ERRORS: ' + data.error);              
		                log_text('ERRORS: ' + data.error);
		            }
		        },
		        error: function(jqXHR, textStatus, errorThrown)
		        {
		            // Handle errors here
		            console.log('ERRORS: ' + jqXHR);
		            log_text('ERRORS: ' + textStatus+" "+jqXHR);
		            spinner.attr("class",oldclass);
	                btn.prop('disabled', false);
		        }
			});
           
			
         } else
         {
             // Handle errors here
             console.log('ERRORS: ' + result.error);              
             log_text('ERRORS: ' + result.error);
         }
    	 
    },
    error: function(jqXHR, textStatus, errorThrown)
    {
        // Handle errors here
        console.log('ERRORS: ' + jqXHR,textStatus,errorThrown);
        log_text('ERRORS: ' + textStatus+" "+jqXHR);
    }	

});	

}, 1500); // This works with 100ms, but pad just to be sure.



 }); 

   //Buttons
   $("#signBtn").click(function(){
	   sign(this,"${param.uuid}");   
   });

    $("#certificates").click(function(){
        btn = $(this);
        spinner = btn.children("i");
        oldclass = spinner.attr("class");
        btn.prop('disabled', true);
   	 spinner.attr("class","fa fa-spinner fa-pulse fa-fw");
    	$.getJSON( "verify",{uuid:"${param.uuid}",random:Math.random()}, function( data ) {
    		var items = [];
    		 $.each( data, function( key, val ) {		  
    		    items.push( "<li id='" + key + "' class='list-group-item'>" + val.signerName + " στίς "+val.signDate+"</li>" );
    		  });
    		  $("#certs").html($( "<ul/>", { "class": "certs-list list-group", html: items.join( "" )} ) );
    		  $('#certsModal').modal('show');
              spinner.attr("class",oldclass);
              btn.prop('disabled', false);
    	});
    });    

$("#fpdf").click(function(e){
	$("#pdf").click();
	});
	
$("#pdf").click(function(e){
     btn = $(this);
     spinner = btn.children("i");
     oldclass = spinner.attr("class");
     btn.prop('disabled', true);
	 spinner.attr("class","fa fa-spinner fa-pulse fa-fw");
     
	var oReq = new XMLHttpRequest();
	oReq.open("GET", "getPDF?uuid="+doc_uuid+"&loggedUser=${param.loggedUser}&taskUUID=${param.taskUUID}+&rand="+Math.random(), true);
	oReq.responseType = "arraybuffer";

	oReq.onload = function(oEvent) {
	  var blob = new Blob([oReq.response], {type: "application/pdf"});
	  var pdf= URL.createObjectURL(blob);
	  $("#pdfView").attr("data",pdf);
      $('#pdfModal').modal('show');
      btn.prop('disabled', false);
      spinner.attr("class",oldclass);
	};
	oReq.send();
});

$("#pdfModal").on('hidden.bs.modal', function () { 
	   $(".modal-body").css('filenamemax-height', 150);
	   $(".modal-body").css('height', 150);

});

$("#pdfModal").on('show.bs.modal', function () {
    $("#pdfModal").css('overflow','hidden');
    $(".modal-body").css('overflow-y', 'hidden'); 
    $(".modal-body").css('filenamemax-height', 1200);
    $(".modal-body").css('height', 1200);
});


$("#certsModal").on('hidden.bs.modal', function () { 
	   $(".modal-body").css('filenamemax-height', 150);
	   $(".modal-body").css('height', 150);
});

$("#certsModal").on('show.bs.modal', function () {
    $("#certsModal").css('overflow','hidden');
 $(".modal-body").css('overflow-y', 'auto'); 
 $(".modal-body").css('filenamemax-height', 250);
 $(".modal-body").css('height', 250);
});

});
 
</script>

<div id="initstate" class="panel panel-default hidden">

  <div class="panel-body">
     <div class="row text-center">
         <button id="fpdf" class="hidden btn btn-default"><i class="fa fa-file-pdf-o" aria-hidden="true"></i> Σχέδιο</button>
         <button id="fpost" class="btn btn-default "><i class="fa fa-cloud-upload" aria-hidden="true"></i> Επισύναψη και Υπογραφή Σχεδίου</button>
         <input id="file" type="file" aria-hidden="true" class="hidden" />
     </div>
  </div>
    
</div>

<div id="signstate" class="panel panel-default hidden">

  <div class="panel-body">
     <div class="row text-center">
     
      <button id="pdf" class=" btn btn-default"><i class="fa fa-file-pdf-o" aria-hidden="true"></i> Σχέδιο</button>
      <button id="certificates" class=" btn btn-default"><i class="fa fa-certificate" aria-hidden="true"></i> Υπογραφές</button>
      
      <div class="btn-group" id="sign" class="hidden" aria-hidden="true" >
      <button type="button" id="signBtn" class="btn btn-primary "><i class="fa fa-check"></i> Υπογραφή</button>
      <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
      <span class="caret"></span>
      <span class="sr-only">Toggle Dropdown</span>
     </button>
      <ul class="dropdown-menu">
      <li><input id="reason" name="reason" placeholder="Αιτιολογία υπογραφής"/></li>
     </ul>
     </div>
      
      <button id="fget" type="button" class="btn btn-default"><i class="fa fa-cloud-download" aria-hidden="true"></i> Λήψη</button>
 
     </div>
  </div>
</div>


<br />
<hr />
<br />
<div id="log" class="row"></div>


 <div class="modal fade" tabindex="-1" role="dialog" id="certsModal">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Υπογραφές Σχεδίου</h4>
      </div>
      <div class="modal-body" id="certs">
         <!--Certs here -->
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Κλείσιμο</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

 <div class="modal pdfModal fade" tabindex="-1" role="dialog" id="pdfModal">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content" >
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Σχέδιο</h4>
      </div>
      <div class="modal-body" id="certs">
        <object  id="pdfView" type="application/pdf" width="100%" height="100%"><h1>Ο περιηγητής διαδικτύου δεν υποστηρίζει προεπισκόπηση, χρησιμοποιήστε μια απο τις τελευταίες εκδόσεις των Chrome, Firefox.. Επικοινωνήστε με το aitimata@athens.gr για εγκατάστασή τους.</h1></object>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Κλείσιμο</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

</body>
</html>