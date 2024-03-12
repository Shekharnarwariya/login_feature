package com.hti.smpp.common.request;


public class ConversionData {
    private String charsTA;
    private String htmlTA;
    private String uniTA;
    private String percTA;
    private String utf8TA;
    private String utf16TA;
    private String hexTA;
    private String decTA;
    private String hncrTA;
    private String dncrTA;
    private String jsTA;
    private String cssTA;
//    private String MMCTA;
//    private String mod;
//    private String category;
//    private String MMCPicTAmsg;
//    private String MMCLinkTBmsg;
//    private String  CharacterEncoding;
    // Default constructor
    private String hidnUtf16SpcOpt;
    private String hidnUniEscOpt;
    private String  hidnUniSpcOpt;
    private String hidnUtf8SpcOpt;
    
    
    
    
    
    
    
    
    
    public String getHidnUtf16SpcOpt() {
		return hidnUtf16SpcOpt;
	}

	public void setHidnUtf16SpcOpt(String hidnUtf16SpcOpt) {
		this.hidnUtf16SpcOpt = hidnUtf16SpcOpt;
	}

	public String getHidnUniEscOpt() {
		return hidnUniEscOpt;
	}

	public void setHidnUniEscOpt(String hidnUniEscOpt) {
		this.hidnUniEscOpt = hidnUniEscOpt;
	}

	public String getHidnUniSpcOpt() {
		return hidnUniSpcOpt;
	}

	public void setHidnUniSpcOpt(String hidnUniSpcOpt) {
		this.hidnUniSpcOpt = hidnUniSpcOpt;
	}

	public String getHidnUtf8SpcOpt() {
		return hidnUtf8SpcOpt;
	}

	public void setHidnUtf8SpcOpt(String hidnUtf8SpcOpt) {
		this.hidnUtf8SpcOpt = hidnUtf8SpcOpt;
	}

	public ConversionData() {
    }

    // Getters and setters
    public String getCharsTA() {
        return charsTA;
    }

    public void setCharsTA(String charsTA) {
        this.charsTA = charsTA;
    }

    public String getHtmlTA() {
        return htmlTA;
    }

    public void setHtmlTA(String htmlTA) {
        this.htmlTA = htmlTA;
    }

  
	public String getUniTA() {
        return uniTA;
    }

    public void setUniTA(String uniTA) {
        this.uniTA = uniTA;
    }

    public String getPercTA() {
        return percTA;
    }

    public void setPercTA(String percTA) {
        this.percTA = percTA;
    }

    public String getUtf8TA() {
        return utf8TA;
    }

    public void setUtf8TA(String utf8TA) {
        this.utf8TA = utf8TA;
    }

    public String getUtf16TA() {
        return utf16TA;
    }

    public void setUtf16TA(String utf16TA) {
        this.utf16TA = utf16TA;
    }

    public String getHexTA() {
        return hexTA;
    }

    public void setHexTA(String hexTA) {
        this.hexTA = hexTA;
    }

    public String getDecTA() {
        return decTA;
    }

    public void setDecTA(String decTA) {
        this.decTA = decTA;
    }

    public String getHncrTA() {
        return hncrTA;
    }

    public void setHncrTA(String hncrTA) {
        this.hncrTA = hncrTA;
    }

    public String getDncrTA() {
        return dncrTA;
    }

    public void setDncrTA(String dncrTA) {
        this.dncrTA = dncrTA;
    }

    public String getJsTA() {
        return jsTA;
    }

    public void setJsTA(String jsTA) {
        this.jsTA = jsTA;
    }

    public String getCssTA() {
        return cssTA;
    }

    public void setCssTA(String cssTA) {
        this.cssTA = cssTA;
    }

    // You might want to add a toString() method for debugging purposes
    @Override
    public String toString() {
        return "ConversionData{" +
                "charsTA='" + charsTA + '\'' +
                ", htmlTA='" + htmlTA + '\'' +
                // Add the rest of the fields here for completeness
                '}';
    }
}
