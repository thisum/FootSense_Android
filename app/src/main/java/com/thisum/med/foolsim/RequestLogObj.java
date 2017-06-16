package com.thisum.med.foolsim;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by thisum on 1/27/2017.
 */
public class RequestLogObj implements Serializable
{
    private List<Integer> leftLeg;
    private List<Integer> rightLeg;
    private String patientEmail = "";
    private String patientName = "";

    public RequestLogObj( List<Integer> leftLeg, List<Integer> rightLeg, String patientName, String patientEmail )
    {
        this.leftLeg = leftLeg;
        this.rightLeg = rightLeg;
        this.patientEmail = patientEmail;
        this.patientName = patientName;
    }

    public String getLeftLeg()
    {
        return leftLeg.toString().replace( "[", "" ).replace( "]", "" );
    }

    public void setLeftLeg( List<Integer> leftLeg )
    {
        this.leftLeg = leftLeg;
    }

    public String getRightLeg()
    {
        return rightLeg.toString().replace( "[", "" ).replace( "]", "" );
    }

    public void setRightLeg( List<Integer> rightLeg )
    {
        this.rightLeg = rightLeg;
    }

    public String getPatientEmail()
    {
        return patientEmail;
    }

    public void setPatientEmail( String patientEmail )
    {
        this.patientEmail = patientEmail;
    }

    public String getPatientName()
    {
        return patientName;
    }

    public void setPatientName( String patientName )
    {
        this.patientName = patientName;
    }
}
