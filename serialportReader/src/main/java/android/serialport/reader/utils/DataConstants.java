package android.serialport.reader.utils;

/**
 * Created by ning on 17/9/7.
 */

public class DataConstants {

    public static final int DATA_FRAME_LENGTH = 534;
    public static final int COMMAND_FRAME_LENGTH = 7;

    public static final byte FRAME_HEAD = (byte) 0xFE;
    public static final byte FRAME_TAIL = (byte) 0xC3;

    public static final byte command_send_check = (byte) 0x00;
    public static final byte command_send_sensitivity = (byte) 0x01;
    public static final byte command_send_workmode = (byte) 0x02;
    public static final byte command_send_battery = (byte) 0x03;
    public static final byte command_send_power = (byte) 0x05;
    public static final byte command_send_szbzpl = (byte) 0x06;
    public static final byte command_send_szfdzy = (byte) 0x07;

    public static final byte command_receive_check = (byte) 0x80;
    public static final byte command_receive_sesitivity = (byte) 0x81;
    public static final byte command_receive_workmode = (byte) 0x82;
    public static final byte command_receive_battery = (byte) 0x83;
    public static final byte command_receive_power = (byte) 0x85;
    public static final byte command_receive_szbzpl = (byte) 0x86;
    public static final byte command_receive_szfdzy = (byte) 0x87;

    public static final byte command_receive_data = (byte) 0x88;

    public static byte[] getControlCommandBytes(byte commandID, byte byteContent) {
        byte[] commandBytes = new byte[7];
        commandBytes[0] = FRAME_HEAD;
        commandBytes[1] = commandID;
        commandBytes[2] = (byte) 0x01;//数据长度
        commandBytes[3] = (byte) 0x00;//数据长度
        commandBytes[5] = (byte) 0x00;//校验??
        commandBytes[6] = FRAME_TAIL;

        switch (commandID) {
            case command_send_check:
                commandBytes[4] = (byte) 0x00;//数据内容
                break;
            case command_send_sensitivity:
                commandBytes[4] = byteContent;
                break;
            case command_send_workmode:
                commandBytes[4] = byteContent;
                break;
            case command_send_battery:
                commandBytes[4] = (byte) 0x00;//数据内容
                break;
            case command_send_power:
                commandBytes[4] = byteContent;
                break;
            case command_send_szbzpl:
                commandBytes[4] = byteContent;
                break;
            case command_send_szfdzy:
                commandBytes[4] = byteContent;
                break;
        }

        commandBytes[5] = getCheckSumByte(commandBytes);

        return commandBytes;
    }

    public static byte getCheckSumByte(byte[] byteArray) {
        int sum = 0;
        for (int i = 0; i < byteArray.length; i++) {
            sum += byteArray[i];
        }
        int checksum = 0 - sum;
        return (byte) checksum;
    }

}
