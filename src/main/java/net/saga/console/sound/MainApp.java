package net.saga.console.sound;

import java.nio.ByteBuffer;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import net.saga.console.sound.chips.TISN76489;


public class MainApp  {


        public static void main(String args[]) throws LineUnavailableException, InterruptedException {

        final AudioFormat af
                = new AudioFormat(4 * 1024 * 1024 / 16, 8, 1, true, true);
        
            TISN76489 chip = new TISN76489();

            chip.write(0b11100101);
            chip.cycle();

            chip.write(0b00001100);
            chip.cycle();

            AudioLooper looper = new AudioLooper();
            looper.setChip(chip);
            
            looper.start();
            Thread.sleep(2000);
            looper.stop();
        
    }

}
