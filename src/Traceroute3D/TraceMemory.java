package Traceroute;

import java.util.ArrayList;

class TraceMemory implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList <String []> m;
	
	TraceMemory(ArrayList<String []> m){
		
			this.m=m;
	}
}