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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

import moa.ds.service.athens.gr.radweriel;

/**
 * Servlet implementation class postSign
 */
@WebServlet(
		description = "get sign from hwcrypto", 
		urlPatterns = { "/postSign" }, 
		initParams = { 
				@WebInitParam(name = "sign", value = "")
		})
public class postSign extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public postSign() {
        super();
     /* System.setProperty("https.proxyHost","10.1.20.68");
        System.setProperty("https.proxyPort","3128");
        System.setProperty("http.proxyHost","10.1.20.68");
        System.setProperty("http.proxyPort","3128");
      */ 
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("application/octet-stream");
		HttpSession session = request.getSession(false);
		byte[] hash = (byte[]) session.getAttribute("hash");
		PdfPKCS7 sgn = (PdfPKCS7) session.getAttribute("sgn");
		Calendar cal = (Calendar) session.getAttribute("cal");
		PdfSignatureAppearance sap = (PdfSignatureAppearance) session.getAttribute("sap");
		ByteArrayOutputStream baos = (ByteArrayOutputStream) session.getAttribute("baos");

		String uuid = (String) session.getAttribute("uuid");
	
		session.invalidate();
			
		byte[] sign = hexStringToByteArray(request.getParameter("sign"));

		sgn.setExternalDigest(sign, null, "RSA");
		
		//byte[] encodedSign = sgn.getEncodedPKCS7(hash, cal, null, null, null, CryptoStandard.CMS);
		TSAClient tsaClient = new TSAClientBouncyCastle("https://timestamp.aped.gov.gr/qtss", "", "");
		byte[] encodedSign = sgn.getEncodedPKCS7(hash, cal, tsaClient, null, null, CryptoStandard.CMS);
	
       //System.out.write(Base64.encode(encodedSign)); //analyzer https://lapo.it/asn1js/

		
		int estimatedSize =16384;

		byte[] paddedSig = new byte[estimatedSize];

		System.arraycopy(encodedSign, 0, paddedSig, 0, encodedSign.length);
		
		PdfDictionary dic2 = new PdfDictionary();

		dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));
			 
		try {
			sap.close(dic2);
		} catch (DocumentException e) {
			//e.printStackTrace();
			response.getWriter().println("{\"error\":\""+e.getMessage()+"\"}");
		}
		
		final byte[] pdf = baos.toByteArray();
		 radweriel.updateDocByID(uuid,pdf);
		 response.getWriter().println("{\"status\":\"ok\",\"uuid\":\""+uuid+"\"}");
	}
}
