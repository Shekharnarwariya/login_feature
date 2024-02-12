package com.hti.smpp.common.services.Impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.dto.MobileDataDto;
import com.hti.smpp.common.dto.UpdateMobileInfoDto;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NumberFormatError;
import com.hti.smpp.common.mobileDb.dto.MobileDbEntity;
import com.hti.smpp.common.mobileDb.repository.MobileDbRepo;
import com.hti.smpp.common.request.ChooseRequest;
import com.hti.smpp.common.request.DeleteMobDataRequest;
import com.hti.smpp.common.request.ShowMobileDataRequest;
import com.hti.smpp.common.request.UpdateMobileInfo;
import com.hti.smpp.common.request.UpdateSingleRequest;

import com.hti.smpp.common.response.ChooseRequestResponse;
import com.hti.smpp.common.response.EditDataResponse;
import com.hti.smpp.common.response.MobileDbResponse;
import com.hti.smpp.common.response.UpdateMobileDbResponse;
import com.hti.smpp.common.services.MobileDbService;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.IConstants;
//import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

@Service
public class MobileDbServiceImpl implements MobileDbService {

	private final Logger logger = LoggerFactory.getLogger(MobileDbServiceImpl.class);
	
	@Autowired
	private MobileDbRepo mobileDbRepo;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Override
	public ResponseEntity<?> addMobileData(String mobileDb, MultipartFile file, String username) {
		
		logger.info(messageResourceBundle.getLogMessage("mobileDb.enter.addMobileDbData"));
		String target = "";
        long mob_number = 0;
        String num = "";
        
        List<MobileDbEntity> isInsert = null;
        MobileDataDto form;
        try {
            MobileDataDto addNewMobileDbDTO = new MobileDataDto();
            MobileDbEntity mobileDbEntity = new MobileDbEntity();
            MobileDataDto tempMobileDbDTO = null;
            
            ArrayList<MobileDataDto> mobileUserdata = new ArrayList<>();
            ArrayList<MobileDbEntity> mobileUserdataEntry = new ArrayList<>();
            
//            String type = addNewMobileDbDTO.getListType();
            
            logger.info(messageResourceBundle.getLogMessage("mobileDb.addDataEntry.getAge"),addNewMobileDbDTO.getAge());
            logger.info(messageResourceBundle.getLogMessage("mobileDb.getUsername"), username);
            
            String format = "";
            if (file != null && file.getName().length() > 0) {
                MultipartFile uploadedFile = file;
               
                String file_name = uploadedFile.getOriginalFilename();
                if (file_name.indexOf(".xlsx") > 0) {
                    format = " : Excel(.xlsx)";

                    target = IConstants.FAILURE_KEY;
         
                } else if (file_name.indexOf(".txt") > 0) {
                    format = "Text";
                } else if (file_name.indexOf(".xls") > 0) {
                    format = "Excel";
                } else {
                    format = "FormatNotSupported";
                    target = IConstants.FAILURE_KEY;

                }

                if (format.equalsIgnoreCase("Text")) {
                	
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        InputStream stream = file.getInputStream();
                       
                        int bytesRead = 0;
                        byte[] buffer = new byte[8192];

                        while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        String data = new String(baos.toByteArray());

                        StringTokenizer st = new StringTokenizer(data, "\n");
                        while (st.hasMoreTokens()) {
                            String entry = (String) st.nextToken();
                            StringTokenizer st1 = new StringTokenizer(entry, ",");
                            tempMobileDbDTO = new MobileDataDto();
                            
                            try {
                            	
                                mob_number = Long.parseLong(st1.nextToken());
                               
                                num = String.valueOf(mob_number);
                               
                                tempMobileDbDTO.setMobileNumber(num);
                                
                                tempMobileDbDTO.setSex(st1.nextToken());
                                tempMobileDbDTO.setAge(Integer.parseInt(st1.nextToken()));
                                tempMobileDbDTO.setVip(st1.nextToken());
                               
                                tempMobileDbDTO.setArea(st1.nextToken());
                                tempMobileDbDTO.setSubarea(st1.nextToken());
                               
                                tempMobileDbDTO.setClassType(st1.nextToken());
                                
                                tempMobileDbDTO.setProfession(st1.nextToken().trim());
                                
                                mobileUserdata.add(tempMobileDbDTO);
                            }catch (NumberFormatException e) {
                            	 logger.error(messageResourceBundle.getLogMessage("mobileDb.mobileUserList.numberFormatException"), e.getMessage());
                                throw new NumberFormatError(messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_EXCEPTION));
                            }catch (Exception e) {
                            	 logger.error(messageResourceBundle.getLogMessage("mobileDb.mobileUserList.generalException"), e.getMessage());
                                throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR)); 
                            }
                        }

                    } catch (Exception ex) {
                    	 logger.error(messageResourceBundle.getLogMessage("mobileDb.mobileUserList.SaveContactList"), ex.getMessage());
                        throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR)); 

                    }
                } else if (format.equalsIgnoreCase("Excel")) {
                    POIFSFileSystem fs = new POIFSFileSystem(addNewMobileDbDTO.getMobileNumbersList().getInputStream());
                    HSSFWorkbook wb = new HSSFWorkbook(fs);
                    HSSFSheet sheet = wb.getSheetAt(0);
                    HSSFRow row;
                    HSSFCell cell;
                    String s; // will be used to store the cell content.
                    boolean isNumSet = false;
                    boolean isNameSet = false;
                    boolean isemailSet = false;


                    int rows; // No of rows
                    rows = sheet.getPhysicalNumberOfRows();

                    int cols = 0; // No of columns
                    int tmp = 0;

                    // To get the data properly if some rows are blank
                    for (int i = 0; i < 10 || i < rows; i++) {
                        row = sheet.getRow(i);
                        if (row != null) {
                            tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                            if (tmp > cols) {
                                cols = tmp;
                            }
                        }
                    }
                    for (int r = 0; r < rows; r++) {
                        row = sheet.getRow(r);
                        if (row != null) {
                            tempMobileDbDTO = new MobileDataDto();
                            for (int c = 0; c < cols; c++) {
                                cell = row.getCell((short) c);
                                if (cell != null) {
                                    s = (String) cell.toString();
                                    if (c == 0) {
                                        BigDecimal b = new BigDecimal(s);
                                        tempMobileDbDTO.setMobileNumber("" + b);
                                    } else if (c == 1) {
                                        tempMobileDbDTO.setSex(s);
                                    } else if (c == 2) {
                                        double d = Double.parseDouble(s);
                                        int i = (int) Math.abs(d);
                                        tempMobileDbDTO.setAge(i);
                                    } else if (c == 3) {
                                        tempMobileDbDTO.setVip(s);
                                    } else if (c == 4) {
                                        tempMobileDbDTO.setArea(s);
                                    } else if (c == 5) {
                                        tempMobileDbDTO.setSubarea(s);
                                    } else if (c == 6) {
                                        tempMobileDbDTO.setClassType(s);
                                    } else if (c == 7) {
                                        tempMobileDbDTO.setProfession(s.trim());
                                    }
                                }
                            }
                            mobileUserdata.add(tempMobileDbDTO);
                        }
                    }

                } else {
                    target = IConstants.FAILURE_KEY;
                }

                logger.info(messageResourceBundle.getLogMessage("file.format"), format);
                
                mobileUserdata.forEach(l -> {
 				     MobileDbEntity response = new MobileDbEntity ();				
                     response.setMobileNumber(l.getMobileNumber());
                     response.setSex(l.getSex());
                     response.setAge(l.getAge());
                     response.setVip(l.getVip());
                     response.setArea(l.getArea());
                     response.setSubArea(l.getSubarea());
                     response.setClassType(l.getClassType());
                     response.setProfession(l.getProfession());
                     
    				
                     mobileUserdataEntry.add(response);
   			});
               
        
    			
            } else {
            	
            	 ObjectMapper objectMapper = new ObjectMapper();
                 form= objectMapper.readValue(mobileDb, MobileDataDto.class);
                 
                 BeanUtils.copyProperties(form,addNewMobileDbDTO );
                 
                 if (addNewMobileDbDTO.getMobileNumber() == null ||
                	 addNewMobileDbDTO.getVip() == null ||
                	 addNewMobileDbDTO.getSex() == null ||
                	 addNewMobileDbDTO.getAge() == 0 ||
                	 addNewMobileDbDTO.getArea() == null ||
                	 addNewMobileDbDTO.getSubarea() == null ||
                	 addNewMobileDbDTO.getProfession() == null ||
                	 addNewMobileDbDTO.getClassType() == null) {
         	            // Handle the case where any of the arrays is null
         	         logger.info(messageResourceBundle.getLogMessage("mobileDb.addMobileData.nullValues"));
         	         return new ResponseEntity<>(messageResourceBundle.getExMessage(ConstantMessages.INSUFFICIENT_DATA_VALUES), HttpStatus.BAD_REQUEST);
         	        }
         	 
               
                 String type = addNewMobileDbDTO.getListType();
                mobileUserdata.add(addNewMobileDbDTO);
                mobileDbEntity.setMobileNumber(addNewMobileDbDTO.getMobileNumber());
                mobileDbEntity.setAge(addNewMobileDbDTO.getAge());
                mobileDbEntity.setSex(addNewMobileDbDTO.getSex());
                mobileDbEntity.setVip(addNewMobileDbDTO.getVip());
                mobileDbEntity.setArea(addNewMobileDbDTO.getArea());
                mobileDbEntity.setClassType(addNewMobileDbDTO.getClassType());
                mobileDbEntity.setProfession(addNewMobileDbDTO.getProfession());
                mobileDbEntity.setSubArea(addNewMobileDbDTO.getSubarea());
                
                
                mobileUserdataEntry.add(mobileDbEntity);
            }
                      
             isInsert = mobileDbRepo.saveAll(mobileUserdataEntry);
      
            if (!isInsert.isEmpty()) {
            	 logger.info(messageResourceBundle.getLogMessage("mobileDb.saveSuccess"));
                target = IConstants.SUCCESS_KEY;
            } else {
            	logger.warn(messageResourceBundle.getLogMessage("mobileDb.saveFailure"));
                target = IConstants.FAILURE_KEY;
            }
            
        } catch (Exception e) {
        	 logger.error(messageResourceBundle.getLogMessage("mobileDb.uploadFailure"), e.getLocalizedMessage());
        	throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR)); 
        }
        
        logger.info(messageResourceBundle.getLogMessage("mobileDb.addMobileData.exiting"));
		return new ResponseEntity<>(isInsert,HttpStatus.CREATED);
	}

	
	
	
	@Override
	public ResponseEntity<?> showMobileData(ShowMobileDataRequest mobileDb, String username) {


		String target = "";
        String mobileNumber="";

        try {
			
        	MobileDataDto addNewMobileDbDTO = new MobileDataDto();
            BeanUtils.copyProperties(mobileDb, addNewMobileDbDTO);
            
            mobileNumber = addNewMobileDbDTO.getMobileNumber();
            
            logger.info(messageResourceBundle.getLogMessage("mobileDb.showData.mobileNumber"), mobileNumber);
            
            
            ArrayList<MobileDbEntity> tempMob = mobileDbRepo.findByMobileNumber(mobileNumber);
            
            ArrayList<MobileDbResponse> responseMobileDb = new ArrayList<MobileDbResponse>();
            
            
            tempMob.forEach(l -> {
    		     MobileDbResponse response = new MobileDbResponse ();				
                response.setMobileNumber(l.getMobileNumber());
                response.setSex(l.getSex());
                response.setAge(l.getAge());
                response.setVip(l.getVip());
                response.setArea(l.getArea());
                response.setSubarea(l.getSubArea());
                response.setClassType(l.getClassType());
                response.setProfession(l.getProfession());
                
    			
                responseMobileDb.add(response);
    		});
                          
            
            if (tempMob.size() > 0) {
            	logger.info(messageResourceBundle.getLogMessage("mobileDb.showData.success"));
                target = IConstants.SUCCESS_KEY;
            } else {
            	 logger.info(messageResourceBundle.getLogMessage("mobileDb.showData.failure"));
                target = IConstants.FAILURE_KEY;
                String message ="No data Available with this Mobile Number : " + mobileNumber;
                return new ResponseEntity<>(message,HttpStatus.BAD_REQUEST);

            }
            
            return new ResponseEntity<>(tempMob,HttpStatus.OK);
            
		} catch (Exception e) {
			 logger.error(messageResourceBundle.getLogMessage("mobileDb.showData.exception"), e);
			  throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
		}
        
	}



	

	@Override
	public ResponseEntity<?> updateMobileDataList(UpdateMobileInfo updateMobileInfo, String username) {

		  // Check for null arrays
        if (updateMobileInfo.getMobile_id() == null ||
            updateMobileInfo.getVip() == null ||
            updateMobileInfo.getSex() == null ||
            updateMobileInfo.getAge() == null ||
            updateMobileInfo.getArea() == null ||
            updateMobileInfo.getSubarea() == null ||
            updateMobileInfo.getProfession() == null ||
            updateMobileInfo.getClassType() == null) {
            // Handle the case where any of the arrays is null
        	  logger.info(messageResourceBundle.getLogMessage("mobileDb.updateDataList.nullArrays"));
            return new ResponseEntity<>("One or more arrays are null", HttpStatus.BAD_REQUEST);
        }
       

		 String target = null;
        
                UpdateMobileInfoDto updateMobileInfoDTO = new UpdateMobileInfoDto();
		        MobileDbEntity updateMobileSingle = null;
		        int checkedC = 0;
		        ArrayList<MobileDbEntity> entryList = null;
		        List<MobileDbEntity> updatedList = new ArrayList<>();
		        checkedC = updateMobileInfo.getCheckedC();
		        entryList = new ArrayList<>();  //list
		        
		        ArrayList<UpdateMobileDbResponse> responseList = new ArrayList<>();

		        logger.info(messageResourceBundle.getLogMessage("mobileDb.getUsername"), username);   
		        BeanUtils.copyProperties(updateMobileInfo, updateMobileInfoDTO);	
		        
		        //--------------------
		        int[] mob_id = updateMobileInfoDTO.getMobile_id();
		        String[] vip = updateMobileInfoDTO.getVip();
		        String[] sex = updateMobileInfoDTO.getSex();
		        int[] age = updateMobileInfoDTO.getAge();
		        String[] area = updateMobileInfoDTO.getArea();
		        String[] subarea = updateMobileInfoDTO.getSubarea();
		        String[] profession = updateMobileInfoDTO.getProfession();
		        String[] classType = updateMobileInfoDTO.getClassType();
		        //--------------------
                try {
                    for (int i = 0; i < checkedC; i++) {
    		        	
    		        	MobileDbEntity entry = mobileDbRepo.findById(mob_id[i]).get();
    		        	updateMobileSingle = new MobileDbEntity();
    		        	updateMobileSingle.setMobile_id(mob_id[i]);
    		        	updateMobileSingle.setVip(vip[i]);
    		        	updateMobileSingle.setSex(sex[i]);
    		        	updateMobileSingle.setAge(age[i]);
    		        	updateMobileSingle.setProfession(profession[i]);
    		        	updateMobileSingle.setArea(area[i]);
    		        	updateMobileSingle.setSubArea(subarea[i]);
    		        	updateMobileSingle.setClassType(classType[i]);
    		        	updateMobileSingle.setMobileNumber(entry.getMobileNumber());
    		            entryList.add(updateMobileSingle);
    		        }
    		        updateMobileInfoDTO.setCheckedC(checkedC);
    		        
    		         updatedList = mobileDbRepo.saveAll(entryList);
    		        
    		        
    		        int update_s = updatedList.size();
    		        if (update_s != 0) {
    		        	 logger.info(messageResourceBundle.getLogMessage("mobileDb.updateDataList.success"));
    		            target = IConstants.SUCCESS_KEY;
    		        }
    		        if (update_s == 0) {
    		        	 logger.info(messageResourceBundle.getLogMessage("mobileDb.updateDataList.failure"));
    		            target = IConstants.FAILURE_KEY;
    		        }

				}catch (NoSuchElementException e) {
					  logger.error(messageResourceBundle.getLogMessage("mobileDb.updateDataList.noSuchElementError"));
					throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INVALID_PROVIDED_ID));
				}	 catch (Exception e) {
					 logger.error(messageResourceBundle.getLogMessage("mobileDb.updateDataList.exception"), e);
					throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
				}		       
		    
		       		        
		return new ResponseEntity<>(updatedList,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> deleteMobileDataList(DeleteMobDataRequest mobileData, String username) {

//		 UpdateMobileInfoDto updateMobileInfoDTO = new UpdateMobileInfoDto();
		 ArrayList<MobileDbEntity> entryList = null;
		 MobileDbEntity updateMobileSingle = null;
		    String target = null;
        String messaage ="";
		int delete_s = 0;
       int checkedC = mobileData.getCheckedC();
       logger.info(messageResourceBundle.getLogMessage("mobileDb.deleteDataList.checkedC"), checkedC);
//        BeanUtils.copyProperties(mobileData, updateMobileInfoDTO);
        int[] mob_id = mobileData.getMobile_id();
        entryList = new ArrayList<MobileDbEntity>();
        
        try {
			
        	 for (int i = 0; i < checkedC; i++) {
             	MobileDbEntity entry = mobileDbRepo.findById(mob_id[i]).get();
             	updateMobileSingle = new MobileDbEntity();
             	updateMobileSingle.setMobile_id(mob_id[i]);
                 entryList.add(updateMobileSingle);
             }
        	 
        	 try {
        		 
                mobileDbRepo.deleteAll(entryList);             	 
             	 target = IConstants.SUCCESS_KEY;
             	 logger.info(messageResourceBundle.getLogMessage("mobileDb.deleteDataList.success"));
			} catch (Exception e) {
				 target = IConstants.FAILURE_KEY;
				 logger.error(messageResourceBundle.getLogMessage("mobileDb.deleteDataList.failure"));
				 throw new InternalServerException("Failed to Delete the Entry");
			}
             
             
		}catch (NoSuchElementException e) {
			  logger.error(messageResourceBundle.getLogMessage("mobileDb.deleteDataList.noSuchElementError"));
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INVALID_PROVIDED_ID));
		}catch (ArrayIndexOutOfBoundsException e) {
			 logger.error(messageResourceBundle.getLogMessage("mobileDb.deleteDataList.arrayIndexOutOfBoundsError"));
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.MISMATCH_COUNT_NUMBER));
		}  catch (Exception e) {
		    logger.error(messageResourceBundle.getLogMessage("mobileDb.deleteDataList.exception"), e.getLocalizedMessage());
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
		}
       
        
        
		return new ResponseEntity<>(target,HttpStatus.OK);
	}




	@Override

	public ResponseEntity<?> chooseRequired(ChooseRequest mobileDbRequest, String username) {

		

		ChooseRequestResponse chooseRequestResponse = new ChooseRequestResponse();
	        ArrayList mobileRecordList=new ArrayList();
	        List newList=(List)mobileDbRequest.getNumberList();

	        Collections.shuffle(newList);
	        String target = "";
	        try{

	        MobileDataDto addNewMobileDbDTO = new MobileDataDto();
	        BeanUtils.copyProperties(mobileDbRequest, addNewMobileDbDTO);
	        String smsCount ="";
	        smsCount=addNewMobileDbDTO.getSendNowMsgCount();
	        int smsCount_I =0;
	        smsCount_I=Integer.parseInt(smsCount);
	        logger.info(messageResourceBundle.getLogMessage("mobileDb.chooseRequired.smsCount"), smsCount);
	        Iterator iterator=newList.iterator();
	        String number_list="";
	       
	        	for(int j=0;j<smsCount_I;j++)
		        {
		         if(iterator.hasNext()){
		         number_list=(String)newList.get(j);
		         mobileRecordList.add(number_list);
		        }}  

	        	
	        	chooseRequestResponse.setMobileRecordList(mobileRecordList);
	        	
	            String actionType = addNewMobileDbDTO.getActionReq();
	

	        if (actionType.equalsIgnoreCase("sendnow")) {
	        	target = "sendnow";
	            
	        	chooseRequestResponse.setSmsCount(smsCount);
	        } else if (actionType.equalsIgnoreCase("schedule")) {
	            String actiondo = addNewMobileDbDTO.getActionDo();
	            if (actiondo.equalsIgnoreCase("partial")) {
	                 Collections.shuffle(mobileRecordList);
	                 ArrayList r_num = new ArrayList();
	                Iterator itr=mobileRecordList.iterator();
	                String partDay = addNewMobileDbDTO.getDaysPart();
	                int partDay_I = Integer.parseInt(partDay);
	                int totalSmsParDay = smsCount_I / partDay_I;
	                int remin = smsCount_I % partDay_I;
	                if (remin != 0) 
	                {
	                    partDay_I = partDay_I + 1;
	                }
	                 for (int i = 0; i < smsCount_I; i++) {
	                if(itr.hasNext()){
	                String rand_num = (String) mobileRecordList.get(i);
	                r_num.add(rand_num);
	                     }
	               }
	                 
	                 chooseRequestResponse.setMobileRecord_s(r_num);
	                 chooseRequestResponse.setSmsCount_I(smsCount_I);
	                 chooseRequestResponse.setPartDay_I(partDay_I);     
	                 chooseRequestResponse.setTotalSmsParDay(totalSmsParDay);  
	                
	                 target = "schedulePartial";

	            } else if (actiondo.equalsIgnoreCase("oneTime")) {
	            	chooseRequestResponse.setSmsCount(smsCount);
	                 target = "scheduleOneTime";
	            }
	        }

	    }catch(IndexOutOfBoundsException e)
	    {	 logger.info(messageResourceBundle.getLogMessage("mobileDb.chooseRequired.smsCountOutOfBounds"));
	    	throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.MOBILEDB_SMSCOUNT_OUTOFBOUND));
	    }catch (NumberFormatException e) {
	        logger.error(messageResourceBundle.getLogMessage("mobileDb.chooseRequired.parseError"));
	        throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.NO_VALID_NUMBERS_FOUND_EXCEPTION));
	    } catch(Exception e)
	    {	 logger.info(messageResourceBundle.getLogMessage("mobileDb.chooseRequired.sendNowSmsCountError"));   	
	    	throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.MOBILEDB_MISMATCH_SMSCOUNT));
	    }

	    chooseRequestResponse.setTarget(target);
		return  new ResponseEntity<>(chooseRequestResponse,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> editData(String username) {
	
	    logger.info(messageResourceBundle.getLogMessage("mobileDb.editData.info"));
        
        EditDataResponse editDataResponse = new EditDataResponse();
    
        try {
        	 ArrayList<String> professionList = mobileDbRepo.findDistinctByProfession();
           
             ArrayList<String> areaList = mobileDbRepo.findDistinctByArea();

             ArrayList<String> subAreaList = mobileDbRepo.findDistinctSubareaByAreaIn(null);
             
             Collections.sort(professionList);
             Collections.sort(areaList);
             
       
             
             editDataResponse.setProfessionList(professionList);
             editDataResponse.setProfessionListSize(String.valueOf(professionList.size()));
             editDataResponse.setAreaList(areaList);
             editDataResponse.setAreaListSize(String.valueOf(areaList.size()));
             editDataResponse.setSubAreaList(subAreaList);
             editDataResponse.setSubareaListSize(String.valueOf(subAreaList.size()));
             
             
		} catch (Exception e) {
			   logger.info(messageResourceBundle.getLogMessage("mobileDb.editData.exception"), e);
			throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
		}
      
		return new ResponseEntity<>(editDataResponse,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> getSubArea(String area ,String username) {


		String subAreas="";

        try {
        	logger.info(messageResourceBundle.getLogMessage("mobileDb.getSubArea.info"), area);
//            String area = request.getParameter("q");
            
            if (area != null && area.length() == 0) {
                area = null;
            }
                
            ArrayList<String> subAreaList = mobileDbRepo.findDistinctSubareaByAreaIn(area);
                              
            subAreas = subAreaList.stream().collect(Collectors.joining(","));

            logger.info(messageResourceBundle.getLogMessage("mobileDb.getSubArea.subAreas"), subAreas);
         
            if(subAreaList.isEmpty()) {
            	 logger.info(messageResourceBundle.getLogMessage("mobileDb.getSubArea.subAreaDoesNotExist"));
            	throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.MOBILEDB_SUBAREA_ERROR));
            }

        } catch (Exception ex) {
        	 logger.info(messageResourceBundle.getLogMessage("mobileDb.getSubArea.exception"), ex.getMessage());
           throw new InternalServerException(messageResourceBundle.getExMessage(ConstantMessages.INTERNAL_SERVER_ERROR));
        }       
        
		return new ResponseEntity<>(subAreas,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> mobileBulkUpload(String username) {

		
		
		return null;
	}

	
	
	
	
	
	

//	-------------------------------Separate API------------------------------------------------------------------------------------

	@Override
	public ResponseEntity<?> updateMobileData(UpdateSingleRequest mobileDb, String username) {
		
		List<MobileDbEntity> updatedContact = new ArrayList<>();
		try {
			
			MobileDbEntity mobileDbEntity = new MobileDbEntity();
			mobileDbEntity.setMobileNumber(mobileDb.getMobileNumber());
			mobileDbEntity.setSex(mobileDb.getSex());
			mobileDbEntity.setAge(mobileDb.getAge());
			mobileDbEntity.setVip(mobileDb.getVip());
			mobileDbEntity.setArea(mobileDb.getArea());
			mobileDbEntity.setClassType(mobileDb.getClassType());
			mobileDbEntity.setProfession(mobileDb.getProfession());
			mobileDbEntity.setSubArea(mobileDb.getSubarea());
			
			String mobileNumber = mobileDb.getMobileNumber();
			String sex = mobileDb.getSex();
			int age = mobileDb.getAge();
			String vip = mobileDb.getVip();
			String area = mobileDb.getArea();
			String classType = mobileDb.getClassType();
			String profession = mobileDb.getProfession();
			String subArea = mobileDb.getSubarea();
		    
		    
			mobileDbRepo.updateByMobileNumber(mobileNumber, sex, age,vip, area, classType, profession, subArea);
		
		    updatedContact = mobileDbRepo.findByMobileNumber(mobileNumber);
			
		} catch (Exception e) {
		  throw new InternalServerException("Something Went Wrong");
		}
	
		return new ResponseEntity<>(updatedContact,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> deleteMobileData(String mobNumber, String username) {

		 String Message ="";
		 
		try {
			

		       String mobileNumber = mobNumber;

		       System.out.println(mobileNumber);
		       
		       int response = mobileDbRepo.deleteByMobileNumber(mobileNumber);
		       System.out.println(response);
		       if(response>0) {
		    	   Message = "Contact Deleted Successfully";
		       }else {
		    	   Message = "Can Not Delete Contact";
		       }
			
		} catch (Exception e) {
			  throw new InternalServerException("Something Went Wrong");
		}
	  
		
		return new ResponseEntity<>(Message,HttpStatus.OK);
	}

	
	
}
