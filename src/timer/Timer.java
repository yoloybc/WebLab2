package timer;

import GBN.GBNClient;
import GBN.GBNServer;
import Rdt3_0.Rdt3_0Client;
import Rdt3_0.Rdt3_0Server;
import SR.SRClient;
import SR.SRServer;

/**
 * 计时器
 */
public class Timer extends Thread {

    private Model model;
    private GBNClient gbnClient;
    private SRClient srClient;
    private GBNServer gbnServer;
    private SRServer srServer;
    private Rdt3_0Client rdt3_0Client;
    private Rdt3_0Server rdt3_0Server;
    public Timer(GBNClient gbnClient, Model model){
        this.gbnClient = gbnClient;
        this.model = model;
    }
    public Timer(GBNServer gbnServer, Model model){
        this.gbnServer = gbnServer;
        this.model = model;
    }
    public Timer(SRClient srClient, Model model){
        this.srClient = srClient;
        this.model = model;
    }
    public Timer(SRServer srServer, Model model){
        this.srServer = srServer;
        this.model = model;
    }
    public Timer(Rdt3_0Client rdt3_0Client, Model model){
        this.rdt3_0Client = rdt3_0Client;
        this.model = model;
    }
    public Timer(Rdt3_0Server rdt3_0Server, Model model){
        this.rdt3_0Server = rdt3_0Server;
        this.model = model;
    }
    @Override
    public void run(){
        do{
            int time = model.getTime();
            if(time > 0){
                try {
                    Thread.sleep(time*1000);//毫秒

                    System.out.println("\n");
                    if(srClient != null){
                        System.out.println("SR客户端等待ACK超时");
                        srClient.timeOut();
                    }else if(gbnServer != null){
                        System.out.println("GBN服务端等待超时");
                        gbnServer.timeOut();
                    }else if(gbnClient != null){
                        System.out.println("GBN客户端等待超时");
                        gbnClient.timeOut();
                    }else if(srServer != null){
                        System.out.println("SR服务端等待超时");
                        srServer.timeOut();
                    }else if(rdt3_0Client != null){
                        System.out.println("RDT3_0客户端等待超时");
                        rdt3_0Client.timeOut();
                    }else if(rdt3_0Server != null){
                        System.out.println("RDT3_0服务端等待超时");
                        rdt3_0Server.timeOut();
                    }
                    model.setTime(0);
                } catch (InterruptedException e) {
                } catch (Exception e) {
                }
            }
        }while (true);
    }
}
