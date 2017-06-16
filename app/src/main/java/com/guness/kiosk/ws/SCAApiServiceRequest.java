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

public class SCAApiServiceRequest extends AttributeContainer implements KvmSerializable
{

    
    public SCAApiContext Context;
    private transient Object __source;
    

    public SCAApiServiceRequest ()
    {
    }

    public SCAApiServiceRequest (Object paramObj,SCAExtendedSoapSerializationEnvelope __envelope)
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
                if (info.name.equals("Context"))
                {
                    if(obj!=null)
                    {
                        Object j = obj;
                        this.Context = (SCAApiContext)__envelope.get(j,SCAApiContext.class,false);
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
            return this.Context!=null?this.Context:SoapPrimitive.NullNilElement;
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
            info.type = SCAApiContext.class;
            info.name = "Context";
            info.namespace= "http://schemas.datacontract.org/2004/07/F2M.Api.Soap.CardService.Contracts.Request.Common";
        }
    }
    
    @Override
    public void setProperty(int arg0, Object arg1)
    {
    }

    
}
