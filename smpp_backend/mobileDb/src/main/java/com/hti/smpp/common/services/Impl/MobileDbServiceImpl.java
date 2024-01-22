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
import com.hti.smpp.common.mobileDb.dto.MobileDbEntity;
import com.hti.smpp.common.mobileDb.repository.MobileDbRepo;
import com.hti.smpp.common.request.MobileDbRequest;
import com.hti.smpp.common.request.UpdateMobileInfo;
import com.hti.smpp.common.response.MobileDbResponse;
import com.hti.smpp.common.services.MobileDbService;
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
//        ActionMessages messages = null;
//        messages = new ActionMessages();
//        ActionMessage message = null;
        
        List<MobileDbEntity> isInsert = null;
        MobileDataDto form;
        try {
//            AddNewMobileDbForm addNewMobileDbForm = (AddNewMobileDbForm) actionForm;
            MobileDataDto addNewMobileDbDTO = new MobileDataDto();
            MobileDbEntity mobileDbEntity = new MobileDbEntity();
            MobileDataDto tempMobileDbDTO = null;
            
            ArrayList<MobileDataDto> mobileUserdata = new ArrayList<>();
            ArrayList<MobileDbEntity> mobileUserdataEntry = new ArrayList<>();
            
//            ObjectMapper objectMapper = new ObjectMapper();
//            form= objectMapper.readValue(mobileDb, MobileDataDto.class);
//            
//            BeanUtils.copyProperties(form,addNewMobileDbDTO );
//
//            String type = addNewMobileDbDTO.getListType();

            System.out.println(addNewMobileDbDTO.getAge());
            
            String format = "";
            if (file != null && file.getName().length() > 0) {
                MultipartFile uploadedFile = file;
               
                String file_name = uploadedFile.getOriginalFilename();
                if (file_name.indexOf(".xlsx") > 0) {
                    format = " : Excel(.xlsx)";
//                    message = new ActionMessage("message.FileFormatError");
//                    messages.add(ActionMessages.GLOBAL_MESSAGE, message);
//                    saveMessages(request, messages);
//                    request.setAttribute("param_value", format);
//                    target = IConstants.FAILURE_KEY;
//                    return mapping.findForward(target);
                    
                    
                } else if (file_name.indexOf(".txt") > 0) {
                    format = "Text";
                } else if (file_name.indexOf(".xls") > 0) {
                    format = "Excel";
                } else {
                    format = "FormatNotSupported";
//                    message = new ActionMessage("message.FileFormatError");
//                    messages.add(ActionMessages.GLOBAL_MESSAGE, message);
//                    saveMessages(request, messages);
//                    target = IConstants.FAILURE_KEY;
//                    return mapping.findForward(target);
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
//                    target = IConstants.FAILURE_KEY;
//                    message = new ActionMessage("error.processError");
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
//                 IMobileDBServices mobileDbServc = new MobileDBServices();
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

//            boolean isInsert = mobileDbServc.insertMobileDataList(mobileUserdata);
                 
            System.out.println(mobileUserdata);
            System.out.println("12");
            
            
             isInsert = mobileDbRepo.saveAll(mobileUserdataEntry);
      
            if (!isInsert.isEmpty()) {
//                target = IConstants.SUCCESS_KEY;
//                message = new ActionMessage("message.operation.success");
            } else {
//                target = IConstants.FAILURE_KEY;
//                message = new ActionMessage("error.processError");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        messages.add(ActionMessages.GLOBAL_MESSAGE, message);
//        saveMessages(request, messages);
//        return mapping.findForward(target);
		return new ResponseEntity<>(isInsert,HttpStatus.CREATED);
	}

	
	
	
	@Override
	public ResponseEntity<?> showMobileData(MobileDbRequest mobileDb, String username) {


		String target = "";
        String mobileNumber="";
//        AddNewMobileDbForm addNewMobileDbForm = (AddNewMobileDbForm) actionForm;
        MobileDataDto addNewMobileDbDTO = new MobileDataDto();
        BeanUtils.copyProperties(mobileDb, addNewMobileDbDTO);
        
        mobileNumber = addNewMobileDbDTO.getMobileNumber();
        
        System.out.println("MOBILE>>>>>"+mobileNumber);
        
//        IMobileDBServices mobileDbServc = new MobileDBServices();
        
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
        
        
        
        
//        request.setAttribute("moNumber", mobileNumber);
//        System.out.println("mobileNumber--->"+mobileNumber);
        if (tempMob.size() > 0) {
//            request.setAttribute("mobinfo", tempMob);
        	 System.out.println("MOBILE>>>>>"+mobileNumber);
//            target = IConstants.SUCCESS_KEY;
        } else {
//            target = IConstants.FAILURE_KEY;
        }
//        return mapping.findForward(target);
        
        
		return new ResponseEntity<>(tempMob,HttpStatus.OK);
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
            updateMobileInfo.getProfession() == null ) {
            // Handle the case where any of the arrays is null
            return new ResponseEntity<>("One or more arrays are null", HttpStatus.BAD_REQUEST);
        }
       

//		 String target = null;
//		    MobileDBServices mobileDBServices = new MobileDBServices();
//		    MobileDataDto addNewMobileDbDTO = new MobileDataDto();
//		    ActionMessages msg = null;
//		    ActionMessage message = null;
        
                UpdateMobileInfoDto updateMobileInfoDTO = new UpdateMobileInfoDto();
		        MobileDbEntity updateMobileSingle = null;
		        int checkedC = 0;
		        ArrayList<MobileDbEntity> entryList = null;
		    
		        checkedC = updateMobileInfo.getCheckedC();
		        entryList = new ArrayList<>();  //list
//		        UpdateMobileInfoForm updateMobileInfoForm = (UpdateMobileInfoForm) form;
		       
		        System.out.println(updateMobileInfo.getMobile_id());
		        
		        BeanUtils.copyProperties(updateMobileInfo, updateMobileInfoDTO);
		        System.out.println(updateMobileInfo.getMobile_id());
		        System.out.println(updateMobileInfoDTO.getMobile_id());
		        
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
		        
		        for (int i = 0; i < checkedC; i++) {
		        	updateMobileSingle = new MobileDbEntity();
		        	updateMobileSingle.setMobile_id(mob_id[i]);
		        	updateMobileSingle.setVip(vip[i]);
		        	updateMobileSingle.setSex(sex[i]);
		        	updateMobileSingle.setAge(age[i]);
		        	updateMobileSingle.setProfession(profession[i]);
		        	updateMobileSingle.setArea(area[i]);
		        	updateMobileSingle.setSubArea(subarea[i]);
		        	updateMobileSingle.setClassType(classType[i]);
		            entryList.add(updateMobileSingle);
		        }
		        updateMobileInfoDTO.setCheckedC(checkedC);
		        
		        List<MobileDbEntity> updatedList = mobileDbRepo.saveAll(entryList);
//		        int update_s = mobileDBServices.UpdateMobileInfo(entryList);
		        
		        int update_s = updatedList.size();
		        if (update_s != 0) {
//		            msg = new ActionMessages();
//		            message = new ActionMessage("message.DBUpdateSuccess");
//		            msg.add("param_message", message);
//		            saveMessages(request, msg);
//		            request.setAttribute("param_value", update_s + "");
//		            target = IConstants.SUCCESS_KEY;
		        }
		        if (update_s == 0) {
//		            msg = new ActionMessages();
//		            message = new ActionMessage("message.DBUpdateSuccess");
//		            msg.add("param_message", message);
//		            saveMessages(request, msg);
//		            request.setAttribute("param_value", update_s + "");
//		            target = IConstants.FAILURE_KEY;
		        }
//		        return mapping.findForward(target);
		        
		        
		return new ResponseEntity<>(updatedList,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> deleteMobileDataList(UpdateMobileInfo mobileData, String username) {

		 UpdateMobileInfoDto updateMobileInfoDTO = new UpdateMobileInfoDto();
		 ArrayList<MobileDbEntity> entryList = null;
		 MobileDbEntity updateMobileSingle = null;
		 
		 
		int delete_s = 0;
       int checkedC = mobileData.getCheckedC();
//        UpdateMobileInfoForm updateMobileInfoForm = (UpdateMobileInfoForm) form;
        BeanUtils.copyProperties(mobileData, updateMobileInfoDTO);
        int[] mob_id = updateMobileInfoDTO.getMobile_id();
        entryList = new ArrayList<MobileDbEntity>();
        for (int i = 0; i < checkedC; i++) {
        	updateMobileSingle = new MobileDbEntity();
        	updateMobileSingle.setMobile_id(mob_id[i]);
            entryList.add(updateMobileSingle);
        }
        
        try {
        	 mobileDbRepo.deleteAll(entryList);
		} catch (Exception e) {
		   System.out.println(e);
		}
        
        if (delete_s != 0) {
//            msg = new ActionMessages();
//            message = new ActionMessage("message.DBDeleteSuccess");
//            msg.add("param_message", message);
//            saveMessages(request, msg);
//            request.setAttribute("param_value", delete_s + "");
//            target = IConstants.SUCCESS_KEY;
        }
        if (delete_s == 0) {
//            msg = new ActionMessages();
//            message = new ActionMessage("message.DBDeleteSuccess");
//            msg.add("param_message", message);
//            saveMessages(request, msg);
//            request.setAttribute("param_value", delete_s + "");
//            target = IConstants.FAILURE_KEY;
        }
//        return mapping.findForward(target);
        
        
		return new ResponseEntity<>(delete_s,HttpStatus.OK);
	}



	
	



	@Override
	public ResponseEntity<?> chooseRequired(MobileDbRequest mobileDbRequest, String username) {


//		  HttpSession session = request.getSession(false);
	        ArrayList mobileRecordList=new ArrayList();
	        List newList=(List)mobileDbRequest.getNumberList();
//	        session.removeAttribute("numberList");
	        Collections.shuffle(newList);
	        String target = "";
	        try{
//	        AddNewMobileDbForm addNewMobileDbForm = (AddNewMobileDbForm) actionForm;
	        MobileDataDto addNewMobileDbDTO = new MobileDataDto();
	        BeanUtils.copyProperties(mobileDbRequest, addNewMobileDbDTO);
	        String smsCount ="";
	        smsCount=addNewMobileDbDTO.getSendNowMsgCount();
	        int smsCount_I =0;
	        smsCount_I=Integer.parseInt(smsCount);
	        Iterator iterator=newList.iterator();
	        String number_list="";
	        for(int j=0;j<smsCount_I;j++)
	        {
	         if(iterator.hasNext()){
	         number_list=(String)newList.get(j);
	         mobileRecordList.add(number_list);
	        }}
	     
//	        session.setAttribute("mobileRecord",mobileRecordList);
	        String actionType = addNewMobileDbDTO.getActionReq();

	        if (actionType.equalsIgnoreCase("sendnow")) {
	            target = "sendnow";
//	            request.setAttribute("smscounts", smsCount);
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
//	                session.setAttribute("mobileRecord",r_num);
//	                request.setAttribute("smscounts", smsCount_I+"");
//	                session.setAttribute("smscount_s",smsCount_I+"");
//	                request.setAttribute("partDay", partDay_I+"");
//	                session.setAttribute("partDay_s", partDay_I+"");
//	                request.setAttribute("temp_list_size",smsCount_I+"");
//	                request.setAttribute("totalSmsParDay", "" + totalSmsParDay);
//	                session.setAttribute("totalSmsParDay", "" + totalSmsParDay);
//	                target = "schedulePartial";

	            } else if (actiondo.equalsIgnoreCase("oneTime")) {
//	                request.setAttribute("smscounts", smsCount);
	                target = "scheduleOneTime";
	            }
	        }

	    }catch(Exception e)
	    {
	   System.out.println("Exception e :===>"+e);
	    }
//	        return mapping.findForward(target);
	        
	        
	        
		
		return null;
	}




	@Override
	public ResponseEntity<?> editData(String username) {
		// TODO Auto-generated method stub
		
		

//        String target="";
        System.out.println("<-- EditDataAction Called -> ");
//         HttpSession session = request.getSession(false);
//        MobileDBServices mobileDBServices = new MobileDBServices();
        
        
//        ArrayList professionList = mobileDBServices.getProfessionList();
        
        ArrayList<String> professionList = mobileDbRepo.findDistinctByProfession();
        
//        ArrayList areaList=mobileDBServices.getAreaList();
        
        ArrayList<String> areaList = mobileDbRepo.findDistinctByArea();
        
//        ArrayList subAreaList=mobileDBServices.getSubAreaList(null);
        
        ArrayList<String> subAreaList = mobileDbRepo.findDistinctSubareaByAreaIn(null);
        
        Collections.sort(professionList);
        Collections.sort(areaList);
//        request.setAttribute("professionList",professionList);
//        request.setAttribute("professionListSize", professionList.size()+"");
//        request.setAttribute("areaList", areaList);
//        request.setAttribute("areaListSize", areaList.size()+"");
//        request.setAttribute("subareaList", subAreaList);
//        request.setAttribute("subareaListSize", subAreaList.size()+"");
//        target = IConstants.SUCCESS_KEY;
//        return mapping.findForward(target);
        
        
    
        HashMap<String, Object> attributeMap = new HashMap<>();
        
        attributeMap.put("professionList", professionList);
        attributeMap.put("professionListSize", String.valueOf(professionList.size()));
        attributeMap.put("areaList", areaList);
        attributeMap.put("areaListSize", String.valueOf(areaList.size()));
        attributeMap.put("subareaList", subAreaList);
        attributeMap.put("subareaListSize", String.valueOf(subAreaList.size()));
        
        
        
        
        
		return new ResponseEntity<>(attributeMap,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> getSubArea(String area ,String username) {


		String subAreas="";
//        StringBuffer strBuff = new StringBuffer();
//        PrintWriter printw = response.getWriter();
//        IDatabaseService dbservice = HtiSmsDB.getInstance();
        try {

//            String area = request.getParameter("q");
            
            if (area != null && area.length() == 0) {
                area = null;
            }
            
            
            ArrayList<String> subAreaList = mobileDbRepo.findDistinctSubareaByAreaIn(area);
            
//            Iterator itr = sublist.iterator();
//            while (itr.hasNext()) {
//                String str = (String) itr.next();
//                strBuff.append(str).append(",");
//            }
//            String finalstr = strBuff.toString();
//            finalstr = finalstr.substring(0, finalstr.length() - 1);
//
//            printw.print(finalstr);
//            printw.flush();
//            printw.close();
//            System.out.println("Sub Areas :: "+finalstr);
            
            
            subAreas = subAreaList.stream().collect(Collectors.joining(","));

            System.out.println("Sub Areas :: " + subAreas);

            
            

        } catch (Exception ex) {
            ex.printStackTrace();
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
	
		List<MobileDbEntity> updatedContact = mobileDbRepo.findByMobileNumber(mobileNumber);
		
		
		return new ResponseEntity<>(updatedContact,HttpStatus.OK);
	}




	@Override
	public ResponseEntity<?> deleteMobileData(MobileDbRequest mobileData, String username) {

	   String Message ="";
       String mobileNumber = mobileData.getMobileNumber();
       System.out.println(mobileNumber);
       
       int response = mobileDbRepo.deleteByMobileNumber(mobileNumber);
       System.out.println(response);
       if(response>0) {
    	   Message = "Contact Deleted Successfully";
       }else {
    	   Message = "Can Not Delete Contact";
       }
		
		return new ResponseEntity<>(Message,HttpStatus.OK);
	}

	
	
}
