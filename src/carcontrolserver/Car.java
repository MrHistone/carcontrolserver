package carcontrolserver;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.text.SimpleDateFormat;
import java.util.Date;
import carcontrol.Defaults.*;

public class Car {

    private SimpleDateFormat sdf;
    private GpioController gpio;
    final GpioPinDigitalOutput pin_0;
    final GpioPinDigitalOutput pin_1;
    final GpioPinDigitalOutput pin_2;
    final GpioPinDigitalOutput pin_3;

    private boolean setPin0 = false, setPin1 = false, setPin2 = false, setPin3 = false;
    private Coordinates coordinates;
    private boolean continueScanning = true;

    public Car() {
        sdf = new SimpleDateFormat("HH:mm:ss");
        // create gpio controller
        gpio = GpioFactory.getInstance();

        // provision gpio pins output pins
        pin_0 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "LED0", PinState.LOW);
        pin_1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "LED1", PinState.LOW);
        pin_2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "LED2", PinState.LOW);
        pin_3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "LED3", PinState.LOW);

        // set shutdown state for the pins
        pin_0.setShutdownOptions(true, PinState.LOW);
        pin_1.setShutdownOptions(true, PinState.LOW);
        pin_2.setShutdownOptions(true, PinState.LOW);
        pin_3.setShutdownOptions(true, PinState.LOW);

        // Start scanning the coordinates
        Thread t = new Thread(new ScanCoordinates());
        t.setName("ScanCoordinates");
        t.start();
    }

    protected void moveCar(CarAction carAction, int delay) {
        // Move until stopped
        // Set delay

        switch (carAction) {
            case FORWARD:
                display("Forward.");
                pin_0.setState(PinState.HIGH);
                pin_3.setState(PinState.HIGH);
                break;
            case BACKWARD:
                display("Backward.");
                pin_1.setState(PinState.HIGH);
                pin_2.setState(PinState.HIGH);
                break;
            case LEFT:
                display("Left.");
                pin_1.setState(PinState.HIGH);
                pin_3.setState(PinState.HIGH);
                break;
            case RIGHT:
                display("Right.");
                pin_0.setState(PinState.HIGH);
                pin_2.setState(PinState.HIGH);
                break;
            case STOP:
                display("Stop.");
                pin_0.setState(PinState.LOW);
                pin_1.setState(PinState.LOW);
                pin_2.setState(PinState.LOW);
                pin_3.setState(PinState.LOW);
                break;
        }

    }

    protected void setCoordinates(Coordinates coordinates){
        this.coordinates = coordinates;
    }
    
    
    private void display(String msg) {
        String time = sdf.format(new Date()) + " Car: " + msg;
        System.out.println(time);
    }

    private class ScanCoordinates implements Runnable {

        @Override
        public void run() {
            while (continueScanning) {
                setPin0 = false;
                setPin1 = false;
                setPin2 = false;
                setPin3 = false;
                // Determine which pins should be activated.
                // Turn left
                if (coordinates.getXPercentage() < 0) {
                    setPin1 = true;
                    setPin3 = true;
                }
                // Turn right
                if (coordinates.getXPercentage() > 0) {
                    setPin0 = true;
                    setPin2 = true;
                }
                // Go back
                if (coordinates.getYPercentage() > 0) {
                    setPin1 = true;
                    setPin2 = true;
                }
                // Go forward
                if (coordinates.getYPercentage() < 0) {
                    setPin0 = true;
                    setPin3 = true;
                }

                if (setPin0 == true) {
                    pin_0.setState(PinState.HIGH);
                } else {
                    pin_0.setState(PinState.LOW);
                }

                if (setPin1 == true) {
                    pin_1.setState(PinState.HIGH);
                } else {
                    pin_1.setState(PinState.LOW);
                }

                if (setPin2 == true) {
                    pin_2.setState(PinState.HIGH);
                } else {
                    pin_2.setState(PinState.LOW);
                }

                if (setPin3 == true) {
                    pin_3.setState(PinState.HIGH);
                } else {
                    pin_3.setState(PinState.LOW);
                }
                
                
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ex) {

                }

            }
        }
    }

}
