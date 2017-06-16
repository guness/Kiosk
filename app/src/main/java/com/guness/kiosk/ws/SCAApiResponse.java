package com.guness.kiosk.ws;

//----------------------------------------------------
//
// Generated by www.easywsdl.com
// Version: 5.0.10.1
//
// Created by Quasar Development 
//
//---------------------------------------------------


import java.util.Hashtable;
import org.ksoap2.serialization.*;

public class SCAApiResponse extends AttributeContainer implements KvmSerializable
{

    
    public SCAArrayOfApiEx ExceptionData;
    private transient Object __source;
    

    public SCAApiResponse ()
    {
    }

    public SCAApiResponse (Object paramObj,SCAExtendedSoapSerializationEnvelope __envelope)
    {
	    
	    if (paramObj == null)
            return;
        AttributeContainer inObj=(AttributeContainer)paramObj;
        __source=inObj;   
	    

        if(inObj instanceof SoapObject)
        {
            SoapObject soapObject=(SoapObject)inObj;
            int size = soapObject.getPropertyCount();
            for (int i0=0;i0< size;i0++)
            {
                //if you have compilation error here, please use a ksoap2.jar and ExKsoap2.jar from libs folder (in the generated zip file)
                PropertyInfo info=soapObject.getPropertyInfo(i0);
                Object obj = info.getValue();
                if (info.name.equals("ExceptionData"))
                {
                    if(obj!=null)
                    {
                        Object j = obj;
                        this.ExceptionData = new SCAArrayOfApiEx(j,__envelope);
                    }
                    continue;
                }

            }

        }



    }
    
    public Object getOriginalXmlSource()
    {
        return __source;
    }    
    

    @Override
    public Object getProperty(int propertyIndex) {
        //!!!!! If you have a compilation error here then you are using old version of ksoap2 library. Please upgrade to the latest version.
        //!!!!! You can find a correct version in Lib folder from generated zip file!!!!!
        if(propertyIndex==0)
        {
            return this.ExceptionData!=null?this.ExceptionData:SoapPrimitive.NullSkip;
        }
        return null;
    }


    @Override
    public int getPropertyCount() {
        return 1;
    }

    @Override
    public void getPropertyInfo(int propertyIndex, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info)
    {
        if(propertyIndex==0)
        {
            info.type = PropertyInfo.VECTOR_CLASS;
            info.name = "ExceptionData";
            info.namespace= "http://schemas.datacontract.org/2004/07/F2M.Api.Soap.CardService.Contracts.Response.Common";
        }
    }
    
    @Override
    public void setProperty(int arg0, Object arg1)
    {
    }

    
}

