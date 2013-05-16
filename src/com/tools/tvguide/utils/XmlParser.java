package com.tools.tvguide.utils;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;

public class XmlParser 
{
    private static final String XML_FILE = "channels.xml";
    private static final String XML_ELEMENT_LOGO = "logo";
    private static final String XML_ELEMENT_CHANNEL = "channel";
    
    public static HashMap<String, HashMap<String, Object>> parseChannelInfo(Context context)
    {
        boolean parseSuccess = true;
        HashMap<String, HashMap<String, Object>> channelInfo = new HashMap<String, HashMap<String,Object>>();
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(context.getAssets().open(XML_FILE));
            NodeList nl1 = doc.getElementsByTagName(XML_ELEMENT_CHANNEL);
            for (int i=0; i<nl1.getLength(); ++i) 
            {
                String id = null, logo = null;
                Node n = nl1.item(i);
                if (n.hasAttributes())
                {
                    id = n.getAttributes().getNamedItem("id").getNodeValue();
                }
                // 获取 n 节点下所有的子节点。此处值得注意，在DOM解析时会将所有回车都视为 n 节点的子节点。
                NodeList nl2 = n.getChildNodes();
                for (int j=0; j<nl2.getLength(); ++j) 
                {
                    Node n2 = nl2.item(j);
                    if (n2.hasChildNodes()) 
                    {
                        String name = n2.getNodeName();
                        if (name.equalsIgnoreCase(XML_ELEMENT_LOGO))
                        {
                            logo = n2.getFirstChild().getNodeValue();
                        }
                    }
                }
                if (id != null && logo != null)
                {
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put(XML_ELEMENT_LOGO, logo);
                    channelInfo.put(id, item);
                }
            }
        }
        catch (ParserConfigurationException e) 
        {
            parseSuccess = false;
            e.printStackTrace();
        } 
        catch (SAXException e) 
        {
            parseSuccess = false;
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            parseSuccess = false;
            e.printStackTrace();
        }
        
        if (parseSuccess == true)
        {
            return channelInfo;
        }
        else 
        {
            return new HashMap<String, HashMap<String,Object>>();
        }
    }
}
