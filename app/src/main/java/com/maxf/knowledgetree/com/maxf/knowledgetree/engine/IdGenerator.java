package com.maxf.knowledgetree.com.maxf.knowledgetree.engine;

import android.content.Context;

/**
 * Created by Max on 16/03/2015.
 */
public class IdGenerator {
    private FileIO fileIO;
    private String url = "counter.txt";
    public IdGenerator(Context context){
        fileIO = new FileIO(url, context, "0");
    }
    public int getNew(){
        String current_string = fileIO.readFromFile();
        int new_id = Integer.valueOf(current_string)+1;
        fileIO.writeToFile(String.valueOf(new_id));
        return new_id;
    }
}
