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

package moa.ds.service.athens.gr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.GeneralSecurityException;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.DocumentException;

/**
 * Servlet implementation class getPDF
 */
@WebServlet(
		urlPatterns = { "/getPDF" }, 
		initParams = { 
				@WebInitParam(name = "uuid", value = "")
		})
public class getPDF extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public getPDF() {
        super();
    }

    
    public static long stream(InputStream input, OutputStream output) throws IOException {
        try (
            ReadableByteChannel inputChannel = Channels.newChannel(input);
            WritableByteChannel outputChannel = Channels.newChannel(output);
        ) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(10240);
            long size = 0;

            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                size += outputChannel.write(buffer);
                buffer.clear();
            }

            return size;
        }
    }
    
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
   
	   String uuid = request.getParameter("uuid").trim();	
       String taskUUID = request.getParameter("taskUUID").trim();

	   String  loggedUser = request.getParameter("loggedUser").trim();

	   String unsense = "false";
	  

	   
	   if (request.getParameterMap().containsKey("unsense")) { 
		   unsense = request.getParameter("unsense").trim();
		}
	      
		
       String browserType=request.getHeader("User-Agent"); //TODO   ELEGXOS an einai sosta parakato
             
       try {
 	      //TODO rethinks security
    	
      //Elegxos an o logged user paei na ypograpsei eggrafo se site pou anikei
       //String UserOU = galgallin.getOUbyUID(loggedUser).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
       String UserOU = MOASSUtils.setOuInAppropriateFormat(loggedUser);
       String site = radweriel.siteByDocUUID("workspace://SpacesStore/"+uuid).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");        
	   
       //TODO rethinks security
	/*      
       boolean isProtocolMember = galgallin.checkMembershipInGroupByUID(loggedUser, "protocol", "ou=groups,ou=DIAKINISI_EGGRAFON,ou=APPLICATIONS");
       if (!isProtocolMember && !UserOU.equals("ΓΕΝΙΚΟΣ ΓΡΑΜΜΑΤΕΑΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) && 
	!UserOU.equals("ΔΗΜΑΡΧΟΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) && 
	!UserOU.equals("Αντιδήμαρχος".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) && 
	!UserOU.equals("Εντεταλμένος Σύμβουλος".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) &&
	!UserOU.equals("ΓΕΝΙΚΟΣ ΔΙΕΥΘΥΝΤΗΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""))){
         if (!Objects.equals(site,UserOU)) throw new RuntimeException("Doc get is not allowed");
       }
       */
       if(!MOASSUtils.transcendPersonRole(loggedUser)){
         if (!Objects.equals(site,UserOU)) throw new RuntimeException("Doc get is not allowed");
       }
	       
       //elenxos an o xristis einai allowed sto sygkekrimeno taskUUID
       if (!thales.cantitateIsValid(taskUUID,loggedUser)) throw new RuntimeException("Not allowed");
       
       
	  //response.setContentLength(
		
       String fileName= URLEncoder.encode(radweriel.getFilenameByID(uuid),"UTF-8");
		
		response.setCharacterEncoding("UTF-8");

		response.setContentType("application/octec-stream");//Den evala application/pdf giati kapia plugin (pdf.js) agnooun to attachment kai anigoun sto idio tab!
                response.setHeader("Content-Disposition","attachment; filename=\""+fileName+"\"");
		if(browserType.equals("IE")||browserType.equals("Chrome"))
	            response.setHeader("Content-Disposition","attachment; filename="+fileName);
	    if(browserType.endsWith("Firefox"))
	            response.setHeader("Content-Disposition","attachment; filename="+fileName+"; filename*=UTF-8''"+fileName);
	   

	    if (!Objects.equals(unsense,"false")){
	    	
	    Cookie userCookie = new Cookie("unsense", unsense);
	    userCookie.setMaxAge(60*60); //Store cookie for 60 sec? 
	    response.addCookie(userCookie);
	    }
	    
		stream(radweriel.getDocByID("workspace://SpacesStore/"+uuid),response.getOutputStream());
	
	} catch (DocumentException e) {
	    throw new IOException(e);
	 } catch (GeneralSecurityException e) {
	     throw new IOException(e);
	 } catch (Exception e) {
		 response.getWriter().println("{\"error\":\""+e.getMessage()+"\"}");
	}
	}
}
