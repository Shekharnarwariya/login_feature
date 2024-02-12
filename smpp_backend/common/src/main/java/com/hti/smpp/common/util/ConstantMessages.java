package com.hti.smpp.common.util;

public class ConstantMessages {
    public static final String AUTHENTICATION_FAILED_USERNAME = "authentication.failed.username";
    public static final String AUTHENTICATION_FAILED_PASSWORD = "authentication.failed.password";
    public static final String INTERNAL_SERVER_ERROR = "internal.server.error";
    public static final String OTP_EXPIRED = "otp.expired";
    public static final String INVALID_OTP = "invalid.otp";
    public static final String NOT_FOUND = "not.found";
    public static final String ROLE_NOT_FOUND_ERROR = "role.not.found.error";
    public static final String PROFESSION_ENTRY_ERROR = "profession.entry.error";

    
    public static final String INVALID_OLD_PASSWORD = "invalid.old.password";
    public static final String UNAUTHORIZED_OPERATION = "unauthorized.operation";
    public static final String TEMPLATE_NOT_FOUND = "template.not.found";
    public static final String TEMPLATE_CREATION_ERROR = "template.creation.error";
    public static final String TEMPLATE_UPDATE_ERROR = "template.update.error";
    public static final String TEMPLATE_DELETE_ERROR = "template.delete.error";
    public static final String RECENT_USE_TEMPLATE_ERROR = "recent.use.template.error";
    
    public static final String USER_NOT_FOUND = "user.not.found";
    
    public static final String UNAUTHORIZED_EXCEPTION = "exception.unauthorized";
    public static final String SCHEDULED_TIME_BEFORE_CURRENT_TIME_EXCEPTION = "exception.scheduledTimeBeforeCurrentTime";
    public static final String INSUFFICIENT_BALANCE_EXCEPTION = "exception.insufficientBalance";
    public static final String INSUFFICIENT_ADMIN_BALANCE_EXCEPTION = "exception.insufficientAdminBalance";
    public static final String SINGLE_SCHEDULE_ERROR_EXCEPTION = "exception.singleScheduleError";
    public static final String DUPLICATE_SCHEDULE_ERROR_EXCEPTION = "exception.duplicateScheduleError";
    public static final String HOST_CONNECTION_ERROR_EXCEPTION = "exception.hostConnectionError";
    public static final String SMS_ERROR_EXCEPTION = "exception.smsError";
    public static final String INSUFFICIENT_WALLET_BALANCE_EXCEPTION = "exception.insufficientWalletBalance";
    public static final String NO_VALID_NUMBERS_FOUND_EXCEPTION = "exception.noValidNumbersFound";
    public static final String INSUFFICIENT_CREDITS_EXCEPTION = "exception.insufficientCredits";
    public static final String INSUFFICIENT_ADMIN_CREDITS_EXCEPTION = "exception.insufficientAdminCredits";
    public static final String INSUFFICIENT_CREDITS_OPERATION_EXCEPTION = "exception.insufficientCreditsOperation";
    public static final String INSUFFICIENT_WALLET_BALANCE_TRANSACTION_EXCEPTION = "exception.insufficientWalletBalanceTransaction";
    public static final String ERROR_ADDING_TO_SUMMARY_REPORT_EXCEPTION = "exception.errorAddingToSummaryReport";
    public static final String DRIVE_INFO_NOT_FOUND_EXCEPTION = "DRIVE_INFO_NOT_FOUND_EXCEPTION";
    public static final String SCHEDULER_ERROR_EXCEPTION = "SCHEDULER_ERROR_EXCEPTION";
    public static final String BULK_SMS_ERROR_EXCEPTION = "BULK_SMS_ERROR_EXCEPTION";
    public static final String BATCH_SUBMISSION_ERROR_EXCEPTION = "BATCH_SUBMISSION_ERROR_EXCEPTION";
    public static final String INVALID_FILE_FORMAT_EXCEPTION = "INVALID_FILE_FORMAT_EXCEPTION";
    public static final String NO_VALID_NUMBERS_FOUND_FILE_EXCEPTION = "NO_VALID_NUMBERS_FOUND_FILE_EXCEPTION";
    public static final String INVALID_BATCH_FINISH_ALERT_NUMBER_MESSAGE = "invalid.batch.finish.alert.number.message";
    public static final String INTERNAL_SERVER_ERROR_INSIDE_BATCH_PROCESS = "internal.server.error.inside.batch.process.message";
    public static final String WEB_MENU_ACCESS_NOT_FOUND = "web.menu.access.not.found.message";
    public static final String NO_BATCHES_AVAILABLE_FOR_PROCESSING = "no.batches.available.for.processing.message";
    public static final String SCHEDULE_NOT_FOUND = "schedule.not.found.message";
    public static final String SCHEDULE_REMOVAL_FAILURE = "schedule.removal.failure.message";
    public static final String DELETE_SCHEDULE_FILE_EXCEPTION = "delete.schedule.file.exception.message";
    public static final String BALANCE_INFO_NOT_FOUND_EXCEPTION = "balance.info.not.found.exception.message";
    
