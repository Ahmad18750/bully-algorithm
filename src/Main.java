import gui.UI;
import service.Process;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args)  {
        if (args.length == 0) {
            startUi();
        }
        else {
            int port = Integer.parseInt(args[0]);
            List<Integer> ports = new ArrayList<>();
            for(int i=0;i< args.length;i++){
                ports.add(Integer.parseInt(args[i]));
            }
            Process process = new Process(ports);
            process.run();
        }
    }

    public static void startUi() {
        UI ui = new UI();
    }
}
