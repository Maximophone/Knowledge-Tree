package com.maxf.knowledgetree.com.maxf.knowledgetree.engine;

import android.support.annotation.NonNull;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Max on 09/03/2015.
 */
public class Info {
    public String content;
    public String id;
    public Map<String,String> metaData = new HashMap<String,String>();
    public Map<String,String> specialMetaData = new HashMap<String,String>();
    private static final Set<String> SPECIAL = new HashSet<>(Arrays.asList(
            new String[]{"t"}
    ));
    private Category parent;

    public Info(String infoString, Category parent) throws Exception{
        this.parent = parent;
        String[] parts = infoString.split("#");
        content = parts[0];
        id = Engine.generateId(content);
        for (int i = 1; i < parts.length; i++) {
            String[] pairs = parts[i].split(":");
            if (pairs.length>2 || pairs.length==0) throw new Exception("Invalid Syntax");
            if(SPECIAL.contains(pairs[0])) specialMetaData.put(pairs[0], pairs.length==2?pairs[1]:"true");
            else metaData.put(pairs[0], pairs.length==2?pairs[1]:"true");
        }
    }

    public Info(Element infoElement){
        this.content = infoElement.getAttribute("content");
        this.id = infoElement.getAttribute("id");
        NamedNodeMap attrs = infoElement.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String attr_key = attr.getNodeName();
            String attr_value = attr.getNodeValue();
            String[] parts = attr_key.split(":");
            if (parts.length == 2 && parts[0].equals("special")) specialMetaData.put(parts[1], attr_value);
            else if (parts.length == 2 && parts[0].equals("meta")) metaData.put(parts[1], attr_value);
        }
        Element catElement = (Element) infoElement.getParentNode();
        if(catElement.getNodeName().equals("root")) this.parent = null;
        else this.parent = new Category(catElement);
    }

    public Boolean hasTitle(){
        return specialMetaData.get("t") != null;
    }

    public String getTitle(){
        return specialMetaData.get("t");
    }

    public Boolean hasMeta(){
        return !metaData.isEmpty();
    }

    public String getPath(){
        if(parent == null) return "";
        else return parent.getPath();
    }

    public String getColor(){
        if(parent == null) return "#ff0000";
        else return parent.getColor();
    }
}
