package com.hti.smpp.common.dto;

public class Network extends MccMncDTO{
	
	private String isPorted;
    private String isRoaming;
    private String portedOperater;
    private String roamingCountry;
    private String roamingOperator;
    private String portedNNC;
    private String roamingNNC;
    private String roamingCC;

    public String getIsPorted() {
        return isPorted;
    }

    public void setIsPorted(String isPorted) {
        this.isPorted = isPorted;
    }

    public String getIsRoaming() {
        return isRoaming;
    }

    public void setIsRoaming(String isRoaming) {
        this.isRoaming = isRoaming;
    }

    public String getPortedOperater() {
        return portedOperater;
    }

    public void setPortedOperater(String portedOperater) {
        this.portedOperater = portedOperater;
    }

    public String getRoamingCountry() {
        return roamingCountry;
    }

    public void setRoamingCountry(String roamingCountry) {
        this.roamingCountry = roamingCountry;
    }

    public String getRoamingOperator() {
        return roamingOperator;
    }

    public void setRoamingOperator(String roamingOperator) {
        this.roamingOperator = roamingOperator;
    }

    public String getPortedNNC() {
        return portedNNC;
    }

    public void setPortedNNC(String portedNNC) {
        this.portedNNC = portedNNC;
    }

    public String getRoamingNNC() {
        return roamingNNC;
    }

    public void setRoamingNNC(String roamingNNC) {
        this.roamingNNC = roamingNNC;
    }

    public String getRoamingCC() {
        return roamingCC;
    }

    public void setRoamingCC(String roamingCC) {
        this.roamingCC = roamingCC;
    }


}
