package com.yamibo.main.yamibolib.model;

/**
 * Created by Remiany on 2015/6/8 0008.
 */
public class UserProfile {
    String cookiepre;
    String auth;
    String saltkey;
    String member_uid;
    String member_username;
    String member_avatar;
    String groupid;
    String formhash;
    String ismoderator;
    String  readaccess;
//    notice
    int newpush;
    int newpm;
    int newprompt;
    int newmypost;

    public String getCookiepre() {
        return cookiepre;
    }

    public void setCookiepre(String cookiepre) {
        this.cookiepre = cookiepre;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getSaltkey() {
        return saltkey;
    }

    public void setSaltkey(String saltkey) {
        this.saltkey = saltkey;
    }

    public String getMember_uid() {
        return member_uid;
    }

    public void setMember_uid(String member_uid) {
        this.member_uid = member_uid;
    }

    public String getMember_username() {
        return member_username;
    }

    public void setMember_username(String member_username) {
        this.member_username = member_username;
    }

    public String getMember_avatar() {
        return member_avatar;
    }

    public void setMember_avatar(String member_avatar) {
        this.member_avatar = member_avatar;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getFormhash() {
        return formhash;
    }

    public void setFormhash(String formhash) {
        this.formhash = formhash;
    }

    public String getIsmoderator() {
        return ismoderator;
    }

    public void setIsmoderator(String ismoderator) {
        this.ismoderator = ismoderator;
    }

    public String getReadaccess() {
        return readaccess;
    }

    public void setReadaccess(String readaccess) {
        this.readaccess = readaccess;
    }

    public int getNewpush() {
        return newpush;
    }

    public void setNewpush(int newpush) {
        this.newpush = newpush;
    }

    public int getNewpm() {
        return newpm;
    }

    public void setNewpm(int newpm) {
        this.newpm = newpm;
    }

    public int getNewprompt() {
        return newprompt;
    }

    public void setNewprompt(int newprompt) {
        this.newprompt = newprompt;
    }

    public int getNewmypost() {
        return newmypost;
    }

    public void setNewmypost(int newmypost) {
        this.newmypost = newmypost;
    }


}
