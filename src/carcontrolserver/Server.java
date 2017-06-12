package carcontrolserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import carcontrol.Defaults.*;

public class Server {

    private SimpleDateFormat sdf;
    private int port;
    private boolean forward = false;
    private CarAction carAction;
    private Car car;
    private boolean carAvailable;
    private Coordinates coordinates;
    private DecimalFormat df = new DecimalFormat("#"); 

    public Server(int port, boolean carAvailable) {
        // the port
        this.port = port;
        this.carAvailable = carAvailable;
        sdf = new SimpleDateFormat("HH:mm:ss");
        if (carAvailable) {
            car = new Car();
        }
    }

    public void start() {
        /* create socket server and wait for connection requests */
        try {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            // format message saying we are waiting
            display("The car is waiting for connections on port " + port + ".");
            display("The IP-address is: " + Inet4Address.getLocalHost().getHostAddress());
            Socket socket = serverSocket.accept();  	// accept connection

            ClientThread t = new ClientThread(socket);  // make a thread of it
            t.start();

        } // something went bad
        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    /*
     * For the GUI to stop the server
     */
    protected void stop() {
        try {
            new Socket("localhost", port);
        } catch (Exception e) {

        }
    }

    private void determineCarAction(String clientMessage) {
        switch (clientMessage) {
            case "RIGHT":
                carAction = carAction.RIGHT;
                break;
            case "LEFT":
                carAction = carAction.LEFT;
                break;
            case "FORWARD":
                carAction = carAction.FORWARD;
                break;
            case "BACKWARD":
                carAction = carAction.BACKWARD;
                break;
            case "STOP":
                carAction = carAction.STOP;
                break;
            case "HORN":
                carAction = carAction.HORN;
                break;
            default:
                carAction = CarAction.STOP;

        }
    }

    private void initiateCarAction() {
        switch (carAction) {
            case BACKWARD:
                car.moveCar(carAction, 100);
                break;
            case FORWARD:
                car.moveCar(carAction, 100);
                break;
            case LEFT:
                car.moveCar(carAction, 100);
                break;
            case RIGHT:
                car.moveCar(carAction, 100);
                break;
            case HORN:
                break;
            case STOP:
                car.moveCar(carAction, 100);
                break;
        }
    }

    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        System.out.println(time);
    }

    class ClientThread extends Thread {

        boolean keepGoing = true;
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        String strMsg;

        // the date I connect
        String date;

        // Constructor
        ClientThread(Socket socket) {
            this.socket = socket;
            /* Creating both Data Stream */
            System.out.println("Thread trying to create Object Input/Output Streams");
            try {
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());

            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            date = new Date().toString() + "\n";
        }

        public void run() {
            while (keepGoing) {
                receiveObject();
            }
            close();
        }

        private void receiveObject() {
            try {
                coordinates = (Coordinates) sInput.readObject();
                if (coordinates.getXCoordinate() != 0 || coordinates.getYCoordinate() != 0) {
                    display("X%: "
                            + df.format(coordinates.getXPercentage())
                            + "\tY%: "
                            + df.format(coordinates.getYPercentage())
                            + "\tX: "
                            + df.format(coordinates.getXCoordinate())
                            + "\tY: "
                            + df.format(coordinates.getYCoordinate())
                            + "\t" + coordinates.getMessage());
                }

                if (coordinates.getMessage() != null && coordinates.getMessage() != "") {
                    display(coordinates.getMessage());
                }

                if (carAvailable) {
                    car.setCoordinates(coordinates);
                }

            } catch (ClassCastException ex) {
                display("Object received cannot be cast to an instance of class Coordinates. " + ex);
            } catch (IOException e) {
                display("Disconnected...: " + e);
                keepGoing = false;
            } catch (ClassNotFoundException e2) {
                keepGoing = false;
            }
        }

        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if (sOutput != null) {
                    sOutput.close();
                }
            } catch (Exception e) {
            }
            try {
                if (sInput != null) {
                    sInput.close();
                }
            } catch (Exception e) {
            };
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
            }
        }

        /*
         * Write a String to the Client output stream
         */
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            } catch (IOException e) {
                display("Error sending message");
                display(e.toString());
            }
            return true;
        }

    }
}
