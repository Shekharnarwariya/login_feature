package com.hti.smpp.common.services.Impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.dto.MobileDataDto;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.mobileDb.repository.MobileDbRepo;
import com.hti.smpp.common.request.MobileDbRequest;
import com.hti.smpp.common.services.MobileDbUserService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.dto.WebMenuAccessEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.IConstants;

@Service
public class MobileDbUserServiceImpl implements MobileDbUserService {

	
	@Autowired
	private UserEntryRepository userRepository;
	
	@Autowired
	private WebMasterEntryRepository webMasterEntryRepo;
	
	@Autowired
	private MobileDbRepo mobileDbRepo;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	
	
	@Override
	public ResponseEntity<?> mobileScheduleUpload(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<?> mobileShedulePartialUpload(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<?> mobileUserList(MobileDbRequest mobileDbRequest, String username) {

//
//        ActionMessages messages = null;
//        ActionMessage message = null;
		  HashMap<String, Object> combinedMap = new HashMap<String, Object>();
        String target = "";
        boolean b_count = true;
        long mob_count = 0;
//        AddNewMobileDbForm addNewMobileDbForm = (AddNewMobileDbForm) actionForm;
        
        MobileDataDto addNewMobileDbDTO = new MobileDataDto();
        BeanUtils.copyProperties(mobileDbRequest, addNewMobileDbDTO);
//        IMobileDBServices mobileDbServc = new MobileDBServices();
//        HttpSession session = request.getSession(false);
        String query = "";

        String viewAs = "NoPopUp";
        viewAs = addNewMobileDbDTO.getViewAs();
        int ageMin = addNewMobileDbDTO.getAgeMin();
        int ageMax = addNewMobileDbDTO.getAgeMax();
        String age_temp = "";
        String area = "", subarea = "";
        String area_temp = "", subarea_temp = "";
        String profession = "";
        String profession_temp = "";
        String classType = "";
        String classType_temp = "";
        age_temp = "age between " + ageMin + " and " + ageMax + "";
        String[] areaArr = addNewMobileDbDTO.getAreaArr();
        String[] subareaArr = addNewMobileDbDTO.getSubareaArr();
        String[] ProfessionArr = addNewMobileDbDTO.getProfessionArr();
        String[] classTypeArr = addNewMobileDbDTO.getClassTypeArr();

        if (areaArr.length == 1) {
            for (int i = 0; i < areaArr.length; i++) {
                if (areaArr[i].equalsIgnoreCase("%")) {
                    area_temp = "area like '%'";
                }
                if (!areaArr[i].equalsIgnoreCase("%")) {
                    area_temp = "area like '" + areaArr[i] + "%'";
                }
            }
        } else {
            for (int i = 0; i < areaArr.length; i++) {
                if (areaArr[i].equalsIgnoreCase("%")) {
                    area_temp = "area like '%'";
                    break;
                } else {
                    if (i == 0) {
                        area += "'" + areaArr[i] + "'";
                    } else {
                        area += "," + "'" + areaArr[i] + "'";
                    }
                    area_temp = "area in(" + area + ")";
                }
            }
        }
//------------------------------
        if (subareaArr.length == 1) {
            for (int i = 0; i < subareaArr.length; i++) {
                if (subareaArr[i].equalsIgnoreCase("%")) {
                    subarea_temp = "subarea like '%'";
                }
                if (!subareaArr[i].equalsIgnoreCase("%")) {
                    subarea_temp = "subarea like '" + subareaArr[i] + "%'";
                }
            }
        } else {
            for (int i = 0; i < subareaArr.length; i++) {
                if (subareaArr[i].equalsIgnoreCase("%")) {
                    subarea_temp = "subarea like '%'";
                    break;
                } else {
                    if (i == 0) {
                        subarea += "'" + subareaArr[i] + "'";
                    } else {
                        subarea += "," + "'" + subareaArr[i] + "'";
                    }
                    subarea_temp = "subarea in(" + subarea + ")";
                }
            }
        }
        //-------------------------
        if (ProfessionArr.length == 1) {
            for (int i = 0; i < ProfessionArr.length; i++) {
                if (ProfessionArr[i].equalsIgnoreCase("%")) {
                    profession_temp = "profession like '%'";
                }
                if (!ProfessionArr[i].equalsIgnoreCase("%")) {
                    profession_temp = "profession like '" + ProfessionArr[i] + "%'";
                }
            }
        } else {
            for (int i = 0; i < ProfessionArr.length; i++) {
                if (ProfessionArr[i].equalsIgnoreCase("%")) {
                    profession_temp = "profession like '%'";
                    break;
                } else {
                    if (i == 0) {
                        profession += "'" + ProfessionArr[i] + "'";
                    } else {
                        profession += "," + "'" + ProfessionArr[i] + "'";
                    }
                    profession_temp = "profession in(" + profession + ")";
                }
            }
        }
        //--------------------------------------------------------

        if (classTypeArr.length == 1) {
            for (int i = 0; i < classTypeArr.length; i++) {
                if (classTypeArr[i].equalsIgnoreCase("%")) {
                    classType_temp = "classType like '%'";
                }
                if (!classTypeArr[i].equalsIgnoreCase("%")) {
                    classType_temp = "classType like '" + classTypeArr[i] + "%'";
                }
            }
        } else {
            for (int i = 0; i < classTypeArr.length; i++) {
                if (classTypeArr[i].equalsIgnoreCase("%")) {
                    classType_temp = "classType like '%'";
                    break;
                } else {
                    if (i == 0) {
                        classType += "'" + classTypeArr[i] + "'";
                    } else {
                        classType += "," + "'" + classTypeArr[i] + "'";
                    }
                    classType_temp = "classType in(" + classType + ")";
                }
            }
        }
        query = "select count(*) as count_total from mobileuserdata where " + area_temp + " and " + subarea_temp + " and " + profession_temp + " and " + age_temp + " and sex like '" + addNewMobileDbDTO.getSex() + "' and vip like '" + addNewMobileDbDTO.getVip() + "' and " + classType_temp + "";
        String query1 = "select count(*) as count_total,area from mobileuserdata where " + area_temp + " and " + subarea_temp + " and " + profession_temp + " and " + age_temp + " and sex like '" + addNewMobileDbDTO.getSex() + "' and vip like '" + addNewMobileDbDTO.getVip() + "' and " + classType_temp + " group by area";
        String query2 = "select area,mobNumber from mobileuserdata where " + area_temp + " and " + subarea_temp + " and " + profession_temp + " and " + age_temp + " and sex like '" + addNewMobileDbDTO.getSex() + "' and vip like '" + addNewMobileDbDTO.getVip() + "' and " + classType_temp + "";
        
       
        
        ArrayList mobileRecord = new ArrayList<>();
         mobileRecord = getMobileRecords(query, b_count);

       
         
        Iterator itr = mobileRecord.iterator();
        if (itr.hasNext()) {
            mob_count = Long.parseLong((itr.next()).toString());
        }
        
        if (mob_count > 0) {
//            request.setAttribute("TotalRecords", "" + mob_count);
         //   session.setAttribute("session_query", session_query);
            if (viewAs.equalsIgnoreCase("popUp")) {
                target = "PopUpView";
            } else {
            	
            	
//                Map recordMap = mobileDbServc.fetchMobileRecords(query1,true);
                
            	 Map<String, Object> recordMap = new HashMap<>();

            	 recordMap = fetchMobileRecords(query1,true);
                 

                
                
//                Map numberMap = mobileDbServc.fetchMobileRecords(query2,false);
                
                Map<String, List<String>> numberMap = new HashMap<>();
                 numberMap = fetchMobileRecords(query2,false);
              

                
              

             // Put recordMap into combinedMap
             combinedMap.putAll(recordMap);

             // Put numberMap into combinedMap
             combinedMap.putAll(numberMap);

                
               
                
//                request.setAttribute("recordMap", recordMap);
//                session.setAttribute("numberMap", numberMap);
//                target = IConstants.SUCCESS_KEY;
            }
        } else {
//            messages = new ActionMessages();
//            message = new ActionMessage("error.record.unavailable");
//            messages.add("param_message", message);
//            saveMessages(request, messages);
//            request.setAttribute("param_value", "Any Record..Try Again");
            if (viewAs.equalsIgnoreCase("popUp")) {
                target = "PopUpFailure";
            } else {
//                target = IConstants.FAILURE_KEY;
            }
        }
//        return mapping.findForward(target);
    
        return new ResponseEntity<>(combinedMap ,HttpStatus.OK); 
       
	}

	@Override
	public ResponseEntity<?> mobileUserListInfo(MobileDbRequest mobileDbRequest ,String username) {


		 HashMap<String, Object> resultMap = new HashMap<>();
        String target = "";
//        ActionMessages messages = null;
//        ActionMessage message = null;
//        AddNewMobileDbForm addNewMobileDbForm = (AddNewMobileDbForm) form;
        MobileDataDto addNewMobileDbDTO = new MobileDataDto();
        BeanUtils.copyProperties(mobileDbRequest, addNewMobileDbDTO);
//        IMobileDBServices mobileDbServc = new MobileDBServices();
//        HttpSession session = request.getSession(false);
        String query = "";
        int ageMin = addNewMobileDbDTO.getAgeMin();
        int ageMax = addNewMobileDbDTO.getAgeMax();
        if (ageMin == 0 || ageMax == 0) {
            if (ageMin == 0) {
                ageMin = 1;
            }
            if (ageMax == 0) {
                ageMax = 99;
            }
        }
        String age_temp = "";
        String area = "", subarea = "";
        String area_temp = "", subarea_temp = "";
        String profession = "";
        String profession_temp = "";
        String classType = "";
        String classType_temp = "";
        age_temp = "age between " + ageMin + " and " + ageMax + "";
        String[] areaArr = addNewMobileDbDTO.getAreaArr();
        String[] subareaArr = addNewMobileDbDTO.getSubareaArr();
        String[] ProfessionArr = addNewMobileDbDTO.getProfessionArr();
        String[] classTypeArr = addNewMobileDbDTO.getClassTypeArr();

        if (areaArr.length == 1) {
            for (int i = 0; i < areaArr.length; i++) {
                if (areaArr[i].equalsIgnoreCase("%")) {
                    area_temp = "area like '%'";
                }
                if (!areaArr[i].equalsIgnoreCase("%")) {
                    area_temp = "area like '" + areaArr[i] + "%'";
                }
            }
        } else {
            for (int i = 0; i < areaArr.length; i++) {
                if (areaArr[i].equalsIgnoreCase("%")) {
                    area_temp = "area like '%'";
                    break;
                } else {
                    if (i == 0) {
                        area += "'" + areaArr[i] + "'";
                    } else {
                        area += "," + "'" + areaArr[i] + "'";
                    }
                    area_temp = "area in(" + area + ")";
                }
            }
        }
        //----------------------------
         if (subareaArr.length == 1) {
            for (int i = 0; i < subareaArr.length; i++) {
                if (subareaArr[i].equalsIgnoreCase("%")) {
                    subarea_temp = "subarea like '%'";
                }
                if (!subareaArr[i].equalsIgnoreCase("%")) {
                    subarea_temp = "subarea like '" + subareaArr[i] + "%'";
                }
            }
        } else {
            for (int i = 0; i < subareaArr.length; i++) {
                if (subareaArr[i].equalsIgnoreCase("%")) {
                    subarea_temp = "subarea like '%'";
                    break;
                } else {
                    if (i == 0) {
                        subarea += "'" + subareaArr[i] + "'";
                    } else {
                        subarea += "," + "'" + subareaArr[i] + "'";
                    }
                    subarea_temp = "subarea in(" + subarea + ")";
                }
            }
        }

        //-------------------------
        if (ProfessionArr.length == 1) {
            for (int i = 0; i < ProfessionArr.length; i++) {
                if (ProfessionArr[i].equalsIgnoreCase("%")) {
                    profession_temp = "profession like '%'";
                }
                if (!ProfessionArr[i].equalsIgnoreCase("%")) {
                    profession_temp = "profession like '" + ProfessionArr[i] + "%'";
                }
            }
        } else {
            for (int i = 0; i < ProfessionArr.length; i++) {
                if (ProfessionArr[i].equalsIgnoreCase("%")) {
                    profession_temp = "profession like '%'";
                    break;
                } else {
                    if (i == 0) {
                        profession += "'" + ProfessionArr[i] + "'";
                    } else {
                        profession += "," + "'" + ProfessionArr[i] + "'";
                    }
                    profession_temp = "profession in(" + profession + ")";
                }
            }
        }
        //--------------------------------------------------------

        if (classTypeArr.length == 1) {
            for (int i = 0; i < classTypeArr.length; i++) {
                if (classTypeArr[i].equalsIgnoreCase("%")) {
                    classType_temp = "classType like '%'";
                }
                if (!classTypeArr[i].equalsIgnoreCase("%")) {
                    classType_temp = "classType like '" + classTypeArr[i] + "%'";
                }
            }
        } else {
            for (int i = 0; i < classTypeArr.length; i++) {
                if (classTypeArr[i].equalsIgnoreCase("%")) {
                    classType_temp = "classType like '%'";
                    break;
                } else {
                    if (i == 0) {
                        classType += "'" + classTypeArr[i] + "'";
                    } else {
                        classType += "," + "'" + classTypeArr[i] + "'";
                    }
                    classType_temp = "classType in(" + classType + ")";
                }
            }
        }

        query = "select * from mobileuserdata where " + area_temp + " and " + subarea_temp + " and " + profession_temp + " and " + age_temp + " and sex like '" + addNewMobileDbDTO.getSex() + "' and vip like '" + addNewMobileDbDTO.getVip() + "' and " + classType_temp + " order by area,subarea";

        Collection mobileRecord = getMobileRecordsFull(query);
        
        if (mobileRecord.size() > 0) {
            int maxSize = IConstants.MaxRecordFetch;
           

         // Put the mobile information list into the HashMap
         resultMap.put("mobinfo", mobileRecord);

         // Put the size of the mobile information list into the HashMap
         resultMap.put("mobinfoSize", String.valueOf(mobileRecord.size()));
         
//            request.setAttribute("mobinfo", mobileRecord);
//            request.setAttribute("mobinfoSize", mobileRecord.size() + "");
            if (mobileRecord.size() <= 10) {
//                request.setAttribute("div_setting", "Not_Scrollable");
            } else {
//                request.setAttribute("div_setting", "Scrollable");
            }
            target = IConstants.SUCCESS_KEY;

        } else {
//            messages = new ActionMessages();
//            message = new ActionMessage("error.record.unavailable");
//            messages.add("param_message", message);
//            saveMessages(request, messages);
//            request.setAttribute("param_value", "Any Record..Try Again");
//            target = IConstants.FAILURE_KEY;
        }
//        return mapping.findForward(target);
    
        
        
        return new ResponseEntity<>(resultMap ,HttpStatus.OK); 
	}

	@Override
	public ResponseEntity<?> queryForMobileRecord(String username) {


//		String target = IConstants.FAILURE_KEY;
//		ActionMessages messages = new ActionMessages();
//		ActionMessage message = null;
//		UserSessionObject userSessionObject = (UserSessionObject) SessionHelper.getSessionObject(request,
//				IConstants.USER_SESSION_KEY);
		
		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry userEntry = null;
		if (userOptional.isPresent()) {
			userEntry = userOptional.get();
			if (!Access.isAuthorized(userEntry.getRole(), "isAuthorizedSuperAdminAndSystem")) {
				throw new UnauthorizedException("User does not have the required roles for this operation.");
			}
		} else {
			throw new NotFoundException("User not found with the provided username.");
		}
		
		int userId = userEntry.getId();
		WebMasterEntry webMenu = null;
		Optional<WebMasterEntry> webEntryOptional = this.webMasterEntryRepo.findById(userId);
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}
		
		Map<String, Object> attributeMap = new HashMap<>();
		
		if (webMenu.isMobileDBAccess()) {
			try {
//				MobileDBServices mobileDBServices = new MobileDBServices();
				ArrayList<String> professionList = mobileDbRepo.findDistinctByProfession();
				ArrayList<String> areaList = mobileDbRepo.findDistinctByArea();
				ArrayList<String> subAreaList = mobileDbRepo.findDistinctSubareaByAreaIn(null);
				
				if (areaList.isEmpty() && subAreaList.isEmpty() && professionList.isEmpty()) {
//					message = new ActionMessage("error.record.unavailable");
				} else {
					Collections.sort(professionList);
					Collections.sort(areaList);
//					request.setAttribute("professionList", professionList);
//					request.setAttribute("professionListSize", professionList.size() + "");
//					request.setAttribute("areaList", areaList);
//					request.setAttribute("areaListSize", areaList.size() + "");
//					request.setAttribute("subareaList", subAreaList);
//					request.setAttribute("subareaListSize", subAreaList.size() + "");
//					target = IConstants.SUCCESS_KEY;
					
					
					
					    attributeMap.put("professionList", professionList);
				        attributeMap.put("professionListSize", String.valueOf(professionList.size()));
				        attributeMap.put("areaList", areaList);
				        attributeMap.put("areaListSize", String.valueOf(areaList.size()));
				        attributeMap.put("subareaList", subAreaList);
				        attributeMap.put("subareaListSize", String.valueOf(subAreaList.size()));
				        
				        
				}
			} catch (Exception ex) {
				ex.printStackTrace();
//				message = new ActionMessage("error.processError");
			}
		} else {
//			target = "invalidRequest";
		}
//		messages.add(ActionMessages.GLOBAL_MESSAGE, message);
//		saveMessages(request, messages);
//		return mapping.findForward(target);
	
		
		
		return new ResponseEntity<>(attributeMap ,HttpStatus.OK); 
	}

	@Override
	public ResponseEntity<?> SendAreaWiseSms(MobileDbRequest mobileDbRequest,String username) {
		
		
		HashMap<String, Object> resultMap = new HashMap<>();
//        String target = IConstants.FAILURE_KEY;
        try {
//            HttpSession session = request.getSession(false);
//            AddNewMobileDbForm addNewMobileDbForm = (AddNewMobileDbForm) form;
            MobileDataDto addNewMobileDbDTO=new MobileDataDto();
            BeanUtils.copyProperties(mobileDbRequest, addNewMobileDbDTO);
            String[] arearArr = addNewMobileDbDTO.getAreaArr();
            String[] areaWiseNumber = addNewMobileDbDTO.getAreaWiseNumber();
            Map numberMap = addNewMobileDbDTO.getNumberMap();
//            session.removeAttribute("numberMap");
            List finalList = new ArrayList();
            for (int i = 0; i < arearArr.length; i++) {
                String area = arearArr[i];
                long count = 0;
                try {
                    count = Long.parseLong(areaWiseNumber[i]);
                } catch (NumberFormatException ne) {
                    System.out.println("NumberFormatException:: "+areaWiseNumber[i]);
                    count = 0;
                }
                
                if (count > 0) {
                    if (numberMap.containsKey(area)) {
                        List list = (List) numberMap.get(area);
                        for (int k = 0; k < count; k++) {
                            finalList.add((String) list.get(k));
                        }
                    }
                }
            }
            
            System.out.println("finalList:: " + finalList.size());
            if (finalList.size() > 0) {
            	
            	

            	// Put the phone numbers list into the HashMap
            	resultMap.put("numberList", finalList);

            	// Put the total records count into the HashMap
            	resultMap.put("TotalRecords", String.valueOf(finalList.size()));
            	
//                session.setAttribute("numberList", finalList);
//                request.setAttribute("TotalRecords", "" + finalList.size());
//                target = IConstants.SUCCESS_KEY;
            } else {
//                target = IConstants.FAILURE_KEY;
//                ActionMessages messages = new ActionMessages();
//                ActionMessage message = new ActionMessage("error.record.unavailable");
//                messages.add("param_message", message);
//                saveMessages(request, messages);
//                request.setAttribute("param_value", "Any Record..Try Again");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//        return mapping.findForward(target);
    
        
        
        
        return new ResponseEntity<>(resultMap ,HttpStatus.OK); 
	}
	
	
	
	public ArrayList<?> getMobileRecords(String query, boolean b_count) {
		ArrayList mobileInfo = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		long count_total = 0;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			rs = pStmt.executeQuery();
			if (b_count == false) {
				while (rs.next()) {
					String mbileNum = rs.getString("mobNumber");
					mobileInfo.add(mbileNum);
				}
			}
			if (b_count == true) {
				while (rs.next()) {
					count_total = rs.getLong("count_total");
				}
				mobileInfo.add(count_total + "");
			}
		} catch (SQLException sqle) {
			System.out.println(sqle);
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return mobileInfo;
	}
	
	public Map fetchMobileRecords(String query, boolean count) {
		Map mobileInfo = new HashMap();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		long count_total = 0;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			rs = pStmt.executeQuery();
			if (count) {
				while (rs.next()) {
					count_total = rs.getLong("count_total");
					String area = rs.getString("area");
					mobileInfo.put(area, new Long(count_total));
				}
			} else {
				while (rs.next()) {
					String number = rs.getString("mobNumber");
					String area = rs.getString("area");
					if (mobileInfo.containsKey(area)) {
						List list = (List) mobileInfo.get(area);
						list.add(number);
						mobileInfo.put(area, list);
					} else {
						List list = new ArrayList();
						list.add(number);
						mobileInfo.put(area, list);
					}
				}
			}
		} catch (SQLException sqle) {
			System.out.println(sqle);
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return mobileInfo;
	}

	public ArrayList getMobileRecordsFull(String query) 
	{
		ArrayList mobileInfo = new ArrayList();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		MobileDataDto addNewMobileDbDTO = null;
//		logger.info("QUERY:: " + query);
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				addNewMobileDbDTO = new MobileDataDto();
				// ------------------------------------
				addNewMobileDbDTO.setMob_id(rs.getInt("mob_id"));
				addNewMobileDbDTO.setMobileNumber(rs.getString("mobNumber"));
				addNewMobileDbDTO.setSex(rs.getString("sex"));
				addNewMobileDbDTO.setAge(rs.getInt("age"));
				addNewMobileDbDTO.setArea(rs.getString("area"));
				addNewMobileDbDTO.setSubarea(rs.getString("subarea"));
				addNewMobileDbDTO.setVip(rs.getString("vip"));
				addNewMobileDbDTO.setClassType(rs.getString("classType"));
				addNewMobileDbDTO.setProfession(rs.getString("profession"));
				// ------------------------------------------
				mobileInfo.add(addNewMobileDbDTO);
			}
		} catch (SQLException sqle) {
			System.out.println(sqle);
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
				if (rs != null) {
					rs.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return mobileInfo;
	}
	
	
}
