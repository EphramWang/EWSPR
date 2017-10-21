package android.serialport.reader.model;

import android.serialport.reader.utils.DataConstants;

/**
 * Created by ningw on 2017/9/24.
 */

public class CommandPackage {

    public long timestamp;

    public byte[] dataBytes = new byte[DataConstants.COMMAND_FRAME_LENGTH];

    public CommandPackage(long timestamp, byte[] dataBytes) {
        this.timestamp = timestamp;
        this.dataBytes = dataBytes;
    }

    public CommandPackage(ReceivedData receivedData) {
        this.timestamp = receivedData.timestamp;
        //this.dataBytes = receivedData.buffer;
        if (receivedData.size == dataBytes.length)
            System.arraycopy(receivedData.buffer, 0, dataBytes, 0, receivedData.size);
    }
}
