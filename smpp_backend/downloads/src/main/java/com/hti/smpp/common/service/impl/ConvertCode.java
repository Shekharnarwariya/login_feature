package com.hti.smpp.common.service.impl;

import java.io.File;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.ConversionData;
import com.hti.smpp.common.responce.ConverterResponse;
import com.hti.smpp.common.responce.Converters;
import com.hti.smpp.common.service.ConverterService;
import com.hti.smpp.common.util.IConstants;

@Service
public class ConvertCode implements ConverterService {

	private Logger logger = LoggerFactory.getLogger(ConvertCode.class);
	String target = IConstants.SUCCESS_KEY;

	public ResponseEntity<?> Converter(List<MultipartFile> files, ConversionData request) {
		System.out.println("run call converter.....");
		ConverterResponse response = new ConverterResponse();
		Converters cnvt = new Converters();

		int l;
		String tempS;
		String msg;
		boolean wasSpc;
		String charsTA, htmlTA, uniTA, percTA, utf8TA, utf16TA, hexTA, decTA, hncrTA, dncrTA, jsTA, cssTA;
		String MMCTA, mod;
		StringBuffer strBuff = new StringBuffer();
		StringBuffer tempBuf = new StringBuffer();
		String Utf16SpcOptValue;
		String Utf8SpcOptValue;
		String UniSpcOptValue;
		String UniEscOptValue;
		String utf16Msg;
		FileUpload upload;
		byte[] byteBuff;
		byte[] var, charsByt;
		try {
			charsTA = "";
			htmlTA = "";
			uniTA = "";
			percTA = "";
			utf8TA = "";
			utf16TA = "";
			hexTA = "";
			decTA = "";
			hncrTA = "";
			dncrTA = "";
			jsTA = "";
			cssTA = "";
			MMCTA = "";
			mod = "";
			String catagry = "";
			String MMCPicTAmsg = "";
			String MMCLinkTBmsg = "";
			try {
				// boolean isMultipart = FileUpload.isMultipartContent(request);
				if (files != null) {
					String HexString = "";
					Iterator iter = files.iterator();
					while (iter.hasNext()) {
						FileItem item = (FileItem) iter.next();
						// System.out.println(item.getString());
						if (item.isFormField()) {
							String name = item.getFieldName();
							// System.out.println("item---->"+name);
							if (name.compareTo("catagory") == 0) {
								catagry = item.getString();
								// System.out.println("category--->"+catagry);
							}
							if (name.compareTo("mod") == 0) {
								mod = item.getString();
								// System.out.println("mod--->"+mod);
							}
							if (name.compareTo("MMCLinkTB") == 0) {
								MMCLinkTBmsg = item.getString();
							}
							if (name.compareTo("MMCPicTA") == 0) {
								MMCPicTAmsg = item.getString();
							}
						} else {
							String fileName = item.getName();
							// System.out.println("filename---->"+fileName);
							String extntion;
							String primaryId;
							String fName;
							File uploadedFile;
							if (catagry.compareTo("logo") == 0 || catagry.compareTo("picture") == 0) {
							}
							if (catagry.compareTo("ringtone") == 0) {
							}
						}
					}
					if (catagry.compareTo("picture") == 0) {
					}
					if (catagry.compareTo("wappush") == 0) {
					}
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: MM_ block :: " + ex);
			}
			try {
				// System.out.println("********* TYR 1 **********");
				if (request.getUtf16TA() != null && request.getUtf16TA().compareTo("") != 0) {
					// System.out.println("********* TYR 1 **********");
					wasSpc = false;
			
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getUtf16TA().toUpperCase();
					if (msg.indexOf(" ") > -1) {
						StringTokenizer st = new StringTokenizer(msg, " ");
						while (st.hasMoreTokens()) {
							strBuff.append(st.nextToken());
						}
						wasSpc = true;
					} else {
						/// System.out.println("********* L1 **********");
						strBuff.append(msg);
					}
					if (strBuff.length() % 4 != 0) {
						utf16TA = "Invalid Length " + msg;
					} else {
						if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
							if (!wasSpc) {
								// System.out.println("********* L2 **********");
								utf16TA = msg;
							} else {
								StringTokenizer st = new StringTokenizer(msg, " ");
								while (st.hasMoreTokens()) {
									tempBuf.append(st.nextToken());
								}
								utf16TA = tempBuf.toString();
								tempBuf.delete(0, tempBuf.length());
							}
						} else {
							if (wasSpc) {
								utf16TA = msg;
							} else {
								String reqSpc = msg;
								for (int y = 0; y < reqSpc.length(); y += 4) {
									tempBuf.append(reqSpc.substring(y, y + 4));
									if (y != reqSpc.length() - 4) {
										tempBuf.append(" ");
									}
								}
								utf16TA = tempBuf.toString();
								tempBuf.delete(0, tempBuf.length());
							}
						}
						// System.out.println("********* L3 **********");
						msg = strBuff.toString();
						// System.out.println("msg>>>>>>>>>>"+msg);
						strBuff.delete(0, strBuff.length());
						String Cmsg = uniHexToCharMsg(msg);
						// System.out.println("Cmsg>>>>>>>>>>"+Cmsg);
						charsTA = Cmsg;
						// System.out.println("charsTA>>>>>>>>>>"+charsTA);
						htmlTA = cnvt.htmlEnc(charsTA);
						// System.out.println("htmlTA>>>>>>>>>>"+htmlTA);
						if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
							utf8TA = cnvt.UTF8(charsTA);
							// System.out.println("utf8TA>>>>>>>>>>"+utf8TA);
						} else {
							String reqSpc = cnvt.UTF8(charsTA);
							for (int y = 0; y < reqSpc.length(); y += 2) {
								strBuff.append(reqSpc.substring(y, y + 2));
								if (y != reqSpc.length() - 2) {
									strBuff.append(" ");
								}
							}
							utf8TA = strBuff.toString();
							strBuff.delete(0, strBuff.length());
						}
						if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
							uniTA = cnvt.Uni(msg, UniEscOptValue, false);
						} else {
							uniTA = cnvt.Uni(msg, UniEscOptValue, true);
						}
						hexTA = cnvt.hexEnc(msg);
						hncrTA = cnvt.hexNCR(msg);
						decTA = cnvt.deciEnc(msg);
						dncrTA = cnvt.decNCR(msg);
						percTA = cnvt.percent(charsTA);
						jsTA = cnvt.jsEnc(charsTA);
						cssTA = cnvt.cssEnc(charsTA);
					}
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: utf16To_ block :: " + ex);
			}
			try {
				// System.out.println("********* TYR 2 **********");
				if (request.getUtf8TA() != null && request.getUtf8TA().compareTo("") != 0) {
					wasSpc = false;
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getUtf8TA().toUpperCase();
					if (msg.indexOf(" ") > -1) {
						StringTokenizer st = new StringTokenizer(msg, " ");
						while (st.hasMoreTokens()) {
							strBuff.append(st.nextToken());
						}
						wasSpc = true;
					} else {
						strBuff.append(msg);
					}
					if (strBuff.length() % 2 != 0) {
						utf8TA = "Invalid Length " + msg;
					} else {
						if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
							if (!wasSpc) {
								utf8TA = msg;
							} else {
								StringTokenizer st = new StringTokenizer(msg, " ");
								while (st.hasMoreTokens()) {
									tempBuf.append(st.nextToken());
								}
								utf8TA = tempBuf.toString();
								tempBuf.delete(0, tempBuf.length());
							}
						} else {
							if (wasSpc) {
								utf8TA = msg;
							} else {
								String reqSpc = msg;
								for (int y = 0; y < reqSpc.length(); y += 2) {
									tempBuf.append(reqSpc.substring(y, y + 2));
									if (y != reqSpc.length() - 2) {
										tempBuf.append(" ");
									}
								}
								utf8TA = tempBuf.toString();
								tempBuf.delete(0, tempBuf.length());
							}
						}
						msg = strBuff.toString();
						strBuff.delete(0, strBuff.length());
						charsByt = new BigInteger(msg, 16).toByteArray();
						if (charsByt[0] == '\0') {
							var = new byte[charsByt.length - 1];
							for (int q = 1; q < charsByt.length; q++) {
								var[q - 1] = charsByt[q];
							}
							charsByt = var;
						}
						charsTA = new String(charsByt, "UTF-8");
						htmlTA = cnvt.htmlEnc(charsTA);
						utf16Msg = cnvt.UTF16(charsTA);
						if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
							utf16TA = utf16Msg;
						} else {
							String reqSpc = utf16Msg;
							for (int y = 0; y < reqSpc.length(); y += 4) {
								strBuff.append(reqSpc.substring(y, y + 4));
								if (y != reqSpc.length() - 4) {
									strBuff.append(" ");
								}
							}
							utf16TA = strBuff.toString();
							strBuff.delete(0, strBuff.length());
						}
						if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
							uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, false);
						} else {
							uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, true);
						}
						hexTA = cnvt.hexEnc(utf16Msg);
						hncrTA = cnvt.hexNCR(utf16Msg);
						decTA = cnvt.deciEnc(utf16Msg);
						dncrTA = cnvt.decNCR(utf16Msg);
						percTA = cnvt.percent(charsTA);
						jsTA = cnvt.jsEnc(charsTA);
						cssTA = cnvt.cssEnc(charsTA);
					}
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: utf8To_ block :: " + ex);
			}
			try {
				// System.out.println("********* TYR 3 **********");
				if (request.getHexTA() != null && request.getHexTA().compareTo("") != 0) {
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getHexTA();
					hexTA = msg;
					StringTokenizer st = new StringTokenizer(msg, " ");
					while (st.hasMoreTokens()) {
						tempS = st.nextToken();
						if ((tempS.length() == 2)) {
							strBuff.append("00" + tempS);
						} else {
							strBuff.append(tempS);
						}
					}
					msg = strBuff.toString();
					// System.out.println("UNIMESSAGGE>>>"+msg);
					strBuff.delete(0, strBuff.length());
					msg = uniHexToCharMsg(msg);
					charsTA = msg;
					// System.out.println("CHARMESSAGGE>>>"+charsTA);
					htmlTA = cnvt.htmlEnc(charsTA);
					if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf8TA = cnvt.UTF8(charsTA);
					} else {
						String reqSpc = cnvt.UTF8(charsTA);
						for (int y = 0; y < reqSpc.length(); y += 2) {
							strBuff.append(reqSpc.substring(y, y + 2));
							if (y != reqSpc.length() - 2) {
								strBuff.append(" ");
							}
						}
						utf8TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					utf16Msg = cnvt.UTF16(charsTA);
					if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf16TA = utf16Msg;
					} else {
						String reqSpc = utf16Msg;
						for (int y = 0; y < reqSpc.length(); y += 4) {
							strBuff.append(reqSpc.substring(y, y + 4));
							if (y != reqSpc.length() - 4) {
								strBuff.append(" ");
							}
						}
						utf16TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, false);
					} else {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, true);
					}
					hncrTA = cnvt.hexNCR(utf16Msg);
					decTA = cnvt.deciEnc(utf16Msg);
					dncrTA = cnvt.decNCR(utf16Msg);
					percTA = cnvt.percent(charsTA);
					jsTA = cnvt.jsEnc(charsTA);
					cssTA = cnvt.cssEnc(charsTA);
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: hexTo_ block :: " + ex);
			}
			try {
				// System.out.println("********* TYR 4 **********");
				if (request.getDecTA() != null && request.getDecTA().compareTo("") != 0) {
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getDecTA();
					decTA = msg;
					StringTokenizer st = new StringTokenizer(msg, " ");
					while (st.hasMoreTokens()) {
						strBuff.append(intToHex(Integer.parseInt(st.nextToken())));
					}
					msg = strBuff.toString();
					strBuff.delete(0, strBuff.length());
					msg = uniHexToCharMsg(msg);
					charsTA = msg;
					htmlTA = cnvt.htmlEnc(charsTA);
					if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf8TA = cnvt.UTF8(charsTA);
					} else {
						String reqSpc = cnvt.UTF8(charsTA);
						for (int y = 0; y < reqSpc.length(); y += 2) {
							strBuff.append(reqSpc.substring(y, y + 2));
							if (y != reqSpc.length() - 2) {
								strBuff.append(" ");
							}
						}
						utf8TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					utf16Msg = cnvt.UTF16(charsTA);
					if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf16TA = utf16Msg;
					} else {
						String reqSpc = utf16Msg;
						for (int y = 0; y < reqSpc.length(); y += 4) {
							strBuff.append(reqSpc.substring(y, y + 4));
							if (y != reqSpc.length() - 4) {
								strBuff.append(" ");
							}
						}
						utf16TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, false);
					} else {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, true);
					}
					hexTA = cnvt.hexEnc(utf16Msg);
					hncrTA = cnvt.hexNCR(utf16Msg);
					dncrTA = cnvt.decNCR(utf16Msg);
					percTA = cnvt.percent(charsTA);
					jsTA = cnvt.jsEnc(charsTA);
					cssTA = cnvt.cssEnc(charsTA);
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: decTo_ block :: " + ex);
			}
			try {
				// System.out.println("********* TYR 5 **********");
				if (request.getHncrTA() != null && request.getHncrTA().compareTo("") != 0) {
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getHncrTA();
					if (msg.length() % 2 != 0) {
						hncrTA = "Invalid Length " + msg;
					} else {
						StringTokenizer st = new StringTokenizer(msg, ";");
						while (st.hasMoreTokens()) {
							tempS = st.nextToken();
							tempS = tempS.substring(3, tempS.length());
							if ((tempS.length() == 2)) {
								strBuff.append("00" + tempS);
							} else {
								strBuff.append(tempS);
							}
						}
						msg = strBuff.toString();
						strBuff.delete(0, strBuff.length());
						msg = uniHexToCharMsg(msg);
						charsTA = msg;
						htmlTA = cnvt.htmlEnc(charsTA);
						if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
							utf8TA = cnvt.UTF8(charsTA); // UTF8 msg Setted.
						} else {
							String reqSpc = cnvt.UTF8(charsTA);
							for (int y = 0; y < reqSpc.length(); y += 2) {
								strBuff.append(reqSpc.substring(y, y + 2));
								if (y != reqSpc.length() - 2) {
									strBuff.append(" ");
								}
							}
							utf8TA = strBuff.toString();
							strBuff.delete(0, strBuff.length());
						}
						utf16Msg = cnvt.UTF16(charsTA);
						if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
							utf16TA = utf16Msg;
						} else {
							String reqSpc = utf16Msg;
							for (int y = 0; y < reqSpc.length(); y += 4) {
								strBuff.append(reqSpc.substring(y, y + 4));
								if (y != reqSpc.length() - 4) {
									strBuff.append(" ");
								}
							}
							utf16TA = strBuff.toString();
							strBuff.delete(0, strBuff.length());
						}
						if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
							uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, false);
						} else {
							uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, true);
						}
						hexTA = cnvt.hexEnc(utf16Msg);
						hncrTA = cnvt.hexNCR(utf16Msg);
						decTA = cnvt.deciEnc(utf16Msg);
						dncrTA = cnvt.decNCR(utf16Msg);
						percTA = cnvt.percent(charsTA);
						jsTA = cnvt.jsEnc(charsTA);
						cssTA = cnvt.cssEnc(charsTA);
					}
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: hexNCRTo_ block :: " + ex);
			}
			try {
				/// System.out.println("********* TYR 6 **********");
				if (request.getDncrTA() != null && request.getDncrTA().compareTo("") != 0) {
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getDncrTA();
					StringTokenizer st = new StringTokenizer(msg, ";");
					while (st.hasMoreTokens()) {
						tempS = st.nextToken();
						tempS = tempS.substring(2, tempS.length());
						strBuff.append(intToHex(Integer.parseInt(tempS)));
					}
					msg = strBuff.toString();
					strBuff.delete(0, strBuff.length());
					msg = uniHexToCharMsg(msg);
					charsTA = msg;
					htmlTA = cnvt.htmlEnc(charsTA);
					if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf8TA = cnvt.UTF8(charsTA);
					} else {
						String reqSpc = cnvt.UTF8(charsTA);
						for (int y = 0; y < reqSpc.length(); y += 2) {
							strBuff.append(reqSpc.substring(y, y + 2));
							if (y != reqSpc.length() - 2) {
								strBuff.append(" ");
							}
						}
						utf8TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					utf16Msg = cnvt.UTF16(charsTA);
					if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf16TA = utf16Msg;
					} else {
						String reqSpc = utf16Msg;
						for (int y = 0; y < reqSpc.length(); y += 4) {
							strBuff.append(reqSpc.substring(y, y + 4));
							if (y != reqSpc.length() - 4) {
								strBuff.append(" ");
							}
						}
						utf16TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, false);
					} else {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, true);
					}
					hexTA = cnvt.hexEnc(utf16Msg);
					hncrTA = cnvt.hexNCR(utf16Msg);
					decTA = cnvt.deciEnc(utf16Msg);
					dncrTA = cnvt.decNCR(utf16Msg);
					percTA = cnvt.percent(charsTA);
					jsTA = cnvt.jsEnc(charsTA);
					cssTA = cnvt.cssEnc(charsTA);
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: decNCRTo_ block :: " + ex);
			}
			try {
				// System.out.println("********* TYR 7 **********");
				if (request.getUniTA() != null && request.getUniTA().compareTo("") != 0) {
					StringTokenizer st;
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getUniTA();
					String hexEsc = msg.substring(0, 2);
					if (msg.indexOf(" ") == -1) {
						st = new StringTokenizer(msg, hexEsc);
						while (st.hasMoreTokens()) {
							strBuff.append(st.nextToken());
						}
					} else {
						st = new StringTokenizer(msg, " ");
						while (st.hasMoreTokens()) {
							strBuff.append(st.nextToken());
						}
						st = new StringTokenizer(strBuff.toString(), hexEsc);
						strBuff.delete(0, strBuff.length());
						while (st.hasMoreTokens()) {
							strBuff.append(st.nextToken());
						}
					}
					if (strBuff.length() % 4 != 0) {
						uniTA = "Invalid Length Or Esc Sign " + msg;
					} else {
						msg = strBuff.toString();
						strBuff.delete(0, strBuff.length());
						if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
							uniTA = cnvt.Uni(msg, UniEscOptValue, false);
						} else {
							uniTA = cnvt.Uni(msg, UniEscOptValue, true);
						}
						charsTA = uniHexToCharMsg(msg);
						htmlTA = cnvt.htmlEnc(charsTA);
						if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
							utf8TA = cnvt.UTF8(charsTA);
						} else {
							String reqSpc = cnvt.UTF8(charsTA);
							for (int y = 0; y < reqSpc.length(); y += 2) {
								strBuff.append(reqSpc.substring(y, y + 2));
								if (y != reqSpc.length() - 2) {
									strBuff.append(" ");
								}
							}
							utf8TA = strBuff.toString();
							strBuff.delete(0, strBuff.length());
						}
						utf16Msg = msg;
						if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
							utf16TA = utf16Msg;
						} else {
							String reqSpc = utf16Msg;
							for (int y = 0; y < reqSpc.length(); y += 4) {
								strBuff.append(reqSpc.substring(y, y + 4));
								if (y != reqSpc.length() - 4) {
									strBuff.append(" ");
								}
							}
							utf16TA = strBuff.toString();
							strBuff.delete(0, strBuff.length());
						}
						hexTA = cnvt.hexEnc(utf16Msg);
						hncrTA = cnvt.hexNCR(utf16Msg);
						decTA = cnvt.deciEnc(utf16Msg);
						dncrTA = cnvt.decNCR(utf16Msg);
						percTA = cnvt.percent(charsTA);
						jsTA = cnvt.jsEnc(charsTA);
						cssTA = cnvt.cssEnc(charsTA);
					}
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: uniTo_ block :: " + ex);
			}
			try {
				// System.out.println("********* TYR 8 **********");
				if (request.getPercTA() != null && request.getPercTA().compareTo("") != 0) {
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getPercTA();
					percTA = msg;
					charsByt = msg.getBytes("UTF-8");
					for (l = 0; l < charsByt.length; l++) {
						if (charsByt[l] != '%') {
							strBuff.append(byteToHex(charsByt[l]));
						} else {
							tempS = String.valueOf((char) charsByt[l + 1]);
							tempS = tempS + String.valueOf((char) charsByt[l + 2]);
							if (tempS.compareToIgnoreCase("0A") != 0) {
								strBuff.append(tempS);
							} else {
								strBuff.append("0D" + tempS);
							}
							l += 2;
						}
					}
					byteBuff = new BigInteger(strBuff.toString(), 16).toByteArray();
					if (byteBuff[0] == '\0') {
						var = new byte[byteBuff.length - 1];
						for (int q = 1; q < byteBuff.length; q++) {
							var[q - 1] = byteBuff[q];
						}
						byteBuff = var;
					}
					msg = new String(byteBuff, "UTF-8");
					strBuff.delete(0, strBuff.length());
					charsTA = new String(msg.getBytes("UTF-16"), "UTF-16");
					htmlTA = cnvt.htmlEnc(charsTA);
					if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf8TA = cnvt.UTF8(charsTA);
					} else {
						String reqSpc = cnvt.UTF8(charsTA);
						for (int y = 0; y < reqSpc.length(); y += 2) {
							strBuff.append(reqSpc.substring(y, y + 2));
							if (y != reqSpc.length() - 2) {
								strBuff.append(" ");
							}
						}
						utf8TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					utf16Msg = cnvt.UTF16(charsTA);
					if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf16TA = utf16Msg;
					} else {
						String reqSpc = utf16Msg;
						for (int y = 0; y < reqSpc.length(); y += 4) {
							strBuff.append(reqSpc.substring(y, y + 4));
							if (y != reqSpc.length() - 4) {
								strBuff.append(" ");
							}
						}
						utf16TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, false);
					} else {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, true);
					}
					hexTA = cnvt.hexEnc(utf16Msg);
					hncrTA = cnvt.hexNCR(utf16Msg);
					decTA = cnvt.deciEnc(utf16Msg);
					dncrTA = cnvt.decNCR(utf16Msg);
					jsTA = cnvt.jsEnc(charsTA);
					cssTA = cnvt.cssEnc(charsTA);
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: percTo_ block :: " + ex);
			}
			try {
				// System.out.println("********* TYR 9 **********");
				if (request.getHtmlTA() != null && request.getHtmlTA().compareTo("") != 0) {
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getHtmlTA();
					byteBuff = msg.getBytes("UTF-8");
					for (int q = 0; q < byteBuff.length; q++) {
						if (byteBuff[q] != '&') {
							strBuff.append(byteToHex(byteBuff[q]));
						} else if (byteBuff[q + 1] == 'q' && byteBuff[q + 2] == 'u' && byteBuff[q + 3] == 'o'
								&& byteBuff[q + 4] == 't') {
							strBuff.append(byteToHex((byte) 34));
							q += 5;
						} else if (byteBuff[q + 1] == 'a' && byteBuff[q + 2] == 'm' && byteBuff[q + 3] == 'p') {
							strBuff.append(byteToHex((byte) 38));
							q += 4;
						} else if (byteBuff[q + 1] == 'l' && byteBuff[q + 2] == 't') {
							strBuff.append(byteToHex((byte) 60));
							q += 3;
						} else if (byteBuff[q + 1] == 'g' && byteBuff[q + 2] == 't') {
							strBuff.append(byteToHex((byte) 62));
							q += 3;
						}
					}
					byteBuff = new BigInteger(strBuff.toString(), 16).toByteArray();
					if (byteBuff[0] == '\0') {
						var = new byte[byteBuff.length - 1];
						for (int q = 1; q < byteBuff.length; q++) {
							var[q - 1] = byteBuff[q];
						}
						byteBuff = var;
					}
					strBuff.delete(0, strBuff.length());
					msg = new String(byteBuff, "UTF-8");
					charsTA = new String(msg.getBytes("UTF-16"), "UTF-16");
					htmlTA = cnvt.htmlEnc(charsTA);
					if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf8TA = cnvt.UTF8(charsTA);
					} else {
						String reqSpc = cnvt.UTF8(charsTA);
						for (int y = 0; y < reqSpc.length(); y += 2) {
							strBuff.append(reqSpc.substring(y, y + 2));
							if (y != reqSpc.length() - 2) {
								strBuff.append(" ");
							}
						}
						utf8TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					utf16Msg = cnvt.UTF16(charsTA);
					if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf16TA = utf16Msg;
					} else {
						String reqSpc = utf16Msg;
						for (int y = 0; y < reqSpc.length(); y += 4) {
							strBuff.append(reqSpc.substring(y, y + 4));
							if (y != reqSpc.length() - 4) {
								strBuff.append(" ");
							}
						}
						utf16TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, false);
					} else {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, true);
					}
					hexTA = cnvt.hexEnc(utf16Msg);
					hncrTA = cnvt.hexNCR(utf16Msg);
					decTA = cnvt.deciEnc(utf16Msg);
					dncrTA = cnvt.decNCR(utf16Msg);
					percTA = cnvt.percent(charsTA);
					jsTA = cnvt.jsEnc(charsTA);
					cssTA = cnvt.cssEnc(charsTA);
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: htmlTo_ block :: " + ex);
			}
			try {
				// System.out.println("********* TYR 10 **********");
				if (request.getJsTA() != null && request.getJsTA().compareTo("") != 0) {
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getJsTA();
					jsTA = msg;
					charsByt = msg.getBytes("UTF-8");
					for (l = 0; l < charsByt.length; l++) {
						if (charsByt[l] != '\\') {
							strBuff.append(byteToHex(charsByt[l]));
						} else {
							if (charsByt[l + 1] == '\\' || charsByt[l + 1] == '\"' || charsByt[l + 1] == '\'') {
								strBuff.append(byteToHex(charsByt[l + 1]));
								l++;
							} else if (charsByt[l + 1] == 'n') {
								strBuff.append("0D0A");
								l++;
							} else {
								l++;
								tempS = "";
								for (int z = 1; z <= 4; z++) {
									tempS = tempS + String.valueOf((char) charsByt[l + z]);
								}
								byteBuff = new BigInteger(tempS, 16).toByteArray();
								if (byteBuff.length != 2) {
									if (byteBuff.length < 2) {
										var = new byte[2];
										var[0] = '\0';
										var[1] = byteBuff[0];
										byteBuff = var;
									} else {
										var = new byte[2];
										var[1] = byteBuff[byteBuff.length - 1];
										var[0] = byteBuff[byteBuff.length - 2];
										byteBuff = var;
									}
								}
								tempS = new String(byteBuff, "UTF-16");
								byteBuff = tempS.getBytes("UTF-8");
								for (int q = 0; q < byteBuff.length; q++) {
									strBuff.append(byteToHex(byteBuff[q]));
								}
								l += 4;
							}
						}
					}
					byteBuff = new BigInteger(strBuff.toString(), 16).toByteArray();
					if (byteBuff[0] == '\0') {
						var = new byte[byteBuff.length - 1];
						for (int q = 1; q < byteBuff.length; q++) {
							var[q - 1] = byteBuff[q];
						}
						byteBuff = var;
					}
					strBuff.delete(0, strBuff.length());
					msg = new String(byteBuff, "UTF-8");
					charsTA = new String(msg.getBytes("UTF-16"), "UTF-16");
					htmlTA = cnvt.htmlEnc(charsTA);
					if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf8TA = cnvt.UTF8(charsTA);
					} else {
						String reqSpc = cnvt.UTF8(charsTA);
						for (int y = 0; y < reqSpc.length(); y += 2) {
							strBuff.append(reqSpc.substring(y, y + 2));
							if (y != reqSpc.length() - 2) {
								strBuff.append(" ");
							}
						}
						utf8TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					utf16Msg = cnvt.UTF16(charsTA);
					if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf16TA = utf16Msg;
					} else {
						String reqSpc = utf16Msg;
						for (int y = 0; y < reqSpc.length(); y += 4) {
							strBuff.append(reqSpc.substring(y, y + 4));
							if (y != reqSpc.length() - 4) {
								strBuff.append(" ");
							}
						}
						utf16TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, false);
					} else {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, true);
					}
					hexTA = cnvt.hexEnc(utf16Msg);
					hncrTA = cnvt.hexNCR(utf16Msg);
					decTA = cnvt.deciEnc(utf16Msg);
					dncrTA = cnvt.decNCR(utf16Msg);
					percTA = cnvt.percent(charsTA);
					cssTA = cnvt.cssEnc(charsTA);
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: jsTo_ block :: " + ex);
			}
			try {
				if (request.getCssTA() != null && request.getCssTA().compareTo("") != 0) {
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getCssTA();
					cssTA = msg;
					charsByt = msg.getBytes("UTF-8");
					for (l = 0; l < charsByt.length; l++) {
						if (charsByt[l] != '\\') {
							strBuff.append(byteToHex(charsByt[l]));
						} else {
							if (charsByt[l + 1] == '\\') {
								strBuff.append(byteToHex(charsByt[l + 1]));
								l++;
							} else if (charsByt[l + 1] == '0' && charsByt[l + 2] == '0' && charsByt[l + 3] == '0'
									&& charsByt[l + 4] == '0' && charsByt[l + 5] == '0') {
								strBuff.append("0D0A");
								l += 6;
							} else {
								tempS = "";
								for (int z = 1; z <= 4; z++) {
									tempS = tempS + String.valueOf((char) charsByt[l + z]);
								}
								byteBuff = new BigInteger(tempS, 16).toByteArray();
								if (byteBuff.length != 2) {
									if (byteBuff.length < 2) {
										var = new byte[2];
										var[0] = '\0';
										var[1] = byteBuff[0];
										byteBuff = var;
									} else {
										var = new byte[2];
										var[1] = byteBuff[byteBuff.length - 1];
										var[0] = byteBuff[byteBuff.length - 2];
										byteBuff = var;
									}
								}
								tempS = new String(byteBuff, "UTF-16");
								byteBuff = tempS.getBytes("UTF-8");
								for (int q = 0; q < byteBuff.length; q++) {
									strBuff.append(byteToHex(byteBuff[q]));
								}
								l += 4;
							}
						}
					}
					byteBuff = new BigInteger(strBuff.toString(), 16).toByteArray();
					if (byteBuff[0] == '\0') {
						var = new byte[byteBuff.length - 1];
						for (int q = 1; q < byteBuff.length; q++) {
							var[q - 1] = byteBuff[q];
						}
						byteBuff = var;
					}
					strBuff.delete(0, strBuff.length());
					msg = new String(byteBuff, "UTF-8");
					charsTA = new String(msg.getBytes("UTF-16"), "UTF-16");
					htmlTA = cnvt.htmlEnc(charsTA);
					if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf8TA = cnvt.UTF8(charsTA);
					} else {
						String reqSpc = cnvt.UTF8(charsTA);
						for (int y = 0; y < reqSpc.length(); y += 2) {
							strBuff.append(reqSpc.substring(y, y + 2));
							if (y != reqSpc.length() - 2) {
								strBuff.append(" ");
							}
						}
						utf8TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					utf16Msg = cnvt.UTF16(charsTA);
					if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf16TA = utf16Msg;
					} else {
						String reqSpc = utf16Msg;
						for (int y = 0; y < reqSpc.length(); y += 4) {
							strBuff.append(reqSpc.substring(y, y + 4));
							if (y != reqSpc.length() - 4) {
								strBuff.append(" ");
							}
						}
						utf16TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, false);
					} else {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, true);
					}
					hexTA = cnvt.hexEnc(utf16Msg);
					hncrTA = cnvt.hexNCR(utf16Msg);
					decTA = cnvt.deciEnc(utf16Msg);
					dncrTA = cnvt.decNCR(utf16Msg);
					percTA = cnvt.percent(charsTA);
					jsTA = cnvt.jsEnc(charsTA);
				}
			} catch (Exception ex) {
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: cssTo_ block :: " + ex);
			}
			try {
				if (request.getCharsTA() != null && request.getCharsTA().compareTo("") != 0) {
					Utf16SpcOptValue = request.getHidnUtf16SpcOpt();
					Utf8SpcOptValue = request.getHidnUtf8SpcOpt();
					UniSpcOptValue = request.getHidnUniSpcOpt();
					UniEscOptValue = request.getHidnUniEscOpt();
					msg = request.getCharsTA();
					charsTA = msg;
					if (Utf8SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf8TA = cnvt.UTF8(msg);
					} else {
						String reqSpc = cnvt.UTF8(msg);
						for (int y = 0; y < reqSpc.length(); y += 2) {
							strBuff.append(reqSpc.substring(y, y + 2));
							if (y != reqSpc.length() - 2) {
								strBuff.append(" ");
							}
						}
						utf8TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					utf16Msg = cnvt.UTF16(msg);
					if (Utf16SpcOptValue.equalsIgnoreCase("Without Space")) {
						utf16TA = utf16Msg;
					} else {
						String reqSpc = utf16Msg;
						for (int y = 0; y < reqSpc.length(); y += 4) {
							strBuff.append(reqSpc.substring(y, y + 4));
							if (y != reqSpc.length() - 4) {
								strBuff.append(" ");
							}
						}
						utf16TA = strBuff.toString();
						strBuff.delete(0, strBuff.length());
					}
					if (UniSpcOptValue.equalsIgnoreCase("Without Space")) {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, false);
					} else {
						uniTA = cnvt.Uni(utf16Msg, UniEscOptValue, true);
					}
					hexTA = cnvt.hexEnc(utf16Msg);
					hncrTA = cnvt.hexNCR(utf16Msg);
					decTA = cnvt.deciEnc(utf16Msg);
					dncrTA = cnvt.decNCR(utf16Msg);
					percTA = cnvt.percent(msg);
					htmlTA = cnvt.htmlEnc(msg);
					jsTA = cnvt.jsEnc(msg);
					cssTA = cnvt.cssEnc(msg);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("EXCEPTION FROM CONVERTER TOOL :: charsTo_ block :: " + ex);
			}

			response.setCharsTA(charsTA);
			response.setHtmlTA(htmlTA);
			response.setPercTA(percTA);
			response.setUniTA(uniTA);
			response.setUtf8TA(utf8TA);
			response.setUtf16TA(utf16TA);
			response.setHexTA(hexTA);
			response.setDecTA(decTA);
			response.setHncrTA(hncrTA);
			response.setDncrTA(dncrTA);
			response.setJsTA(jsTA);
			response.setCssTA(cssTA);
			
		} catch (Exception ex) {

			ex.printStackTrace();
			logger.error("", ex);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	/*
	 * It is a just part(piece of code) of above functionality(method),
	 */
	public String uniHexToCharMsg(String msg) {
		if (msg == null || msg.length() == 0) {
			msg = "0020";
		}
		boolean reqNULL = false;
		byte[] charsByt, var;
		int x = 0;
		try {
			if (msg.substring(0, 2).compareTo("00") == 0) {
				reqNULL = true;
			}
			charsByt = new BigInteger(msg, 16).toByteArray();
			if (charsByt[0] == '\0') {
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
			msg = new String(var, "UTF-16");
		} catch (Exception ex) {
			logger.error(msg, ex);
		}
		return msg;
	}

	/*
	 * Here it will convert byte to its hex value.
	 */
	public String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return (buf.toString()).toUpperCase();
	}

	/*
	 * Here it will convert hex to its character/symbol/number etc.
	 */
	public char toHexChar(int i) {
		if ((i >= 0) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i - 10));
		}
	}

	/*
	 * Here it will convert hex (UTF16) values to its equivlent
	 * character/number/symbol etc.
	 */
	public String getFromUnicode(char[] buffer) {
		String unicode = "";
		int code = 0;
		int j = 0;
		char[] unibuffer = new char[buffer.length / 4];
		try {
			for (int i = 0; i < buffer.length; i += 4) { // 0042
				code += Character.digit(buffer[i], 16) * 4096;
				code += Character.digit(buffer[i + 1], 16) * 256;
				code += Character.digit(buffer[i + 2], 16) * 16;
				code += Character.digit(buffer[i + 3], 16);
				unibuffer[j++] = (char) code;
				code = 0;
			}
			unicode = new String(unibuffer);
		} catch (Exception e) {
			System.out.println("EXCEPTION FROM CONVERTER TOOL :: Excepiton in unicode " + e);
		}
		return unicode;
	}

	/*
	 * Here it will convert int value to corresponding hex value.
	 */
	public String intToHex(int i) {
		String hexString = new String();
		StringBuffer hexBuff;
		hexBuff = new StringBuffer("");
		int rem;
		int cont = 0;
		if (i >= 256) {
			cont = 1;
			rem = i - 256;
			while (rem >= 256) {
				cont++;
				rem = rem - 256;
			}
		} else {
			cont = 0;
			rem = i;
		}
		byte fstbyt = (byte) cont;
		byte lstbyt = (byte) rem;
		hexBuff.append(byteToHex(fstbyt));
		hexBuff.append(byteToHex(lstbyt));
		hexString = hexBuff.toString();
		return hexString;
	}

	/*
	 * Here it will call doPost() method only.
	 */
//		protected void doGet(HttpServletRequest request, HttpServletResponse response)
//				throws ServletException, IOException {
//			doPost(request, response);
//		}

	/*
	 * Here it will return info of this servlet.
	 */
	public String getServletInfo() {
		return "Unicode (base 16) Converter";
	}

}
