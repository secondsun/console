package net.saga.console.sound;

import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import net.saga.console.sound.chips.TISN76489;

public class TIPlayer {

    public static void main(String args[]) throws LineUnavailableException {

        final AudioFormat af
                = new AudioFormat(16 * 1024, 8, 1, true, true);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open(af, 16 * 1024);

            TISN76489 chip = new TISN76489();
            chip.write(0b10001110);
            chip.cycle();
            chip.write(0b00001111);
            chip.cycle();
            for (int i = 0; i < 1024 * 1024 * 4; i++) {
                chip.cycle();
                byte data[] = ByteBuffer.allocate(4).putInt(chip.output).array();
                line.write(data, 0, 4);
            }
            line.drain();
        }
    }

}
