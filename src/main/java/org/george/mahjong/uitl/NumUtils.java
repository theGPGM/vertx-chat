package org.george.mahjong.uitl;

public class NumUtils {

    public static boolean checkDigit(String roomId){
        if(roomId == null || roomId.length() == 0){
            return false;
        }
        char[] arr = roomId.toCharArray();
        for(int i = 0; i < arr.length; i++){
            if(arr[0] == '-') continue;
            if(arr[i] > '9' || arr[i] < '0') return false;
        }
        return true;
    }
}
