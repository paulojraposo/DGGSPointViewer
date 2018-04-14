import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        // Set Nimbus Look and Feel.
        // With thanks to BenjaminLinus,
        // https://stackoverflow.com/questions/4617615/how-to-set-nimbus-look-and-feel-in-main.
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to cross-platform
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                // not worth my time
            }
        }

        MainGUI mGUI = new MainGUI();
        mGUI.start("Point Statistics on Discrete Global Grid", MainGUI.AppFrame.class);

    }

}
