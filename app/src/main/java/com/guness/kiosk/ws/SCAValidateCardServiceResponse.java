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

public class SCAValidateCardServiceResponse extends SCAApiResponse implements KvmSerializable
{

    
    public SCACardServiceResponse CardData;
    
    public Boolean IsValid;
    

    public SCAValidateCardServiceResponse ()
    {
    }

    public SCAValidateCardServiceResponse (Object paramObj,SCAExtendedSoapSerializationEnvelope __envelope)
    {
	    super(paramObj, __envelope);
	    if (paramObj == null)
            return;
        AttributeContainer inObj=(AttributeContainer)paramObj;
	    

        if(inObj instanceof SoapObject)
        {
            SoapObject soapObject=(SoapObject)inObj;
            int size = soapObject.getPropertyCount();
            for (int i0=0;i0< size;i0++)
            {
                //if you have compilation error here, please use a ksoap2.jar and ExKsoap2.jar from libs folder (in the generated zip file)
                PropertyInfo info=soapObject.getPropertyInfo(i0);
                Object obj = info.getValue();
                if (info.name.equals("CardData"))
                {
                    if(obj!=null)
                    {
                        Object j = obj;
                        this.CardData = (SCACardServiceResponse)__envelope.get(j,SCACardServiceResponse.class,false);
                    }
                    continue;
                }
                if (info.name.equals("IsValid"))
                {
                    if(obj!=null)
                    {
        
                        if (obj.getClass().equals(SoapPrimitive.class))
                        {
                            SoapPrimitive j =(SoapPrimitive) obj;
                            if(j.toString()!=null)
                            {
                                this.IsValid = new Boolean(j.toString());
                            }
                        }
                        else if (obj instanceof Boolean){
                            this.IsValid = (Boolean)obj;
                        }
                    }
                    continue;
                }

            }

        }



    }
    
    

    @Override
    public Object getProperty(int propertyIndex) {
        int count = super.getPropertyCount();
        //!!!!! If you have a compilation error here then you are using old version of ksoap2 library. Please upgrade to the latest version.
        //!!!!! You can find a correct version in Lib folder from generated zip file!!!!!
        if(propertyIndex==count+0)
        {
            return this.CardData!=null?this.CardData:SoapPrimitive.NullSkip;
        }
        if(propertyIndex==count+1)
        {
            return this.IsValid!=null?this.IsValid:SoapPrimitive.NullSkip;
        }
        return super.getProperty(propertyIndex);
    }


    @Override
    public int getPropertyCount() {
        return super.getPropertyCount()+2;
    }

    @Override
    public void getPropertyInfo(int propertyIndex, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info)
    {
        int count = super.getPropertyCount();
        if(propertyIndex==count+0)
        {
            info.type = SCACardServiceResponse.class;
            info.name = "CardData";
            info.namespace= "http://schemas.datacontract.org/2004/07/F2M.Api.Soap.CardService.Contracts.Response.Card";
        }
        if(propertyIndex==count+1)
        {
            info.type = PropertyInfo.BOOLEAN_CLASS;
            info.name = "IsValid";
            info.namespace= "http://schemas.datacontract.org/2004/07/F2M.Api.Soap.CardService.Contracts.Response.Card";
        }
        super.getPropertyInfo(propertyIndex,arg1,info);
    }
    
    @Override
    public void setProperty(int arg0, Object arg1)
    {
    }

    
}
