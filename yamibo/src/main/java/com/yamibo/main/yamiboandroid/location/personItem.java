package com.yamibo.main.yamiboandroid.location;

/**
 * Created by Clover on 2015-11-29.
 */
public class PersonItem {
    Object near_logo = null;
    private String near_id = "安达", near_distance = "1.1KM", near_intro = "雪の季節";

    PersonItem() {
        near_intro = near_intro + Long.toString(System.currentTimeMillis());
    }

    PersonItem(String input_id, String input_distance, String input_intro) {
        //  setLogo(input_img);
        setId(input_id);
        setDistance(input_distance);
        setIntro(input_intro);
    }

    public void setLogo(Object input_img) {
        near_logo = input_img;
    }

    public Object getLogo() {
        return near_logo;
    }

    public void setId(String input_id) {
        near_id = input_id;
    }

    public void setDistance(String input_distance) {
        near_distance = input_distance;
    }

    public void setIntro(String input_intro) {
        near_intro = input_intro;
    }

    public String getId() {
        return near_id;
    }

    public String getIntro() {
        return near_intro;
    }

    public String getDistance() {
        return near_distance;
    }

}
