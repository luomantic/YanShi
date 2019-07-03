# yanshi

#### lgsv协议通讯演示 说明

##### 主要使用方法说明

- net目录为网络工具类：

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    lgsvInterface = new lgsv_interface(); // 初始化
    lgsvInterface.StartServer(this, lock); // 开始监听网络状态
}
```



- 要接收卡号信息回调的Activity 要implements makepacket.ConnectStatusListener接口：

  ```java
  public class MainActivity extends AppCompatActivity implements makepacket.ConnectStatusListener // 接收网络状态接口
  ```

  ```java
  @Override
  public void showCardInfo(String cardNum, String ip, int online) { // 显示卡号的回调
      LogUtils.e(cardNum + "  " + ip + "  " + online);
     
  }
  ```



- 发送节目

  ```java
  lgsvInterface.SendShowInfoByJson(cardList.get(i), ipList.get(i), new Gson().toJson(program));
  
  /*
  	参数说明：① 卡号  ② ip ③ 要发送的Json
  	
  	Json示例：如下
  */
  ```



参数json示例：

```json
{
	"picArea": [{
		"filePath": "/storage/emulated/0/DCIM/Camera/直行车道.jpg",
		"inSpeed": 0,
		"inType": 1,
		"nH": 32,
		"nW": 32,
		"nX": 160,
		"nY": 0,
		"stayTime": 200
	}, {
		"filePath": "/storage/emulated/0/Huawei/MagazineUnlock/MagazinePic-13-2.3.001-bigpicture_13_11.jpg",
		"inSpeed": 0,
		"inType": 1,
		"nH": 32,
		"nW": 32,
		"nX": 0,
		"nY": 0,
		"stayTime": 1
	}],
	"playCount": 255,
	"textArea": [{
		"fontColor": 0,
		"fontSize": 1,
		"inSpeed": 16,
		"inType": 1,
		"msgInfo": "公司名称",
		"nH": 16,
		"nW": 128,
		"nX": 32,
		"nY": 0,
		"stayTime": 200
	}, {
		"fontColor": 0,
		"fontSize": 1,
		"inSpeed": 4,
		"inType": 2,
		"msgInfo": "描述信息",
		"nH": 16,
		"nW": 128,
		"nX": 32,
		"nY": 16,
		"stayTime": 1
	}]
}
```

