package com.msproject.multicloud.util;

public class HexCodeUtil {
	
	public static void main(String[] s) {
		String path ="/Test/Another/New folder";
		System.out.println(hexCodeEncode(path));
		System.out.println(decodeHexString("2f546573742f416e6f746865722f4e657720666f6c646572"));
	}
	
	
	public static String hexCodeEncode(String str) {
		 StringBuffer sb = new StringBuffer();
	      char ch[] = str.toCharArray();
	      for(int i = 0; i < ch.length; i++) {
	         String hexString = Integer.toHexString(ch[i]);
	         sb.append(hexString);
	      }
	      return sb.toString();
	}
	
	public static String decodeHexString(String hexStr) {
		StringBuilder result = new StringBuilder();
		
	      char[] charArray = hexStr.toCharArray();
	      for(int i = 0; i < charArray.length; i=i+2) {
	         String st = ""+charArray[i]+""+charArray[i+1];
	         char ch = (char)Integer.parseInt(st, 16);
	         result.append(ch);
	      }
	      
	      return result.toString();
	}

}
