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

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import javax.naming.directory.Attribute;

import javax.naming.directory.Attributes;

import moa.ds.service.athens.gr.MOASSConfig;


public class galgallin {

	private final static String ldapURI = MOASSConfig.getProperty("ldap");
	private final static String contextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
		
    private static boolean haveContactEmail(Attributes attrs){
    	boolean have=false;
    	try {
    		for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMore();) {
		          Attribute attr = (Attribute) ae.next();
		          if (attr.getID()==MOASSConfig.getProperty("mailAttr")) have=true; 
		        }
    	} catch (NamingException e) {
	        return false;
	      }
    	return have;
    }
    
	private static DirContext ldapContext () throws Exception {
		Hashtable<String,String> env = new Hashtable <String,String>();
		return ldapContext(env);
	}	
	
	private static DirContext ldapContext (Hashtable <String,String>env) throws Exception {
		env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
		env.put(Context.PROVIDER_URL, ldapURI);
		DirContext ctx = new InitialDirContext(env);
		return ctx;
	}
	
	
	public static String getUIDbyEmail(String email) throws Exception{
		DirContext ctx = ldapContext();

		String filter = "(|(mail="+email+")(gosaMailAlternateAddress="+email+")("+MOASSConfig.getProperty("mailAttr")+"="+email+"))";
		SearchControls ctrl = new SearchControls();
		ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<?> answer = ctx.search("", filter, ctrl);

		Attributes atrs = null;
		if (answer.hasMore()) {
			SearchResult result = (SearchResult) answer.next();
			atrs = result.getAttributes();

		}
		answer.close();
		return (String) atrs.get("uid").get().toString().trim();

	}
	
	public static String getDNbyUID (String UID) throws Exception {
		DirContext ctx = ldapContext();

		String filter = "(uid=" + UID + ")";
		SearchControls ctrl = new SearchControls();
		ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<?> answer = ctx.search("", filter, ctrl);
		String dn;
		if (answer.hasMore()) {
			SearchResult result = (SearchResult) answer.next();
			dn = result.getNameInNamespace();
		}
		else {
			dn = null;
		}
		answer.close();
		return dn;
	}
	
	
	public static String getOUbyUID (String UID) throws Exception {
		DirContext ctx = ldapContext();
		String filter = "(uid=" + UID + ")";
		SearchControls ctrl = new SearchControls();
		ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<?> answer = ctx.search("", filter, ctrl);

		Attributes atrs = null;
		if (answer.hasMore()) {
			SearchResult result = (SearchResult) answer.next();
			atrs = result.getAttributes();

		}
		answer.close();
		return (String) atrs.get("ou").get();
	}
	
	
	     public static String getDepNumbyUID (String UID) throws Exception {
		DirContext ctx = ldapContext();
		String filter = "(uid=" + UID + ")";
		SearchControls ctrl = new SearchControls();
		ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<?> answer = ctx.search("", filter, ctrl);

		Attributes atrs = null;
		if (answer.hasMore()) {
			SearchResult result = (SearchResult) answer.next();
			atrs = result.getAttributes();

		}
		answer.close();
		return (String) atrs.get("departmentNumber").get();
	}
	
	
	
	
	     public static String getEmpTbyUID (String UID) throws Exception {
		DirContext ctx = ldapContext();
		String filter = "(uid=" + UID + ")";
		SearchControls ctrl = new SearchControls();
		ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<?> answer = ctx.search("", filter, ctrl);

		Attributes atrs = null;
		if (answer.hasMore()) {
			SearchResult result = (SearchResult) answer.next();
			atrs = result.getAttributes();

		}
		answer.close();
		return (String) atrs.get("employeeType").get();
	}
	
	
	
	
	public static boolean checkMembershipInGroupByUID (String UID,String groupCN,String Base) throws Exception {
		boolean isMember=false;
		DirContext ctx = ldapContext();
		String filter = "(cn=" + groupCN + ")";
		SearchControls ctrl = new SearchControls();
		ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<?> answer = ctx.search(Base, filter, ctrl);
		Attributes atrs = null;
		if (answer.hasMore()) {
			SearchResult result = (SearchResult) answer.next();
			atrs = result.getAttributes();
			String mailAttr = MOASSConfig.getProperty("membershipID");
			Attribute attr = atrs.get(mailAttr);
			for (int i=0;i<attr.size();i++)
		     {
			   String memberUUID = (String) (attr.get(i));
		       if (memberUUID.equals(UID)) isMember = true;
		     }
		}
		answer.close();
		return isMember;
	}
	
	public static String[] getEmailsByUID (String UID) throws Exception {
		DirContext ctx = ldapContext();

		String filter = "(uid=" + UID + ")";
		SearchControls ctrl = new SearchControls();
		ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
		NamingEnumeration<?> answer = ctx.search("", filter, ctrl);

		Attributes atrs = null;
		if (answer.hasMore()) {
			SearchResult result = (SearchResult) answer.next();
			atrs = result.getAttributes();

		}
		answer.close();
		

		
		if (haveContactEmail(atrs))	return new String[]{(String) atrs.get("mail").get(),(String) atrs.get(MOASSConfig.getProperty("mailAttr")).get()};
		else return new String[]{(String) atrs.get("mail").get()};
	}
}
