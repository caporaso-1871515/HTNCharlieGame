package it.uniroma1.charlie;

import it.uniroma1.charlie.gui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Classe principale di ingresso dell'applicazione HTN Charlie Game.
 * Configura l'ambiente Swing e avvia l'interfaccia grafica.
 */
public class Main {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Ambiente Headless rilevato. Impossibile avviare la GUI Swing.");
            System.err.println(
                    "Assicurarsi che la variabile d'ambiente $DISPLAY sia impostata e che il server X11 sia accessibile.");
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