    //mobiledb exception message
    public static final String INSUFFICIENT_DATA_VALUES = "exception.insufficientData.message";
    public static final String INVALID_PROVIDED_ID = "exception.invalidId.message";
    public static final String MISMATCH_COUNT_NUMBER = "exception.missMatchCountNo.message";
    public static final String MOBILEDB_SMSCOUNT_OUTOFBOUND = "exception.mobiledb.smscount.message";
    public static final String MOBILEDB_MISMATCH_SMSCOUNT = "exception.mobiledb.misMatchsmscount.message";
    public static final String MOBILEDB_SUBAREA_ERROR = "exception.mobiledb.subAreaError.message";
    public static final String MOBILEDB_AREAWISECOUNT_ERROR = "exception.mobiledb.areaWiseCount.message";
    
    //bsfm exception message
    public static final String EXCEPTION_MSG = "bsfm.msg.error";
    public static final String DUPLICATE_MSG = "bsfm.duplicate.error";
    public static final String ADD_EXCEPTION = "bsfm.add.msg.error";
    public static final String PROFILE_NOT_FOUND = "bsfm.profile.notfound";
    public static final String INVALID_BSFM_USER = "bsfm.user.invalid";
    public static final String BSFM_UPDATE_FAILURE="bsfm.update.failed";
    public static final String BSFM_NO_USER_SELECTED = "bsfm.no.user";
    public static final String BSFM_DUPLICATE_PROFILE="bsfm.duplicate.profilename";
    public static final String BSFM_DELETE_FAILED="bsfm.delete.failed";
    
    //bsfm success message
    public static final String ADD_SUCCESS_BSFM = "bsfm.success.add";
    public static final String UPDATE_SUCCESS_BSFM = "bsfm.success.update";
    public static final String DELETE_SUCCESS_BSFM="bsfm.success.delete";
    public static final String UPDATE_SUCCESS_BSFM_FLAG="bsfm.success.updateFlag";
     
    //sales exception message
    public static final String SALES_USER_EXIST = "sales.user.exist";
    public static final String SALES_ADD_FAILED = "sales.add.failure";
    public static final String SALES_DUPLICATE_USER = "sales.warn.duplicateUsername";
    public static final String SALES_ENTRY_NOTFOUND = "sales.entry.notfound";
    public static final String SALES_UPDATE_FAILED = "sales.update.failure";
    public static final String SALES_MSG_ERROR = "sales.error.msg";
    public static final String SALES_NOTFOUND = "sales.not.found";
    public static final String SALES_NOEXECUTIVE = "sales.msg.noexecutive";
    //sales success message
    public static final String SALES_ADD_SUCCESS = "sales.success.add";
    public static final String SALES_UPDATE_SUCCESS = "sales.success.update";
    public static final String SALES_DELETED_SUCCESS = "sales.success.delete";
    
    //Json Processing Exception
    public static final String JSON_PROCESSING_ERROR = "process.json.error";
    

    //subscription exception message
    public static final String SUBSCRIPTION_ADD_ERROR = "subscription.save.failure";
    public static final String SUBSCRIPTION_DUPLICATE_ENTRY = "subscription.entry.duplicate";
    public static final String SUBSCRIPTION_MSG_ERROR = "subscription.msg.error";
    public static final String SUBSCRIPTION_MSG_NOTFOUND = "subscription.msg.notfound";
    
    //subscription success message
    public static final String SUBSCRIPTION_ADD_SUCCESS = "subscription.add.success";
    public static final String SUBSCRIPTION_UPDATE_SUCCESS = "subscription.update.success";
    public static final String SUBSCRIPTION_DELETE_SUCCESS = "subscription.delete.success";
    
