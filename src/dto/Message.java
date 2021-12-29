package dto;

public class Message {
    private String host = null ;
    private int port = 0 ;
    private long timeStamp = 0 ;
    private ContentType content = null ;
    private String body = " ";

    public Message(String message){
        String tmp[] = message.split(",");
        setTimeStamp(Long.parseLong( tmp[0]));
        setHost(tmp[1]);
        setPort(Integer.parseInt(tmp[2]));
        setContent(ContentType.valueOf(tmp[3]));
        body = tmp.length > 4 ? tmp[4] : " ";
    }

    public Message(long timeStamp,String host ,int port , ContentType contentType){
        this(timeStamp ,host , port ,contentType," ");
    }

    public Message(long timeStamp,String host ,int port , ContentType contentType,String body ){
        this.timeStamp = timeStamp;
        this.host = host;
        this.port = port;
        this.content = contentType;
        this.body = body;
    }

    public String toString(){
        return timeStamp+","+host +"," +port+","+content+","+body;
    }

    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public long getTimeStamp() {
        return timeStamp;
    }
    public ContentType getContent() {
        return content;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public void setHost(String host) {
        host = host;
    }
    public void setContent(ContentType content) {
        this.content = content;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
}