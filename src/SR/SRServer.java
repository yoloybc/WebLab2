package SR;

import timer.Model;
import timer.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
 * 服务器端
 */
public class SRServer {
    private static final int port = 80;
    private InetAddress inetAddress=InetAddress.getLocalHost();
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;
    private Model model;
    private Timer timer;
    private int nextSeq = 1;
    private int base = 1;
    private int N = 3;
    private int rcv_base = 1;
    private Map<Integer,String> bufferFileMap = new HashMap<Integer, String>();
    private Set<Integer> ackSet = new HashSet<Integer>();

    public SRServer() throws Exception {
        model = new Model();
        timer = new Timer(this,model);
        model.setTime(0);
        timer.start();
        datagramSocket = new DatagramSocket(port);
        System.out.println("----------menu----------");
        System.out.println("1.向客户端传输");
        System.out.println("2.从客户端接收");
        System.out.println("------------------------");
        Scanner in = new Scanner(System.in);
        int task = in.nextInt();
        switch (task){
            case 1:
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
            case 2:
                System.out.println("开始接收文件");
                while (true) {
                    //接收文件
                    receiveFile();
                }
            default:
                System.out.println("输入有误");
                return;
        }
    }

    public static final void main(String[] args) throws Exception {
        new SRServer();
    }
    public void flushFile() throws IOException {
        while(bufferFileMap.containsKey(rcv_base)){
            String filename="E:/javacode/WebLab2/src/ServerFile/ClientFile"+rcv_base+".txt";
            File file=new File(filename);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(bufferFileMap.get(rcv_base));
            writer.flush();
            rcv_base++;
        }
    }
    public void receiveFile() throws IOException {
        byte[] bytes = new byte[32];
        datagramPacket = new DatagramPacket(bytes, bytes.length);
        datagramSocket.receive(datagramPacket);
        String received = new String(bytes, 0, bytes.length);
        if(Integer.parseInt(received.substring(10, 11).trim()) == rcv_base){
            String filename="E:/javacode/WebLab2/src/ServerFile/ClientFile"+rcv_base+".txt";
            File file=new File(filename);
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(received);
            writer.flush();
            sendAck(rcv_base);
            System.out.println("接收"+received+"向客户端返回ack:" + rcv_base);
            rcv_base++;
            flushFile();
        }else{
            Integer ack = Integer.parseInt(received.substring(10, 11).trim());
            System.out.println("接收"+received+"向客户端返回ack:" + ack);
            System.out.println("+++++++++++++进入缓存:"+ack+"+++++++++++++");
            bufferFileMap.put(ack,received);
            sendAck(ack);
        }
    }

    public void sendAck(int ack) throws IOException {
        String response = "ack:"+ack;
        byte[] responseData = response.getBytes();
        datagramPacket = new DatagramPacket(responseData,responseData.length,inetAddress,81);
        datagramSocket.send(datagramPacket);
    }

    public void receiveAck() throws IOException {
        byte[] receivedData = new byte[16];
        datagramPacket = new DatagramPacket(receivedData, receivedData.length);
        datagramSocket.receive(datagramPacket);
        String received = new String(receivedData, 0, receivedData.length);//offset是初始偏移量
        int ack = Integer.parseInt(received.substring(received.indexOf("ack:")+4).trim());
        if(base == ack){
            //停止计时器
            model.setTime(0);
            base++;
            flushAck();
        }else {
            //开始计时器
            model.setTime(1);
            ackSet.add(ack);
        }
        System.out.println("从客户端获得的" + received+new Date());
    }

    public void flushAck(){
        while(ackSet.contains(base)){
            ackSet.remove(base);
            base++;
        }
    }

    public void sendData() throws Exception {
        while (nextSeq < base + N && nextSeq <= 5) {
            String filename="E:/javacode/WebLab2/src/ServerFile/ServerFile"+nextSeq+".txt";
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
                continue;
            }

            System.out.println("向客户端发送的文件编号:"+nextSeq+new Date());
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, 81);
            datagramSocket.send(datagramPacket);
            nextSeq++;
            //没有要传送的包
            if(nextSeq == base){
                //开始计时
                model.setTime(1);
            }
        }
    }

    public void timeOut() throws Exception {
        String filename="E:/javacode/WebLab2/src/ServerFile/ServerFile"+base+".txt";
        File file=new File(filename);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[512];
        fis.read(bytes, 0, bytes.length);
        fis.close();
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, inetAddress, 81);
        datagramSocket.send(datagramPacket);
        System.out.println("向客户端重新发送的文件编号:"+base+new Date());
    }
}