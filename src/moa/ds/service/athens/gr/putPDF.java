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
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.itextpdf.text.DocumentException;
import moa.ds.service.athens.gr.MOASSUtils;
import moa.ds.service.athens.gr.radweriel;


/**
 * Servlet implementation class putPDF
 */
@WebServlet(
		urlPatterns = { "/putPDF" }, 
		initParams = { 
				@WebInitParam(name = "uuid", value = "")
		})
@MultipartConfig
public class putPDF extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	static {
        Security.addProvider(new BouncyCastleProvider());
    }



    /**
     * @see HttpServlet#HttpServlet()
     */
    public putPDF() {
        super();
    }

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
try {	
  
	   //String uuid = request.getParameter("uuid"); //Logo bug mallon... den doulevei.. kanonika tha eprepe se servlet 3.1 
		String uuid = MOASSUtils.getValueFromPart(request.getPart("uuid"));
		String loggedUser = MOASSUtils.getValueFromPart(request.getPart("loggedUser"));
		String taskUUID = MOASSUtils.getValueFromPart(request.getPart("taskUUID"));
	    Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
        //String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
	    InputStream fileContentStream = filePart.getInputStream();

	    
	    final byte[] fileContent = IOUtils.toByteArray(fileContentStream);
	   	    
	      //TODO rethinks security

	      //Elegxos an o logged user paei na ypograpsei eggrafo se site pou anikei
	       //String UserOU = galgallin.getOUbyUID(loggedUser).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
		String UserOU = MOASSUtils.setOuInAppropriateFormat(loggedUser);
		       
	       String site = radweriel.siteByDocUUID("workspace://SpacesStore/"+uuid).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");        
	       if (!Objects.equals(site,UserOU)) throw new RuntimeException("Doc put is not allowed");
	      
	       //elenxos an o xristis einai allowed sto sygkekrimeno taskUUID
	       if (!thales.cantitateIsValid(taskUUID,loggedUser)) throw new RuntimeException("Not allowed");	    
		    
	  if (MOASSUtils.isLastSignFromLoggedUser(fileContent,loggedUser)==true)     
  	 radweriel.updateDocByID(uuid, fileContent); else  throw new RuntimeException("Not correct signer");
    
	  //get signatures and write to output 
	  response.getWriter().println("{\"status\":\"ok\"}");
	  
} catch (DocumentException e) {
    throw new IOException(e);
 } catch (GeneralSecurityException e) {
     throw new IOException(e);
 } catch (Exception e) {
	 response.getWriter().println("{\"error\":\""+e.getMessage()+"\"}");
 }
	
}

}
