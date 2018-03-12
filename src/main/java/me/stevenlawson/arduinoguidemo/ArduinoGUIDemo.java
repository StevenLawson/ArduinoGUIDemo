package me.stevenlawson.arduinoguidemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;

public class ArduinoGUIDemo extends javax.swing.JFrame
{
    private static final String COM_PORT_NAME = "COM5";

    private final ConcurrentLinkedQueue<Runnable> outputQueue = new ConcurrentLinkedQueue<>();
    private final Object queueMonitor = new Object();

    private JSCSerialSession session = null;

    public ArduinoGUIDemo()
    {
        initComponents();

        btnBlink3.addActionListener(event -> queueCommand(() ->
        {
            blinkLED(3);
        }));

        btnBlink6.addActionListener(event -> queueCommand(() ->
        {
            blinkLED(6);
        }));

        btnBlinkCustom.addActionListener(event -> queueCommand(() ->
        {
            final int value = (int) spnBlinkCustom.getValue();
            blinkLED(value);
        }));
    }

    public void setup()
    {
        setTitle(getTitle() + getVersionInfo());
        
        setVisible(true);
        setLocationRelativeTo(null);

        setControlsEnabled(false);

        new Thread(() ->
        {
            try
            {
                final AtomicBoolean running = new AtomicBoolean(false);

                session = new JSCSerialSession(COM_PORT_NAME, 9600);

                final AtomicBoolean interrupt = new AtomicBoolean(false);
                session.processMessages(interrupt, 5_000, message ->
                {
                    if (message.contains("@STARTED"))
                    {
                        running.set(true);
                        interrupt.set(true);
                    }
                });

                if (running.get())
                {
                    SwingUtilities.invokeLater(() -> setControlsEnabled(true));
                }
                else
                {
                    System.err.println("Didn't receive initial startup message from Arduino.");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            this,
                            "Didn't receive initial startup message from Arduino.",
                            "Arduino Error",
                            JOptionPane.ERROR_MESSAGE
                    ));
                }

                while (running.get())
                {
                    Runnable cmd;
                    while ((cmd = outputQueue.poll()) != null)
                    {
                        cmd.run();
                    }

                    synchronized (queueMonitor)
                    {
                        queueMonitor.notifyAll();
                    }
                }

                session.terminate();
            }
            catch (IOException ex)
            {
                System.err.println("COM Port IOException:\n" + ex.getMessage());
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this,
                        "COM Port IOException:\n" + ex.getMessage(),
                        "Arduino Error",
                        JOptionPane.ERROR_MESSAGE
                ));
            }
        }).start();
    }

    private void queueCommand(final Runnable runnable)
    {
        if (session != null)
        {
            outputQueue.offer(() -> runnable.run());
        }
    }

    private void blinkLED(int numTimes)
    {
        final AtomicBoolean success = new AtomicBoolean(false);

        if (session != null)
        {
            SwingUtilities.invokeLater(() -> setControlsEnabled(false));

            session.sendCommand(true, String.format("*BLINK_LED,%d\n", numTimes));

            final AtomicBoolean interrupt = new AtomicBoolean(false);
            session.processMessages(interrupt, 10_000, message ->
            {
                if (message.contains("@BLINK_LED,FINISH"))
                {
                    success.set(true);
                    interrupt.set(true);
                }
                else if (message.contains("@BLINK_LED,ERROR"))
                {
                    success.set(false);
                    interrupt.set(true);
                }
            });

            SwingUtilities.invokeLater(() -> setControlsEnabled(true));
        }

        if (!success.get())
        {
            System.err.println("An error occured while sending BLINK_LED command.");
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    this,
                    "An error occured while sending BLINK_LED command.",
                    "Arduino Error",
                    JOptionPane.ERROR_MESSAGE
            ));
        }
    }

    private void setControlsEnabled(boolean state)
    {
        btnBlink3.setEnabled(state);
        btnBlink6.setEnabled(state);
        btnBlinkCustom.setEnabled(state);
        spnBlinkCustom.setEnabled(state);
    }

    public static String getVersionInfo()
    {
        try (final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("my.properties"))
        {
            final Properties properties = new Properties();
            properties.load(inputStream);
            return String.format("v%s - %s", properties.getProperty("version"), properties.getProperty("timestampDate"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return "Unknown";
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        btnBlink3 = new javax.swing.JButton();
        btnBlink6 = new javax.swing.JButton();
        btnBlinkCustom = new javax.swing.JButton();
        spnBlinkCustom = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Arduino GUI Demo - ");

        btnBlink3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnBlink3.setText("Blink LED 3 Times");

        btnBlink6.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnBlink6.setText("Blink LED 6 Times");

        btnBlinkCustom.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnBlinkCustom.setText("Go");

        spnBlinkCustom.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        spnBlinkCustom.setModel(new javax.swing.SpinnerNumberModel(5, 1, 20, 1));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Blink LED");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setText("Times");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1)
                    .addComponent(btnBlink3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spnBlinkCustom, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnBlinkCustom, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                    .addComponent(btnBlink6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnBlink3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBlink6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spnBlinkCustom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(btnBlinkCustom))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[])
    {
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex)
        {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() ->
        {
            final ArduinoGUIDemo instance = new ArduinoGUIDemo();
            instance.setup();
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBlink3;
    private javax.swing.JButton btnBlink6;
    private javax.swing.JButton btnBlinkCustom;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner spnBlinkCustom;
    // End of variables declaration//GEN-END:variables
}
