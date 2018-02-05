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

import java.util.Properties;

public class MOASSConfig {
	private Properties prop;
	private static MOASSConfig instance;
	
	  private MOASSConfig()
	   {
		prop = new java.util.Properties();
		try {
			prop.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
		} catch(Exception eta){
		    eta.printStackTrace();
		}
	   }
	 
	   private String getValue(String key) {
		   return prop.getProperty(key);
		   }
	   
	   public static String getProperty(String key)
	   {
		   if (instance == null) instance = new MOASSConfig();
		   return instance.getValue(key);
		   }
 }
	   
	   
/*
Usage:
$value = MOASSConfig.getProperty(key);
*/