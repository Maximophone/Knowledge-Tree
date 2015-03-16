package com.maxf.knowledgetree.com.maxf.knowledgetree.engine;

/**
 * Created by Max on 09/03/2015.
 */
public class InfoBit {
    public Category[] categories;
    public Info info;
    public InfoBit(String infoString) throws Exception{
        String[] parts = infoString.split("/");
        this.categories = new Category[parts.length-1];
        for (int i = 0; i < parts.length - 1; i++) {
            categories[i] = new Category(parts[i],i==0?null:categories[i-1]);
        }
        if(parts.length==1){
            categories = new Category[1];
            categories[0] = new Category("Various",null);
        }
        this.info = new Info(parts[parts.length-1],categories[categories.length-1]);
    }
}
