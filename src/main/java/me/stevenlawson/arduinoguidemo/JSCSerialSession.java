package me.stevenlawson.arduinoguidemo;

import com.fazecast.jSerialComm.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.StringUtils;

public class JSCSerialSession
{
    private boolean fault = false;
    private final AtomicLong lastRXTXTime = new AtomicLong(0);
    private final LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
    private final JSCSerialHandler handler;
    private String bufferCarry = "";

    public JSCSerialSession(final String portName, final int baudRate) throws IOException
    {
        handler = new JSCSerialHandler(portName, baudRate);

        if (!handler.setEnabled(true))
        {
            throw new IOException(String.format("Error connecting to serial port '%s'.", portName));
        }

        if (!handler.isConnected())
        {
            throw new IOException(String.format("Error connecting to serial port '%s'.", portName));
        }

        final SerialPort serialPort = handler.getSerialPort();
        serialPort.addDataListener(new SerialPortDataListener()
        {
            @Override
            public int getListeningEvents()
            {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event)
            {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                {
                    return;
                }

                final int bytesAvailable = serialPort.bytesAvailable();
                if (bytesAvailable > 0)
                {
                    final byte[] readBytes = new byte[bytesAvailable];
                    serialPort.readBytes(readBytes, bytesAvailable);

                    if (readBytes.length > 0)
                    {
                        final String buffer = bufferCarry + new String(readBytes, 0, readBytes.length);
                        bufferCarry = "";

                        final String[] lines = buffer.split("\\n");
                        for (int i = 0; i < (lines.length - 1); i++)
                        {
                            final String line = StringUtils.trimToEmpty(lines[i]);
                            if (!line.isEmpty())
                            {
                                lastRXTXTime.set(System.currentTimeMillis());
                                messages.offer(line);
                            }
                        }

                        final String lastLine = StringUtils.trimToEmpty(lines[lines.length - 1]);
                        if (buffer.endsWith("\n"))
                        {
                            if (!lastLine.isEmpty())
                            {
                                lastRXTXTime.set(System.currentTimeMillis());
                                messages.offer(lastLine);
                            }
                        }
                        else
                        {
                            bufferCarry = lastLine;
                        }
                    }
                }
            }
        });
    }

    public static byte[] stripCharacter(char c, byte[] in)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte b : in)
        {
            if (b != c)
            {
                out.write(b);
            }
        }
        return out.toByteArray();
    }

    public boolean sendCommand(final boolean purge, final String command)
    {
        if (!isReady())
        {
            return false;
        }

        if (purge)
        {
            messages.clear();
        }

        lastRXTXTime.set(System.currentTimeMillis());

        if (command != null)
        {
            handler.writeData(command);
        }

        return true;
    }

    public LinkedBlockingQueue<String> getMessages()
    {
        return messages;
    }

    public void processMessages(final AtomicBoolean interrupt, final long timeoutMS, final Callback<String> callback)
    {
        interrupt.set(false);
        final Stopwatch sw = new Stopwatch().start();
        while ((!interrupt.get()) && (sw.elapsed() < timeoutMS))
        {
            String msg = null;
            try
            {
                msg = messages.poll(100, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException ex)
            {
            }
            if (msg != null && callback != null)
            {
                callback.run(msg);
            }
        }
    }

    public void waitForRXTXIdle(long setpointMS)
    {
        while ((System.currentTimeMillis() - lastRXTXTime.get()) < setpointMS)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ex)
            {
            }
        }
    }

    public boolean terminate()
    {
        if (!isReady())
        {
            return false;
        }
        return handler.setEnabled(false);
    }

    public boolean isReady()
    {
        if (fault)
        {
            return false;
        }

        if (!handler.isConnected())
        {
            fault = true;
        }

        return !fault;
    }
}
