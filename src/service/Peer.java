package service;

import dto.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Peer {
    private int port;
    private String host = "127.0.0.1";
    private ServerSocket serverSocket = null;
    boolean active = true ;
    Process process = null;

    Peer(int port){
        this.port= port;
    }

    void listen(){
        System.out.println("I'm listening to " + this.getPort());
        while(active){
            if(process.isCoordinator()){
                System.out.println("\n#### process with port "+port+" is coordinator ####\n");
                if(serverSocket == null||serverSocket.isClosed())
                    bindServerSocket();
                receiveAndGiveResponse(2000);
                process.sendAlive();
            }else {
                if(serverSocket == null||serverSocket.isClosed())
                    bindServerSocket();

                receiveAndGiveResponse(5000);
            }
        }
        System.out.println(this.getPort()+" no coordinator");
        if(!process.isCoordinator())
            process.sendElection();

    }

    void bindServerSocket(){
        System.out.println( "binding to "+ this.getPort()+".");
        try {
            serverSocket = new ServerSocket(this.getPort());
        } catch (Exception e) {
            System.out.println("can't bind to "+this.getPort());
        }
    }

    void sendMessageAndGetResponse(Peer peer , Message message , int timeOut){
        try {
            Socket s = new Socket(peer.getHost(), peer.getPort());
            System.out.println("sending "+message.toString()+" from "+getPort()+" to "+ peer.port);
            s.setSoTimeout(timeOut);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            DataInputStream din = new DataInputStream(s.getInputStream());
            dout.writeUTF(message.toString());
            String response = din.readUTF();
            process.handleResponse(response);
            dout.flush();
            dout.close();
            s.close();
        } catch(Exception e){
            process.sendUpdatePeers(peer.getPort());
            System.out.println(e +" at " + peer.getPort());
        }
    }

    void receiveAndGiveResponse(int timeOut){
        try{
            serverSocket.setSoTimeout(timeOut);
            Socket s = serverSocket.accept();
            DataInputStream din = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            String str = din.readUTF();
            Message message = process.handleMessage(str);
            String response = message.toString();
            dout.writeUTF(response);
            dout.flush();
            dout.close();
            din.close();
            new Message(str);
        } catch(Exception e){
            if(!process.isCoordinator())
                active = false;
        }
    }

    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public void setProcess(Process process) {
        this.process = process;
    }
}
