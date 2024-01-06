package com.hti.smpp.common.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;

public class Converter {
	/*
	 * public static String getUTF8toHex(String str) { String dump = ""; boolean status=true; int fina=0; int big=128; int first=0; for(int i=0;i<str.length();) { first=str.charAt(i); if(first>128) {
	 * int count=0; int noofmove=0; int value=128; i=i+1; fina=first; value=big; while(fina>=value) { count+=1; fina=fina-value; value=value/2; noofmove+=1; } if(fina==0) { first=0; value=big; fina=0;
	 * count=1; first=str.charAt(i); i=i+1; fina=first; while(fina>=value) { count+=1; // System.out.println(fina+"val"+value); fina=fina-value; value=value/2; noofmove+=1; } // int count=0; } // else
	 * // { // fina=fina<<count+1; // } //count+=noofmove; // noofmove+=1; //System.out.println(fina); first=0; //fina=fina<<noofmove; if (count>2) { fina=fina<<12; } else { fina=fina<<6; } //
	 * count+=noofmove; // System.out.println(fina); // int no=0; while(count>1) { //first+=Character.digit(ar[i],16)*16; //first+=Character.digit(ar[i+1],16); first=str.charAt(i); first=first-big; //
	 * System.out.println("value fo"+first); count=count-1; if(count>1) { first=first<<6; fina+=first; } else { fina+=first; } i=i+1; first=0; // System.out.println(fina); } //
	 * System.out.println(" This is final"+fina); dump+=(char)fina; } else { dump+=(char)first; first=0; i+=1; } }//for ends here System.out.println("its value of dump"); return dump; }
	 */
	public static String getUTF8toHex(String str) {
		String dump = "";
		boolean status = true;
		int fina = 0;
		int big = 128;
		int first = 0;
		for (int i = 0; i < str.length();) {
			first = str.charAt(i);
			if (first > 128) {
				int count = 0;
				int noofmove = 0;
				int value = 128;
				i = i + 1;
				fina = first;
				value = big;
				while (fina >= value) {
					count += 1;
					fina = fina - value;
					value = value / 2;
					noofmove += 1;
				}
				if (fina == 0) {
					first = 0;
					value = big;
					fina = 0;
					count = 1;
					first = str.charAt(i);
					i = i + 1;
					fina = first;
					while (fina >= value) {
						count += 1;
						// System.out.println(fina+"val"+value);
						fina = fina - value;
						value = value / 2;
						noofmove += 1;
					}
					// int count=0;
				}
				// else
				// {
				// fina=fina<<count+1;
				// }
				// count+=noofmove;
				// noofmove+=1;
				// System.out.println(fina);
				first = 0;
				// fina=fina<<noofmove;
				if (count > 2) {
					fina = fina << 12;
				} else {
					fina = fina << 6;
				}
				// count+=noofmove;
				// System.out.println(fina);
				// int no=0;
				while (count > 1) {
					// first+=Character.digit(ar[i],16)*16;
					// first+=Character.digit(ar[i+1],16);
					first = str.charAt(i);
					first = first - big;
					// System.out.println("value fo"+first);
					count = count - 1;
					if (count > 1) {
						first = first << 6;
						fina += first;
					} else {
						fina += first;
					}
					i = i + 1;
					first = 0;
					// System.out.println(fina);
				}
				// System.out.println(" This is final"+fina);
				dump += (char) fina;
				// dump+=Integer.toString(fina);
			} else {
				dump += (char) first;
				// dump+=Integer.toString(first);
				first = 0;
				i += 1;
			}
		} // for ends here
			// System.out.println("finish converted message="+dump);
		return dump;
	}
	////////////////////////////
	/*
	 * public static String getHexValueFromUTF8(String str) { Temp b=null; try{ b=new Temp(); b.appendString(str,"UTF8"); // }catch(Exception e){e.printStackTrace();} return b.getHexDump(); }
	 */

	public static String GetValue(char[] str) {
		String toreturn = "";
		for (int i = 0; i < str.length; i++) {
			toreturn += Integer.toHexString((int) str[i]);
		}
		return toreturn;
	}

