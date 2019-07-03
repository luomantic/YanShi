package com.luomantic.yanshi.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Environment;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

//import java.io.BufferedReader;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.InputStreamReader;
//import java.net.ServerSocket;
//import java.net.Socket;
public class makepacket {
    class time_struct {
        public int nBeginTImeHH; //��ʼʱ��Сʱ
        public int nBeginTImeMM; //��ʼʱ���
        public int nBeginTImeSS; //��ʼʱ����
        public int nEndTImeHH; //����ʱ��Сʱ
        public int nEndTImeMM; //����ʱ���
        public int nEndTImeSS; //����ʱ����
    }

    ;

    class areaInfo_struct {
        public int nMAreaID;  //������
        public int nX;       //X
        public int nY;       //Y
        public int nW;      //W
        public int nH;      //H
        public int actType; // TEXT_AREA    0 �ı�����/1PIC_AREA ͼƬģʽGIF_AREA 	3CLOCK_AREA3/4ʱ��ģʽʱ��/5����ʱ/6��ͨ�ֲ� /7����Ԥ��ͼƬ����
        //int Bag;  //����  0��ʾ��ʼ��������
        public int PageCount; //��ҳ�� �����ʱ����Ч
        public int FontType;//  ��������   1�ֽڸ�λ
        public int FontColor;//  ������ɫ   1�ֽڵ�λ0��1��2��
        public int InType;//   ���뷽ʽ   1�ֽ�------------------------------------------------------------
        public int inSpeed;//  �����ٶ�   1�ֽ�------------------------------------------------------------
        public int outType;//  �˳���ʽ   1�ֽ�
        public int outSpeed;// �˳��ٶ�   1�ֽ�
        public int stayTime;//  ͣ��ʱ��  1�ֽ�   FFΪһֱ��ʾ
        public int defAlignW;//��ֹʱ����Ч ÿ�еĿ��
        public int defAlignH;//��ֹ��ʱ����Ч��ÿ�еĸ߶�
        public int isdataformat;
        public int datataformat;
        public int istimeformat;
        public int timeformat;
        public int isweekformat;
        public int weekformat;
        public int shicha_hour;
        public int shicha_minute;
        public int shicha;
        public int h_duiqi;//0:����� 1���Ҷ��� 2��ˮƽ����
        public int v_duiqi;//0:�϶��� 1���¶��� 2����ֱ����
        public byte pcontext[] = new byte[4096];//�����ͼƬ����ֱ�Ӵ���ͼƬ��Ϣ ������ı��������ı�����
        public int context_len;
    }

    ;

    class program {
        public int nActID;
        public int nAreaIndex;

        public int nMaCount;//��̬��������
        public int nConfigType;//0������ 1��ʱ��

        public int nDispTimes;//��ʾ����;---------------------
        public int nHaveOut;//�Ƿ������
        public int nweekList;//��������
        public int nTimeCount;//ʱ����------------


        public int nHaveFrame;//0:�ޱ߿�
        public int nFrameColor;//�߿���ɫ��
        public int nFrameSpeed;//�߿��ٶ�
        public int nFrmaeType;//�߿����з�ʽ
        public int nMsgTitle; //����
        public int nState;  //״̬
        public long dBeginTime;//��Ŀ��ʼʱ�� time_t
        public long dEndTime;//��Ŀ����ʱ�� time_t
        public time_struct[] timeobj = new time_struct[8];
        public areaInfo_struct[] areaInfo = new areaInfo_struct[8];
        public int voice_len;
        public byte voice_buf[] = new byte[2048];
    }

    WifiManager.MulticastLock lock = null;

    public class cardinfo {
        String cardnum;
        String cardip;
        int port;
        int isonline;
        int curtime;
    }

    public class Resultinfo {
        String strcard;
        byte cmd;
        int result;
    }

    DatagramSocket socketobj = null;
    LinkedList resultarray = new LinkedList();
    int opreuslt = 0;
    Queue<cardinfo> cardlist = new ArrayDeque<cardinfo>();
    public byte buffer[] = new byte[4096];
    public int bufflen;

    private String m_strvoice;
    private int m_isplayvoice;
    public short datalen_index;
    private program m_pro = new program();

    short sumcheck(byte[] buf, int len, int is232) {
        int i;
        short sum = 0;
        int l = 0;
        if (is232 == 1)
            l = 0;
        else l = 14;
        for (i = l; i < len; i++) {
            sum += buf[i];
        }
        sum = (short) (sum ^ 0xff);
        return sum;
    }

    public makepacket() {
        int i;
        for (i = 0; i < 8; i++) {
            m_pro.areaInfo[i] = new areaInfo_struct();
        }
        for (i = 0; i < 8; i++) {
            m_pro.timeobj[i] = new time_struct();
        }

        m_pro.nMaCount = 0;
        m_pro.nHaveOut = 0;
        m_pro.nActID = 0;
        m_pro.nConfigType = 0;
        m_pro.nDispTimes = 0xff;
        bufflen = 0;
    }

    int makepackhead(String cardno, int command, int is232) {
        int i, len;
        byte[] cn;
        try {
            cn = cardno.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        bufflen = 0;
        len = cn.length;
        if (len > 12)
            len = 12;
        if (is232 == 0) {
            buffer[bufflen++] = (byte) 0xaa;
            buffer[bufflen++] = (byte) 0xbb;
            for (i = 0; i < len; i++) {
                buffer[bufflen++] = (byte) cn[i];
            }
            for (; i < 12; i++) {
                buffer[bufflen++] = 0;
            }
        }
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = (byte) 0xff;
        buffer[bufflen++] = (byte) 0x13;
        buffer[bufflen++] = (byte) command;
        return 0;
    }

    public int SetVoplayvoice(int isplayvoice, String strvoice) {
        m_pro.nHaveOut = isplayvoice;
        this.m_strvoice = strvoice;
        return 0;
    }

    public int initialProgram(int nConfigType, int Times, int ProgramNo) {
        m_pro.nActID = (byte) ProgramNo;
        m_pro.nConfigType = (byte) nConfigType;
        m_pro.nDispTimes = (byte) Times;
        return 0;
    }

    int makeprogram() {
        datalen_index = (short) bufflen;
        bufflen++;
        bufflen++;
        buffer[bufflen++] = 0;//�汾
        buffer[bufflen++] = 0;
        buffer[bufflen++] = (byte) m_pro.nActID;//��Ŀ��
        buffer[bufflen++] = 0;
        buffer[bufflen++] = (byte) m_pro.nConfigType;
        buffer[bufflen++] = (byte) m_pro.nDispTimes;

        buffer[bufflen++] = (byte) m_pro.nHaveOut;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) m_pro.nMaCount;
        return 0;
    }

