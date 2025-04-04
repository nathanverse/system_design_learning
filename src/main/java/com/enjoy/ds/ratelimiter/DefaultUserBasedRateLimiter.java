package com.enjoy.ds.ratelimiter;

import com.enjoy.ds.ratelimiter.model.User;

import java.util.List;

public class DefaultUserBasedRateLimiter implements UserBasedRateLimiter{

    SlidingWindowRecord userRequestRecord = new SlidingWindowRecord();


    public DefaultUserBasedRateLimiter(){

    }

    @Override
    public boolean passOrNot(User user, String apiName) {
        String key = key(user, apiName);
        Long requestTimeStamp = System.currentTimeMillis();

        if(key.isEmpty()){
            throw new IllegalStateException("Key can not be empty");
        }


        if(userRequestRecord.putIfEmpty(key, requestTimeStamp)){
            return true;
        } else {


        }

        return false;
    }

    private String key(User user, String apiName){
        return apiName + ":" + user.getUsername();
    }
}
