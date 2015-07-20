/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.saga.console.test.sound;

import net.saga.console.sound.chips.TISN76489;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author summers
 */
public class TISN76489Tests {
    
    public TISN76489Tests() {
    }
  
    @Test
    /**
     * 
     */
    public void toneChannel0() {
        TISN76489 chip = new TISN76489();
        chip.channel0.tone = 0x0;
        int tone0_out_old = chip.channel0.out;
        int flips = 0;
        for (int cycle = 0; cycle < chip.clock; cycle++) {
            chip.cycle();
            if (tone0_out_old != chip.channel0.out) {
                //only count rising waves
                if (chip.channel0.out == 1) {
                    flips++;
                }
            }
            tone0_out_old = chip.channel0.out;
        }
        
        /**
         * Make sure that our counter is close ish.
         */
        assertEquals(2, Math.max(2, Math.abs(chip.clock/2 - flips)));
        
    }
    
    @Test
    /**
     * 
     */
    public void toneChannel1() {
        TISN76489 chip = new TISN76489();
        chip.channel1.tone = 0x0fe;
        int tone0_out_old = chip.channel1.out;
        int flips = 0;
        for (int cycle = 0; cycle < chip.clock; cycle++) {
            chip.cycle();
            if (tone0_out_old != chip.channel1.out) {
                //only count rising waves
                if (chip.channel1.out == 1) {
                    flips++;
                }
            }
            tone0_out_old = chip.channel1.out;
        }
        
        /**
         * Make sure that our counter is close ish.
         */
        assertEquals(2, Math.max(2, Math.abs(440 - flips)));
        
    }
    @Test
    /**
     * 
     */
    public void toneChannel2() {
        TISN76489 chip = new TISN76489();
        chip.channel2.tone = 0x3ff;
        int tone0_out_old = chip.channel2.out;
        int flips = 0;
        for (int cycle = 0; cycle < chip.clock; cycle++) {
            chip.cycle();
            if (tone0_out_old != chip.channel2.out) {
                //only count rising waves
                if (chip.channel2.out == 1) {
                    flips++;
                }
            }
            tone0_out_old = chip.channel2.out;
        }
        
        /**
         * Make sure that our counter is close ish.
         */
        assertEquals(2, Math.max(2, Math.abs(109 - flips)));
        
    }
    
    @Test
    public void noiseChannelSetup() {
        TISN76489 chip = new TISN76489();
        chip.noiseChannel.tone = 0x0;
        chip.cycle();
        
        /**
         * Make sure that our counter is close ish.
         */
        assertEquals(0x10, chip.noiseChannel.counter);
        chip = new TISN76489();
        chip.noiseChannel.tone = 0x1;
        chip.cycle();
        
        /**
         * Make sure that our counter is close ish.
         */
        assertEquals(0x20, chip.noiseChannel.counter);
        
        chip = new TISN76489();
        chip.noiseChannel.tone = 0x2;
        chip.cycle();
        
        assertEquals(0x40, chip.noiseChannel.counter);
        
        chip = new TISN76489();
        chip.noiseChannel.tone = 0x4;
        chip.cycle();
        
        /**
         * Make sure that our counter is close ish.
         */
        assertEquals(0x10, chip.noiseChannel.counter);
    }
    
    @Test
    public void noiseChannelLFSR() {
        TISN76489 chip = new TISN76489();
        chip.noiseChannel.tone = 4;
        chip.cycle();
        
        /**
         * Make sure that our counter is close ish.
         */
        assertEquals(0, chip.lfsr_out);
        assertEquals(0b0100000000000000, chip.lfsr);
    }
    
    @Test
    public void noiseChannelLFSRPeriodic() {
        TISN76489 chip = new TISN76489();
        chip.noiseChannel.tone = 0;
        for (int i = 0; i < 120; i++) {
            chip.cycle();
        }
        
        /**
         * Make sure that our counter is close ish.
         */
        assertEquals(0, chip.lfsr_out);
        assertEquals(0b0000100000000000, chip.lfsr);
        
        for (int i = 0; i < 120; i++) {
            chip.cycle();
        }
        
        assertEquals(0, chip.lfsr_out);
        assertEquals(0b0000000010000000, chip.lfsr);
        
        for (int i = 0; i < 241; i++) {
            chip.cycle();
        }
        assertEquals(0b1000000000000000, chip.lfsr);
        assertEquals(1, chip.lfsr_out);
        
        
    }
    
    @Test
    public void latching() {
    TISN76489 chip = new TISN76489();
    
    chip.write(0b10001110);
    chip.cycle();
    chip.write(0b00001111);
    chip.cycle();
    
    assertEquals(0xfe, chip.channel0.tone);
    
    chip.write(0b10111111);
    chip.cycle();
    
    assertEquals(0xf, chip.channel1.volume);
    
    chip.write(0b11011111);
    chip.cycle();
    chip.write(0b00000000);
    chip.cycle();
    
    assertEquals(0x0, chip.channel2.volume);
    
    chip.write(0b11100101);
    chip.cycle();
    
    assertEquals(0b101, chip.noiseChannel.tone);
    
    chip.write(0b11100101);
    chip.cycle();
    chip.write(0b00000100);
    chip.cycle();
    
    assertEquals(0b100, chip.noiseChannel.tone);
    
    }
    
}
