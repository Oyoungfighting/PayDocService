package com.oyoung.paydocservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationService extends NotificationListenerService {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Notification notification = sbn.getNotification();
        if (notification == null) {
            return;
        }
        Bundle extras = sbn.getNotification().extras;
        if (extras != null) {
            String packageName = sbn.getPackageName();
            String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
            String notificationText = extras.getString(Notification.EXTRA_TEXT);
            int notificationId = sbn.getId();
            Log.i("TAG", String.format("收到通知，包名：%s，标题：%s，内容：%s, 渠道Id: %d", packageName, notificationTitle, notificationText, notificationId));
            processOnReceive(packageName, notificationTitle, notificationText, notificationId);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processOnReceive(String packageName, String title, String content, int id) {
        if (Constant.ALIPAY_PACKAGE_NAME.equals(packageName)) {
            if (checkMsgValid(title, content, Constant.ALIPAY) && !TextUtils.isEmpty(parseMoney(title))) {
                Log.i("TAG", "parseMoney: " + parseMoney(title));
                Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
                Toast.makeText(this, parseMoney(title), Toast.LENGTH_SHORT).show();
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(id);
            }
        } else if (Constant.WX_PAY_PACKAGE_NAME.equals(packageName)) {
            if (checkMsgValid(title, content, Constant.WX_PAY) && !TextUtils.isEmpty(parseMoney(content))) {
                Toast.makeText(this,content,Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 解析内容字符串，提取金额
     *
     * @param content
     * @return
     */
    private static String parseMoney(String content) {
        Pattern pattern = Pattern.compile("收款(([1-9]\\d*)|0)(\\.(\\d){0,2})?元");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String tmp = matcher.group();
            Pattern patternNum = Pattern.compile("(([1-9]\\d*)|0)(\\.(\\d){0,2})?");
            Matcher matcherNum = patternNum.matcher(tmp);
            if (matcherNum.find())
                return matcherNum.group();
        }
        return null;
    }

    /**
     * 验证消息的合法性，防止非官方消息被处理
     *
     * @param title
     * @param content
     * @param gateway
     * @return
     */
    private static boolean checkMsgValid(String title, String content, String gateway) {
        if ("wxpay".equals(gateway)) {
            //微信支付的消息格式
            //1条：标题：微信支付，内容：微信支付收款0.01元(朋友到店)
            //多条：标题：微信支付，内容：[4条]微信支付: 微信支付收款1.01元(朋友到店)
            Pattern pattern = Pattern.compile("^((\\[\\+?\\d+条])?微信支付:|微信支付收款)");
            Matcher matcher = pattern.matcher(content);
            return "微信支付".equals(title) && matcher.find();
        } else if (Constant.ALIPAY.equals(gateway)) {
            //支付宝的消息格式，标题：支付宝通知，内容：支付宝成功收款1.00元。
            return title.contains("你已成功收款");
        }
        return false;
    }

    /**
     * 提取字符串中的数字
     * @param strInput
     * @return
     */
    public static String getNum(String strInput) {
        //匹配指定范围内的数字
        String regEx = "[^0-9]";
        //Pattern是一个正则表达式经编译后的表现模式
        Pattern p = Pattern.compile(regEx);
        // 一个Matcher对象是一个状态机器，它依据Pattern对象做为匹配模式对字符串展开匹配检查。
        Matcher m = p.matcher(strInput);
        //将输入的字符串中非数字部分用空格取代并存入一个字符串
        String string = m.replaceAll(" ").trim();
        //以空格为分割符在讲数字存入一个字符串数组中
        String[] strArr = string.split(" ");
        StringBuffer stringBuffer = new StringBuffer();
        //遍历数组转换数据类型输出
        for (String s : strArr) {
            stringBuffer.append(s);
            System.out.println(Integer.parseInt(s));
        }
        String num = stringBuffer.toString();
        System.out.println("num is " + num);
        return num;
    }

    private static String getMoney(String str) {
        Pattern pattern = Pattern.compile("[0-9|-|+|.]");  // 因为金额中有小数点，有可能是增加的钱，也可能是减少的钱
        Matcher matcher = pattern.matcher(str);
        StringBuilder sb = new StringBuilder();
        while(matcher.find()) {
            sb.append(matcher.find());
        }
        return sb.toString();
    }

}