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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.Part;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.PdfPKCS7;

public class MOASSUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
	//Get value from form-data 
	public static String getValueFromPart(Part part) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
	    StringBuilder value = new StringBuilder();
	    char[] buffer = new char[1024];
	    for (int length = 0; (length = reader.read(buffer)) > 0;) {
	        value.append(buffer, 0, length);
	    }
	    return value.toString();
	}

    public static X509Certificate getCer(byte[] certDecoded) {
        ///PERNO to certificate string (certDecoded) kai to ftiaxno se X509
        CertificateFactory certFactory;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
			ByteArrayInputStream in = new ByteArrayInputStream(certDecoded);
            return (X509Certificate) certFactory.generateCertificate(in);
        } catch (CertificateException e1) {
            e1.printStackTrace();
        }
        return null;
    }
	
	//Get email from certificate
	public static String getCertificateEmail(X509Certificate certificate) throws CertException {
        String emailOID = "2.5.29.17";
        byte[] emailBytes = certificate.getExtensionValue(emailOID);
        String certificateEmail = null;
        try {
            byte[] octets =  ((DEROctetString) org.bouncycastle.asn1.DEROctetString.fromByteArray(emailBytes)).getOctets();
            GeneralNames generalNameCont = GeneralNames.getInstance(org.bouncycastle.asn1.DEROctetString.fromByteArray(octets));
            org.bouncycastle.asn1.x509.GeneralName[] generalNames = generalNameCont.getNames();
            if (generalNames.length > 0) {
                org.bouncycastle.asn1.x509.GeneralName generalName = generalNames[0];
                certificateEmail = generalName.getName().toString();
            }
        } catch (IOException e) {
            throw new CertException("Email could not be extracted from certificate", e);
        }
        return certificateEmail;
    }

	public static boolean isLastSignFromLoggedUser(byte[] PDF,String loggedUser) throws Exception {
        PdfReader reader = new PdfReader(PDF);
        AcroFields fields = reader.getAcroFields();   
        ArrayList<String> names = fields.getSignatureNames();     
       try {
            String last = names.get(names.size()-1);
            PdfPKCS7 pkcs7 = fields.verifySignature(last);
            X509Certificate cer = (X509Certificate) pkcs7.getSigningCertificate();
            String crtEmail = getCertificateEmail(cer);
            String userUID= galgallin.getUIDbyEmail(crtEmail);        
            if (!Objects.equals(userUID,loggedUser)) return false;
          return true;
       } catch (IndexOutOfBoundsException ex ){
    	   return false;
       } 
	}
	
    public static String getSignatureCantitate(AcroFields fields, String name) throws GeneralSecurityException, IOException {
        PdfPKCS7 pkcs7 = fields.verifySignature(name);
		X509Certificate cert = (X509Certificate) pkcs7.getSigningCertificate();
        return CertificateInfo.getSubjectFields(cert).getField("CN");
	}
	
	//return all signatures as array
	public static List<String> signatures(byte[] PDF) throws IOException, GeneralSecurityException{
		List<String> sigs =new ArrayList<String>();
     PdfReader reader = new PdfReader(PDF);
     AcroFields fields = reader.getAcroFields();   
     ArrayList<String> names = fields.getSignatureNames();	
     for (String name : names) {
    	 sigs.add(getSignatureCantitate(fields, name));
		}
		return sigs;
	}

	public static boolean isSignedBy(AcroFields fields,String loggedUser) throws Exception{
	     ArrayList<String> names = fields.getSignatureNames();	
		for (String name : names) {
		   PdfPKCS7 pkcs7 = fields.verifySignature(name);
           X509Certificate cer = (X509Certificate) pkcs7.getSigningCertificate();
           String crtEmail = getCertificateEmail(cer);
           String userUID= galgallin.getUIDbyEmail(crtEmail);
           if (Objects.equals(userUID,loggedUser)) return true;
		}
	return false;	
	}
	
    public static String signature2json(AcroFields fields, String name) throws GeneralSecurityException, IOException {

        PdfPKCS7 pkcs7 = fields.verifySignature(name);
		X509Certificate cert = (X509Certificate) pkcs7.getSigningCertificate();
		
		Calendar calendar=pkcs7.getSignDate();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String signDate =format.format(calendar.getTime());
		
    	String json="{";
    	json += "\"coverall\":\""+fields.signatureCoversWholeDocument(name)+"\",";//Signature covers whole document
    	json += "\"revision\":\""+fields.getRevision(name)+"\",";//Document revision
    	json += "\"totalRevisions\":\""+fields.getTotalRevisions()+"\",";
    	json += "\"integrity\":\""+pkcs7.verify()+"\",";//Integrity check OK? 
    	json += "\"hashAlgorithm\":\""+pkcs7.getHashAlgorithm()+"\",";//Hash algorithm
    	json += "\"digestAlgorithm\":\""+pkcs7.getHashAlgorithm()+"\",";//Digest algorithm
    	json += "\"encryptionAlgorithm\":\""+pkcs7.getEncryptionAlgorithm()+"\",";//Encryption algorithm
    	json += "\"filterSubtype\":\""+pkcs7.getFilterSubtype()+"\",";//Filter subtype
    	json += "\"signDate\":\""+signDate+"\",";//Sign date
    	//TODO ti alla mporo na epistrefo?? json += "\"\":\""++"\",";
    	String userUID="null";
    	try {
    		userUID= galgallin.getUIDbyEmail(CertificateInfo.getSubjectFields(cert).getField("E"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	json += "\"uid\":\""+userUID+"\",";//Name of the signer 
    	json += "\"signerName\":\""+CertificateInfo.getSubjectFields(cert).getField("CN")+"\",";//Name of the signer
    	json += "\"email\":\""+CertificateInfo.getSubjectFields(cert).getField("E")+"\",";//email of the signer
    	json += "\"surname\":\""+CertificateInfo.getSubjectFields(cert).getField("SURNAME")+"\",";
    	json += "\"givename\":\""+CertificateInfo.getSubjectFields(cert).getField("GIVENNAME")+"\",";    	
    	json += "\"country\":\""+CertificateInfo.getSubjectFields(cert).getField("C")+"\",";  
    	json += "\"organization\":\""+CertificateInfo.getSubjectFields(cert).getField("OU")+"\",";
    	json += "\"certifier\":\""+CertificateInfo.getSubjectFields(cert).getField("O")+"\",";
    	json += "\"serial\":\""+CertificateInfo.getSubjectFields(cert).getField("SN")+"\"";
    	json +="}";
    	return json;
    }
	
	
  public static String setOuInAppropriateFormat (String userUID) throws Exception {
	  
	String UserOU = galgallin.getOUbyUID(userUID).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
	String UserDepNum = galgallin.getDepNumbyUID(userUID).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
	String UserEmpT = galgallin.getEmpTbyUID(userUID).replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
	String UserSite = "";
	  
	if(UserEmpT != null && !UserEmpT.trim().isEmpty() && UserEmpT.trim().contentEquals("null")==false) {
		if(UserEmpT.trim().contentEquals("ΠΡΟΕΔΡΟΣ ΔΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""))==true)
			UserSite = "Ημερήσιες Δημοτικού Συμβουλίου".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
		else if(UserEmpT.trim().contentEquals("ΠΡΟΕΔΡΟΣ ΟΕ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""))==true)
			UserSite = "ΟΙΚΟΝΟΜΙΚΗ ΕΠΙΤΡΟΠΗ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
		else if(UserEmpT.trim().contentEquals("ΠΡΟΕΔΡΟΣ ΕΠΖ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""))==true)
			UserSite = "ΕΠΙΤΡΟΠΗ ΠΟΙΟΤΗΤΑΣ ΖΩΗΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
		}
	
	if(UserSite == null || UserSite.trim().isEmpty() || UserSite.trim().contentEquals("null")!=false) {
	if(UserOU != null && !UserOU.trim().isEmpty() && UserOU.trim().contentEquals("null")==false) { /*
   		if(UserOU.trim().contentEquals("ΔΗΜΑΡΧΟΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""))==true)
        		UserSite = "ΑΥΤΟΤΕΛΕΣ ΤΜΗΜΑ ΔΙΟΙΚΗΤΙΚΗΣ ΥΠΟΣΤΗΡΙΞΗΣ ΔΗΜΑΡΧΟΥ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
     		else if(UserOU.trim().contentEquals("ΓΕΝΙΚΟΣ ΓΡΑΜΜΑΤΕΑΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""))==true)
        		UserSite =  "ΑΥΤΟΤΕΛΕΣ ΤΜΗΜΑ ΔΙΟΙΚΗΤΙΚΗΣ ΥΠΟΣΤΗΡΙΞΗΣ ΓΕΝΙΚΟΥ ΓΡΑΜΜΑΤΕΑ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
    		else if(UserOU.trim().contentEquals("Αντιδήμαρχος".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""))==true) {
        		String returned_ou = "Αντιδήμαρχος " + UserDepNum.trim();
        		UserSite = returned_ou.trim().replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
    		}
    		else if(UserOU.trim().contentEquals("ΓΕΝΙΚΟΣ ΔΙΕΥΘΥΝΤΗΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""))==true)
        		UserSite = UserDepNum.trim();
   	 	else if(UserOU.trim().contentEquals("Εντεταλμένος Σύμβουλος".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", ""))==true) {
        		String returned_ou = "Εντεταλμένος Σύμβουλος - " + UserDepNum.trim();
        		UserSite = returned_ou.replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "");
    		}
    		else 
		*/ 
		UserSite = UserOU.trim();
	} else UserSite = UserDepNum.trim();
	}
	
	return UserSite.trim();
      }
 
	
	
	public boolean transcendPersonRole (String userUID) throws Exception {
	
	boolean isGSecMember = galgallin.checkMembershipInGroupByUID(userUID.trim(), "general_secretary", "ou=groups,ou=DIAKINISI_EGGRAFON,ou=APPLICATIONS");	
	boolean isMayorMember = galgallin.checkMembershipInGroupByUID(userUID.trim(), "mayor", "ou=groups,ou=DIAKINISI_EGGRAFON,ou=APPLICATIONS");	
	boolean isProtocolMember = galgallin.checkMembershipInGroupByUID(userUID.trim(), "protocol", "ou=groups,ou=DIAKINISI_EGGRAFON,ou=APPLICATIONS");
       
       	String UserOU = MOASSUtils.setOuInAppropriateFormat(userUID.trim());
	  	
	if (!isGSecMember && !isMayorMember && !isProtocolMember && 
	!UserOU.equals("ΔΗΜΑΡΧΟΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) && 
	!UserOU.equals("ΓΕΝΙΚΟΣ ΓΡΑΜΜΑΤΕΑΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) && 
	!UserOU.equals("Αντιδήμαρχος".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) && 
	!UserOU.equals("Εντεταλμένος Σύμβουλος".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")) &&
	!UserOU.equals("ΓΕΝΙΚΟΣ ΔΙΕΥΘΥΝΤΗΣ".replaceAll("[ /s\\/,.!@#$%^&*()-+_=]", "")))
		return false;
	else return true;
	}
	
	
	
	public static String json_signatures(InputStream PDF) throws IOException, GeneralSecurityException {

		String json = "[";
		
        PdfReader reader = new PdfReader(PDF);
        AcroFields fields = reader.getAcroFields();
        
        ArrayList<String> names = fields.getSignatureNames();
        Integer nl = 0;
		for (String name : names) {
			nl++;
			json+=signature2json(fields, name);
			if (nl<names.size()) json+=",";
		}
		
		if (json!="[") return json+"]";  
		return "[]";
	}
}
