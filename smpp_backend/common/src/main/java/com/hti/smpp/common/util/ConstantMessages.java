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
    public static final String DATE_PARSE_ERROR = "login.parse.date.error";
    public static final String PASSWORD_EXPIRES_PARSE_ERROR = "login.parse.expireson.error";
    public static final String ACCOUNT_EXPIRED = "user.account.expired";
    public static final String PASSWORD_EXPIRED = "user.password.expired";
    public static final String ACCOUNT_BLOCKED = "user.account.blocked";
    public static final String FLAG_READ_ERROR = "user.read.flag.error";
    public static final String USER_ACCESS_IPNOTALLOWED = "user.access.ip.not.allowed";
    public static final String ACCESS_COUNTRY_NOTCONFIGURED = "user.access.countries.not.configured";
    public static final String IPADDRESS_NOTFOUND = "user.ip.address.notfound";
    public static final String OTPEXPIRYDATE_PARSE_ERROR = "user.otp.expirydate.parse.error";
    public static final String WEBACCESS_DENIED_USER = "user.web.access.denied";
    public static final String INVALID_CREDENTIALS = "user.invalid.credentials";
    
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
    public static final String ERROR_GETTING_EXCLUDE_NUMBER = "error.getting.exclude.number";
    
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
    //Report Messages 
    public static final String NOT_FOUND_EXCEPTION_MESSAGE = "notFoundException.message";
    public static final String INTERNAL_SERVER_EXCEPTION_MESSAGE = "internalServerException.message";
    public static final String BAD_REQUEST_EXCEPTION_MESSAGE = "badRequestException.message";
    public static final String INTERNAL_SERVER_EXCEPTION = "campaign.report.data.not.found";
    public static final String UNAUTHORIZED_EXCEPTION_MESSAGE = "user.unauthorized.operation";
    public static final String DATA_NOT_FOUND_CONTENT = "dlr.content.report.data.not.found";
    public static final String INTERNAL_SERVER_LATENCY_EXCEPTION = "latency.report.error";
    public static final String NO_DATA_FOUND = "report.data.not.found";
    public static final String XLS_GENERATING_ERROR = "xls.report.error";
    public static final String PDF_GENERATION_ERROR = "pdf.report.error";
    public static final String REPORT_DOCUMENT_GENERATING_ERROR = "document.report.error";
    public static final String WEBMASTER_NOT_FOUND = "web.master.entry.not.found";
    public static final String CONTENT_WISE_PROCESS_DATA_ERROR = "content.wise.unprocessed.error";
    public static final String CUSTOMIZED_REPORT_NOT_FOUND_MESSAGE = "customized.report.data.not.found";
    public static final String CUSTOMIZED_REPORT_PROCESSING_ERROR_MESSAGE = "customized.report.processing.error";
    public static final String FAILED_GENERATE_JASPER_PRINT_MESSAGE = "jasperprint.generation.failed";
    
    //Abort
    public static final String NO_DATA_FOUND_ABORT_REPORT = "abort.report.data.not.found";
  //Blocked
    public static final String NO_DATA_FOUND_BLOCKED_REPORT="bloked.report.data.not.found";
    //schedule
    public static final String NO_DATA_FOUND_SCHEDULE_REPORT="schedule.report.data.not.found";
    //Summary
    public static final String SUMMARY_REPORT_NOT_FOUND_MESSAGE="summary.report.data.not.found";
    
    
    
    
    
    //DLR Summary Report
    public static final String NO_DATA_FOUND_DLR_SUMMARY_REPORT = "dlr.summary.report.data.not.found";
    public static final String ERROR_PROCESSING_DLR_SUMMARY_REPORT_MESSAGE = "dlr.summary.report.processing.error";
    public static final String GETTING_ERROR_DLR_SUMMARY_REPORT_MESSAGE = "dlr.summary.reportgetting.error";
    
    //SMS latency message
    
    public static final String SMS_LATENCY_REPORT_NOT_FOUND_MESSAGE = "sms.latency.report.not.found";
    public static final String RESOURCE_NOT_FOUND_EXCEPTION = "not.found.exception.message";
    public static final String ERROR_GENERATING_JASPER_REPORT_MESSAGE = "error.generating.jasper.report";
    public static final String NO_LATENCY_REPORT_DATA_FOUND_MESSAGE = "no.latency.report.data.found";
    public static final String WEBMASTER_ENTRY_NOT_FOUND_MESSAGE = "web.master.entry.notfound";
    public static final String ERROR_GETTING_LATENCY_REPORT_MESSAGE = "error.getting.latency.report";
  
    
    //lookup report
    
    public static final String ERROR_GENERATING_PDF_LOOKUP_REPORT_MESSAGE = "error.generating.pdf.lookup.report";
    public static final String ERROR_MESSAGE = "error.message";
    public static final String PROCESS_ERROR_MESSAGE = "process.error.message";
    
    //performance Report
    public static final String PERFORMANCE_REPORT_NOT_FOUND_MESSAGE = "performance.report.not.found";
    public static final String ERROR_GETTING_PERFORMANCE_REPORT_MESSAGE = "error.getting.performance.report";
    public static final String SCHEDULE_REPORT_NOT_FOUND_MESSAGE = "schedule.report.not.found";
    public static final String ERROR_GETTING_PROFIT_REPORT_MESSAGE = "error.getting.profit.report";
    public static final String PROFIT_REPORT_NOT_FOUND_MESSAGE = "profit.report.not.found";
    public static final String PROFIT_REPORT_XLS_NOT_FOUND_MESSAGE = "profit.report.xls.not.found";
    public static final String ERROR_GETTING_PROFIT_REPORT_XLS_MESSAGE = "error.getting.profit.report.xls";
    public static final String PROFIT_REPORT_PDF_NOT_FOUND_MESSAGE = "profit.report.pdf.not.found";
    public static final String ERROR_GETTING_PROFIT_REPORT_PDF_MESSAGE = "error.getting.profit.report.pdf";
    public static final String PROFIT_REPORT_DOC_NOT_FOUND_MESSAGE = "profit.report.doc.not.found";

    //balance report
    public static final String NO_BALANCE_REPORT_DATA_FOUND_MESSAGE = "no.balance.report.data.found";
    public static final String ERROR_PROCESSING_BALANCE_REPORT_MESSAGE = "error.processing.balance.report";
    public static final String EMPTY_XLS_CONTENT_ERROR_MESSAGE = "empty.xls.content.error";
    public static final String ERROR_CLOSING_XLS_OUTPUTSTREAM_MESSAGE = "error.closing.xls.outputstream";
    public static final String ERROR_GENERATING_REPORT_MESSAGE = "error.generating.report";
    public static final String EMPTY_PDF_CONTENT_ERROR_MESSAGE = "empty.pdf.content.error";
    public static final String ERROR_GENERATING_BALANCE_REPORT_PDF_MESSAGE = "error.generating.balance.report.pdf";

    public static final String EMPTY_DOC_CONTENT_ERROR_MESSAGE = "empty.doc.content.error";
    public static final String ERROR_GENERATING_BALANCE_REPORT_DOC_MESSAGE = "error.generating.balance.report.doc";
    public static final String UNEXPECTED_ERROR_GENERATING_REPORT_MESSAGE = "unexpected.error.generating.report";
    public static final String UNEXPECTED_ERROR_GENERATING_PDF_REPORT_MESSAGE = "unexpected.error.generating.pdf.report";

    public static final String NO_RECORDS_FOUND_MESSAGE = "no.records.found";
    public static final String UNEXPECTED_ERROR_GENERATING_XLS_REPORT_MESSAGE = "unexpected.error.generating.xls.report";
    public static final String ERROR_GENERATING_DOC_REPORT_MESSAGE = "error.generating.doc.report";

    public static final String SMS_DLR_REPORT_NOT_FOUND_MESSAGE = "sms.dlr.report.not.found";
    public static final String NO_USER_DELIVERY_NOT_REPORT_DATA_FOUND_MESSAGE = "no.user.delivery.not.report.data.found";
    public static final String NO_DELIVERY_REPORT_DATA_FOUND_MESSAGE = "no.delivery.report.data.found";
    public static final String USER_DELIVERY_REPORT_NOT_FOUND_MESSAGE = "user.delivery.report.not.found";
    public static final String ERROR_GETTING_DELIVERY_REPORT_MESSAGE = "error.getting.delivery.report";
    public static final String NO_USERS_FOUND_UNDER_MESSAGE = "no.users.found.under";
    public static final String USER_SUBMISSION_REPORT_NOT_FOUND_MESSAGE = "user.submission.report.not.found";
    public static final String ERROR_GETTING_SUBMISSION_REPORT_MESSAGE = "error.getting.submission.report";
    public static final String USER_SUMMARY_REPORT_NOT_FOUND_MESSAGE = "user.summary.report.not.found";
    public static final String ERROR_GETTING_DLR_CONTENT_REPORT_MESSAGE = "error.getting.dlr.content.report";
  
    //hlr-smsc exception message
    public static final String HLR_SMSC_SAVE_ERROR = "hlr.smsc.save.error";
    public static final String HLR_SMSC_CONVERT_ERROR = "hlr.smsc.convert.error";
    public static final String HLR_SMSC_ENTRY_NOT_FOUND = "hlr.smsc.entry.notfound";
    public static final String HLR_SMSC_DATA_ACCESS_ERROR = "hlr.smsc.data.access.error";
    public static final String HLR_SMSC_UPDATE_ERROR = "hlr.smsc.update.error";
    public static final String HLR_SMSC_NOT_FOUND = "hlr.smsc.notfound";
    
    //hlr-smsc success message
    public static final String HLR_SMSC_DELETE_SUCCESS = "hlr.smsc.delete.success";
    public static final String HLR_SMSC_UPDATED_SUCCESS = "hlr.smsc.update.success";
    public static final String HLR_SMSC_SAVED_SUCCESS = "hlr.smsc.add.success";
    
    
    // route
    public static final String USERWISE_ROUTING_EMPTY = "userwise.routing.empty";
    public static final String RECORD_UNAVAILABLE_EXCEPTION = "record.unavailable.exception";
    public static final String DELETION_ERROR_EXCEPTION = "deletion.error.exception";
    public static final String INTERNAL_SERVER_ERROR_EXCEPTION_SAVE = "internal.server.error.exception.save";
    public static final String RECORD_NOT_FOUND_EMPTY_LIST = "record.not.found.empty.list";
    public static final String RECORD_UNAVAILABLE = "record.unavailable";
    public static final String RECORD_UNAVAILABLE_ID_UNAVAILABLE = "record.unavailable.id.unavailable";
    public static final String FAILED_TO_ADD_OPTIONAL_ROUTE_ENTRIES = "record.unavailable.id.unavailablefailed.to.add.optional.route.entries";
    public static final String SALES_ENTRY_NOT_FOUND_EXCEPTION = "sales.entry.notfound";
    public static final String DISTINCT_COUNTRIES_NOT_FOUND_EXCEPTION = "distinct.countries.notfound";
    public static final String ROUTING_LIST_NOT_FOUND_EXCEPTION = "routing.list.notfound";
    public static final String NO_DATA_IN_LIST_EXCEPTION = "no.data.in.list.exception";
    public static final String LOG_LIST_NOT_FOUND = "log.list.not.found";
    public static final String ROUTE_ENTRY_EXT_LIST_NOT_FOUND = "route.entry.ext.list.not.found";
    public static final String FAILED_TO_ADD_HLR_ROUTE_ENTRIES = "failed.to.add.hlr.route.entries";
    public static final String NO_DATA_FOUND_IN_ROUTE_ENTRY_EXT_LIST = "no.data.found.in.route.entry.ext.list";
    public static final String ID_NOT_FOUND_OR_NULL = "id.not.found.or.null";
    public static final String ERROR_ADDING_ROUTE_ENTRIES = "error.adding.route.entries";

	public static final String INTERNAL_SERVER_MESSAGE = "An unexpected internal server error has occurred. Please try again later or contact support if the problem persists.";

    //two-way exception
    public static final String TWOWAY_WEBMENU_ACCESS_ENTRY_NOT_FOUND = "twoway.webmenu.access.entry.notfound";
    public static final String TWOWAY_KEYWORD_EXIST = "twoway.keyword.exist";
    public static final String TWOWAY_KEYWORD_NOT_FOUND = "twoway.keyword.notfound";
    public static final String TWOWAY_INTERNALSERVER_ERROR = "twoway.internalserver.error";


   // S M S C 
    public static final String FAILED_TO_SAVE_SMSC_ENTRY = "failed.to.save.smsc.entry";
    public static final String FAILED_TO_UPDATE_SMSC_ENTRY = "failed.to.update.smsc.entry";
    public static final String SMS_ENTRY_NOT_FOUND_FOR_UPDATE = "sms.entry.not.found.for.update";
    public static final String SMS_ENTRY_UPDATE_FAILED_UNEXPECTED_ERROR = "sms.entry.update.failed.unexpected.error";
    public static final String SMS_ENTRY_NOT_FOUND_UPDATE_FAILED = "sms.entry.not.found.update.failed";
    public static final String SMS_ENTRY_NOT_FOUND = "sms.entry.not.found";
    public static final String CUSTOM_ENTRY_NOT_FOUND = "custom.entry.not.found";
    public static final String FAILED_RETRIEVE_CUSTOM_ENTRY = "failed.retrieve.custom.entry";
    public static final String FAILED_SAVE_CUSTOM_ENTRY = "failed.save.custom.entry";
    public static final String FAILED_SAVE_CUSTOM_ENTRY_UN = "failed.save.custom.entry.un";
    public static final String UPDATE_CUSTOM_ENTRY_DATA_ACCESS_ERROR = "update.custom.entry.data.access.error";
    public static final String UPDATE_CUSTOM_ENTRY_UNEXPECTED_ERROR = "update.custom.entry.unexpected.error";
    public static final String DELETE_CUSTOM_ENTRY_ERROR = "delete.custom.entry.error";
    public static final String SAVE_LIMIT_ENTRY_ERROR = "save.limit.entry.error";
    public static final String SAVE_LIMIT_ENTRY_ERROR_UN = "save.limit.entry.error.un";
    public static final String UPDATE_LIMIT_ENTRY_ERROR = "update.limit.entry.error";
    public static final String LIMIT_NOT_FOUND = "limit.not.found";
    public static final String UPDATE_LIMIT_ENTRY_UNEXPECTED_ERROR = "update.limit.entry.unexpected.error";
    public static final String DELETE_LIMIT_ENTRY_DATA_ACCESS_ERROR = "delete.limit.entry.data.access.error";
    public static final String DELETE_LIMIT_ENTRY_UNEXPECTED_ERROR = "delete.limit.entry.unexpected.error";
    public static final String LIST_SMSC_LIMIT_ENTRIES_DATA_ACCESS_ERROR = "list.smsc.limit.entries.data.access.error";
    public static final String LIST_SMSC_LIMIT_ENTRIES_UNEXPECTED_ERROR = "list.smsc.limit.entries.unexpected.error";
    public static final String SAVE_GROUP_ENTRY_DATA_ACCESS_ERROR = "save.group.entry.data.access.error";
    public static final String SAVE_GROUP_ENTRY_UNEXPECTED_ERROR = "save.group.entry.unexpected.error";
    public static final String GROUP_NOT_FOUND = "group.not.found";
    public static final String FAILED_UPDATE_GROUP_ENTRY = "failed.update.group.entry";
    public static final String FAILED_UPDATE_GROUP_ENTRY_UN = "failed.update.group.entry.un";
    public static final String GROUP_DELETE_ERROR = "group.delete.error";
    public static final String GROUP_DELETE_UNEXPECTED_ERROR = "group.delete.unexpected.error";
    public static final String GROUP_LIST_DATA_ACCESS_ERROR = "group.list.data.access.error";
    public static final String GROUP_LIST_UNEXPECTED_ERROR = "group.list.unexpected.error";
    public static final String GROUP_MEMBER_SAVE_DATA_ACCESS_ERROR = "group.member.save.data.access.error";
    public static final String GROUP_MEMBER_SAVE_UNEXPECTED_ERROR = "group.member.save.unexpected.error";
    public static final String GROUP_MEMBER_NOT_FOUND = "group.member.not.found";
    public static final String UPDATE_GROUP_MEMBER_ENTRY_ERROR = "update.group.member.entry.error";
    public static final String UPDATE_GROUP_MEMBER_ENTRY_UNEXPECTED_ERROR = "update.group.member.entry.unexpected.error";
    public static final String SAVE_TRAFFIC_SCHEDULE_ENTRY_DATA_ACCESS_ERROR = "save.traffic.schedule.entry.data.access.error";
    public static final String SAVE_TRAFFIC_SCHEDULE_ENTRY_UNEXPECTED_ERROR = "save.traffic.schedule.entry.unexpected.error";
    public static final String TRAFFIC_SCHEDULE_NOT_FOUND = "traffic.schedule.not.found";
    public static final String TRAFFIC_SCHEDULE_UPDATE_ERROR = "traffic.schedule.update.error";
    public static final String TRAFFIC_SCHEDULE_DELETE_ERROR = "traffic.schedule.delete.error";
    public static final String TRAFFIC_SCHEDULE_DELETE_UNEXPECTED_ERROR = "traffic.schedule.delete.unexpected.error";
    public static final String TRAFFIC_SCHEDULE_LIST_DATA_ACCESS_ERROR = "traffic.schedule.list.data.access.error";
    public static final String TRAFFIC_SCHEDULE_LIST_UNEXPECTED_ERROR = "traffic.schedule.list.unexpected.error";
    public static final String SMS_LOOPING_SAVE_DATA_ACCESS_ERROR = "sms.looping.save.data.access.error";
    public static final String SMS_LOOPING_ENTRY_NOT_FOUND = "sms.looping.entry.not.found";
    public static final String FAILED_UPDATE_SMS_LOOPING_ENTRY = "failed.update.sms.looping.entry";
    public static final String FAILED_TO_DELETE_SMS_LOOPING_ENTRY = "failed.to.delete.sms.looping.entry";
    public static final String SMS_LOOPING_RULE_NOT_FOUND = "sms.looping.rule.not.found";
    public static final String FAILED_TO_RETRIEVE_SMS_LOOPING_RULE = "failed.to.retrieve.sms.looping.rule";
    public static final String FAILED_TO_LIST_SMS_LOOPING_RULES = "failed.to.list.sms.looping.rules";
    public static final String ERROR_CONVERTING_REQUEST_TO_ENTRY = "error.converting.request.to.entry";
    public static final String ERROR_CONVERTING_REQUEST_TO_CUSTOM_ENTRY = "error.converting.request.to.custom.entry";
    public static final String ERROR_CONVERTING_REQUEST_TO_GROUP_ENTRY = "error.converting.request.to.group.entry";
    public static final String ERROR_CONVERTING_REQUEST_TO_GROUP_MEMBER_ENTRY = "error.converting.request.to.group.member.entry";
    public static final String ERROR_CONVERTING_REQUEST_TO_LIMIT_ENTRY = "error.converting.request.to.limit.entry";
    public static final String ERROR_CONVERTING_REQUEST_TO_SMSC_LOOPING = "error.converting.request.to.smsc.looping";
    public static final String ERROR_CONVERTING_REQUEST_TO_TRAFFIC_SCHEDULE = "error.converting.request.to.traffic.schedule";
    public static final String UNEXPECTED_ERROR_PROCESSING_REQUEST = "unexpected.error.processing.request";
    public static final String ENTRY_NOT_FOUND = "entry.not.found";
    public static final String UNEXPECTED_ERROR = "unexpected.error";
    //two-way success messages
    public static final String TWOWAY_KEYWORD_ADD_SUCCESS = "twoway.add.success";
    public static final String TWOWAY_KEYWORD_UPDATED_SUCCESS = "twoway.update.success";
    public static final String TWOWAY_KEYWORD_DELETED_SUCCESS = "twoway.delete.success";
    public static final String TWOWAY_RECORD_UNAVAILABLE = "twoway.record.unavailable";

     //ALERT
    public static final String ALERT_ENTITY_NOT_FOUND = "alert.entity.not.found";
    public static final String UNABLE_TO_DELETE_ALERT = "unable.to.delete.alert";
    public static final String UNABLE_TO_FIND_ALL_ALERTS = "unable.to.find.all.alerts";
    public static final String ALERT_ENTRIES_NOT_FOUND = "alert.entries.not.found";
    public static final String UPDATE_ALERT_ENTITY_FAILED = "update.alert.entity.failed";
	public static final String CAMPAIGN_REPORT_NOT_FOUND_MESSAGE = "campaign.report.not.found";

	public static final String TRASACTION_DATA_NOT_FOUND = "trasaction.data.not.found";



    
}

