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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.dto.MobileDataDto;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.NumberFormatError;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.mobileDb.repository.MobileDbRepo;
import com.hti.smpp.common.request.MobileUserListInfoRequest;
import com.hti.smpp.common.request.MobileUserListRequest;
import com.hti.smpp.common.request.SendAreaSmsRequest;
import com.hti.smpp.common.response.GetMobileRecordFullResponse;
import com.hti.smpp.common.response.MobileUserListInfoResponse;
import com.hti.smpp.common.response.MobileUserListResponse;
import com.hti.smpp.common.response.QueryMobileRecordResponse;
import com.hti.smpp.common.response.SendAreaWiseSmsResponse;
import com.hti.smpp.common.services.MobileDbUserService;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;


@Service
public class MobileDbUserServiceImpl implements MobileDbUserService {

	
	@Autowired
	private UserEntryRepository userRepository;
	
	@Autowired
	private WebMasterEntryRepository webMasterEntryRepo;
	
	@Autowired
	private MobileDbRepo mobileDbRepo;
	

	@Autowired
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;
	
	
	private final Logger logger = LoggerFactory.getLogger(MobileDbUserServiceImpl.class);
	
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
	public ResponseEntity<?> mobileUserList(MobileUserListRequest mobileDbRequest, String username) {

		MobileUserListResponse mobileUserListResponse = new MobileUserListResponse();
        String target = "";
        boolean b_count = true;
        long mob_count = 0;
        logger.info(messageResourceBundle.getLogMessage("mobileDb.enter.mobileUserList"));
        
        MobileDataDto addNewMobileDbDTO = new MobileDataDto();
        BeanUtils.copyProperties(mobileDbRequest, addNewMobileDbDTO);

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

        logger.info(messageResourceBundle.getLogMessage("mobileDb.intermediate.ageRange"), age_temp);
        
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
//--------------------------------------------------------
        logger.info(messageResourceBundle.getLogMessage("mobileDb.intermediate.areaArrayLength"), areaArr.length);
        logger.info(messageResourceBundle.getLogMessage("mobileDb.intermediate.subareaArrayLength"), subareaArr.length);
        logger.info(messageResourceBundle.getLogMessage("mobileDb.intermediate.professionArrayLength"), ProfessionArr.length);
        logger.info(messageResourceBundle.getLogMessage("mobileDb.intermediate.classTypeArrayLength"), classTypeArr.length);
//  ------------------------------------------------------------      
        
       try {
		
    	   query = "select count(*) as count_total from mobileuserdata where " + area_temp + " and " + subarea_temp + " and " + profession_temp + " and " + age_temp + " and sex like '" + addNewMobileDbDTO.getSex() + "' and vip like '" + addNewMobileDbDTO.getVip() + "' and " + classType_temp + "";
           String query1 = "select count(*) as count_total,area from mobileuserdata where " + area_temp + " and " + subarea_temp + " and " + profession_temp + " and " + age_temp + " and sex like '" + addNewMobileDbDTO.getSex() + "' and vip like '" + addNewMobileDbDTO.getVip() + "' and " + classType_temp + " group by area";
           String query2 = "select area,mobNumber from mobileuserdata where " + area_temp + " and " + subarea_temp + " and " + profession_temp + " and " + age_temp + " and sex like '" + addNewMobileDbDTO.getSex() + "' and vip like '" + addNewMobileDbDTO.getVip() + "' and " + classType_temp + "";
           
          
           
           ArrayList mobileRecord = new ArrayList<>();
            mobileRecord = getMobileRecords(query, b_count);
            logger.info(messageResourceBundle.getLogMessage("mobileDb.intermediate.executingQuery"), query);
            
          
            
           Iterator itr = mobileRecord.iterator();
           if (itr.hasNext()) {
               mob_count = Long.parseLong((itr.next()).toString());
           }
           
           if (mob_count > 0) {
//               request.setAttribute("TotalRecords", "" + mob_count);
           	   mobileUserListResponse.setMobileRecord(mob_count);
            //   session.setAttribute("session_query", session_query);
               if (viewAs.equalsIgnoreCase("popUp")) {
                   target = "PopUpView";
                   mobileUserListResponse.setTarget(target);
               } else {
               	
               	               
               	 Map<String, Object> recordMap = new HashMap<>();
               	 recordMap = fetchMobileRecords(query1,true);
                                    
                 Map<String, List<String>> numberMap = new HashMap<>();
                 numberMap = fetchMobileRecords(query2,false);
                 
                   
                 mobileUserListResponse.setNumberMap(numberMap);
                 mobileUserListResponse.setRecordMap(recordMap);          
                   

                   target = IConstants.SUCCESS_KEY;
                   mobileUserListResponse.setTarget(target);
               }
           } else {


               if (viewAs.equalsIgnoreCase("popUp")) {
                   target = "PopUpFailure";
               } else {
                   target = IConstants.FAILURE_KEY;
               }
           }
           
	    } catch (Exception e) {
	    	 logger.error(messageResourceBundle.getLogMessage("mobileDb.error.mobileUserList"), e.getMessage(), e);
		     throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
	    }
        
       logger.info(messageResourceBundle.getLogMessage("mobileDb.exit.mobileUserList"));
        return new ResponseEntity<>(mobileUserListResponse ,HttpStatus.OK); 
       
	}

