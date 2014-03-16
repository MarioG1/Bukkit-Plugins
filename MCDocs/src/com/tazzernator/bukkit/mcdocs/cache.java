/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tazzernator.bukkit.mcdocs;

import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * @author Mario
 */
public class cache {
    
    private final HashMap<String,ArrayList> cachedfiles = new HashMap<>();
    
    public cache(){
        
    }
    
    /*
    * @param file Name of the chached file
    * @return ArrayList of the cached file if the requestd fiel was not in the chache returns null
    */
    public ArrayList<String> get(String file){
        
        ArrayList<String> list = this.cachedfiles.get(file);        
        return (ArrayList<String>) list.clone();        
    }

    /*
    * @param file Name of the chached file
    * @param content content of the file
    */
    public void add(String file, ArrayList<String> content){
        if(file != null && content !=null){
            this.cachedfiles.put(file, content);
        }
    }
    
    /*
    * @param file Name of the chached file
    * @param content content of the file
    */
    public boolean exist(String file){
        return file != null && this.cachedfiles.get(file) !=null;
    }
    
    /*
    * Removes all entries form the cache
    */
    public void clear(){
        this.cachedfiles.clear();
    }
    
    /*
    * @param file name of the entry that should be removed 
    * Removes only one entry from the cache
    */
    public void remove(String file){
        this.cachedfiles.remove(file);
    }
    
}
