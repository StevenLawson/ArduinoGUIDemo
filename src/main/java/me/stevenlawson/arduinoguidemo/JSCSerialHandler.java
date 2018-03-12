package me.stevenlawson.arduinoguidemo;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;

public class JSCSerialHandler
{
    private final SerialPort serialPort;
    private final int baudRate;

    public JSCSerialHandler(final String portName, final int baudRate) throws IOException
    {
        this.baudRate = baudRate;

        final SerialPort _serialPort = SerialPort.getCommPort(portName);

        if (_serialPort.getDescriptivePortName().contains("Bad Port"))
        {
            serialPort = null;
        }
        else
        {
            serialPort = _serialPort;
        }

        if (serialPort == null)
        {
            throw new IOException(String.format(
                    "Port '%s' is not a valid serial port. Valid ports: %s",
                    portName,
                    StringUtils.join(getCommPortNames(), ", ")
            ));
        }
    }

    private boolean open()
    {
        if (serialPort == null)
        {
            return false;
        }
        serialPort.closePort();
        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        return serialPort.openPort();
    }

    private boolean close()
    {
        if (serialPort == null)
        {
            return false;
        }
        return serialPort.closePort();
    }

    public boolean setEnabled(boolean enabled)
    {
        if (enabled)
        {
            return open();
        }
        else
        {
            return close();
        }
    }

    public boolean isConnected()
    {
        if (serialPort == null)
        {
            return false;
        }
        return serialPort.isOpen();
    }

    public boolean writeData(String data)
    {
        if (serialPort == null)
        {
            return false;
        }
        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        return serialPort.writeBytes(bytes, bytes.length) != -1;
    }

    public SerialPort getSerialPort()
    {
        return serialPort;
    }

    public static String[] getCommPortNames()
    {
        final SerialPort[] ports = SerialPort.getCommPorts();
        final String[] names = new String[ports.length];
        for (int i = 0; i < ports.length; i++)
        {
            names[i] = ports[i].getSystemPortName();
        }
        return names;
    }
}
