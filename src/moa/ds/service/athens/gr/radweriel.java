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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import moa.ds.service.athens.gr.MOASSConfig;


public class radweriel {

	
	public static String alf_server="radweriel";

	private static Session connectToRadweriel(){
    	SessionFactory factory = SessionFactoryImpl.newInstance();
    	Map<String, String> parameter = new HashMap<String, String>();
    	// user credentials
    	parameter.put(SessionParameter.USER, MOASSConfig.getProperty("alfUser"));
    	parameter.put(SessionParameter.PASSWORD, MOASSConfig.getProperty("alfPassword"));
    	// connection settings
    	parameter.put(SessionParameter.ATOMPUB_URL, "http://"+alf_server+"."+MOASSConfig.getProperty("alfServer")+"/alfresco/api/-default-/public/cmis/versions/1.1/atom");
    	parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
    	parameter.put(SessionParameter.REPOSITORY_ID, "-default-");
    	// create session
    	return factory.createSession(parameter);
    }

    public static InputStream getDocByID(String uuid){
    	  Session radweirelSession= connectToRadweriel();
    	  CmisObject object = radweirelSession.getObject(radweirelSession.createObjectId(uuid));
    	  Document document = (Document) object;
    	//  String filename = document.getName();
    	  return document.getContentStream().getStream();
    }
    
    public static void updateDocByID(String uuid,byte[] content){
     Session radweirelSession= connectToRadweriel();
   	 CmisObject object = radweirelSession.getObject(radweirelSession.createObjectId(uuid));
   	 Document document = (Document) object;
   	 String name = document.getName();
   	
    //set content
   	InputStream stream = new ByteArrayInputStream(content);
   	ContentStream contentStream = new ContentStreamImpl(name, BigInteger.valueOf(content.length), "application/pdf", stream);

   	//update
   	document.setContentStream(contentStream,true,true);
    }
    
    public static String getFilenameByID(String uuid){
  	  Session radweirelSession= connectToRadweriel();
  	  CmisObject object = radweirelSession.getObject(radweirelSession.createObjectId(uuid));
  	  Document document = (Document) object;
  	 return document.getName();
    }
    
    
    public static String getUUIDbyPath(String Path){	
    	Session radweirelSession= connectToRadweriel();
      	CmisObject object = radweirelSession.getObjectByPath(Path);
        return object.getId();
    }
    
    public static String getTitleByUUID(String uuid){
      	Session radweirelSession= connectToRadweriel();
    	CmisObject object = radweirelSession.getObject(radweirelSession.createObjectId(uuid));
    	return object.getPropertyValue("cm:title").toString();
    }
    
    public static String siteByDocUUID(String uuid){
 	  
    try {
    String SiteName= "";	
  	Session radweirelSession= connectToRadweriel();
  	CmisObject object = radweirelSession.getObject(radweirelSession.createObjectId(uuid));
  	Document document = (Document) object;

  	String Paths= document.getPaths().get(0); 
  	Pattern pattern = Pattern.compile("^/Sites/(?<sitename>[^/]+)/documentLibrary/.*$");
  	Matcher matcher = pattern.matcher(Paths);
  	 if (matcher.find()){
  		SiteName= matcher.group("sitename");
  	 }
  	return getTitleByUUID(getUUIDbyPath("/Sites/"+SiteName));
  	/* 
  	List<Folder> PARENTS = document.getParents();    
  	Folder site = PARENTS.get(0).getFolderParent().getFolderParent().getFolderParent().getFolderParent();
	return  site.getPropertyValue("cm:title").toString();
    */
    } catch (CmisObjectNotFoundException nf){};
  	return "";
    }

    public static long deleteChildsOnFolderByUUID(String uuid){
    try {

  	Session radweirelSession= connectToRadweriel();
  	CmisObject object = radweirelSession.getObject(radweirelSession.createObjectId(uuid));
  	Folder folder = (Folder) object;
  	long size = countChildsOnFolderByUUID(uuid);
  	if(size>0){
  	folder.getChildren().forEach((CmisObject cobject)->{
  		cobject.delete();
  		
  	});
  	}
  	return size;
    } catch (CmisObjectNotFoundException nf){};
  	return -1;
    }   
  
    public static long countChildsOnFolderByUUID(String uuid){
    try {

  	Session radweirelSession= connectToRadweriel();
  	CmisObject object = radweirelSession.getObject(radweirelSession.createObjectId(uuid));
  	Folder folder = (Folder) object;
  	long size = folder.getChildren().getTotalNumItems();
 
  	return  size;
  	
    } catch (CmisObjectNotFoundException nf){};
  	return -1;
    }
    
    public static String uploadToFolderByPath(String FOLDER_PATH,byte[] content,String filename,String description){
    	Session radweirelSession= connectToRadweriel();
    	Folder parent = (Folder) radweirelSession.getObjectByPath(FOLDER_PATH);
    	Map<String, Object> properties = new HashMap<String, Object>();
    	properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
    	properties.put(PropertyIds.NAME, filename);
    	properties.put(PropertyIds.DESCRIPTION, description);
    	  
    	InputStream stream = new ByteArrayInputStream(content);
    	ContentStream contentStream = new ContentStreamImpl(filename, BigInteger.valueOf(content.length), "application/pdf", stream);
    	Document newDoc = parent.createDocument(properties, contentStream, VersioningState.MAJOR);
 	
    	return newDoc.getId().split(";")[0];
    }
    
    public static String uploadToFolderByUUID(String uuid,byte[] content,String filename,String description){
    	Session radweirelSession= connectToRadweriel();
    	Folder parent = (Folder) radweirelSession.getObject(radweirelSession.createObjectId(uuid));// .getObjectByPath(FOLDER_PATH);
    	Map<String, Object> properties = new HashMap<String, Object>();
    	properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
    	properties.put(PropertyIds.NAME, filename);
    	properties.put(PropertyIds.DESCRIPTION, description);
    	  
    	InputStream stream = new ByteArrayInputStream(content);
    	ContentStream contentStream = new ContentStreamImpl(filename, BigInteger.valueOf(content.length), "application/pdf", stream);
    	Document newDoc = parent.createDocument(properties, contentStream, VersioningState.MAJOR);
 	
    	return newDoc.getId().split(";")[0];
    }
}
