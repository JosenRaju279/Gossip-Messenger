package com.example.gossipmessenger;

public class Users {
    String profilepic, mails, userName, password, userId, lastMessage, status;

    public Users() {}

    public Users(String profilepic, String mails, String userName, String password, String userId, String status) {
        this.profilepic = profilepic;
        this.mails = mails;
        this.userName = userName;
        this.password = password;
        this.userId = userId;
        this.status = status;
    }

    // Getters and setters
    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getMail() {
        return mails;
    }

    public void setMail(String mail) {
        this.mails = mail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
