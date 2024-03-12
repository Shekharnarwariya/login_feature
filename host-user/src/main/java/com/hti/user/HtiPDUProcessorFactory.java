/*
 * HtiPDUProcessorFactory.java
 *
 * Created on 09 March 2004, 19:42
 */

package com.hti.user;


import com.hti.user.UserSession;
import com.hti.user.PDUProcessorFactory;
import com.hti.user.PDUProcessor;
import com.hti.user.PDUProcessorGroup;

/**
 *
 * @author  administrator
 */
public class HtiPDUProcessorFactory implements PDUProcessorFactory {

    private PDUProcessorGroup procGroup;

    /** Creates a new instance of HtiPDUProcessorFactory */
    public HtiPDUProcessorFactory(PDUProcessorGroup procGroup) {

        this.procGroup = procGroup;

    }

    /**
     * Creates a new instance of <code>SimulatorPDUProcessor</code> with
     * parameters provided in construction of th factory.
     *
     * @param session the sessin the PDU processor will work for
     * @return newly created <code>SimulatorPDUProcessor</code>
     */
    public PDUProcessor createPDUProcessor(UserSession session) {

        HtiPDUProcessor hti_pdu_Processor=null;
        hti_pdu_Processor
        = new HtiPDUProcessor(session);//,users
        hti_pdu_Processor.setGroup(procGroup);
      //  System.out.println(" << Client Traying To Make Connection >> ");
        return hti_pdu_Processor;
    }

}