    int MakeTail(int is232) {
        if (m_pro.nHaveOut == 1) {
            int voice_len;
            byte unicode[] = null;
            int len = 0, i;
            try {
                unicode = m_strvoice.getBytes("utf-16LE");
                voice_len = unicode.length;
            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();
                return -2;
            }
            if ((voice_len < 0) || (voice_len > 256))
                voice_len = 0;
            buffer[bufflen++] = (byte) (voice_len & 0x000000ff);
            buffer[bufflen++] = (byte) ((voice_len & 0x0000ff00) >> 8);
            if (voice_len > 0) {
                for (i = 0; i < len; i++) {
                    buffer[bufflen++] = unicode[i];
                }
            }
        }
        buffer[bufflen++] = 0;
        buffer[bufflen++] = 0;
        this.buffer[datalen_index] = (byte) (bufflen - datalen_index - 2);
        this.buffer[datalen_index + 1] = (byte) (((bufflen - datalen_index - 2) & 0xff00) >> 8);

        buffer[bufflen++] = (byte) sumcheck(buffer, bufflen, is232);
        buffer[bufflen++] = (byte) 0xef;
        if (is232 == 0) {
            buffer[bufflen++] = (byte) 0xbb;
            buffer[bufflen++] = (byte) 0xaa;
        }
        return 0;
    }

    public int AnalyzeRecvBuf(byte[] buffer, int len) {
        int ret = 0;
        if ((buffer[0] != (byte) 0xeb) || (buffer[2] != (byte) 0x90))
            return -1;//�޷������İ�
        if ((buffer[7] != 0x01) && (buffer[7] != 0x02))
            return -2;//����ʧ��
        ret = (int) buffer[8];
        return ret;
    }

    ;

    public int OperateRelayTo232Buf(byte[] buffer232, byte registerid, byte openorclose) {
        int len = 0;
        buffer232[len++] = (byte) 0xeb;
        buffer232[len++] = (byte) 0x90;
        buffer232[len++] = (byte) 0xff;
        buffer232[len++] = (byte) 0x13;
        buffer232[len++] = (byte) 0xA0;
        buffer232[len++] = 2;
        buffer232[len++] = 0;
        buffer232[len++] = registerid;
        buffer232[len++] = openorclose;
        byte sun = (byte) (sumcheck(buffer232, len, 1) & 0x00ff);
        buffer232[len++] = sun;
        buffer232[len++] = (byte) 0xef;
        return len;
    }

    public int TimeCalibrationTo232Buf(byte[] buffer232, short year, short month, short day, short hour, short minute, short second, short week) {
        int len = 0;
        buffer232[len++] = (byte) 0xeb;
        buffer232[len++] = (byte) 0x90;
        buffer232[len++] = (byte) 0xff;
        buffer232[len++] = (byte) 0x13;
        buffer232[len++] = (byte) 0x14;
        //EB 90 FF 13 14 09 00 DD 07 09 0B 0B 14 06 04 00 34 EF
        buffer232[len++] = (byte) 0x09;
        buffer232[len++] = (byte) 0x00;
        buffer232[len++] = (byte) (year & 0x000000ff);
        buffer232[len++] = (byte) ((year >> 8) & 0x000000ff);
        buffer232[len++] = (byte) (month & 0x000000ff);
        buffer232[len++] = (byte) (day & 0x000000ff);
        buffer232[len++] = (byte) (hour & 0x000000ff);
        buffer232[len++] = (byte) (minute & 0x000000ff);
        buffer232[len++] = (byte) (second & 0x000000ff);
        buffer232[len++] = (byte) (week & 0x000000ff);
        buffer232[len++] = (byte) 0;
        byte sun = (byte) (sumcheck(buffer232, len, 1) & 0x00ff);
        buffer232[len++] = (byte) sun;
        buffer232[len++] = (byte) 0xef;
        return len;
    }

    public int setBright(String host, String strcard, int value) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        //EB 90 FF 13 14 09 00 DD 07 09 0B 0B 14 06 04 00 34 EF
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = 0x13;
        buffer[bufflen++] = 0x1c;
        buffer[bufflen++] = 02;
        buffer[bufflen++] = 0;
        buffer[bufflen++] = 1;
        buffer[bufflen++] = (byte) value;
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        try {
            len = SendProgramByUdp(host, strcard);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return len;
    }

