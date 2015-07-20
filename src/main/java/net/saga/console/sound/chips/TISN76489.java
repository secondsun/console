package net.saga.console.sound.chips;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Emulates a https://en.wikipedia.org/wiki/Texas_Instruments_SN76489
 *
 * Got info from http://www.smspower.org/Development/SN76489
 *
 * @author summers
 */
public class TISN76489 {

    public Channel channel0 = new Channel();
    public Channel channel1 = new Channel();
    public Channel channel2 = new Channel();
    public Channel noiseChannel = new Channel();

    private Channel[] channels = {channel0, channel1, channel2, noiseChannel};

    public int lfsr = 0b1000000000000000;
    public int lfsr_out = 0;

    public int output = 0;
    private int latch = 0;

    private static int volumeTable[] = {
        32767, 26028, 20675, 16422, 13045, 10362, 8231, 6568,
        5193, 4125, 3277, 2603, 2067, 1642, 1304, 0
    };

    /**
     * Clock value defaults to 3579545 / 16 Hz
     */
    public int clock = 3579545 / 16;

    private int LATCH_MASK = 0b10000000;

    /**
     * Latch register selection stuff
     */
    private int CHANNEL_MASK = 0b01100000;
    private int CHANNEL_SHIFT = 5;
    private int TYPE_MASK = 0b00010000;
    private int LOW_DATA_MASK = 0b00001111;
    private int HIGH_DATA_MASK = 0b00111111;

    /**
     * Data byte mask
     */
    private int DATA_MASK = 0b00111111;
    private Integer data = null;

    public void write(int data) {
        this.data = data;
    }

    public void cycle() {
        try {
            if (data != null) {
                read(data);
                data = null;
            }
            cycleTone(channel0);
            cycleTone(channel1);
            cycleTone(channel2);
            cycleNoise();
            applyVolumes();
            Thread.sleep(0, 1 / clock);
        } catch (InterruptedException ex) {
            Logger.getLogger(TISN76489.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cycleTone(Channel channel) {

        channel.counter--;
        if (channel.counter <= 0) {
            channel.counter = channel.tone;
            if (channel.out == 0) {
                channel.out = 1;
            } else {
                channel.out = 0;
            }
        }

    }

    private void cycleNoise() {
        noiseChannel.counter--;
        if (noiseChannel.counter <= 0) {
            switch (noiseChannel.tone & 0b11) {
                case 0:
                    noiseChannel.counter = 0x10;
                    break;
                case 1:
                    noiseChannel.counter = 0x20;
                    break;
                case 2:
                    noiseChannel.counter = 0x40;
                    break;
                case 3:
                    noiseChannel.counter = channel2.tone;
                    break;

            }
            if (noiseChannel.out == 0) {
                noiseChannel.out = 1;
                shiftLFSR();
            } else {
                noiseChannel.out = 0;
            }

        }
    }

    private void shiftLFSR() {
        //assign last bit to lsfr_out
        lfsr_out = lfsr & 0b0000000000000001;
        //shift
        lfsr = lfsr >> 1;

        if ((noiseChannel.tone & 0b100) == 0b100) {

            int lsfr_bit4 = (this.lfsr & 0b0000000000001000) >> 3;
            int lsfr_bit1 = this.lfsr & 0b0000000000000001;

            if ((lsfr_bit1 ^ lsfr_bit4) != 0) {
                lfsr = lfsr | (0b1000000000000000);
            } else {
                lfsr = lfsr & (0b0111111111111111);
            }
        } else {

            if (lfsr == 0) {
                lfsr = (0b1000000000000000);
            }
        }

    }

    private void applyVolumes() {
        output = channel0.out * volumeTable[channel0.volume] + channel1.out * volumeTable[channel1.volume] + channel2.out * volumeTable[channel2.volume] + noiseChannel.out * volumeTable[noiseChannel.volume];
    }

    private void read(int input) {

        int mask;
        if ((input & LATCH_MASK) > 0) {
            latch = input;
            mask = LOW_DATA_MASK;
        } else {
            mask = HIGH_DATA_MASK;
        }

        int channelIndex = ((latch & CHANNEL_MASK) >> CHANNEL_SHIFT);
        Channel selectedChannel = this.channels[channelIndex];
        int volumeLatch = (latch & TYPE_MASK);
        if (volumeLatch > 0) {
            //latch volume
            int data = input & LOW_DATA_MASK;
            selectedChannel.volume = (selectedChannel.volume & 0b1111110000) | data;

        } else {
            //latch tone
            int data = input & mask;
            if (selectedChannel == noiseChannel || mask == LOW_DATA_MASK) {
                selectedChannel.tone = (selectedChannel.tone & 0b1111110000) | data;
            } else {
                selectedChannel.tone = (selectedChannel.tone & 0b0000001111) | (data << 4);
            }
        }

    }

    public static class Channel {

        /**
         * Volume registers
         *
         * The value represents the attenuation of the output.
         *
         * Hence, %0000 is full volume and %1111 is silence.
         */
        public int volume = 0;
        /**
         * Tone registers
         *
         * These give a counter reset value for the tone generators. Hence, low
         * values give high frequencies and vice versa.
         *
         * Noise register
         *
         * One bit selects the mode ("periodic" or "white") and the other two
         * select a shift rate.
         *
         *
         */
        public int tone = 0;

        
        /**
         * Tone register counters
         */
        public int counter = 0;
        /**
         * Tone register outputs
         */
        public int out = 0;
    }

}
