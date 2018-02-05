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
import java.nio.file.Paths;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import moa.ds.service.athens.gr.MOASSUtils;
import moa.ds.service.athens.gr.radweriel;


/**
 * Servlet implementation class postPDF
 */
@WebServlet(
		description = "create new pdf on folder", 
		urlPatterns = { "/postPDF" }, 
		initParams = { 
				@WebInitParam(name = "init", value = "", description = "folder uuid")
		})

//To parakato oposdipote!!!
@MultipartConfig
public class postPDF extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public postPDF() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			response.setContentType("application/json; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
		          
			String uuid=MOASSUtils.getValueFromPart(request.getPart("parentUUID"));
			String description=MOASSUtils.getValueFromPart(request.getPart("description")); 
			String loggedUser = MOASSUtils.getValueFromPart(request.getPart("loggedUser"));
			String taskUUID = MOASSUtils.getValueFromPart(request.getPart("taskUUID"));
		    Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
		    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
		    InputStream fileContentStream = filePart.getInputStream();
		    
		    final byte[] fileContent = IOUtils.toByteArray(fileContentStream);

		      //TODO rethinks security
		   	
		  //elegxos an o fakelos einai adios!   
		     if (radweriel.countChildsOnFolderByUUID(uuid)!=0) throw new RuntimeException("Ο φάκελος περιέχει ήδη σχέδιο");

		  // TODO elegxos an o xrhsths mporei na grapsei ston fakelo
		  // MIPOS na koitazo sta diekomata tou fakelou an    
		     
		  // Elegxos an o logged user einai yparktos
		     String UserOU = galgallin.getOUbyUID(loggedUser).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""); 
		     if (UserOU=="") throw new RuntimeException("Not found");
		      
		  // elenxos an o xristis einai allowed sto sygkekrimeno taskUUID
		     if (!thales.cantitateIsValid(taskUUID,loggedUser)) throw new RuntimeException("Not allowed");
		   
		     String pdf_uuid=radweriel.uploadToFolderByUUID(uuid,fileContent,fileName,description);
			  response.getWriter().println("{\"uuid\":\""+pdf_uuid+"\"}");
		} catch (Exception e) {
			 response.getWriter().println("{\"error\":\""+e.getMessage()+"\"}");
		 }
		
	}

}
