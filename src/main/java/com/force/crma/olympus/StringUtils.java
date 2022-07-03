package com.force.crma.olympus;

import java.util.Arrays;
import java.util.List;

public class StringUtils {

	public StringUtils() {
		// TODO Auto-generated constructor stub
	}
	
	protected static boolean  isBlank(String aString) {
		return aString == null || aString.isEmpty();
		
	}
	
	protected static boolean arrayContainsValue(String[] aArray, String aString) {
		if(!isEmptyArray(aArray)) {
			List<String> list =Arrays.asList(aArray);
			return list.contains(aString);
		}else
			return false;
		
	}
	
	protected static boolean isEmptyArray(String[] aArray) {
		return aArray==null||aArray.length==0;
	}

}
