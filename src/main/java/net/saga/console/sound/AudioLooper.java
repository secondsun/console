package net.saga.console.sound;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import net.saga.console.sound.chips.TISN76489;
import net.saga.console.sound.chips.TISN76489.Channel;


public final class AudioLooper extends Thread {
    private static final String TAG = "AudioLooper";
    private static final float SAMPLE_RATE_IN_HZ = 44100;
    private static final int BYTES_PER_CHANNEL = 2;
    private static final int BITS_PER_CHANNEL = 16;
    private static final int NUM_OF_CHANNELS = 2;
    private static final int BUFFER_SIZE_IN_BYTES = 8192;
    private static final int BUFFER_SIZE_IN_SHORTS =
        BUFFER_SIZE_IN_BYTES / BYTES_PER_CHANNEL;
    private static final int BITS_PER_BYTE = 8;
    private static final int SHORT_MASK = 0xffff;
    private static final AudioFormat AUDIO_FORMAT =
        new AudioFormat(SAMPLE_RATE_IN_HZ,
            BITS_PER_CHANNEL,
            NUM_OF_CHANNELS,
            true,
            true);

    private SourceDataLine line = null;
    private TISN76489 chip = null;
    private byte[] buffer = new byte[BUFFER_SIZE_IN_BYTES];

    /**
     * Class constructor.
     * @throws LineUnavailableException Exception happened to get SoundDataLine
     * @see SoundDataLine
     */
    public AudioLooper() throws LineUnavailableException {
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info(TAG
                + "> J2SE Audio Looper");

        line = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info(TAG
                + "> " + line.toString());
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info(TAG
                + "> DefaultBufferSize: " + line.getBufferSize());
        line.open(AUDIO_FORMAT, BUFFER_SIZE_IN_BYTES);
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).info(TAG
                + "> ConfiguredBufferSize: " + line.getBufferSize());
    }

    /**
     * Register sound generator.
     * @param newChannel sound generator
     */
    public void setChip(final TISN76489 newChannel) {
        
        chip = newChannel;
    }

    /**
     * Run audio generation loop forever!
     * If you run audio loop in an individual thread, call start().
     * On running in your own thread, call run() directly.
     * It blocks and never be back.
     */
    public void run() {
        line.start();
        for (;;) {
            if (null != chip) {
                chip.cycle();
                byte[] channelBuffer = ByteBuffer.allocate(4).putInt(chip.output).array();
                buffer = new byte[]{channelBuffer[0], channelBuffer[1]};
                line.write(channelBuffer, 0, 4);
            }
        }
    }
}