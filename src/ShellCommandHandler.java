import java.io.BufferedReader;
import java.io.InputStreamReader;

// With thanks to Mkyong: https://www.mkyong.com/java/how-to-execute-shell-command-from-java/

public class ShellCommandHandler {

    String commandText;
    String commandOutput;

    public ShellCommandHandler() {

    }

    public ShellCommandHandler(String cText) {
        this.commandText = cText;
    }

    public void setCommandText(String cText){
        this.commandText = cText;
    }

    public String getCommandText(){
        return this.commandText;
    }

    public String getCommandOutput(){
        return this.commandOutput;
    }

    public void executeCommand(String command){

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
            this.commandOutput = output.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void executeStoredCommand(){

        this.executeCommand(this.commandText);

    }

}
