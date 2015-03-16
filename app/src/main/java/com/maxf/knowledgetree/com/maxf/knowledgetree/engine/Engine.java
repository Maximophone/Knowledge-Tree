package com.maxf.knowledgetree.com.maxf.knowledgetree.engine;

import android.content.Context;
import android.util.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Created by Max on 09/03/2015.
 */
public class Engine {

    private FileIO fileio;
    private String url;
    public Document doc = null;
    private static IdGenerator id_gen;



    public Engine(String url, Context context)
    {
        this.url = url;
        fileio = new FileIO(url, context, "<root xmlns:special=\"special keywords\" xmlns:meta=\"meta data\"></root>");
        //try {reset();}
        //catch (Exception e) {Log.e("login activity",e.toString());}
        id_gen = new IdGenerator(context);
        try {
            doc = readXml(fileio.readFromFile());
        }
        catch (ParserConfigurationException e){
            Log.e("login activity",e.toString());
        }
        catch (SAXException e){
            Log.e("login activity",e.toString());
        }
        catch (IOException e){
            Log.e("login activity",e.toString());
        }
    }

    public static String generateId(String s) {
        //return String.valueOf(System.currentTimeMillis());
        return String.valueOf(id_gen.getNew());
    }

    public Info[] getInfos() throws XPathExpressionException
    {
        // create an XPathFactory
        XPathFactory xFactory = XPathFactory.newInstance();

        // create an XPath object
        XPath xpath = xFactory.newXPath();

        XPathExpression expr = xpath.compile("//info");
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        Info[] infos = new Info[nodes.getLength()];

        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            infos[i] = new Info(e);
        }
        return infos;
    }

    public void reset() throws Exception{
        this.fileio.writeToFile("<root xmlns:special=\"special keywords\" xmlns:meta=\"meta data\"></root>");
        doc = readXml(fileio.readFromFile());
    }

    public void editInfo(String id, String new_content) throws XPathExpressionException, TransformerException
    {
        // create an XPathFactory
        XPathFactory xFactory = XPathFactory.newInstance();

        // create an XPath object
        XPath xpath = xFactory.newXPath();

        String xpathString = ".//info[@id='"+id+"']";

        XPathExpression expr = xpath.compile(xpathString);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        Element node = (Element) nodes.item(0);

        node.setAttribute("content", new_content);

        String newXml = writeXml(doc);
        this.fileio.writeToFile(newXml);

    }

    public void addInfo(String infoString) throws Exception
    {
        if (infoString.equals("!reset")) {
            reset();
            return;
        }

        InfoBit infobit = new InfoBit(infoString);

        // create an XPathFactory
        XPathFactory xFactory = XPathFactory.newInstance();

        // create an XPath object
        XPath xpath = xFactory.newXPath();

        //Add the categories if needed
        String xpathStringPrev = "/root";
        String xpathString = "/root";

        for (int i = 0; i < infobit.categories.length; i++) {
            xpathString += "/cat[@name='" + infobit.categories[i].name + "']";
            //For each category, check if it exists in the xml. If it doesn't, create the rest of the path.
            //If it does exist, check that the metadata corresponds and add some if needed
            XPathExpression expr = xpath.compile(xpathString);
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            if (nodes.getLength() == 0)
            {
                //The category doesn't exist yet, create it
                Element cat = doc.createElement("cat");
                cat.setAttribute("id",infobit.categories[i].id);
                cat.setAttribute("name",infobit.categories[i].name);

                if(infobit.categories[i].metaData!=null) {
                    for (Map.Entry<String, String> entry : infobit.categories[i].metaData.entrySet()) {
                        cat.setAttribute("meta:"+entry.getKey(), entry.getValue());
                    }
                }

                if(infobit.categories[i].specialMetaData!=null) {
                    for (Map.Entry<String, String> entry : infobit.categories[i].specialMetaData.entrySet()) {
                        cat.setAttribute("special:"+entry.getKey(), entry.getValue());
                    }
                }

                //Get last existing category in the path
                XPathExpression exprPrev = xpath.compile(xpathStringPrev);
                NodeList nodePrev = (NodeList) exprPrev.evaluate(doc, XPathConstants.NODESET);
                Element e = (Element) nodePrev.item(0);
                e.appendChild(cat);
            }
            else if (nodes.getLength() == 1)
            {
                Element node = (Element) nodes.item(0);
                //The category already exists, check the attributes (meta data) and update if needed
                for ( Map.Entry<String, String> entry : infobit.categories[i].metaData.entrySet())
                {
                   if (!node.hasAttribute("meta:"+entry.getKey())){
                       node.setAttribute("meta:"+entry.getKey(),entry.getValue());
                   }
                }
                //special metadata
                for ( Map.Entry<String, String> entry : infobit.categories[i].specialMetaData.entrySet())
                {
                    if (!node.hasAttribute("special:"+entry.getKey())){
                        node.setAttribute("special:"+entry.getKey(),entry.getValue());
                    }
                }
            }
            else
            {
                throw new Exception("XML File Corrupted");
            }
            xpathStringPrev = xpathString;
        }

        //Add the info
        //First get the category node
        XPathExpression expr = xpath.compile(xpathString);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        Element node = (Element) nodes.item(0);

        //Create the info node
        Element info = doc.createElement("info");
        info.setAttribute("id",infobit.info.id);
        info.setAttribute("content",infobit.info.content);

        if(infobit.info.metaData!=null) {
            for (Map.Entry<String, String> entry : infobit.info.metaData.entrySet()) {
                info.setAttribute("meta:"+entry.getKey(), entry.getValue());
            }
        }

        if(infobit.info.specialMetaData!=null) {
            for (Map.Entry<String, String> entry : infobit.info.specialMetaData.entrySet()) {
                info.setAttribute("special:"+entry.getKey(), entry.getValue());
            }
        }

        //Append info node as child to category node
        node.appendChild(info);

        //Write xml file
        String newXml = writeXml(doc);
        this.fileio.writeToFile(newXml);
    }

    public Document readXml(String xml) throws ParserConfigurationException, SAXException, IOException{
        // standard for reading an XML file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    public String writeXml(Document doc) throws TransformerConfigurationException, TransformerException{
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");

    }
}
