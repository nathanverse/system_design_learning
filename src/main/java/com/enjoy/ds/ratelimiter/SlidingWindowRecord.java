package com.enjoy.ds.ratelimiter;

import lombok.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowRecord {
    private final ConcurrentHashMap<String, List<Long>> slidingWindowRecordMap = new ConcurrentHashMap<>();

    public SlidingWindowRecord(){

    }

    /**
     *
     * @param key
     * @param timestamp
     * @return false if there is value of the key, true otherwise
     */
    public boolean putIfEmpty(String key, Long timestamp){
        synchronized (this){
            if(slidingWindowRecordMap.containsKey(key)){
                return false;
            } else {
                slidingWindowRecordMap.put(key, List.of(timestamp));
                return true;
            }
        }
    }

    public int searchMaximumElementLowerThan(@NonNull List<Long> timestamps, @NonNull Long timestamp){
        if(timestamps.isEmpty()){
            throw new IllegalArgumentException("Can not search in empty list");
        }

        int left = 0;
        int right = timestamps.size() - 1;
        int mid = left + (right - left) / 2;

        while(left < right){
            if((timestamps.get(mid) < timestamp && timestamps.get(mid + 1) >= timestamp)) {
                return mid;
            }

            if((timestamps.get(mid) >= timestamp)){
                right = mid - 1;
            } else {
                left = mid + 1;
            }

            mid = left + (right - left) / 2;
        }

        return -1;
    }

    /**
     *  Clear outdated timestamps of the key from [outDatedPoint]
     *  to before and return the number of request after clearing.
     * @param outDatedPoint: in ms.
     * @return false if there is value of the key, true otherwise
     */
    public int putAndClearOutdatedTimeStamps(String key, Long outDatedPoint, Long newTimeStamp){
        synchronized (this){
            List<Long> timestamps = slidingWindowRecordMap.get(key);
            int maximumOutdatedElementIndex = searchMaximumElementLowerThan(timestamps, outDatedPoint);

            List<Long> timeStampsAfterClear = null;
            if(maximumOutdatedElementIndex == timestamps.size() - 1){
                timeStampsAfterClear = List.of();
            } else {
                timeStampsAfterClear = timestamps.subList(maximumOutdatedElementIndex, timestamps.size());
            }

            timeStampsAfterClear.add(newTimeStamp);

            slidingWindowRecordMap.putIfAbsent(key, timeStampsAfterClear);

            return timeStampsAfterClear.size();
        }
    }
}