    //dlt exception
    public static final String DUPLICATE_ENTRY_FOUND="dlt.duplicate.entry";
    public static final String FAILED_TO_ADD_ENTRY="dlt.failed.addEntry";
    public static final String DLT_DATA_ACCESS_SUCC="dlt.dataAccess.success";
    public static final String DLT_ERROR_JSON="dlt.error.json";
    public static final String DLT_SUCC_ADD="dlt.sccess.add";
    public static final String DLT_REQ_UNSUCC="dlt.request.unsucc";
    public static final String DLT_DUPLI_ENTY="dlt.dupli.entry";
    public static final String DLT_ERR_UNSUCC="dlt.err.unsucc";
    public static final String DLT_TEMP_EMPTY="dlt.temp.empty";
    public static final String DLT_NOT_FOUND_ERROR="dlt.notFound.error";
    public static final String DLT_UPDATE_SUCCESS="dlt.success.update";
    public static final String DLT_ENTRY_NOTFOUND="dlt.entry.notfound";
    public static final String DLT_TEMP_UPDATE_SUCCES="dlt.temp.update.success";
    public static final String DLT_TEMP_NOTFOUND="dlt.temp.notfound";
    public static final String DLT_DELETE_ENTRY="dlt.deleted.success";
    public static final String DLT_RESOURCE_NOTFOUND="dlt.resource.notfound";
    public static final String DLT_TEMP_DELETED="dlt.temp.deleted.success";
    
    
    //network exception
    public static final String NETWORK_SELECT_SINGLE = "network.not.single";
    public static final String NETWORK_FILE_NOTSELECTED = "network.file.notselected";
    public static final String NETWORK_MSG_ERROR = "network.msg.error";
    public static final String NETWORK_RECORD_UNAVAILABLE = "network.record.notfound";
    public static final String NETWORK_NOT_FOUND = "network.entry.notfound";
    public static final String NETWORK_DELETE_FAILED = "network.delete.failure";
    public static final String NETWORK_MCCMNC_NOTFOUND = "network.mccdata.notfound";
    public static final String NETWORK_NOMAP_RECORD = "network.map.notfound";
    public static final String NETWORK_DATA_EMPTY = "network.data.notfound";
    public static final String NETWORK_MCC_NOTFOUND = "network.mcc.notfound";
    public static final String NETWORK_FETCHMCC_FAILURE = "network.failure.mcc";
    public static final String NETWORK_MNC_NOTFOUND = "network.mnc.notfound";
    public static final String NETWORK_FETCHMNC_FAILURE = "network.failure.mnc";
    
    //network success messages
    public static final String NETWORK_UPDATE_SUCCESS = "network.update.success";
    public static final String NETWORK_DELETE_SUCCESS = "network.delete.success";
    
    //addressbook exception
    public static final String ADDBOOK_UPLOAD_FAILED = "addbook.upload.failed";
    public static final String ADDBOOK_ERROR_MSG = "addbook.msg.error";
    public static final String WORKBOOK_PROCESSING_ERROR = "addbook.workbook.process.error";
    public static final String ADDBOOK_NUMBER_NOT_FOUND_ERROR = "addbook.number.notfound";
    public static final String ADDBOOK_EMPTY_DATASET = "addbook.empty.dataset";
    public static final String NOT_FOUND_TEMPLATES_ERROR = "addbook.not.found.templates.error";
    public static final String NOT_FOUND_WEBMASTER_ERROR = "addbook.not.found.webmaster.error";
    public static final String ADDBOOK_NORECORD = "addbook.no.record";
    public static final String ADDBOOK_NO_CONTACT = "addbook.no.contact.found";
    public static final String ADDBOOK_TEMPLATE_UNABLEFIND = "addbook.template.error";
    public static final String ADDBOOK_CONTACT_UPDATE_ERROR = "addbook.contact.update.error";
    public static final String ADDBOOK_INCOMPLETE_DATA = "addbook.incomplete.entry";
    public static final String ADDBOOK_DELETECONTACT_NOTFOUND = "addbook.delete.contact.notfound";
    public static final String ADDBOOK_ERROR_DELETE_CONTACT = "addbook.error.delete.contact";
    public static final String ADDBOOK_GROUPDATA_AGEPARSE_ERROR = "addbook.groupdata.ageparse.error";
    public static final String ADDBOOK_GROUPDATA_INVALID_NUMBER = "addbook.groupdata.invalid.number";
    public static final String ADDBOOK_GROUPDATA_EMPTYDATASET = "addbook.groupdata.empty.datalist";
    public static final String ADDBOOK_GROUPDATA_UPDATE_ERROR = "addbook.groupdata.error.update";
    public static final String ADDBOOK_GROUP_DUPLICATE_ENTRY = "addbook.group.duplicate.entry";
    public static final String ADDBOOK_GROUP_NOTFOUND = "addbook.group.notfound";
    public static final String ADDBOOK_GROUP_DELETE_FAILED = "addbook.group.delete.failure";
    //addressbook success message
    public static final String ADDBOOK_CONTACT_SAVED = "addbook.addcontact.success";
    public static final String ADDBOOK_CONTACT_UPDATED = "addbook.updatecontact.success";
    public static final String ADDBOOK_CONTACT_DELETED = "addbook.deletecontact.success";
    public static final String ADDBOOK_GROUPDATA_SAVED = "addbook.groupdata.save.success";
    public static final String ADDBOOK_GROUPDATA_UPDATED = "addbook.groupdata.update.success";
    public static final String ADDBOOK_GROUPDATA_DELETED = "addbook.groupdata.delete.success";
    public static final String ADDBOOK_GROUP_SAVED = "addbook.group.save.success";
    public static final String ADDBOOK_GROUP_UPDATED = "addbook.group.update.success";
    public static final String ADDBOOK_GROUP_DELETED = "addbook.group.delete.success";
}
