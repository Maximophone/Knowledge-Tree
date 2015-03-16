package com.maxf.knowledgetree.com.maxf.knowledgetree.engine;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by Max on 09/03/2015.
 */
public class Category {
    private static Random r = new Random();
    public String name;
    public String id;
    public Map<String,String> metaData = new HashMap<String,String>();
    public Map<String,String> specialMetaData = new HashMap<String,String>();
    private static final Set<String> SPECIAL = new HashSet<>(Arrays.asList(
            new String[]{"color","check","to","temp","hidden"}
    ));
    private Category parent;

    public Category(String categoryString, Category parent) throws Exception{
        //Building a category from a newly entered string
        this.parent=parent;
        String[] parts = categoryString.split("#");
        name = parts[0];
        id = Engine.generateId(name);
        for (int i = 1; i < parts.length; i++) {
            String[] pairs = parts[i].split(":");
            if (pairs.length>2 || pairs.length==0) throw new Exception("Invalid Syntax");
            if(SPECIAL.contains(pairs[0])) specialMetaData.put(pairs[0], pairs.length==2?pairs[1]:"true");
            else metaData.put(pairs[0], pairs.length==2?pairs[1]:"true");
        }
        if(this.parent == null && specialMetaData.get("color") == null) specialMetaData.put("color",genRandomColor());
    }

    public Category(Element categoryElement){
        this.name = categoryElement.getAttribute("name");
        this.id = categoryElement.getAttribute("id");
        NamedNodeMap attrs = categoryElement.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String attr_key = attr.getNodeName();
            String attr_value = attr.getNodeValue();
            String[] parts = attr_key.split(":");
            if (parts.length == 2 && parts[0].equals("special")) specialMetaData.put(parts[1], attr_value);
            else if (parts.length == 2 && parts[0].equals("meta")) metaData.put(parts[1], attr_value);
        }
        Element parentElement = (Element) categoryElement.getParentNode();
        if(parentElement.getNodeName().equals("root")) this.parent = null;
        else this.parent = new Category(parentElement);
        //Dont need the following line if xml is clean
        if(this.parent == null && specialMetaData.get("color") == null) specialMetaData.put("color",genRandomColor());
    }

    public String getPath(){
        if(parent==null) return name;
        else return parent.getPath()+" -> "+name;
    }

    private static String genRandomColor(){
        float saturation = 0.4f;
        float value = 0.95f;
        float hue = r.nextFloat();
        return hsvToRgb(hue,saturation,value);
    }

    public static String hsvToRgb(float hue, float saturation, float value) {
        float r, g, b;

        int h = (int)(hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        if (h == 0) {
            r = value;
            g = t;
            b = p;
        } else if (h == 1) {
            r = q;
            g = value;
            b = p;
        } else if (h == 2) {
            r = p;
            g = value;
            b = t;
        } else if (h == 3) {
            r = p;
            g = q;
            b = value;
        } else if (h == 4) {
            r = t;
            g = p;
            b = value;
        } else if (h == 5) {
            r = value;
            g = p;
            b = q;
        } else {
            throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
        }

        String rs = Integer.toHexString((int)(r * 256));
        String gs = Integer.toHexString((int)(g * 256));
        String bs = Integer.toHexString((int)(b * 256));
        return rs + gs + bs;
    }

    public String getColor(){
        if(parent==null) return specialMetaData.get("color");
        else return parent.getColor();
    }
}
