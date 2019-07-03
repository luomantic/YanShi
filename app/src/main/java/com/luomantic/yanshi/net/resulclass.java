package com.luomantic.yanshi.net;

public class resulclass {

    public String cardnum;
    public int an_result;//0:��ʾ�����ɹ�  <0��ʾʧ��
    public int type;//0:��ʾ������� 1:��ʾ������
    public int cmdresult;//0:��ʾ������������ɹ�  <0��ʾ�������ʧ��
    public byte[] buffer;

    public resulclass() {
        // TODO Auto-generated constructor stub

    }

    public String getCardnum() {
        return cardnum;
    }

    public void setCardnum(String cardnum) {
        this.cardnum = cardnum;
    }

    public int getAn_result() {
        return an_result;
    }

    public void setAn_result(int an_result) {
        this.an_result = an_result;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCmdresult() {
        return cmdresult;
    }

    public void setCmdresult(int cmdresult) {
        this.cmdresult = cmdresult;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }


}
