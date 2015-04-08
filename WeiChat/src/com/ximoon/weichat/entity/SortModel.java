package com.ximoon.weichat.entity;

import android.util.Log;

public class SortModel {  
  
    private ClientInfo info;   //显示的数据  
    private String sortLetters;  //显示数据拼音的首字母  
      
    public ClientInfo getClientInfo() {  
        return info;  
    }  
    public void setClientInfo(ClientInfo info) {  
        this.info = info;  
    }  
    public String getSortLetters() {  
        return sortLetters;  
    }  
    public void setSortLetters(String sortLetters) {  
        this.sortLetters = sortLetters;  
    }  
}  