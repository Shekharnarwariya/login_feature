package com.hti.smpp.common.alert.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "perform_alert")
public class AlertEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "percent")
	private int percent;
	
	@Column(name = "number")
	private String alertNumber;
	
	@Column(name = "duration")
	private int duration;
	
	@Column(name = "country")
	private String countries;
	
	@Column(name = "smsc")
	private String routes;
	
	@Column(name = "remarks")
	private String remarks;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "min_submit")
	private int minlimit;
	
	@Column(name = "hold_traffic")
	private boolean holdTraffic;
	
	@Column(name = "sender")
	private String senders;
	
	
   public AlertEntity() {
		
	}


public int getId() {
	return id;
}


public void setId(int id) {
	this.id = id;
}


public String getEmail() {
	return email;
}


public void setEmail(String email) {
	this.email = email;
}


public int getPercent() {
	return percent;
}


public void setPercent(int percent) {
	this.percent = percent;
}


public String getAlertNumber() {
	return alertNumber;
}


public void setAlertNumber(String alertNumber) {
	this.alertNumber = alertNumber;
}


public int getDuration() {
	return duration;
}


public void setDuration(int duration) {
	this.duration = duration;
}


public String getCountries() {
	return countries;
}


public void setCountries(String countries) {
	this.countries = countries;
}


public String getRoutes() {
	return routes;
}


public void setRoutes(String routes) {
	this.routes = routes;
}


public String getRemarks() {
	return remarks;
}


public void setRemarks(String remarks) {
	this.remarks = remarks;
}


public String getStatus() {
	return status;
}


public void setStatus(String status) {
	this.status = status;
}


public int getMinlimit() {
	return minlimit;
}


public void setMinlimit(int minlimit) {
	this.minlimit = minlimit;
}


public boolean isHoldTraffic() {
	return holdTraffic;
}


public void setHoldTraffic(boolean holdTraffic) {
	this.holdTraffic = holdTraffic;
}


public String getSenders() {
	return senders;
}


public void setSenders(String senders) {
	this.senders = senders;
}
   
   
   
   


}
