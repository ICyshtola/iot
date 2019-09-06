package com.icarus.iot.model;

public class User {
    //    private int id;
    private String name;
    private String password;
    private String avatar;
    private String email;
    private String mydevices;//List to String
    private String register_time;
    private String last_modified_time;

    public User(String name, String password, String avatar, String email, String register_time, String last_modified_time) {
        this.name = name;
        this.password = password;
        this.avatar = avatar;
        this.email = email;
        this.register_time = register_time;
        this.last_modified_time = last_modified_time;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMydevices() {
        return mydevices;
    }

    public void setMydevices(String mydevices) {
        this.mydevices = mydevices;
    }

    public String getRegister_time() {
        return register_time;
    }

    public void setRegister_time(String register_time) {
        this.register_time = register_time;
    }

    public String getLast_modified_time() {
        return last_modified_time;
    }

    public void setLast_modified_time(String last_modified_time) {
        this.last_modified_time = last_modified_time;
    }
}
