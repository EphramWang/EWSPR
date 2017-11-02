package android.serialport.reader.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class AlertThread extends Thread {
    public static final int RATE = 44100;
//    public static final float MAXVOLUME = 100f;

    public static int audioLength = 1000;

    AudioTrack mAudioTrack;
    public static boolean ISPLAYSOUND;

    /**
     * 总长度
     **/
    int length;
    /**
     * 一个正弦波的长度
     **/
    int waveLen;
    /**
     * 频率
     **/
    int Hz;
    /**
     * 正弦波
     **/
    byte[][] wave;

    int[] rateArray = {100, 200, 400, 800, 1600, 3200};
    int[] lengthArray;

    /**
     * 初始化
     */
    public AlertThread() {
        wave = new byte[rateArray.length][];
        lengthArray = new int[rateArray.length];
        for (int i = 0; i < rateArray.length; i++) {
            int rate = rateArray[i];
                Hz = rate;
                waveLen = RATE / Hz;
                length = waveLen * Hz;
                lengthArray[i] = length;
                wave[i] = new byte[RATE];
                wave[i] = SinWave.sin(wave[i], waveLen, length);
        }

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, RATE,
                AudioFormat.CHANNEL_CONFIGURATION_STEREO, // CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_8BIT, length, AudioTrack.MODE_STREAM);
        ISPLAYSOUND = true;

    }

    @Override
    public void run() {
        super.run();
        if (null != mAudioTrack)
            mAudioTrack.play();
        //一直播放
//        while (ISPLAYSOUND) {
//            mAudioTrack.write(wave[0], 0, lengthArray[0]);
//            mAudioTrack.write(wave[4], 0, lengthArray[4]);
//        }

    }

    public void addSound(int vol) {
        //mAudioTrack.write(wave[vol], 0, lengthArray[vol] / 8);
        mAudioTrack.write(wave[vol], 0, audioLength);
    }

    /**
     * 设置左右声道，左声道时设置右声道音量为0，右声道设置左声道音量为0
     *
     * @param left  左声道
     * @param right 右声道
     */
    public void setChannel(boolean left, boolean right) {
        if (null != mAudioTrack) {
            mAudioTrack.setStereoVolume(left ? 1 : 0, right ? 1 : 0);
        }
    }

    //设置音量
    public void setVolume(float left, float right) {
        if (null != mAudioTrack) {
            mAudioTrack.setStereoVolume(left,right);
        }
    }

    public void stopPlay() {
        ISPLAYSOUND = false;
        releaseAudioTrack();
    }

    private void releaseAudioTrack() {
        if (null != mAudioTrack) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }
}