	////////////////////////////
	public static String getUTF8toHexDigX(String str) throws Exception {
		String dump = "";
		// boolean status = true;
		int fina = 0;
		int big = 128;
		int first = 0;
		for (int i = 0; i < str.length();) {
			first = str.charAt(i);
			char ch = str.charAt(i);
			if (first > 128) {
				int count = 0;
				// int noofmove = 0;
				int value = 128;
				i = i + 1;
				fina = first;
				value = big;
				while (fina >= value) {
					count += 1;
					fina = fina - value;
					value = value / 2;
					// noofmove += 1;
				}
				if (fina == 0) {
					first = 0;
					value = big;
					fina = 0;
					count = 1;
					first = str.charAt(i);
					i = i + 1;
					fina = first;
					while (fina >= value) {
						count += 1;
						fina = fina - value;
						value = value / 2;
						// noofmove += 1;
					}
				}
				first = 0;
				if (count > 2) {
					fina = fina << 12;
				} else {
					fina = fina << 6;
				}
				while (count > 1) {
					first = str.charAt(i);
					first = first - big;
					// System.out.println("value fo"+first);
					count = count - 1;
					if (count > 1) {
						first = first << 6;
						fina += first;
					} else {
						fina += first;
					}
					i = i + 1;
					first = 0;
					// System.out.println(fina);
				}
				// System.out.println(" This is final"+fina);
				String temp1 = "";
				String temp = Integer.toHexString(fina);
				for (int j = temp.length(); j < 4; j++) {
					temp1 += "0";
				}
				dump += temp1 + temp;
			} else {
				String onechar = String.valueOf(ch);
				dump += getUnicodeTOHexaRaj(onechar);
				first = 0;
				i += 1;
			}
		}
		if (dump.toUpperCase().contains("000D")) {
			dump = dump.toUpperCase().replaceAll("000D", "");
		}
		// System.out.println("Removed CR: " + dump);
		return dump;
	}

	public static String getURLtoUTF16(char[] urlhold) {
		String toReturn = "";
		char initial;
		char middle;
		char fina = '@';
		String retur = "";
		for (int i = 0; i < urlhold.length;) {
			initial = urlhold[i];
			// System.out.println("Value of i"+i);
			// System.out.println(initial);
			if (initial == '&') {
				middle = urlhold[i + 1];
				// System.out.println(middle);
				if (middle == '#') {
					// fina=urlhold[i+2];
					int j;
					for (j = i + 2; fina != ';'; j++) {
						// System.out.println(j+1);
						if (j < urlhold.length) {
							// System.out.println(fina+"j=="+j+"lenght"+urlhold.length);
							fina = urlhold[j];
							if (fina != ';') {
								if (fina <= '9' && fina >= '0') {
									retur += fina;
									// System.out.println("Value of iin inner loop"+fina);
									if (fina == -1) {
										break;
									}
								} else {
									// System.out.println("we are in else block of innermost"+fina);
									retur += initial;
									retur += middle;
									retur += fina;
									i = i + 3;
								}
							} else {
								break;
							}
						} else {
							break;
						}
					}
					i = j + 1;
					fina = '@';
					// System.out.println("Value of inner loop"+fina);
				} else {
					// retur+=initial;
					int cha = (int) initial;
					toReturn += getConversion((new Integer(cha)).toString());
					cha = (int) middle;
					toReturn += getConversion((new Integer(cha)).toString());
					// retur+=middle;
					i = i + 2;
				}
				toReturn += getConversion(retur);
				retur = "";
			} else {
				// retur+=initial;
				int cha = (int) initial;
				toReturn += getConversion((new Integer(cha)).toString());
				i = i + 1;
			} // try{
				// Thread.sleep(1000);}
				// catch(Exception e){e.printStackTrace();}
		}
		return toReturn;
	}

	public static String getConversion(String value) {
		// System.out.println("In getConverSion Method"+value);
		// System.out.println("In getConverSion Method"+Integer.parseInt(value));
		int hex = Integer.parseInt(value);
		String temp = Integer.toHexString(hex);
		String add = "";
		for (int i = temp.length(); i < 4; i++) {
			add += "0";
		}
		add += temp;
		return add;
	}
	/*
	 * public static void main(String[] args) { String sho=getURLtoUTF16("abcd&#2349;&#2366;&#2352;&#2340;&#32;&#2325;&#2375;&#32;&#2346;&#2344;&#2381;&#2344;&#2375;".toCharArray());
	 * System.out.println(sho); }
	 */

