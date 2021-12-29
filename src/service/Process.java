package service;

import dto.ContentType;
import dto.Message;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static dto.ContentType.*;

public class Process {
    private boolean coordinator = false;
    private Peer myPeer = null;
    List<Peer> otherPeers = new ArrayList<>();

    public Process(List<Integer> ports) {
        for(int i = 0;i<ports.size()-1;i++) {
            otherPeers.add(new Peer(ports.get(i)));
        }
        this.myPeer = new Peer(ports.get(ports.size()-1));
        this.myPeer.setProcess(this);
    }

    public void run() {
        sendVictory();
        myPeer.listen();
    }

    void sendElection() {
        boolean isCoordinator = true;
        for (Peer peer : otherPeers) {
            if (peer.getPort() > myPeer.getPort()) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        myPeer.sendMessageAndGetResponse(peer, encodeMessage(ELECTION),1000);
                    }
                }) ;
                t.start();
                isCoordinator = false;
            }
        }
        if (isCoordinator) {
            sendVictory();
        }
    }

    void sendVictory() {
        setCoordinator(true);
        for (Peer peer : otherPeers) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    myPeer.sendMessageAndGetResponse(peer, encodeMessage(VICTORY, encodePeers()), 1000);
                }
            });
            t.start();
        }
    }

    void sendAlive(){
        for (Peer peer : otherPeers) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    myPeer.sendMessageAndGetResponse(peer, encodeMessage(ALIVE),500);
                }
            }) ;
            t.start();
        }
    }

    void sendUpdatePeers(int missingPeerPort) {
        int missingPeerIndex = 0;
        for (Peer p : otherPeers) {
            if (p.getPort() == missingPeerPort) {
                break;
            }
            missingPeerIndex++;
        }
        otherPeers.remove(missingPeerIndex);
        for (Peer peer : otherPeers) {
            if(peer != myPeer) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        myPeer.sendMessageAndGetResponse(peer, encodeMessage(UPDATE_LIST, encodePeers()), 500);
                    }
                });
                t.start();
            }
        }
    }

    String encodePeers() {
        String peers = myPeer.getPort() + "";
        for(Peer p : otherPeers) {
            peers += " " + p.getPort();
        }
        return peers;
    }

    private void updatePeers(String body) {
        String[] peers = body.split(" ");
        setCoordinator(false);
        otherPeers.clear();
        for (int i=0;i<peers.length;i++) {
            otherPeers.add(new Peer(Integer.parseInt(peers[i])));
        }
        otherPeers.remove(myPeer);
    }

    private void updatePeersMissingPeer(String body) {
        String[] peers = body.split(" ");
        otherPeers.clear();
        for (int i=0;i<peers.length;i++) {
            otherPeers.add(new Peer(Integer.parseInt(peers[i])));
        }
        otherPeers.remove(myPeer);
    }

    Message handleMessage(String m){
        Message message = new Message(m);
        String messageBody =  message.getBody();
        ContentType contentType = message.getContent();
        switch (contentType){
            case ELECTION:
                sendElection();
                return encodeMessage(OK);
            case VICTORY:
                updatePeers(messageBody);
                return encodeMessage(OK);
            case UPDATE_LIST:
                updatePeersMissingPeer(messageBody);
                return encodeMessage(OK);
            case ALIVE:
                return encodeMessage(OK);
            default:
                return encodeMessage(OK);
        }
    }

    void handleResponse(String response){
        Message message = new Message(response);
        String msg =  message.getBody();
        int sender = message.getPort();
        ContentType contentType = message.getContent();
        switch (contentType){
            case OK:
                break;
            default:
                break;
        }
    }

    public Message encodeMessage(ContentType contentType){
        return encodeMessage(contentType, "");
    }

    public Message encodeMessage(ContentType contentType, String body){
        switch (contentType){
            case VICTORY:
                return new Message(getNowTimeStamp(), myPeer.getHost(), myPeer.getPort(), contentType ,body);
            case OK:
                return new Message(getNowTimeStamp() , myPeer.getHost(), myPeer.getPort(), OK);
            case ALIVE:
                return new Message(getNowTimeStamp() , myPeer.getHost(), myPeer.getPort(), OK);
            default:
                return null;
        }
    }

    private long getNowTimeStamp(){
        return new Timestamp(System.currentTimeMillis()).getTime();
    }
    public boolean isCoordinator() {
        return coordinator;
    }
    public void setCoordinator(boolean coordinator) {
        this.coordinator = coordinator;
    }
    public Peer getMyPeer() {
        return myPeer;
    }
    public void setMyPeer(Peer myPeer) {
        this.myPeer = myPeer;
    }
}
