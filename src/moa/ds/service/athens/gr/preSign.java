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
   <authors' address : Λιοσίων 22, Αθήνα - Ελλάδα, ΤΚ:10438 ---- Liosion 22, Athens - Greeece, PC:10438>
*/

package moa.ds.service.athens.gr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import moa.ds.service.athens.gr.radweriel;
import moa.ds.service.athens.gr.MOASSUtils;
import moa.ds.service.athens.gr.MOASSConfig;

import java.lang.String;

/**
 * Servlet implementation class preSign
 */
@WebServlet(
		description = "Get PDF from radweriel uuid and returns hash", 
		urlPatterns = { "/preSign" }, 
		initParams = { 
				@WebInitParam(name = "reason", value = "", description = "reason to sign"),
				@WebInitParam(name = "signers", value = "", description = "signers of pdf file"),
				@WebInitParam(name = "uuid", value = "", description = "uuid of pdf file"),
				@WebInitParam(name = "taskUUID", value = "", description = "uuid of runing task"),
				@WebInitParam(name = "loggedUser", value = "", description = "the bonita logged user")
		})

public class preSign extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static X509Certificate cer = null;
	
    private static Calendar cal = null;

	private static String reason = null; 
	
	private static String uuid = null;

	private static String taskUUID = null;

	private static String loggedUser = null;

	private static String crtEmail = null;
	
	private static Integer sigpage = 1; 
	
	private static Rectangle size = null;
	
	private static PdfSignatureAppearance sap=null;
	
    private static PdfStamper stamper = null;
    
    private static PdfReader reader = null;
    
    private static ByteArrayOutputStream baos = null;

    private static float signHeight=50;
    
    private static String signers[]=null;
   
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * @see HttpServlet#HttpServlet()
     */
    public preSign() {
        super();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
    private void setPlaceHolders(int numOfSignatures) throws DocumentException, IOException{
        float offset=20;
        float pageHeight = (numOfSignatures*signHeight)+offset;
    	 stamper.insertPage(sigpage, new Rectangle(0, 0, size.getWidth(),pageHeight)); 
    	 String path = getServletContext().getRealPath("/assets/arialuni.ttf");
    	 BaseFont fonty = BaseFont.createFont(path, BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
         fonty.setSubset(true);//Όλες οι θέσεις των υπογραφών πρέπει να συμπληρωθούν απο τους υπογράφοντες ώστε το έγγραφο να είναι έγκυρο
         Phrase myWarning = new Phrase("----- Θέσεις Υπογραφών -----", new Font(fonty,10));
    	 
    	 ColumnText.showTextAligned(stamper.getOverContent(sigpage),Element.ALIGN_LEFT, myWarning, 0, pageHeight-(offset/2), 0);

        for (int i=1; i<=numOfSignatures; i++ ) {
        //TODO na vazo ta cn auton pou prepei na ypograpsoun
        //TODO on reverse order
         stamper.addSignature("sig-"+i, sigpage, 0,((signHeight)*(i-1)) , size.getWidth(), (signHeight*i));        
        }
    }
        
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	   response.setContentType("application/json; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");

	  int numOfSignatures=0;
		
      try{ 
		signers = request.getParameter("signers").split(",");
		numOfSignatures=signers.length;
		if (numOfSignatures<1||numOfSignatures>6)  throw new RuntimeException("λάθος υπογράφοντες");
		cer = MOASSUtils.getCer(request.getParameter("cer").getBytes("UTF-8"));
		
	    reason = request.getParameter("reason");
	    if (reason=="") reason="signed"; 
	    uuid = request.getParameter("uuid").trim();
	    taskUUID = request.getParameter("taskUUID").trim();
	    loggedUser = request.getParameter("loggedUser").trim();
      } catch (Exception e) {
 		 response.getWriter().println("{\"error\":\" Πρόβλημα στις παραμέτρους "+e.getMessage()+"\"}");
 		 return;
         }
        
	  try {
	
		    cal = Calendar.getInstance(); //cal.set(1969, 3, 1,5, 0,0); //cal.setTimeInMillis(5000);
		    baos = new ByteArrayOutputStream();		  
		    reader = new PdfReader(radweriel.getDocByID("workspace://SpacesStore/"+uuid));
		

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//                      AUTHORIZE CHECKS
//		  
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Elenxos an o katoxos tou certificate einai o idios me auton pou exei kanei login
        crtEmail=MOASSUtils.getCertificateEmail(cer);
        
        String userUID= galgallin.getUIDbyEmail(crtEmail); 
        if (!Objects.equals(userUID,loggedUser)) throw new RuntimeException("Δεν έχετε δικαίωμα υπογραφής σε αυτό το βήμα");
        
                       
        //Elegxos an o logged user paei na ypograpsei eggrafo se site pou anikei
        //String UserOU = galgallin.getOUbyUID(userUID).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
	String UserOU = MOASSUtils.setOuInAppropriateFormat(userUID);
	String site = radweriel.siteByDocUUID("workspace://SpacesStore/"+uuid).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");        

        
        //TODO Dirty workarrounds FIX!!!!
	/*
        boolean isProtocolMember = galgallin.checkMembershipInGroupByUID(loggedUser, "protocol", "ou=groups,ou=DIAKINISI_EGGRAFON,ou=APPLICATIONS");
        
        if (!isProtocolMember && !UserOU.equals("ΓΕΝΙΚΟΣ ΓΡΑΜΜΑΤΕΑΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) && 
	!UserOU.equals("ΔΗΜΑΡΧΟΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) && 
	!UserOU.equals("Αντιδήμαρχος".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) && 
	!UserOU.equals("Εντεταλμένος Σύμβουλος".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) &&
	!UserOU.equals("ΓΕΝΙΚΟΣ ΔΙΕΥΘΥΝΤΗΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""))){
        	String site = radweriel.siteByDocUUID("workspace://SpacesStore/"+uuid).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");        
        	if (!Objects.equals(site,UserOU)) throw new RuntimeException("1 Δέν επιτρέπεται η υπογραφή, το λάθος καταγράφηκε και θα χρησιμοποιηθεί προς διώξή σας αν διαπιστωθεί εσκεμμένη προσπάθεια παραβίασης");
        }
	*/
	if(!MOASSUtils.transcendPersonRole(userUID)){
         if (!Objects.equals(site,UserOU)) throw new RuntimeException("1 Δέν επιτρέπεται η υπογραφή, το λάθος καταγράφηκε και θα χρησιμοποιηθεί προς διώξή σας αν διαπιστωθεί εσκεμμένη προσπάθεια παραβίασης");
       }
        //elenxos an o xristis einai allowed sto sygkekrimeno taskUUID
        if (!thales.cantitateIsValid(taskUUID,loggedUser)) throw new RuntimeException("2 Δέν επιτρέπεται η υπογραφή, το λάθος καταγράφηκε και θα χρησιμοποιηθεί προς διώξή σας αν διαπιστωθεί εσκεμμένη προσπάθεια παραβίασης");
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////        

        //Stoixeia tou xrhsth
		String certInfo = cer.getSubjectX500Principal().getName();
		String creator = certInfo.substring(certInfo.indexOf("CN=") + 3,certInfo.indexOf(",OU", certInfo.indexOf("CN=") + 3));

	    //System.out.println(reader.getCertificationLevel()); //TODO isos prepei na to koitao 
    
        AcroFields fields = reader.getAcroFields();
        ArrayList<String> names = fields.getSignatureNames();
        int sigs=names.size();

    	if ((sigs+1)>numOfSignatures) throw new RuntimeException("Έχουν πραγματοποιηθεί όλες οι υπογραφές");
   	      //if (MOASSUtils.isSignedBy(fields,loggedUser)) throw new RuntimeException("Έχετε ήδη υπογράψει");  	       
        if (!Objects.equals(userUID,signers[sigs])) throw new RuntimeException("Δεν αναμένεται υπογραφή απο εσάς σε αύτο το βήμα");

        if (sigs==0) 
        {        	
       
        //Create signatures placeholders in new created page
        stamper = new PdfStamper(reader,baos);
                
        Map<String, String> info = reader.getInfo();
       //info.put("Title", "Hello World");
       //info.put("Subject", "Hello World with changed metadata");
       //info.put("Keywords", signers);
       info.put("Creator", "MOASS signer");
       info.put("Author", creator);
       info.put("ModDate", cal.getTime().toString());
       info.put("CreationDate", cal.getTime().toString()); //info.put("CreationDate", "D:20120816122649+02'00'");
       stamper.setMoreInfo(info);	
        
        
       	//create new page to hold signatures
	    int pages =reader.getNumberOfPages();
    
	    size = reader.getPageSize(1);
	    sigpage=pages+1;

        setPlaceHolders(numOfSignatures);

        stamper.close();
        reader.close(); 
        
        //Create a copy from modified PDF with signature placeholders 
        PdfReader copy = new PdfReader(new ByteArrayInputStream(baos.toByteArray()));
        baos.reset();//Reset stream to reuse it
        
        stamper =  PdfStamper.createSignature(copy, baos, '7', new File("/tmp"),false); 
		
        sap = stamper.getSignatureAppearance();
        
		sap.setVisibleSignature("sig-1"); //H proth ypografh einai gia to certificate
		
		//CERTIFIED_NO_CHANGES_ALLOWED Gia eggrafa pou den dexontai alles ypografes.TODO Na do ta locks gia thn teleutea ypografh
		
	    sap.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS);
        
        }   
        
       else{
    	   
    	  stamper = PdfStamper.createSignature(reader, baos, '7', new File("/tmp"),true);
    	  
          sap = stamper.getSignatureAppearance(); 
          
          // sap.setVisibleSignature(new Rectangle(0, 0, size.getWidth(), 200), sigpage, "sig1");  
          sap.setVisibleSignature("sig-"+(sigs+1));//TODO select based on logged user
		  sap.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
       }
       
        
        
        //set Font
   	    String path = getServletContext().getRealPath(MOASSConfig.getProperty("font"));
   	    BaseFont fonty = BaseFont.createFont(path, BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
        fonty.setSubset(true);
        Font sapFont = new Font(fonty,8);
        sap.setLayer2Font(sapFont);
        
        
		sap.setCertificate(cer);
		sap.setSignDate(cal);
		sap.setReason(reason);
        sap.setLocation("Athens");
        sap.setSignatureCreator(creator);
        sap.setContact("Municipality of Athens");
       
        //set the dic
		PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
        dic.setReason(sap.getReason());
        dic.setLocation(sap.getLocation());
        dic.setName(creator);
        dic.setSignatureCreator(sap.getSignatureCreator());
        dic.setContact(sap.getContact());
        dic.setDate(new PdfDate(sap.getSignDate()));
		dic.setCert(cer.getEncoded());
     
        sap.setCryptoDictionary(dic);
        
        HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
        int estimatedSize =16384;
		exc.put(PdfName.CONTENTS, new Integer(estimatedSize  * 2 + 2));//
		sap.preClose(exc);

		ExternalDigest externalDigest = new ExternalDigest() {
			 public MessageDigest getMessageDigest(String hashAlgorithm)
			 throws GeneralSecurityException {
			 return DigestAlgorithms.getMessageDigest(hashAlgorithm, null);
			 }
			};

  		PdfPKCS7 sgn = new PdfPKCS7(null, new Certificate[] { cer }, "SHA256", null, externalDigest, false);
			
		InputStream data = sap.getRangeStream();

				byte hash[] = DigestAlgorithms.digest(data,externalDigest.getMessageDigest("SHA256"));
							
				byte[] aab = sgn.getAuthenticatedAttributeBytes(hash, cal, null, null, CryptoStandard.CMS);
				
				byte[] sh = MessageDigest.getInstance("SHA256", "BC").digest(aab);
				
				 HttpSession session = request.getSession(true);
 
				 session.setAttribute("sgn", sgn);
				 session.setAttribute("hash", hash);
				 session.setAttribute("cal", cal);
				 session.setAttribute("sap", sap);
				 session.setAttribute("baos", baos);
				 session.setAttribute("uuid", uuid);
 
			     String HASH2SIGN = new String(String.format("%064x", new java.math.BigInteger(1, sh)));
				 			     
			     response.getWriter().println("{\"HASH2SIGN\":\""+HASH2SIGN+"\"}");

	    } catch (DocumentException e) {
	    	    throw new IOException(e);
	    	 } catch (GeneralSecurityException e) {
	    	     throw new IOException(e);
	    	 } catch (Exception e) {
	    		 System.out.println(e);
	    		 //e.printStackTrace();
	    		 response.getWriter().println("{\"error\":\""+e.getMessage()+"\"}");
			}
		
	}

}
