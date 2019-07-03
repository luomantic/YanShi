package com.luomantic.yanshi.bean;

import java.util.List;

public class Program {

    /**
     * playCount : 1
     * textArea : [{"nX":0,"nY":0,"nW":64,"nH":16,"FontSize":3,"FontColor":0,"InType":1,"InSpeed":16,"StayTime":200,"MsgInfo":"分区1"},{"nX":0,"nY":16,"nW":64,"nH":16,"FontSize":1,"FontColor":0,"InType":1,"InSpeed":16,"StayTime":1,"MsgInfo":"分区2"}]
     * picArea : [{"nX":0,"nY":0,"nW":64,"nH":16,"inType":1,"inSpeed":16,"stayTime":200,"filePath":"c: \\1. bmp"},{"nX":0,"nY":0,"nW":64,"nH":16,"inType":1,"inSpeed":16,"stayTime":200,"filePath":"c: \\1. bmp"}]
     */

    private int playCount;
    private List<TextAreaBean> textArea;
    private List<PicAreaBean> picArea;

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public List<TextAreaBean> getTextArea() {
        return textArea;
    }

    public void setTextArea(List<TextAreaBean> textArea) {
        this.textArea = textArea;
    }

    public List<PicAreaBean> getPicArea() {
        return picArea;
    }

    public void setPicArea(List<PicAreaBean> picArea) {
        this.picArea = picArea;
    }

    public static class TextAreaBean {
        /**
         * nX : 0
         * nY : 0
         * nW : 64
         * nH : 16
         * FontSize : 3
         * FontColor : 0
         * InType : 1
         * InSpeed : 16
         * StayTime : 200
         * MsgInfo : 分区1
         */

        private int nX;
        private int nY;
        private int nW;
        private int nH;
        private int fontSize;
        private int fontColor;
        private int inType;
        private int inSpeed;
        private int stayTime;
        private String msgInfo;

        public int getnX() {
            return nX;
        }

        public void setnX(int nX) {
            this.nX = nX;
        }

        public int getnY() {
            return nY;
        }

        public void setnY(int nY) {
            this.nY = nY;
        }

        public int getnW() {
            return nW;
        }

        public void setnW(int nW) {
            this.nW = nW;
        }

        public int getnH() {
            return nH;
        }

        public void setnH(int nH) {
            this.nH = nH;
        }

        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }

        public int getFontColor() {
            return fontColor;
        }

        public void setFontColor(int fontColor) {
            this.fontColor = fontColor;
        }

        public int getInType() {
            return inType;
        }

        public void setInType(int inType) {
            this.inType = inType;
        }

        public int getInSpeed() {
            return inSpeed;
        }

        public void setInSpeed(int inSpeed) {
            this.inSpeed = inSpeed;
        }

        public int getStayTime() {
            return stayTime;
        }

        public void setStayTime(int stayTime) {
            this.stayTime = stayTime;
        }

        public String getMsgInfo() {
            return msgInfo;
        }

        public void setMsgInfo(String msgInfo) {
            this.msgInfo = msgInfo;
        }

        @Override
        public String toString() {
            return "TextAreaBean{" +
                    "nX=" + nX +
                    ", nY=" + nY +
                    ", nW=" + nW +
                    ", nH=" + nH +
                    ", fontSize=" + fontSize +
                    ", fontColor=" + fontColor +
                    ", inType=" + inType +
                    ", inSpeed=" + inSpeed +
                    ", stayTime=" + stayTime +
                    ", msgInfo='" + msgInfo + '\'' +
                    '}';
        }
    }

    public static class PicAreaBean {
        /**
         * nX : 0
         * nY : 0
         * nW : 64
         * nH : 16
         * inType : 1
         * inSpeed : 16
         * stayTime : 200
         * filePath : c: \1. bmp
         */

        private int nX;
        private int nY;
        private int nW;
        private int nH;
        private int inType;
        private int inSpeed;
        private int stayTime;
        private String filePath;

        public int getNX() {
            return nX;
        }

        public void setNX(int nX) {
            this.nX = nX;
        }

        public int getNY() {
            return nY;
        }

        public void setNY(int nY) {
            this.nY = nY;
        }

        public int getNW() {
            return nW;
        }

        public void setNW(int nW) {
            this.nW = nW;
        }

        public int getNH() {
            return nH;
        }

        public void setNH(int nH) {
            this.nH = nH;
        }

        public int getInType() {
            return inType;
        }

        public void setInType(int inType) {
            this.inType = inType;
        }

        public int getInSpeed() {
            return inSpeed;
        }

        public void setInSpeed(int inSpeed) {
            this.inSpeed = inSpeed;
        }

        public int getStayTime() {
            return stayTime;
        }

        public void setStayTime(int stayTime) {
            this.stayTime = stayTime;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }
}
