package android.serialport.reader.model;

/**
 * Created by ningw on 2017/9/24.
 */

public class ReceivedData {

    public byte[] buffer;
    public int size;
    public long timestamp;

    public ReceivedData(byte[] buffer, int size, long timestamp) {
        this.buffer = buffer;
        this.size = size;
        this.timestamp = timestamp;
    }
}
