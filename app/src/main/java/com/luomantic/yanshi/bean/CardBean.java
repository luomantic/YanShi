package com.luomantic.yanshi.bean;

public class CardBean {

    private String cardNum;
    private String ipAddress;
    private int isOnline;

    public CardBean(String cardNum, String ipAddress, int isOnline) {
        this.cardNum = cardNum;
        this.ipAddress = ipAddress;
        this.isOnline = isOnline;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int isOnline() {
        return isOnline;
    }

    public void setOnline(int online) {
        isOnline = online;
    }

    @Override
    public String toString() {
        return "CardBean{" +
                "cardNum='" + cardNum + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }
}
