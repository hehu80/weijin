/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.huhehu.weijin.wechat.session;

import java.util.concurrent.ThreadFactory;

/**
 *
 * @author henning
 */
public class WeChatSessionThreadFactory implements ThreadFactory {
    private String identifier;
    private int index;
    
    public WeChatSessionThreadFactory(String identifier){
        this.identifier = identifier;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(identifier + "-" + ++index);
        return thread;
    }       
}