    public byte[] setBrightToArray(String host, String strcard, int value) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        //EB 90 FF 13 14 09 00 DD 07 09 0B 0B 14 06 04 00 34 EF
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = 0x13;
        buffer[bufflen++] = 0x1c;
        buffer[bufflen++] = 02;
        buffer[bufflen++] = 0;
        buffer[bufflen++] = 1;
        buffer[bufflen++] = (byte) value;
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        byte[] buf = new byte[bufflen];
        for (i = 0; i < bufflen; i++)
            buf[i] = buffer[i];
        return buf;
    }

    public int SetVolum(String host, String strcard, int value) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        //EB 90 FF 13 14 09 00 DD 07 09 0B 0B 14 06 04 00 34 EF
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = 0x13;
        buffer[bufflen++] = (byte) 0x83;
        buffer[bufflen++] = 2;
        buffer[bufflen++] = 0;
        buffer[bufflen++] = 1;
        buffer[bufflen++] = (byte) value;
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        try {
            len = SendProgramByUdp(host, strcard);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return len;
    }

    public byte[] SetVolumToArray(String host, String strcard, int value) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        //EB 90 FF 13 14 09 00 DD 07 09 0B 0B 14 06 04 00 34 EF
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = 0x13;
        buffer[bufflen++] = (byte) 0x83;
        buffer[bufflen++] = 2;
        buffer[bufflen++] = 0;
        buffer[bufflen++] = 1;
        buffer[bufflen++] = (byte) value;
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        byte[] buf = new byte[bufflen];
        for (i = 0; i < bufflen; i++)
            buf[i] = buffer[i];
        return buf;
    }

    public int TimeCalibration(String strcard, String strIP, short year, short month, short day, short hour, short minute, short second, short week) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[len++] = (byte) 0xeb;
        buffer[len++] = (byte) 0x90;
        buffer[len++] = (byte) 0xff;
        buffer[len++] = (byte) 0x13;
        buffer[len++] = (byte) 0x14;
        //EB 90 FF 13 14 09 00 DD 07 09 0B 0B 14 06 04 00 34 EF
        buffer[bufflen++] = (byte) 0x09;
        buffer[bufflen++] = (byte) 0x00;
        buffer[bufflen++] = (byte) (year & 0x000000ff);
        buffer[bufflen++] = (byte) ((year >> 8) & 0x000000ff);
        buffer[bufflen++] = (byte) (month & 0x000000ff);
        buffer[bufflen++] = (byte) (day & 0x000000ff);
        buffer[bufflen++] = (byte) (hour & 0x000000ff);
        buffer[bufflen++] = (byte) (minute & 0x000000ff);
        buffer[bufflen++] = (byte) (second & 0x000000ff);
        buffer[bufflen++] = (byte) (week & 0x000000ff);
        buffer[bufflen++] = (byte) 0;
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        try {
            len = SendProgramByUdp(strIP, strcard);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return len;

    }

    public byte[] TimeCalibrationToArray(String strcard, String strIP, short year, short month, short day, short hour, short minute, short second, short week) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[len++] = (byte) 0xeb;
        buffer[len++] = (byte) 0x90;
        buffer[len++] = (byte) 0xff;
        buffer[len++] = (byte) 0x13;
        buffer[len++] = (byte) 0x14;
        //EB 90 FF 13 14 09 00 DD 07 09 0B 0B 14 06 04 00 34 EF
        buffer[bufflen++] = (byte) 0x09;
        buffer[bufflen++] = (byte) 0x00;
        buffer[bufflen++] = (byte) (year & 0x000000ff);
        buffer[bufflen++] = (byte) ((year >> 8) & 0x000000ff);
        buffer[bufflen++] = (byte) (month & 0x000000ff);
        buffer[bufflen++] = (byte) (day & 0x000000ff);
        buffer[bufflen++] = (byte) (hour & 0x000000ff);
        buffer[bufflen++] = (byte) (minute & 0x000000ff);
        buffer[bufflen++] = (byte) (second & 0x000000ff);
        buffer[bufflen++] = (byte) (week & 0x000000ff);
        buffer[bufflen++] = (byte) 0;
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        byte[] buf = new byte[bufflen];
        for (i = 0; i < bufflen; i++)
            buf[i] = buffer[i];
        return buf;

    }

    public int GetPowerNumberTo232Buf(byte[] buffer232) {
        int len = 0;
        short ret = 0;
        buffer232[len++] = (byte) 0xeb;
        buffer232[len++] = (byte) 0x90;
        buffer232[len++] = (byte) 0xff;
        buffer232[len++] = (byte) 0x13;
        buffer232[len++] = (byte) 0xf0;
        buffer232[len++] = (byte) 0x01;
        buffer232[len++] = (byte) 0x00;
        buffer232[len++] = (byte) 0x00;
        ret = (short) (sumcheck(buffer232, len, 1) & 0x00ff);
        buffer232[len++] = (byte) ret;
        buffer232[len++] = (byte) 0xef;
        return len;
    }

    int MakeProgramTextZero(int index) {
        String s = new String(m_pro.areaInfo[index].pcontext);
        byte unicode[] = null;
        int len = 0, i;
        try {
            unicode = s.getBytes("utf-16LE");
            len = unicode.length;
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
            return -2;
        }
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].nMAreaID;//���� ��
        buffer[bufflen++] = (byte) (len & 0x00ff);
        buffer[bufflen++] = (byte) ((len >> 8) & 0x00ff);//��Ŀ��
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nX & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nX >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nY & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nY >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nW & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nW >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nH & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nH >> 8) & 0x00ff);

        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) 0;

        buffer[bufflen++] = (byte) m_pro.areaInfo[index].FontType;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].FontColor;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].InType;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].inSpeed;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].outType;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].outSpeed;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].stayTime;
        //len=m_pro.areaInfo[index].context_len;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = unicode[i];
        }
        return 0;
    }

    int MakeProgramTimeZero(int index) {
        int len = 0x0c;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].nMAreaID;//���� ��
        buffer[bufflen++] = (byte) (len & 0x00ff);
        buffer[bufflen++] = (byte) ((len >> 8) & 0x00ff);//��Ŀ��
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nX & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nX >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nY & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nY >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nW & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nW >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nH & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nH >> 8) & 0x00ff);

        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) 0;

        buffer[bufflen++] = (byte) m_pro.areaInfo[index].FontType;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].FontColor;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].InType;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].inSpeed;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].outType;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].outSpeed;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].stayTime;

        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].datataformat & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].datataformat >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].timeformat & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].timeformat >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].weekformat & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].weekformat >> 8) & 0x00ff);

        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].shicha & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].shicha >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].shicha_hour & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].shicha_hour >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].shicha_minute & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].shicha_minute >> 8) & 0x00ff);
        return 0;
    }

    int MakeProgramTableZero(int i) {
        return 0;
    }

    //@SuppressWarnings("resource")
    int GetFileBufferFromFile(byte[] filebuf, String filename) {

        int len;

        File f = new File(filename);
        RandomAccessFile rfd = null;

        try {
            rfd = new RandomAccessFile(f, "r");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            System.out.println(filename);
            System.out.println("�ļ�ʧ��");
            return -1;
        }
        try {
            len = (int) rfd.length();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("�ļ�changduʧ��");
            return -2;
        }

        for (int i = 0; i < len; i++) {
            try {
                filebuf[i] = rfd.readByte();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("�ļ�read changduʧ��");
                return -1;
            }
        }
        try {
            rfd.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return -2;
        }
        return len;
    }

    int MakeProgramPicZero(int index) {
        String strtemp = Environment.getExternalStorageDirectory().getPath() + "/aaa.jpg";
        byte filebuf[] = new byte[2048];
        int len, i;
        String s = new String(m_pro.areaInfo[index].pcontext);
        //����ͼƬ����
        //Mat image = Imgcodecs.imread(s);
//        Mat initMat = new Mat();
        // Imgproc.resize(src, dst, new Size(src.cols()/2,src.rows()/2), 0, 0, Imgproc.INTER_AREA);
        System.out.print("m_pro.areaInfo[index].nH=" + m_pro.areaInfo[index].nH + "  m_pro.areaInfo[index].nW=" + m_pro.areaInfo[index].nW + " index=" + index + "\r\n");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(fis);
//        Imgproc.resize(image, initMat, new Size(m_pro.areaInfo[index].nH, m_pro.areaInfo[index].nW), 0, 0, Imgproc.INTER_AREA);
//        Imgcodecs.imwrite(strtemp, initMat);
        Bitmap small = ImageUtils.scale(bitmap, 32, 32);
        try {
            FileOutputStream out = new FileOutputStream(new File(strtemp));
            boolean compress = small.compress(Bitmap.CompressFormat.JPEG, 60, out);
            if (compress) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        len = GetFileBufferFromFile(filebuf, strtemp);
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].nMAreaID;//���� ��
        buffer[bufflen++] = (byte) (len & 0x00ff);
        buffer[bufflen++] = (byte) ((len >> 8) & 0x00ff);//��Ŀ��


        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nX & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nX >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nY & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nY >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nW & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nW >> 8) & 0x00ff);
        buffer[bufflen++] = (byte) (m_pro.areaInfo[index].nH & 0x00ff);
        buffer[bufflen++] = (byte) ((m_pro.areaInfo[index].nH >> 8) & 0x00ff);

        buffer[bufflen++] = (byte) 1;
        buffer[bufflen++] = (byte) 1;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].InType;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].inSpeed;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].outType;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].outSpeed;
        buffer[bufflen++] = (byte) m_pro.areaInfo[index].stayTime;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = filebuf[i];
        }

        return 0;
    }

    int MakeProgramZero() {
        int i, ret;
        int maxstaytime = 0;
        for (i = 0; i < m_pro.nMaCount; i++)//��������ͣ��ʱ��
        {
            if (maxstaytime < m_pro.areaInfo[i].stayTime)
                maxstaytime = m_pro.areaInfo[i].stayTime;
        }
        m_pro.areaInfo[0].stayTime = maxstaytime;
        for (i = 0; i < m_pro.nMaCount; i++) {
            switch (m_pro.areaInfo[i].actType) {
                case 0://�ı�����
                {
                    ret = MakeProgramTextZero(i);
                    if (ret < 0)
                        return ret;
                    break;
                }
                case 1://ͼƬ����
                {
                    ret = MakeProgramPicZero(i);
                    if (ret < 0)
                        return ret;
                    break;
                }
                case 2://gif����
                {
                    return -2;
                }
                case 3://���̷���
                {
                    return -3;
                }
                case 4://����ʱ��
                {
                    ret = MakeProgramTimeZero(i);
                    if (ret < 0)
                        return ret;
                    break;
                }
                case 5://����ʱ
                {
                    return -5;
                }
                case 6://����ͼ��(��ͨ����)
                {
                    ret = MakeProgramTableZero(i);
                    if (ret < 0)
                        return ret;
                    break;
                }
                default: {
                    return -100;
                }
            }
        }
        return 0;
    }

    /*public int SendProgramByUdp(String ipaddr,String cardno) throws IOException
	{
		int ret=0;
		byte[] buf=new byte[1024];
		DatagramPacket rp=new DatagramPacket(buf,1024);
		DatagramSocket socketobj=null;
		try
		{
			socketobj=new DatagramSocket();
		}
		catch (SocketException e)
		{
			// TODO Auto-generated catch block
			System.out.print("sadfa1");
			e.printStackTrace();
			return -1;
		}

		DatagramPacket dp=new DatagramPacket(buffer,bufflen,InetAddress.getByName(ipaddr),9005);
		socketobj.send(dp);
		socketobj.setSoTimeout(5000);
		try
		{
			socketobj.receive(rp);
		}
		catch(InterruptedIOException e)
		{
            //�����������ʱ������ʱ���ط�������һ���ط��Ĵ���
			System.out.print("timeout");
		   socketobj.close();
           return -2;
        }
		ret=rp.getLength();
		socketobj.close();
		if(ret<14)
		{
			System.out.print("error");
			return -3;
		}
		if((buf[21]==0x01)||(buf[21]==0x02))
		{
			System.out.print("success");
		    return 0;
		}
		else System.out.print("errorcode ");
		return buf[21];
	}
	*/
    public int getSecondTimestampTwo(Date date) {
        if (null == date) {
            return 0;
        }
        String timestamp = String.valueOf(date.getTime() / 1000);
        return Integer.valueOf(timestamp);
    }

    void memcpy(byte[] srcbuf, int startsrcindex, byte[] dstbuf, int startdstx, int len) {
        int i;
        for (i = 0; i < len; i++)
            dstbuf[startdstx + i] = srcbuf[startsrcindex + i];
    }

    public int SendProgramByUdp(String ipaddr, String cardno) throws IOException {

        int ret = 0, i, j;
        byte[] sbuf = new byte[1800];
        int totallen = bufflen - 27;
        int num = totallen / 1022;
        if ((totallen % 1022) > 0)
            num++;
        System.out.print(totallen + "break:" + bufflen + "\r\n");

        for (i = 0; i < (num - 1); i++) {
            //打包
            memcpy(buffer, 0, sbuf, 0, 19);

            sbuf[20] = 0x04;
            sbuf[19] = 0;//长度为1024;
            sbuf[21] = (byte) num;
            sbuf[22] = (byte) i;
            //if(i==0)
            memcpy(buffer, 23 + i * 1022, sbuf, 23, 1022);
            //else System.arraycopy(sbuf,23,buf,21+i*1022,1022);
            short k = sumcheck(sbuf, 1022 + 23, 0);
            sbuf[1022 + 23] = (byte) k;
            sbuf[1022 + 24] = (byte) 0xef;
            sbuf[1022 + 25] = (byte) 0xbb;
            sbuf[1022 + 26] = (byte) 0xaa;
            DatagramPacket dp = new DatagramPacket(sbuf, 1049, InetAddress.getByName(ipaddr), 9005);

            opreuslt = 0;
            // synchronized(socketobj)
            {
                socketobj.send(dp);
            }


            System.out.print("send " + i + " break:" + opreuslt + "\r\n");
            //int j,jj;
            for (j = 0; j < 1000; j++) {
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (opreuslt != 0)
                    break;
            }
            if (j == 1000) {

                return -20;
            }
            if (opreuslt == -1) {

                return -10;
            }

        }
        if (buffer[18] == 0x20) {
            memcpy(buffer, 0, sbuf, 0, 19);
            int lk = totallen % 1022 + 2;
            sbuf[20] = (byte) (lk >> 8);
            sbuf[19] = (byte) lk;//长度为1024;
            sbuf[21] = (byte) num;
            sbuf[22] = (byte) i;
            memcpy(buffer, 23 + i * 1022, sbuf, 23, totallen % 1022);

            short k = sumcheck(sbuf, totallen % 1022 + 23, 0);
            sbuf[totallen % 1022 + 23] = (byte) k;
            sbuf[totallen % 1022 + 24] = (byte) 0xef;
            sbuf[totallen % 1022 + 25] = (byte) 0xbb;
            sbuf[totallen % 1022 + 26] = (byte) 0xaa;
            DatagramPacket dp = new DatagramPacket(sbuf, totallen % 1022 + 27, InetAddress.getByName(ipaddr), 9005);
            opreuslt = 0;
            //synchronized(socketobj)
            {
                socketobj.send(dp);
            }
            System.out.print("send end===========" + ipaddr + "===========\r\n");
            for (j = 0; j < 1000; j++) {
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (opreuslt != 0)
                    break;
            }
            if (j == 1000) {

                return -20;
            }
            if (opreuslt == -1) {

                return -10;
            }
            return 1;
        } else {

            DatagramPacket dp = new DatagramPacket(buffer, bufflen, InetAddress.getByName(ipaddr), 9005);
            opreuslt = 0;
            //synchronized(socketobj)
            {
                socketobj.send(dp);
            }
            for (j = 0; j < 1000; j++) {
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (opreuslt != 0)
                    break;
            }
            if (j == 1000) {

                return -20;
            }

            if (opreuslt == -1) {

                return -10;
            }
            return 1;
        }

    }


    public int SetHotInfo(String strcard, String strIP, String hotname, String strPassword) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        len = cn.length;
        if (len > 12)
            len = 12;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = (byte) 0xff;
        buffer[bufflen++] = (byte) 0x13;
        buffer[bufflen++] = (byte) 0x37;

        buffer[bufflen++] = (byte) 96;
        buffer[bufflen++] = (byte) 0;

        byte[] usr;
        try {
            usr = hotname.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        len = usr.length;
        if (len > 63)
            len = 63;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) usr[i];
        }
        for (; i < 64; i++) {
            buffer[bufflen++] = 0;
        }
        byte[] pwd;
        try {
            pwd = strPassword.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        len = pwd.length;
        if (len > 31)
            len = 31;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) pwd[i];
        }
        for (; i < 32; i++) {
            buffer[bufflen++] = 0;
        }
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        try {
            len = SendProgramByUdp(strIP, strcard);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return len;
    }

    public int ClearAllProgram(String strcard, String strIP) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = (byte) 0xff;
        buffer[bufflen++] = (byte) 0x13;

        buffer[bufflen++] = (byte) 0x2e;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) 0;
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        try {
            len = SendProgramByUdp(strIP, strcard);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return len;
    }

    public byte[] ClearAllProgramToArray(String strcard, String strIP) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = (byte) 0xff;
        buffer[bufflen++] = (byte) 0x13;

        buffer[bufflen++] = (byte) 0x2e;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) 0;
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        byte[] buf = new byte[bufflen];
        for (i = 0; i < bufflen; i++)
            buf[i] = buffer[i];
        return buf;
    }

    public byte[] DeleteOneProgramToArray(String strcard, String strIP, int programno) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = (byte) 0xff;
        buffer[bufflen++] = (byte) 0x13;

        buffer[bufflen++] = (byte) 0x2f;
        buffer[bufflen++] = (byte) 2;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) (programno & 0x000000ff);
        buffer[bufflen++] = (byte) ((programno & 0x0000ff00) >> 8);
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        byte[] buf = new byte[bufflen];
        for (i = 0; i < bufflen; i++)
            buf[i] = buffer[i];
        return buf;
    }

    public int DeleteOneProgram(String strcard, String strIP, int programno) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = (byte) 0xff;
        buffer[bufflen++] = (byte) 0x13;

        buffer[bufflen++] = (byte) 0x2f;
        buffer[bufflen++] = (byte) 2;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) (programno & 0x000000ff);
        buffer[bufflen++] = (byte) ((programno & 0x0000ff00) >> 8);
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        try {
            len = SendProgramByUdp(strIP, strcard);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return len;
    }

    public int OperateRelay(String strcard, String strIP, int registerid, int openorclose) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = (byte) 0xff;
        buffer[bufflen++] = (byte) 0x13;

        buffer[bufflen++] = (byte) 0xA0;
        buffer[bufflen++] = (byte) 2;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) (registerid);
        buffer[bufflen++] = (byte) (openorclose);
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        try {
            len = SendProgramByUdp(strIP, strcard);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return len;
    }

    public byte[] OperateRelayToArray(String strcard, String strIP, int registerid, int openorclose) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = (byte) 0xff;
        buffer[bufflen++] = (byte) 0x13;

        buffer[bufflen++] = (byte) 0xA0;
        buffer[bufflen++] = (byte) 2;
        buffer[bufflen++] = (byte) 0;
        buffer[bufflen++] = (byte) (registerid);
        buffer[bufflen++] = (byte) (openorclose);
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        byte[] buf = new byte[bufflen];
        for (i = 0; i < bufflen; i++)
            buf[i] = buffer[i];
        return buf;
    }

    int printdata() {
        int i, l, j, n, index;
        String s = new String("");
        l = this.bufflen / 16;
        j = this.bufflen % 16;
        s = "";
        index = 0;
        for (i = 0; i < l; i++) {

            for (n = 0; n < 16; n++) {
                s += Integer.toHexString((int) this.buffer[index++]);
                s += " ";
            }
            System.out.println(s);
            s = "";
        }
        if (j > 0) {
            for (n = 0; n < j; n++) {
                s += Integer.toHexString((int) this.buffer[index++]);
                s += " ";
            }
            System.out.println(s);
            s = "";
        }
        s = "=================================================";
        System.out.println(s);
        return 0;
    }

    public int OpenOrCloseLedTo232Buf(int isopen, byte[] outbuffer) {
        int len = 0;
        outbuffer[len++] = (byte) 0xeb;
        outbuffer[len++] = (byte) 0x90;
        outbuffer[len++] = (byte) 0xff;
        outbuffer[len++] = (byte) 0x13;
        if (isopen == 1) {
            outbuffer[len++] = (byte) 0x13;
            outbuffer[len++] = (byte) 0;
            outbuffer[len++] = (byte) 0;
        } else {
            outbuffer[len++] = (byte) 0x12;
            outbuffer[len++] = (byte) 0;
            outbuffer[len++] = (byte) 0;
        }
        outbuffer[len++] = (byte) sumcheck(outbuffer, len, 1);
        outbuffer[len++] = (byte) 0xef;
        return len;
    }

    public byte[] OpenOrCloseLedToArray(String strcard, String strIP, int isopen) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = (byte) 0xff;
        buffer[bufflen++] = (byte) 0x13;
        if (isopen == 1) {
            buffer[bufflen++] = (byte) 0x13;
            buffer[bufflen++] = (byte) 0;
            buffer[bufflen++] = (byte) 0;
        } else {
            buffer[bufflen++] = (byte) 0x12;
            buffer[bufflen++] = (byte) 0;
            buffer[bufflen++] = (byte) 0;
        }
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        byte[] buf = new byte[bufflen];
        for (i = 0; i < bufflen; i++)
            buf[i] = buffer[i];
        return buf;
    }

    public int OpenOrCloseLed(String strcard, String strIP, int isopen) {
        int i, len;
        bufflen = 0;
        byte[] cn;
        try {
            cn = strcard.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        len = cn.length;
        buffer[bufflen++] = (byte) 0xaa;
        buffer[bufflen++] = (byte) 0xbb;
        for (i = 0; i < len; i++) {
            buffer[bufflen++] = (byte) cn[i];
        }
        for (; i < 12; i++) {
            buffer[bufflen++] = 0;
        }
        buffer[bufflen++] = (byte) 0xeb;
        buffer[bufflen++] = (byte) 0x90;
        buffer[bufflen++] = (byte) 0xff;
        buffer[bufflen++] = (byte) 0x13;
        if (isopen == 1) {
            buffer[bufflen++] = (byte) 0x13;
            buffer[bufflen++] = (byte) 0;
            buffer[bufflen++] = (byte) 0;
        } else {
            buffer[bufflen++] = (byte) 0x12;
            buffer[bufflen++] = (byte) 0;
            buffer[bufflen++] = (byte) 0;
        }
        i = sumcheck(buffer, bufflen, 0);
        buffer[bufflen++] = (byte) i;
        buffer[bufflen++] = (byte) 0xef;
        buffer[bufflen++] = (byte) 0xbb;
        buffer[bufflen++] = (byte) 0xaa;
        try {
            len = SendProgramByUdp(strIP, strcard);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
        return len;
    }

    //������ַ���
    public int AddTextZero(int left, int top, int width, int high) {
        int index;
        index = m_pro.nMaCount;
        if (m_pro.nMaCount > 4)
            return -1;
        m_pro.areaInfo[index].actType = 0;
        m_pro.areaInfo[index].nMAreaID = index;
        m_pro.areaInfo[index].nX = left;
        m_pro.areaInfo[index].nY = top;
        m_pro.areaInfo[index].nW = width;
        m_pro.areaInfo[index].nH = high;
        m_pro.areaInfo[index].PageCount = 0;
        m_pro.areaInfo[index].FontType = 1;
        m_pro.areaInfo[index].FontColor = 0;
        m_pro.areaInfo[index].inSpeed = 16;
        m_pro.areaInfo[index].InType = 1;
        m_pro.areaInfo[index].outSpeed = 0;
        m_pro.areaInfo[index].outType = 0;
        m_pro.areaInfo[index].stayTime = 0xff;
        m_pro.nMaCount++;
        return index;
    }

    public int ModifyTextZeroInfo(int index, int left, int top, int width, int high) {
        if (index > 3)
            return -1;
        m_pro.areaInfo[index].actType = 0;
        m_pro.areaInfo[index].nMAreaID = index;
        m_pro.areaInfo[index].nX = left;
        m_pro.areaInfo[index].nY = top;
        m_pro.areaInfo[index].nW = width;
        m_pro.areaInfo[index].nH = high;
        m_pro.areaInfo[index].PageCount = 0;
        m_pro.areaInfo[index].FontType = 1;
        m_pro.areaInfo[index].FontColor = 0;
        m_pro.areaInfo[index].inSpeed = 16;
        m_pro.areaInfo[index].InType = 1;
        m_pro.areaInfo[index].outSpeed = 0;
        m_pro.areaInfo[index].outType = 0;
        m_pro.areaInfo[index].stayTime = 0xff;
        return 0;
    }

    public int AddPicZero(int left, int top, int width, int high) {
        int index;
        index = m_pro.nMaCount;
        if (m_pro.nMaCount > 4)
            return -1;
        m_pro.areaInfo[index].actType = 1;
        m_pro.areaInfo[index].nMAreaID = index;
        m_pro.areaInfo[index].nX = left;
        m_pro.areaInfo[index].nY = top;
        m_pro.areaInfo[index].nW = width;
        m_pro.areaInfo[index].nH = high;
        m_pro.areaInfo[index].PageCount = 0;
        m_pro.areaInfo[index].FontType = 1;
        m_pro.areaInfo[index].FontColor = 0;
        m_pro.areaInfo[index].inSpeed = 16;
        m_pro.areaInfo[index].InType = 1;
        m_pro.areaInfo[index].outSpeed = 0;
        m_pro.areaInfo[index].outType = 0;
        m_pro.areaInfo[index].stayTime = 0xff;
        m_pro.nMaCount++;
        return index;
    }

    ;

    public int ModPicZeroInfo(int index, int left, int top, int width, int high) {
        if (index > 3)
            return -1;
        m_pro.areaInfo[index].actType = 1;
        m_pro.areaInfo[index].nMAreaID = index;
        m_pro.areaInfo[index].nX = left;
        m_pro.areaInfo[index].nY = top;
        m_pro.areaInfo[index].nW = width;
        m_pro.areaInfo[index].nH = high;
        m_pro.areaInfo[index].PageCount = 0;
        m_pro.areaInfo[index].FontType = 1;
        m_pro.areaInfo[index].FontColor = 0;
        m_pro.areaInfo[index].inSpeed = 16;
        m_pro.areaInfo[index].InType = 1;
        m_pro.areaInfo[index].outSpeed = 0;
        m_pro.areaInfo[index].outType = 0;
        m_pro.areaInfo[index].stayTime = 0xff;
        return 0;
    }

    ;

    int SetTimeFormatByIndex(int area_index, short Date_format, short Time_format, short Week_format, short iscal, short offset_minute, short offset_second) {
        m_pro.areaInfo[area_index].datataformat = Date_format;

        m_pro.areaInfo[area_index].timeformat = Time_format;

        m_pro.areaInfo[area_index].weekformat = Week_format;
        m_pro.areaInfo[area_index].shicha = iscal;

        m_pro.areaInfo[area_index].shicha_hour = offset_minute;
        m_pro.areaInfo[area_index].shicha_minute = offset_second;
        return 0;
    }

    public int SetTextZeroByZeroIndex(int index, String strcontext, int intype, int inspeed, int fontsize, int fontcolor, int staytime) {
        if (index > 4)
            return -1;
       // m_pro.areaInfo[index].pcontext = strcontext.getBytes(StandardCharsets.UTF_16LE);
        m_pro.areaInfo[index].pcontext = strcontext.getBytes();
        m_pro.areaInfo[index].inSpeed = inspeed;
        m_pro.areaInfo[index].InType = intype;
        m_pro.areaInfo[index].FontType = fontsize;
        m_pro.areaInfo[index].FontColor = fontcolor;
        m_pro.areaInfo[index].stayTime = (byte) staytime;
        return 0;
    }

    public int SetPicFileNameByZeroIndex(int index, String strcontext, int intype, int inspeed, int fontcolor, int staytime) {
        if (index > 4)
            return -1;
        m_pro.areaInfo[index].pcontext = strcontext.getBytes();
        m_pro.areaInfo[index].inSpeed = inspeed;
        m_pro.areaInfo[index].InType = intype;
        m_pro.areaInfo[index].FontColor = fontcolor;
        m_pro.areaInfo[index].stayTime = (byte) staytime;
        return 0;
    }

    public int makeOneprogram(String strcard, String strIP, byte isshowprogram) {
        int ret = 0;
        bufflen = 0;
        if (isshowprogram == 0) {
            ret = makepackhead(strcard, 0x24, 0);
        } else ret = makepackhead(strcard, 0x20, 0);
        if (ret < 0)
            return -1;
        makeprogram();
        MakeProgramZero();
        MakeTail(0);

        try {
            ret = SendProgramByUdp(strIP, strcard);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("-----------SendProgramByUdp --IOException-----\r\n");
        }

        bufflen = 0;
        m_pro.nMaCount = 0;
        return ret;
    }

    public byte[] makeOneprogramToArray(String strcard, String strIP, byte isshowprogram) {
        int ret = 0, i;

        if (isshowprogram == 0) {
            ret = makepackhead(strcard, 0x24, 0);
        } else ret = makepackhead(strcard, 0x20, 0);
        if (ret < 0)
            return null;
        makeprogram();
        MakeProgramZero();
        MakeTail(0);
        byte[] buf = new byte[bufflen];
        for (i = 0; i < bufflen; i++)
            buf[i] = buffer[i];
        return buf;
    }

    public int MakeProgramTo232Buf(byte isshowprogram, byte[] buffer232) {
        String s = new String("");
        s = "123456789012";
        int ret = 0;
        if (isshowprogram == 0) {
            ret = makepackhead(s, 0x24, 1);
        } else ret = makepackhead(s, 0x20, 1);
        if (ret < 0) {
            return -1;

        }
        makeprogram();
        MakeProgramZero();
        MakeTail(1);
        if (bufflen > 1024)
            return -1;
        for (int i = 0; i < this.bufflen; i++)
            buffer232[i] = buffer[i];
        return this.bufflen;
    }

    static short sumcheck(byte[] buf, int len) {
        int i;
        short sum = 0;
        for (i = 0; i < len; i++) {
            sum += buf[i];
        }
        sum = (short) (sum ^ 0xff);
        return sum;
    }

    public byte[] getSendByte(String strjson, String pwd, String ip, int port) {
        byte buffer[] = new byte[162];
        int idnex = 0, len = 0, i = 0;
        //���
        buffer[idnex++] = (byte) 0xeb;
        buffer[idnex++] = (byte) 0x90;
        buffer[idnex++] = (byte) 0xff;
        buffer[idnex++] = (byte) 0x13;
        buffer[idnex++] = (byte) 0x35;

        buffer[idnex++] = (byte) 153;    //���ݳ��ȵ�
        buffer[idnex++] = (byte) 0x00;//���ݳ��ȸ�

        buffer[idnex++] = (byte) 1;
        buffer[idnex++] = (byte) 0;
        buffer[idnex++] = (byte) 1;
        /*��serverip���*/
        byte[] cn;
        try {
            cn = ip.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = cn.length;
        if (len > 15)
            return null;
        buffer[idnex++] = (byte) len;

        for (i = 0; i < len; i++) {
            buffer[idnex++] = (byte) cn[i];
        }
        for (; i < 15; i++) {
            buffer[idnex++] = 0;
        }
        /*��serverport���*/
        String s = String.valueOf(port);
        byte[] cn1;
        try {
            cn1 = s.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = cn1.length;
        buffer[idnex++] = (byte) len;
        if (len > 6)
            return null;
        for (i = 0; i < len; i++) {
            buffer[idnex++] = (byte) cn1[i];
        }
        for (; i < 6; i++) {
            buffer[idnex++] = (byte) 0;
        }
        /*��localAddr���*/
        buffer[idnex++] = 0;
        for (i = 0; i < 15; i++) {
            buffer[idnex++] = (byte) 0;
        }
        /*��submask���*/
        buffer[idnex++] = 0;
        for (i = 0; i < 15; i++) {
            buffer[idnex++] = (byte) 0;
        }
        /*��gateway���*/
        buffer[idnex++] = 0;
        for (i = 0; i < 15; i++) {
            buffer[idnex++] = (byte) 0;
        }
        /*��DNS���*/

        buffer[idnex++] = 0;
        for (i = 0; i < 15; i++) {
            buffer[idnex++] = (byte) 0;
        }

        /*��WIFF_name���*/
        byte[] cn6;
        try {
            cn6 = strjson.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = (byte) cn6.length;
        buffer[idnex++] = (byte) len;
        if (len > 20)
            return null;
        for (i = 0; i < len; i++) {
            buffer[idnex++] = (byte) cn6[i];
        }
        for (; i < 20; i++) {
            buffer[idnex++] = 0;
        }
        /*��PwdType���*/
        buffer[idnex++] = 0;
        for (i = 0; i < 20; i++) {
            buffer[idnex++] = (byte) 0;
        }
        /*��PwdKey���*/
        byte[] cn8;
        try {
            cn8 = pwd.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        len = cn8.length;
        buffer[idnex++] = (byte) len;
        if (len > 20)
            return null;
        for (i = 0; i < len; i++) {
            buffer[idnex++] = (byte) cn8[i];
        }
        for (; i < 20; i++) {
            buffer[idnex++] = 0;
        }
        len = idnex;
        buffer[idnex++] = (byte) sumcheck(buffer, len);
        buffer[idnex++] = (byte) 0xef;
        return buffer;
    }

    //�������յ��ķ���ֵ
    //���鳤��Ϊ0��� ��ʾ����������,
    //���鳤��Ϊ1��ʾ��ʾ�����������,�����0���ֽڱ�ʾ�������ֵ,0��ʾ��gogn,����Ϊʧ��
    //>1������������,������������Ӧ���
    public resulclass parserReceiveByte(byte[] receive) {
        int i;
        resulclass result = new resulclass();
        if (receive.length < 25) {
            //result.an_result=-1;
            result.setAn_result(-1);
            return result;
        }
        if ((receive[0] != (byte) 0xaa) || (receive[1] != (byte) 0xbb)) {
            //result.an_result=-2;
            result.setAn_result(-2);
            return result;
        }
        if (receive[18] == (byte) 0xa5) {
            char[] cardno = new char[12];
            byte[] buf1 = new byte[26];
            for (i = 0; i < 19; i++)
                buf1[i] = receive[i];
            for (i = 0; i < 12; i++)
                cardno[i] = (char) buf1[i + 2];
            buf1[19] = 1;
            buf1[20] = 0;
            buf1[21] = 1;
            i = sumcheck(buf1, 22, 0);
            buf1[22] = (byte) i;
            buf1[23] = (byte) 0xef;
            buf1[24] = (byte) 0xbb;
            buf1[25] = (byte) 0xaa;
            //result.an_result=0;
            //result.type=1;
            //result.buffer=buf1;
            result.setAn_result(0);
            result.setType(1);
            result.setBuffer(buf1);
            result.cardnum = String.copyValueOf(cardno);
            return result;
        } else {
            //result.an_result=0;
            //result.type=0;
            result.setAn_result(0);
            result.setType(0);
            if ((receive[21] != 0x01) && (receive[21] != 0x02)) {
                //result.cmdresult=-1;
                result.setCmdresult(-1);
            } else result.setCmdresult(0);//result.cmdresult=0;
            return result;
        }
    }


    void printlogtocontrol(String strlog) {
        System.out.print(strlog);
    }

    static byte[] StringTohex(String strhex) {
        String tmp = "0123456789ABCDEF";
        byte btmp = 0;
        String strtemp = strhex.toUpperCase();
        int length = strtemp.length() / 2;
        byte[] array = new byte[length];

        byte[] aa = strtemp.getBytes();
        for (int i = 0; i < length; i++) {
            int pos = i * 2;

            int k = tmp.indexOf(aa[pos]);
            if (k < 0)
                return null;
            btmp = (byte) k;
            array[i] = (byte) ((btmp << 4) & 0xff);

            k = tmp.indexOf(aa[pos + 1]);
            if (k < 0)
                return null;
            btmp = (byte) k;
            array[i] = (byte) (array[i] | btmp);
        }

        return array;
    }

    void insertlist(String cardnum, String cardip, int port) {

        //首先看卡号在表里没有
        int isexit = 0;
        int isneedinfo = 0;
        synchronized (cardlist)//互斥操作
        {
            if (cardlist != null) {

                for (cardinfo obj : cardlist) {
                    if (obj.cardnum.equals(cardnum)) {
                        if ((obj.isonline == 0) || (!cardip.equals(obj.cardip)))
                            isneedinfo = 1;
                        obj.port = port;
                        obj.cardip = cardip;
                        isexit = 1;
                        obj.isonline = 1;
                        obj.curtime = 120;
                    }
                }
                if (isexit == 0) {
                    cardinfo eo = new cardinfo();
                    eo.cardip = cardip;
                    eo.curtime = 120;
                    eo.cardnum = cardnum;
                    eo.isonline = 1;
                    cardlist.add(eo);
                    isneedinfo = 1;
                }
                if (isneedinfo == 1) {
//                    calserver.ShowCardinfo(cardnum, cardip, 1);
                    connectStatusListener.showCardInfo(cardnum, cardip, 1);
                }
            }
        }
    }

    class listenthread extends Thread {

        public void run() {

            int threadstate = 0, ret, i;
            String strtcard;
            byte[] buf = new byte[1024];
            char[] cardnum = new char[13];


            //  MulticastSocket socketobj = null;

            threadstate = 1;

            while (true) {
                //监听是否有心跳包
                buf[0] = 0;
                buf[1] = 0;
                DatagramPacket rp = new DatagramPacket(buf, 1024);
                try {
                    socketobj.setSoTimeout(5000);//设置5秒超时}
                } catch (IOException e) {
                    //�����������ʱ������ʱ���ط�������һ���ط��Ĵ���
                    e.printStackTrace();
                    continue;
                }
                try {
                    // if(lock==null)
                    //     continue;
                    lock.acquire();
                    socketobj.receive(rp);
                    lock.release();
                } catch (IOException e) {
                    //�����������ʱ������ʱ���ط�������һ���ط��Ĵ���
                    continue;
                }
                ret = rp.getLength();
                if (ret < 1)
                    continue;//表示没有收到数据


                System.out.print("=============data\r\n");
                if ((buf[0] != (byte) 0xaa) || (buf[1] != (byte) 0xbb)) {
                    System.out.print("=============if((buf[0]!=(byte)0xaa)||(buf[1]!=(byte)0xbb))\r\n");
                    continue;
                }
                InetAddress sendIP = rp.getAddress();
                int sendPort = rp.getPort();
                for (i = 0; i < 12; i++) {
                    cardnum[i] = (char) buf[i + 2];
                }
                String strip1 = sendIP.toString();
                String strip;

                strip = strip1.substring(1);
                strtcard = String.valueOf(cardnum);
                System.out.println("------------------ip-------------" + buf[18]);
                insertlist(strtcard, strip, sendPort);
                if (buf[18] != (byte) 0xa5)//表明是结果返回,将此包发给
                {
                    System.out.print("=============if(buf[18]!=(byte)0xa5)  " + buf[21] + "\r\n");
                    //Resultinfo on=new Resultinfo();
                    //on.cmd=buf[18];
                    if ((buf[21] == (byte) 1) || (buf[21] == (byte) 2)) {
                        System.out.print("=============opreuslt=1;\r\n");
                        opreuslt = 1;
                    } else {
                        opreuslt = -1;

                    }

                } else {
                    DatagramPacket ssp;
                    try {
                        ssp = new DatagramPacket(buf, 26, InetAddress.getByName(strip), 9005);
                    } catch (IOException e) {
                        //�����������ʱ������ʱ���ط�������һ���ط��Ĵ���
                        continue;
                    }
                    System.out.print("=============if(buf[18]==(byte)0xa5)  " + buf[21] + "\r\n");
                    buf[19] = 1;
                    buf[20] = 0;
                    buf[21] = 1;
                    i = sumcheck(buf, 22, 0);
                    buf[22] = (byte) i;
                    buf[23] = (byte) 0xef;
                    buf[24] = (byte) 0xbb;
                    buf[25] = (byte) 0xaa;
                    try {
                        //synchronized(socketobj)
                        {
                            socketobj.send(ssp);
                        }
                    } catch (IOException e) {
                        //�����������ʱ������ʱ���ط�������һ���ط��Ĵ���
                        continue;
                    }
                }
            }
        }
    }

    listenthread listenobj = new listenthread();

    class sendthread extends Thread {
        int threadstate = 0;

        public void run() {
            threadstate = 1;
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (cardlist)//互斥操作
                {
                    for (cardinfo obj : cardlist) {
                        if (obj.isonline == 1) {
                            if (obj.curtime > 0) {
                                obj.curtime--;
                                if (obj.curtime == 0) {
                                    obj.isonline = 0;
//                                    calserver.ShowCardinfo(obj.cardnum, obj.cardip, 0);
                                    connectStatusListener.showCardInfo(obj.cardnum, obj.cardip, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    sendthread sendthreadobj = new sendthread();

    public void startserver(ConnectStatusListener listener, WifiManager.MulticastLock lock1) {
        try {
            socketobj = new DatagramSocket(null);//MulticastSocket(9001) ;
            socketobj.setReuseAddress(true);
            socketobj.bind(new InetSocketAddress(9001));
            // InetAddress groupAddress = InetAddress.getByName("192.168.1.255");
            //socketobj.joinGroup(groupAddress);

        } catch (SocketException e) {
            LogUtils.e("-----------9001端口被占用-------");
            e.printStackTrace();
            return;
        }
        lock = lock1;
        this.connectStatusListener = listener;
        sendthreadobj.start();
        listenobj.start();
    }

    // 2019/7/3 改callbackserver为回调

    private ConnectStatusListener connectStatusListener;

    public interface ConnectStatusListener {
        void showCardInfo(String cardNum, String ip, int statusCode);
    }
}
