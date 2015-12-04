package com.yamibo.main.yamibolib.Utils;

/**
 * Created by wangxiaoyan on 15/4/20.
 */
public class YMIUtils {

    /**
     * 把带有&nbsp;的字符串转换成字符串" "
     * @param content
     * @return
     */
    public static String convertNbsp2Space(String content){
        return content.replaceAll("&nbsp;"," ");
    }

    /**
     * 把Uid转换成头像地址
     * @param uid
     * @param imgSize 1:small 2:middle 3:big
     * @return
     */
    private static String convertUid2ImgUri(String uid,int imgSize){
        String prefixUri = Environment.PORTRAIT_BASE_ADDRESS;
        prefixUri +="/000";
        StringBuffer buffer = new StringBuffer(uid);
        int uidLength = buffer.length();
        if(uidLength < 6){  //当uid小于6位的时候,在前面填0补足6位
            for (int i = 0;i< 6-uidLength;i++)
                buffer.insert(0,"0");
        }
        buffer.insert(0,"/").insert(3,"/").insert(6,"/");   //加斜杠
        switch (imgSize){
            case 1:buffer.append("_avatar_small.jpg");
                break;
            case 2:buffer.append("_avatar_middle.jpg");
                break;
            case 3:buffer.append("_avatar_big.jpg");
                break;
        }
        buffer.insert(0,prefixUri);
        return buffer.toString();
    }

    public static String convertUid2SmallImgUri(String uid){
        return convertUid2ImgUri(uid,1);
    }
    public static String convertUid2MiddleImgUri(String uid){
        return convertUid2ImgUri(uid,2);
    }
    public static String convertUid2BigImgUri(String uid){
        return convertUid2ImgUri(uid,3);
    }


}
