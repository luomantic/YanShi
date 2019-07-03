package com.luomantic.yanshi.net;

import android.net.wifi.WifiManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class lgsv_interface {

    makepacket mk = new makepacket();
    int m_cmdstate=0;
    public lgsv_interface() {

    }

    //�������յ��ķ���ֵ
    //���鳤��Ϊ0��� ��ʾ����������,
    //���鳤��Ϊ1��ʾ��ʾ�����������,�����0���ֽڱ�ʾ�������ֵ,0��ʾ�ɹ�,����Ϊʧ��
    //>1������������,������������Ӧ���

    public void StartServer(makepacket.ConnectStatusListener listener, WifiManager.MulticastLock  lock1) {
        mk.startserver(listener,lock1);
    }

    public resulclass parserReceiveByte(byte[] receive) {

        return mk.parserReceiveByte(receive);
    }

    public int SendJsonCmmd(String strcardip, String strjson) {
        //JSONObject  dataJson=new JSONObject(program);

        int[] left = new int[4];
        int[] top = new int[4];
        int[] width = new int[4];
        int[] height = new int[4];
        int[] contentId = new int[4];
        String[] content = new String[4];
        String strcard;
        int program_no, RelayID, Operator;
        int[] fontsize = new int[4];
        int[] fontcolor = new int[4];

        int[] inmethod = new int[4];
        int[] inspeed = new int[4];
        int[] stoptime = new int[4];
        int cmd;
        int i = 0,ret = 0,zero=0;

        if(m_cmdstate==1)
            return -3;
        System.out.print(strjson);
        JSONObject obj = JSONObject.parseObject(strjson);
        cmd = obj.getIntValue("Cmd");
        strcard = obj.getString("Number");

        if (cmd == 0x13)//����
        {
            ret=mk.OpenOrCloseLed(strcard, strcardip, 1);
        } else if (cmd == 0x12)//����
        {
            ret=mk.OpenOrCloseLed(strcard, strcardip, 0);
        } else if (cmd == 0x2e)//��ս�Ŀ
        {
            ret=mk.ClearAllProgram(strcard, strcardip);
        } else if (cmd == 0x2f)//ɾ����Ŀ
        {
            program_no = obj.getIntValue("ProgramNo");
            ret=mk.DeleteOneProgram(strcard, strcardip, program_no);
        } else if (cmd == 0x1c)//���ȵ���
        {
            JSONArray jsarr = obj.getJSONArray("LightInfo");//LightType
            zero = jsarr.size();
            if (zero < 1) {
                m_cmdstate=0;
                return -10001;
            }
            JSONObject ao = jsarr.getJSONObject(i);
            int value = ao.getIntValue("LightValue");
            ret=mk.setBright(strcardip, strcard, value);
            //��ȡ
        } else if (cmd == 0x83)//��������
        {

            JSONArray jsarr = obj.getJSONArray("VolumnInfo");//LightType
            zero = jsarr.size();
            if (zero < 1)
                return -10001;
            JSONObject ao = jsarr.getJSONObject(i);
            int value = ao.getIntValue("VolumnValue");
            ret=mk.SetVolum(strcardip, strcard, value);
        } else if (cmd == 0x43)//���ƿ���λ
        {
            m_cmdstate=0;
            return -1;
        } else if (cmd == 0xA0)//����������
        {
            RelayID = obj.getIntValue("RelayID");
            Operator = obj.getIntValue("Operator");
            ret=mk.OperateRelay(strcard, strcardip, RelayID, Operator);
        } else if (cmd == 0x14)//Уʱ
        {

            int nyear = obj.getIntValue("nyear");
            int nmonth = obj.getIntValue("nmonth");
            int nday = obj.getIntValue("nday");
            int nhour = obj.getIntValue("nhour");
            int nminute = obj.getIntValue("nminute");
            int nsecond = obj.getIntValue("nsecond");
            int nweek = obj.getIntValue("nweek");
            ret=mk.TimeCalibration(strcard, strcardip, (short) nyear, (short) nmonth, (short) nday, (short) nhour, (short) nminute, (short) nsecond, (short) nweek);
        } else if (cmd == 0x24)//������Ŀ
        {
            int nConfigType = 0;
            int Times = 0;
            int ProgramNo = 0;
            nConfigType = obj.getIntValue("nConfigType");
            Times = obj.getIntValue("Times");
            ProgramNo = obj.getIntValue("ProgramNo");
            mk.initialProgram(nConfigType, Times, ProgramNo);
            JSONArray jsarr = obj.getJSONArray("AreaMsgs");
            //���Ȼ�ȡ������Ϣ
            String voicebuf = obj.getString("VoiceMsg");
            int isHaveOut = obj.getIntValue("isHaveOut");
            zero = jsarr.size();
            if (zero > 4)
                zero = 4;
            for (i = 0; i < zero; i++) {

                JSONObject ao = jsarr.getJSONObject(i);
                left[i] = ao.getIntValue("nX");
                top[i] = ao.getIntValue("nY");
                width[i] = ao.getIntValue("nW");
                height[i] = ao.getIntValue("nH");
                contentId[i] = ao.getIntValue("contentId");
                content[i] = ao.getString("MsgInfo");
                fontsize[i] = ao.getIntValue("FontSize");
                fontcolor[i] = ao.getIntValue("FontColor");
                inmethod[i] = ao.getIntValue("InType");
                inspeed[i] = ao.getIntValue("InSpeed");
                if (inspeed[i] == 0)
                    inspeed[i] = 1;
                stoptime[i] = ao.getIntValue("StayTime");
                mk.AddTextZero(left[i], top[i], width[i], height[i]);
                mk.SetTextZeroByZeroIndex(i, content[i], inmethod[i], inspeed[i], fontsize[i], fontcolor[i], stoptime[i]);
            }
            if (isHaveOut == 1)
                mk.SetVoplayvoice(1, voicebuf);
            ret=mk.makeOneprogram(strcard, strcardip, (byte) 0);
        }
        m_cmdstate=0;
        return ret;
    }

    public int SetHotInfo(String strcard, String strIP, String hotname, String strPassword) {
        return mk.SetHotInfo(strcard, strIP, hotname, strPassword);
    }

    public int SendShowInfoByJson(String strcardnum, String strip, String strjson) {

        int[] left = new int[4];
        int[] top = new int[4];
        int[] width = new int[4];
        int[] height = new int[4];
        String[] content = new String[4];
        int[] fontsize = new int[4];
        int[] fontcolor = new int[4];
        int[] inmethod = new int[4];
        int[] inspeed = new int[4];
        int[] stoptime = new int[4];
        int i = 0;
        int zero = 0;

        System.out.print(strjson);
        JSONObject obj = JSONObject.parseObject(strjson);
        int nConfigType = 0;
        int Times = 0;
        int ProgramNo = 0;
        nConfigType = 0;
        Times = obj.getIntValue("playCount");
        ProgramNo = 0;
        if(m_cmdstate==1)
            return -3;
        m_cmdstate=1;
        mk.initialProgram(nConfigType, Times, ProgramNo);
        JSONArray jsarr = obj.getJSONArray("textArea");
        zero = jsarr.size();
        if (zero > 4)
            zero = 4;
        for (i = 0; i < zero; i++) {
            JSONObject ao = jsarr.getJSONObject(i);
            left[i] = ao.getIntValue("nX");
            top[i] = ao.getIntValue("nY");
            width[i] = ao.getIntValue("nW");
            height[i] = ao.getIntValue("nH");
            content[i] = ao.getString("msgInfo");
            fontsize[i] = ao.getIntValue("fontSize");
            fontcolor[i] = ao.getIntValue("fontColor");
            inmethod[i] = ao.getIntValue("inType");
            inspeed[i] = ao.getIntValue("inSpeed");
            if (inspeed[i] == 0)
                inspeed[i] = 1;
            stoptime[i] = ao.getIntValue("stayTime");
            mk.AddTextZero(left[i], top[i], width[i], height[i]);
            mk.SetTextZeroByZeroIndex(i, content[i], inmethod[i], inspeed[i], fontsize[i], fontcolor[i], stoptime[i]);
        }
        JSONArray jpicsarr = obj.getJSONArray("picArea");
        zero = jsarr.size();
        if (zero > 4)
            zero = 4;
        int j = i;
        for (i = 0; i < zero; i++) {
            JSONObject ao = jpicsarr.getJSONObject(i);
            left[i] = ao.getIntValue("nX");
            top[i] = ao.getIntValue("nY");
            width[i] = ao.getIntValue("nW");
            height[i] = ao.getIntValue("nH");
            content[i] = ao.getString("filePath");
            inmethod[i] = ao.getIntValue("inType");
            inspeed[i] = ao.getIntValue("inSpeed");
            if (inspeed[i] == 0)
                inspeed[i] = 1;
            stoptime[i] = ao.getIntValue("stayTime");
            if(content[i]==null)
                continue;
            if(content[i].length()<4)
                continue;
            mk.AddPicZero(left[i], top[i], width[i], height[i]);
            mk.SetPicFileNameByZeroIndex(j + i, content[i], inmethod[i], inspeed[i], fontcolor[i], stoptime[i]);
        }
        int ret=mk.makeOneprogram(strcardnum, strip, (byte) 1);
        m_cmdstate=0;
        return ret;
    }

    public byte[] SendJsonCmmdToArray(String strcardip, String strjson) {
        //JSONObject  dataJson=new JSONObject(program);

        int[] left = new int[4];
        int[] top = new int[4];
        int[] width = new int[4];
        int[] height = new int[4];
        int[] contentId = new int[4];
        String[] content = new String[4];
        String strcard;
        int program_no, RelayID, Operator;
        int[] fontsize = new int[4];
        int[] fontcolor = new int[4];
        int[] alignment = new int[4];
        int[] inmethod = new int[4];
        int[] inspeed = new int[4];
        int[] stoptime = new int[4];
        int cmd;
        int i = 0;
        int zero = 0;

        System.out.print(strjson);
        JSONObject obj = JSONObject.parseObject(strjson);
        cmd = obj.getIntValue("Cmd");
        strcard = obj.getString("Number");

        if (cmd == 0x13)//����
        {
            return mk.OpenOrCloseLedToArray(strcard, strcardip, 1);
        } else if (cmd == 0x12)//����
        {
            return mk.OpenOrCloseLedToArray(strcard, strcardip, 0);
        } else if (cmd == 0x2e)//��ս�Ŀ
        {
            return mk.ClearAllProgramToArray(strcard, strcardip);
        } else if (cmd == 0x2f)//ɾ����Ŀ
        {
            program_no = obj.getIntValue("ProgramNo");
            return mk.DeleteOneProgramToArray(strcard, strcardip, program_no);
        } else if (cmd == 0x1c)//���ȵ���
        {
            JSONArray jsarr = obj.getJSONArray("LightInfo");//LightType
            zero = jsarr.size();
            if (zero < 1)
                return null;
            JSONObject ao = jsarr.getJSONObject(i);
            int value = ao.getIntValue("LightValue");
            return mk.setBrightToArray(strcardip, strcard, value);
            //��ȡ
        } else if (cmd == 0x83)//��������
        {

            JSONArray jsarr = obj.getJSONArray("VolumnInfo");//LightType
            zero = jsarr.size();
            if (zero < 1)
                return null;
            JSONObject ao = jsarr.getJSONObject(i);
            int value = ao.getIntValue("VolumnValue");
            return mk.SetVolumToArray(strcardip, strcard, value);
        } else if (cmd == 0x43)//���ƿ���λ
        {
            return null;
        } else if (cmd == 0xA0)//����������
        {
            RelayID = obj.getIntValue("RelayID");
            Operator = obj.getIntValue("Operator");
            return mk.OperateRelayToArray(strcard, strcardip, RelayID, Operator);
        } else if (cmd == 0x14)//Уʱ
        {

            int nyear = obj.getIntValue("nyear");
            int nmonth = obj.getIntValue("nmonth");
            int nday = obj.getIntValue("nday");
            int nhour = obj.getIntValue("nhour");
            int nminute = obj.getIntValue("nminute");
            int nsecond = obj.getIntValue("nsecond");
            int nweek = obj.getIntValue("nweek");
            return mk.TimeCalibrationToArray(strcard, strcardip, (short) nyear, (short) nmonth, (short) nday, (short) nhour, (short) nminute, (short) nsecond, (short) nweek);
        } else if ((cmd == 0x20) || (cmd == 0x24))//������Ŀ
        {
            int nConfigType = 0;
            int Times = 0;
            int ProgramNo = 0;
            nConfigType = obj.getIntValue("nConfigType");
            Times = obj.getIntValue("Times");
            ProgramNo = obj.getIntValue("ProgramNo");
            mk.initialProgram(nConfigType, Times, ProgramNo);
            JSONArray jsarr = obj.getJSONArray("AreaMsgs");
            //���Ȼ�ȡ������Ϣ
            String voicebuf = obj.getString("VoiceMsg");
            int isHaveOut = obj.getIntValue("isHaveOut");
            zero = jsarr.size();
            if (zero > 4)
                zero = 4;
            for (i = 0; i < zero; i++) {

                JSONObject ao = jsarr.getJSONObject(i);
                left[i] = ao.getIntValue("nX");
                top[i] = ao.getIntValue("nY");
                width[i] = ao.getIntValue("nW");
                height[i] = ao.getIntValue("nH");
                contentId[i] = ao.getIntValue("contentId");
                content[i] = ao.getString("MsgInfo");
                fontsize[i] = ao.getIntValue("FontSize");
                fontcolor[i] = ao.getIntValue("FontColor");
                inmethod[i] = ao.getIntValue("InType");
                inspeed[i] = ao.getIntValue("InSpeed");
                if (inspeed[i] == 0)
                    inspeed[i] = 1;
                stoptime[i] = ao.getIntValue("StayTime");
                mk.AddTextZero(left[i], top[i], width[i], height[i]);
                mk.SetTextZeroByZeroIndex(i, content[i], inmethod[i], inspeed[i], fontsize[i], fontcolor[i], stoptime[i]);
            }
            if (isHaveOut == 1)
                mk.SetVoplayvoice(1, voicebuf);
            if (cmd == 0x20)
                return mk.makeOneprogramToArray(strcard, strcardip, (byte) 1);
            else return mk.makeOneprogramToArray(strcard, strcardip, (byte) 0);
        }
        return null;
    }

}