	public static String getUnicode(char[] buffer) throws Exception {
		String unicode = "";
		int code = 0;
		int j = 0;
		char[] unibuffer = new char[buffer.length / 4];
		try {
			for (int i = 0; i < buffer.length; i += 4) {
				code += Character.digit(buffer[i], 16) * 4096;
				code += Character.digit(buffer[i + 1], 16) * 256;
				code += Character.digit(buffer[i + 2], 16) * 16;
				code += Character.digit(buffer[i + 3], 16);
				unibuffer[j++] = (char) code;
				code = 0;
			}
			unicode = new String(unibuffer);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		return unicode;
	}

	public static String getUTF8Unicode(char[] buffer) {
		String unicode = "";
		int code = 0;
		int j = 0;
		char[] unibuffer = new char[buffer.length / 2];
		try {
			for (int i = 0; i < buffer.length; i += 2) {
				code += Character.digit(buffer[i], 16) * 16;
				code += Character.digit(buffer[i + 1], 16);
				unibuffer[j++] = (char) code;
				code = 0;
			}
			unicode = new String(unibuffer);
		} catch (Exception e) {
			System.out.println("Excepiton in unicode " + e);
		}
		return unicode;
	}

	public static String GetLanguage(String str) {
		int flag = 1;
		String toreturn = "";
		int val = 0;
		for (int i = 0; i < str.length();) {
			val = (int) str.charAt(i);
			if (val < 128) {
				toreturn = "English";
				i++;
			} else if (val > 192 && val < 224) {
				val = val - 192;
				i = i + 1;
				val = val << 6;
				int valSecond = (int) str.charAt(i);
				valSecond = valSecond - 128;
				val = val + valSecond;
				i = i + 1;
				// toreturn="english1";
			} else if (val > 224) {
				val = val - 224;
				i = i + 1;
				val = val << 12;
				int valSecond = (int) str.charAt(i);
				valSecond = valSecond - 128;
				valSecond = valSecond << 6;
				i = i + 1;
				int valThird = (int) str.charAt(i);
				valThird = valThird - 128;
				val = val + valSecond + valThird;
				// toreturn="unicode";
				i = i + 1;
			}
			if (val > 255) {
				flag = flag * 0;
			} else {
				flag = flag * 1;
			}
		}
		// System.out.println("return type is " + flag);
		if (flag == 1) {
			return "English";
		} else {
			return "arabic";
		}
	}

	public static boolean GetLanguageUnicode(String str) {
		boolean flag = false;
		int val = 0;
		for (int i = 0; i < str.length(); i++) {
			val = (int) str.charAt(i);
			if ((val >= 48 && val <= 57) || (val >= 65 && val <= 78) || (val >= 97 && val <= 102)) {
				flag = true;
			} else {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public static String getUnicodeTOHexaRaj(String unicode) {
		String hexa = "";
		InputStreamReader in = null;
		try {
			Writer file_writer;
			FileOutputStream fos;
			fos = new FileOutputStream("Utf");
			file_writer = new OutputStreamWriter(fos, "UTF-8");
			file_writer.write(unicode);
			file_writer.close();
			in = new InputStreamReader(new FileInputStream(new File("Utf")), "UTF-8");
			int a = 0;
			while ((a = in.read()) != -1) {
				if (a > 65000) {
					continue;
				}
				String temp = Integer.toHexString(a);
				String put = "";
				for (int i = temp.length(); i < 4; i++) {
					put += "0";
				}
				put += temp;
				hexa += put;
			}
		} catch (IOException e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		// System.out.println("converted message="+hexa);
		return (hexa);
	}

	public static String getContent(char[] buffer) {
		String unicode = "";
		int code = 0;
		int j = 0;
		char[] unibuffer = new char[buffer.length / 2];
		try {
			for (int i = 0; i < buffer.length; i += 2) {
				// System.out.println(buffer[i]);
				code += Character.digit(buffer[i], 16) * 16;
				// System.out.println("code--->"+code);
				code += Character.digit(buffer[i + 1], 16);
				// System.out.println("code1--->"+code);
				unibuffer[j++] = (char) code;
				code = 0;
			}
			unicode = new String(unibuffer);
			// System.out.println("unicode----->"+unicode);
		} catch (Exception e) {
			System.out.println("Excepiton in unicode " + e);
		}
		return unicode;
	}

	public static String getUTF8toHexDigFinal(String unicode) {
		String hexa = "";
		String dump = "";
		boolean status = true;
		int fina = 0;
		int big = 128;
		int first = 0;
		// FileOutputStream out;
		// PrintStream p;
		try {
			Writer file_writer;
			FileOutputStream fos;
			fos = new FileOutputStream("Utf");
			file_writer = new OutputStreamWriter(fos, "UTF-8");
			file_writer.write(unicode);
			file_writer.close();
			// out = new FileOutputStream("msg.txt");
			// p = new PrintStream( out );
			// p.println (unicode);
			// p.close();
			InputStreamReader in = new InputStreamReader(new FileInputStream(new File("Utf")), "UTF-8");
			// InputStreamReader in=new InputStreamReader(msg,"UTF-8");
			// FileWriter out=new FileWriter("Utf");
			while ((first = in.read()) != -1) {
				int first1 = first;
				if (first > 65000) {
					continue;
				}
				if (first <= 128) {
					// System.out.println("a="+a);
					String temp = Integer.toHexString(first);
					String put = "";
					// System.out.println("temp="+temp);
					for (int i = temp.length(); i < 4; i++) {
						put += "0";
					}
					put += temp;
					dump += put;
					// System.out.println("I am inside the English Conversion");
				} else {
					// System.out.println("I am inside the Arabic conversions");
					int count = 0;
					int noofmove = 0;
					int value = 128;
					fina = first;
					value = big;
					while (fina >= value) {
						count += 1;
						fina = fina - value;
						value = value / 2;
						noofmove += 1;
					}
					if (fina == 0) {
						first = 0;
						value = big;
						fina = 0;
						count = 1;
						first = first1;
						fina = first;
						while (fina >= value) {
							count += 1;
							// System.out.println(fina+"val"+value);
							fina = fina - value;
							value = value / 2;
							noofmove += 1;
						}
					}
					first = 0;
					if (count > 2) {
						fina = fina << 12;
					} else {
						fina = fina << 6;
					}
					while (count > 1) {
						first = first1;
						first = first - big;
						count = count - 1;
						if (count > 1) {
							first = first << 6;
							fina += first;
						} else {
							fina += first;
						}
						first = 0;
						// System.out.println(fina);
					}
					// System.out.println(" This is final"+fina);
					String temp1 = "";
					String temp = Integer.toHexString(fina);
					for (int j = temp.length(); j < 4; j++) {
						temp1 += "0";
					}
					dump += temp1 + temp;
				}
				// out.write(put);
			}
			// out.close();
		} catch (IOException e) {
		}
		// System.out.println("message after conversion is " + dump);
		return dump;
	}

	public static String hexCodePointsToCharMsg(String msg)// Implemented by Abhishek Sahu
	{
		// this mthd made decreasing codes, only.
		//// This mthd will take msg who contain hex values of unicode, then it will convert this msg to Unicode from hex.
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		if (msg.substring(0, 2).compareTo("00") == 0) // if true means first byte is null, then null is required in first byte, after header.
		{
			reqNULL = true;
		}
		charsByt = new BigInteger(msg, 16).toByteArray(); // this won't give null value in first byte if occured, so i have to append it .
		if (charsByt[0] == '\0') // cut this null.
		{
			var = new byte[charsByt.length - 1];
			for (int q = 1; q < charsByt.length; q++) {
				var[q - 1] = charsByt[q];
			}
			charsByt = var;
		}
		if (reqNULL) {
			var = new byte[charsByt.length + 1];
			x = 0;
			var[0] = '\0';
			reqNULL = false;
		} else {
			var = new byte[charsByt.length];
			x = -1;
		}
		for (int l = 0; l < charsByt.length; l++) {
			var[++x] = charsByt[l];
		}
		try {
			msg = new String(var, "UTF-16"); // charsTA msg Setted.
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msg;
	}
}