	@Override
	public ResponseEntity<?> mobileUserListInfo(MobileUserListInfoRequest mobileDbRequest ,String username) {


		 MobileUserListInfoResponse mobileUserListInfoResponse = new MobileUserListInfoResponse();
        String target = "";

        MobileDataDto addNewMobileDbDTO = new MobileDataDto();
        BeanUtils.copyProperties(mobileDbRequest, addNewMobileDbDTO);

        logger.info(messageResourceBundle.getLogMessage("mobileDb.enter.mobileUserListInfo"));
        
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

        try {
			
        	   query = "select * from mobileuserdata where " + area_temp + " and " + subarea_temp + " and " + profession_temp + " and " + age_temp + " and sex like '" + addNewMobileDbDTO.getSex() + "' and vip like '" + addNewMobileDbDTO.getVip() + "' and " + classType_temp + " order by area,subarea";

               Collection mobileRecord = getMobileRecordsFull(query);
               System.out.println(mobileRecord);
               
               if (mobileRecord.size() > 0) {
                   int maxSize = IConstants.MaxRecordFetch;
                  
                
                mobileUserListInfoResponse.setMobileRecord(mobileRecord);
                
                mobileUserListInfoResponse.setSize(mobileRecord.size());
                
                   if (mobileRecord.size() <= 10) {
                       mobileUserListInfoResponse.setDiv_setting("Not_Scrollable");
                   } else {
                       mobileUserListInfoResponse.setDiv_setting("Scrollable");
                   }
                   target = IConstants.SUCCESS_KEY;
                   logger.info(messageResourceBundle.getLogMessage("mobileDb.success.mobileUserListInfo"));
                   mobileUserListInfoResponse.setTarget(target);

               } else {

               	   mobileUserListInfoResponse.setParam_value("Any Record..Try Again");
                   target = IConstants.FAILURE_KEY;
                   logger.error(messageResourceBundle.getLogMessage("mobileDb.failure.mobileUserListInfo"));
                   mobileUserListInfoResponse.setTarget(target);
               }
               
		} catch (Exception e) {
			logger.error(messageResourceBundle.getLogMessage("mobileDb.failure.mobileUserListInfo"), e.getMessage());
			  throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
		}
          
        return new ResponseEntity<>(mobileUserListInfoResponse ,HttpStatus.OK); 
	}

	
	@Override
	public ResponseEntity<?> queryForMobileRecord(String username) {


	    QueryMobileRecordResponse queryMobileRecordResponse = new QueryMobileRecordResponse();
		String target = IConstants.FAILURE_KEY;
		
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
		
		 logger.info(messageResourceBundle.getLogMessage("mobileDb.userId.queryForMobileRecord"), userId);
		
		WebMasterEntry webMenu = null;
		Optional<WebMasterEntry> webEntryOptional = this.webMasterEntryRepo.findById(userId);
	
		 logger.info(messageResourceBundle.getLogMessage("mobileDb.webEntryOptional.queryForMobileRecord"), webEntryOptional);
		 
		if (webEntryOptional.isPresent()) {
			webMenu = webEntryOptional.get();
		} else {
			throw new NotFoundException("WebMenuAccessEntry not found.");
		}

		
		if (webMenu.isMobileDBAccess()) {
			try {
				ArrayList<String> professionList = mobileDbRepo.findDistinctByProfession();
				ArrayList<String> areaList = mobileDbRepo.findDistinctByArea();
				ArrayList<String> subAreaList = mobileDbRepo.findDistinctSubareaByAreaIn(null);
				
				if (areaList.isEmpty() && subAreaList.isEmpty() && professionList.isEmpty()) {
//					message = new ActionMessage("error.record.unavailable");
				} else {
					Collections.sort(professionList);
					Collections.sort(areaList);
					target = IConstants.SUCCESS_KEY;
					
					 logger.info(messageResourceBundle.getLogMessage("mobileDb.success.queryForMobileRecord"));
					
					
					queryMobileRecordResponse.setProfessionList(professionList);
					queryMobileRecordResponse.setProfessionListSize(professionList.size());
					queryMobileRecordResponse.setAreaList(areaList);
					queryMobileRecordResponse.setAreaListSize(areaList.size());
					queryMobileRecordResponse.setSubAreaList(subAreaList);
					queryMobileRecordResponse.setSubareaListSize(subAreaList.size());
					queryMobileRecordResponse.setTarget(target);
				        
				        
				}
			} catch (Exception ex) {
				  throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
			}
		} else {
			target = "invalidRequest";
			 logger.info(messageResourceBundle.getLogMessage("mobileDb.failed.queryForMobileRecord"));
			queryMobileRecordResponse.setTarget(target);
		}
		
		return new ResponseEntity<>(queryMobileRecordResponse ,HttpStatus.OK); 
	}

	
	@Override
	public ResponseEntity<?> SendAreaWiseSms(SendAreaSmsRequest mobileDbRequest,String username) {
		
		
		SendAreaWiseSmsResponse sendAreaWiseSmsResponse = new SendAreaWiseSmsResponse();
        String target = IConstants.FAILURE_KEY;
        try {

            MobileDataDto addNewMobileDbDTO=new MobileDataDto();
            BeanUtils.copyProperties(mobileDbRequest, addNewMobileDbDTO);
            
            String[] arearArr = addNewMobileDbDTO.getAreaArr();
            String[] areaWiseNumber = addNewMobileDbDTO.getAreaWiseNumber();
            Map numberMap = addNewMobileDbDTO.getNumberMap();

            List finalList = new ArrayList();
            for (int i = 0; i < arearArr.length; i++) {
                String area = arearArr[i];
                long count = 0;
                try {
                    count = Long.parseLong(areaWiseNumber[i]);
                } catch (NumberFormatException ne) {
                    logger.info(messageResourceBundle.getLogMessage("mobileDb.NumberFormatException.SendAreaWiseSms")+areaWiseNumber[i]);
                    count = 0;
                    throw new NumberFormatError(messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_EXCEPTION));
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
            
            logger.info(messageResourceBundle.getLogMessage("mobileDb.finalList.SendAreaWiseSms") + finalList.size());
            if (finalList.size() > 0) {
            	
            	sendAreaWiseSmsResponse.setNumberList(finalList);

            	sendAreaWiseSmsResponse.setTotalRecords(finalList.size());
            	
                target = IConstants.SUCCESS_KEY;
                               
            } else {
                target = IConstants.FAILURE_KEY;
            }
            sendAreaWiseSmsResponse.setTarget(target);
        } catch (IndexOutOfBoundsException ex) {
           throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.MOBILEDB_AREAWISECOUNT_ERROR )); 
        }catch (Exception ex) {
        	throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR)); 
        }     
             
        return new ResponseEntity<>(sendAreaWiseSmsResponse ,HttpStatus.OK); 
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
		GetMobileRecordFullResponse addNewMobileDbDTO = null;
//		logger.info("QUERY:: " + query);
		try {
			con = getConnection();
			pStmt = con.prepareStatement(query);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				addNewMobileDbDTO = new GetMobileRecordFullResponse();
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
