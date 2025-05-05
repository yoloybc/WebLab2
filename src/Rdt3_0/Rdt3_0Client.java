package Rdt3_0;

import timer.Model;
import timer.Timer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.Scanner;

public class Rdt3_0Client {
    private static final int port = 81;
    private InetAddress inetAddress=InetAddress.getLocalHost();
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;
    private Model model;
    private Timer timer;
    private int nextSeq = 1;
    private int rcv_ack = 1;
    private int sed_ack = 1;
    
    public Rdt3_0Client() throws Exception {
        model = new Model();
        timer = new Timer(this,model);
        model.setTime(0);
        timer.start();
        datagramSocket = new DatagramSocket(port);
        System.out.println("----------menu----------");
        System.out.println("1.从服务器接收");
        System.out.println("2.向服务器传输");
        System.out.println("------------------------");
        Scanner in = new Scanner(System.in);
        int task = in.nextInt();
        switch (task){
            case 1:
                System.out.println("开始接收文件");
                while (true) {
                    //接收文件
                    receiveFile();
                }
            case 2:
                in.nextLine();
                System.out.println("输入任意键开始发送");
                in.nextLine();
                System.out.println("开始发送文件");
                while (true) {
                    //发送数据
                    sendData();
                    //收到的ack
                    receiveAck();
                }
            default:
                System.out.println("输入有误");
                return;
        }
    }

    private void receiveFile() throws Exception {
        byte[] bytes = new byte[32];
        datagramPacket = new DatagramPacket(bytes, bytes.length);
        datagramSocket.receive(datagramPacket);
        String received = new String(bytes, 0, bytes.length);
        if(Integer.parseInt(received.substring(10, 11).trim()) == sed_ack){
            String filename="E:/javacode/WebLab2/src/ClientFile/ServerFile"+sed_ack+".txt";
            File file=new File(filename);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(received);
            writer.flush();
            sendAck(sed_ack);
            System.out.println("接收"+received+"向服务器返回ack:" + sed_ack);
            sed_ack++;
        }else{
            return;
        }
    }

    public void sendAck(int ack) throws Exception{
        String response = "ack:"+ack;
        byte[] responseData = response.getBytes();
        datagramPacket = new DatagramPacket(responseData,responseData.length,inetAddress,80);
        datagramSocket.send(datagramPacket);
    }

    public void sendData() throws Exception {
        if (nextSeq==rcv_ack&&nextSeq<=5){
            String filename="E:/javacode/WebLab2/src/ClientFile/ClientFile"+nextSeq+".txt";
            File file=new File(filename);
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[32];
            fis.read(bytes, 0, bytes.length);
            fis.close();
            int p=3;
            //不发编号模p为0的数据，模拟数据丢失
            if(nextSeq % p == 0) {
                System.out.println("传输丢失:"+nextSeq);
                nextSeq++;
                return;
            }

            System.out.println("向服务器发送的文件编号:"+nextSeq+new Date());
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, 80);
            datagramSocket.send(datagramPacket);
            nextSeq++;
        }
    }
    
    public void receiveAck() throws IOException {
        model.setTime(1);
        byte[] receivedData = new byte[16];
        datagramPacket = new DatagramPacket(receivedData, receivedData.length);
        datagramSocket.receive(datagramPacket);
        String received = new String(receivedData, 0, receivedData.length);//offset是初始偏移量
        int ack = Integer.parseInt(received.substring(received.indexOf("ack:")+4).trim());
        if(rcv_ack == ack){
            rcv_ack++;
            System.out.println("从服务器获得的" + received+new Date());
            model.setTime(0);
        }else {

        }
    }
    
    public static void main(String[] args) throws Exception {
        new Rdt3_0Client();
    }
    
    public void timeOut() throws IOException {
        String filename="E:/javacode/WebLab2/src/ClientFile/ClientFile"+rcv_ack+".txt";
        File file=new File(filename);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[512];
        fis.read(bytes, 0, bytes.length);
        fis.close();
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, 80);
        datagramSocket.send(datagramPacket);
        System.out.println("向服务器重新发送的文件编号:"+rcv_ack+new Date());
    }
    
}
