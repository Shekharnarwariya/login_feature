package com.hti.smpp.common.services.Impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.dto.MobileDataDto;
import com.hti.smpp.common.dto.UpdateMobileInfoDto;
import com.hti.smpp.common.dto.UpdateMobileInfoDtoSingle;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.mobileDb.dto.MobileDbEntity;
import com.hti.smpp.common.mobileDb.repository.MobileDbRepo;
import com.hti.smpp.common.request.MobileDbRequest;
import com.hti.smpp.common.request.UpdateMobileInfo;
import com.hti.smpp.common.response.ChooseRequestResponse;
import com.hti.smpp.common.response.EditDataResponse;
import com.hti.smpp.common.response.MobileDbResponse;
import com.hti.smpp.common.response.UpdateMobileDbResponse;
import com.hti.smpp.common.services.MobileDbService;
import com.hti.smpp.common.util.IConstants;
//import com.hti.smpp.common.util.IConstants;

@Service
public class MobileDbServiceImpl implements MobileDbService {

	private final Logger logger = LoggerFactory.getLogger(MobileDbServiceImpl.class);
	
	@Autowired
	private MobileDbRepo mobileDbRepo;

	@Override
	public ResponseEntity<?> addMobileData(String mobileDb, MultipartFile file, String username) {
		
		logger.info("add mobile data method");
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

            logger.info("Add Mobile DTO get Age-----------> "+addNewMobileDbDTO.getAge());
            
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
                            } catch (Exception e) {
                                System.out.println("Exception in Uploading number :" + e);
                            }
                        }

                    } catch (Exception ex) {
                        System.out.println("Error In Saving Multiple Conacts From List :" + ex);

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
                 
            logger.info("Moble user object ------------> "+mobileUserdata);
            
            
             isInsert = mobileDbRepo.saveAll(mobileUserdataEntry);
      
            if (!isInsert.isEmpty()) {
                target = IConstants.SUCCESS_KEY;
            } else {
                target = IConstants.FAILURE_KEY;
            }
        } catch (Exception e) {
        	logger.error("Failure in Upload the contact List-------------->"+e.getLocalizedMessage());
        	throw new InternalServerException("Something went Wrong"); 
        }

		return new ResponseEntity<>(isInsert,HttpStatus.CREATED);
	}

	
	
	
	@Override
	public ResponseEntity<?> showMobileData(MobileDbRequest mobileDb, String username) {


		String target = "";
        String mobileNumber="";

        try {
			
        	MobileDataDto addNewMobileDbDTO = new MobileDataDto();
            BeanUtils.copyProperties(mobileDb, addNewMobileDbDTO);
            
            mobileNumber = addNewMobileDbDTO.getMobileNumber();
            
            logger.info("MOBILE------------>"+mobileNumber);
            
            
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
                          
//            request.setAttribute("moNumber", mobileNumber);
            logger.info("mobileNumber--->"+tempMob);
            
            if (tempMob.size() > 0) {
            	 logger.info("MOBILE------------------>>"+mobileNumber);
                target = IConstants.SUCCESS_KEY;
            } else {
                target = IConstants.FAILURE_KEY;
                return new ResponseEntity<>(target,HttpStatus.OK);
            }
            
            return new ResponseEntity<>(tempMob,HttpStatus.OK);
            
		} catch (Exception e) {
			logger.info("Cannot find the mobile number in database"+e.getLocalizedMessage());
			  throw new InternalServerException("Something Went Wrong");
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

		     	        
		        BeanUtils.copyProperties(updateMobileInfo, updateMobileInfoDTO);
		        logger.info("User Mobile get Mobile ID------->"+updateMobileInfo.getMobile_id());
//		        System.out.println(updateMobileInfoDTO.getMobile_id());
		        
		        //--------------------
		        int[] mob_id = updateMobileInfoDTO.getMobile_id();
		        String[] vip = updateMobileInfoDTO.getVip();
		        String[] sex = updateMobileInfoDTO.getSex();
		        int[] age = updateMobileInfoDTO.getAge();
		        String[] area = updateMobileInfoDTO.getArea();
		        String[] subarea = updateMobileInfoDTO.getSubarea();
		        String[] profession = updateMobileInfoDTO.getProfession();
		        String[] classType = updateMobileInfoDTO.getClassType();
		        System.out.println("classtype"+classType);
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
    		            logger.info("param_value", update_s + "");
    		            target = IConstants.SUCCESS_KEY;
    		        }
    		        if (update_s == 0) {
    		            logger.info("param_value", update_s + "");
    		            target = IConstants.FAILURE_KEY;
    		        }

				}catch (NoSuchElementException e) {
					logger.error("NO Such Element Error occured");
					throw new InternalServerException("Entry with given id is not present");
				}	 catch (Exception e) {
					throw new InternalServerException("Something went Wrong");
				}		       
		    
		       		        
		return new ResponseEntity<>(updatedList,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> deleteMobileDataList(UpdateMobileInfo mobileData, String username) {

		 UpdateMobileInfoDto updateMobileInfoDTO = new UpdateMobileInfoDto();
		 ArrayList<MobileDbEntity> entryList = null;
		 MobileDbEntity updateMobileSingle = null;
		    String target = null;
        String messaage ="";
		int delete_s = 0;
       int checkedC = mobileData.getCheckedC();
        logger.info("Checked Counts--------->"+checkedC);
        BeanUtils.copyProperties(mobileData, updateMobileInfoDTO);
        int[] mob_id = updateMobileInfoDTO.getMobile_id();
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
			} catch (Exception e) {
				 target = IConstants.FAILURE_KEY;
				 throw new InternalServerException("Failed to Delete the Entry");
			}
             
             
		}catch (NoSuchElementException e) {
			throw new InternalServerException("Entry with given ID not present");
		}catch (ArrayIndexOutOfBoundsException e) {
			throw new InternalServerException("Count of entry mobile ID and Check_Count Does not match");
		}  catch (Exception e) {
			logger.error("ERROR ---------->" + e.getLocalizedMessage());
			throw new InternalServerException("Something Went Wrong");
		}
       
        
        
		return new ResponseEntity<>(target,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> chooseRequired(MobileDbRequest mobileDbRequest, String username) {
		

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
	        logger.info("SMS Count - "+ smsCount);
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
	    {	 logger.info("error",e.getLocalizedMessage());
	    	throw new InternalServerException("SMS count is out of bound");
	    }catch (NumberFormatException e) {
	        logger.error("Error parsing integer", e);
	        throw new InternalServerException("Error parsing integer");
	    } catch(Exception e)
	    {	 logger.info("error",e.getLocalizedMessage());   	
	    	throw new InternalServerException("Send Now Sms Count is more than number list count");
	    }

	    chooseRequestResponse.setTarget(target);
		return  new ResponseEntity<>(chooseRequestResponse,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> editData(String username) {
	
		
        logger.info("<-- EditDataAction Called -> ");
        
        EditDataResponse editDataResponse = new EditDataResponse();
    
        try {
        	 ArrayList<String> professionList = mobileDbRepo.findDistinctByProfession();
           
             ArrayList<String> areaList = mobileDbRepo.findDistinctByArea();
             System.out.println(areaList);
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
			logger.info("Error occurred",e);
			throw new InternalServerException("Can not Process your request");
		}
      
		return new ResponseEntity<>(editDataResponse,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> getSubArea(String area ,String username) {


		String subAreas="";

        try {
        	 logger.info("Area - "+ area);
//            String area = request.getParameter("q");
            
            if (area != null && area.length() == 0) {
                area = null;
            }
                
            ArrayList<String> subAreaList = mobileDbRepo.findDistinctSubareaByAreaIn(area);
                              
            subAreas = subAreaList.stream().collect(Collectors.joining(","));

            logger.info("Sub Areas :: " + subAreas);
         
            if(subAreaList.isEmpty()) {
            	logger.info("SubArea Does not exists for the given area");
            	throw new InternalServerException("Subarea Does not exists for the given area");
            }

        } catch (Exception ex) {
        	logger.info(ex.getMessage());
           throw new InternalServerException(ex.getLocalizedMessage());
        }       
        
		return new ResponseEntity<>(subAreas,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> mobileBulkUpload(String username) {

		
		
		return null;
	}

	
	
	
	
	
	

//	-------------------------------Separate API------------------------------------------------------------------------------------

	@Override
	public ResponseEntity<?> updateMobileData(MobileDbRequest mobileDb, String username) {
		
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
	public ResponseEntity<?> deleteMobileData(MobileDbRequest mobileData, String username) {

		 String Message ="";
		 
		try {
			
		       String mobileNumber = mobileData.getMobileNumber();
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
