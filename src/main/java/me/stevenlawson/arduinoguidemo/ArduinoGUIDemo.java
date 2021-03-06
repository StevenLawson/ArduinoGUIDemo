package me.stevenlawson.arduinoguidemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import org.apache.commons.lang3.StringUtils;

public class ArduinoGUIDemo extends javax.swing.JFrame
{
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<Runnable> outputQueue = new ConcurrentLinkedQueue<>();
    private JSCSerialSession session = null;

    public ArduinoGUIDemo()
    {
        initComponents();
    }

    public void setup()
    {
        setTitle(getTitle() + getVersionInfo());

        setVisible(true);
        setLocationRelativeTo(null);

        setConnectControlsEnabled(true);
        setBlinkControlsEnabled(false);

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
            final int numBlinks = (int) spnBlinkCustom.getValue();
            blinkLED(numBlinks);
        }));

        btnConnect.addActionListener(event ->
        {
            final String portName = StringUtils.trimToEmpty(txtPort.getText());
            startSerialThread(portName);
        });

        btnDisconnect.addActionListener(event ->
        {
            running.set(false);
        });
    }

    private void startSerialThread(final String portName)
    {
        if (running.get())
        {
            return;
        }

        setConnectControlsEnabled(false);
        setBlinkControlsEnabled(false);

        new Thread(() ->
        {
            try
            {
                session = new JSCSerialSession(portName, 9600);

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
                    SwingUtilities.invokeLater(() -> setBlinkControlsEnabled(true));
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

            running.set(false);

            SwingUtilities.invokeLater(() ->
            {
                setConnectControlsEnabled(true);
                setBlinkControlsEnabled(false);
            });
        }).start();
    }

    private void queueCommand(final Runnable runnable)
    {
        if (session != null)
        {
            outputQueue.offer(() -> runnable.run());
        }
    }

    private boolean blinkLED(int numBlinks)
    {
        final AtomicBoolean success = new AtomicBoolean(false);

        if (session != null)
        {
            SwingUtilities.invokeLater(() -> setBlinkControlsEnabled(false));

            session.sendCommand(true, String.format("*BLINK_LED,%d\n", numBlinks));

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

            SwingUtilities.invokeLater(() -> setBlinkControlsEnabled(true));
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

        return success.get();
    }

    private void setBlinkControlsEnabled(boolean state)
    {
        btnBlink3.setEnabled(state);
        btnBlink6.setEnabled(state);
        btnBlinkCustom.setEnabled(state);
        spnBlinkCustom.setEnabled(state);
    }

    private void setConnectControlsEnabled(boolean state)
    {
        btnConnect.setEnabled(state);
        txtPort.setEnabled(state);
        btnDisconnect.setEnabled(!state);
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

    // initComponents() - IDE Generated code for Swing GUI components:
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
        jLabel3 = new javax.swing.JLabel();
        txtPort = new javax.swing.JTextField();
        btnConnect = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        btnDisconnect = new javax.swing.JButton();

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

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("Port:");

        txtPort.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        txtPort.setText("COM5");

        btnConnect.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnConnect.setText("Connect");

        btnDisconnect.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnDisconnect.setText("Disconnect");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnBlink3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spnBlinkCustom, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnBlinkCustom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnBlink6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnConnect)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDisconnect)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnConnect, btnDisconnect});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConnect)
                    .addComponent(btnDisconnect))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnConnect, jLabel3, txtPort});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnBlinkCustom, jLabel1, jLabel2, spnBlinkCustom});

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
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnDisconnect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSpinner spnBlinkCustom;
    private javax.swing.JTextField txtPort;
    // End of variables declaration//GEN-END:variables
}
