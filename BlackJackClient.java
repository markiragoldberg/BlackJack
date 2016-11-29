// Fig. 21.8: TicTacToeClient.java
// Client for the TicTacToe program
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.*;

// Client class to let a user play Tic-Tac-Toe with
// another user across a network.
public class BlackJackClient extends JApplet
                             implements Runnable {
   private JTextField id;
   private JTextArea display;
   
   private Socket connection;
   private DataInputStream input;
   private DataOutputStream output;
   private Thread outputThread;
   
   private boolean myTurn;
   private String[] players;

   // Set up user-interface and board
   public void init()
   {
      display = new JTextArea( 20, 30 );
      display.setEditable( false );
      getContentPane().add( new JScrollPane( display ),
                            BorderLayout.SOUTH );

      id = new JTextField();
      id.setEditable( false );
      
      players = new String[6];
      players[0] = "your name here";
      
      getContentPane().add( id, BorderLayout.NORTH );
      
      setSize(500,500);
   }

   // Make connection to server and get associated streams.
   // Start separate thread to allow this applet to
   // continually update its output in text area display.
   public void start()
   {
      try {
         connection = new Socket(
            InetAddress.getByName( "127.0.0.1" ), 5000 );
         input = new DataInputStream(
                        connection.getInputStream() );
         output = new DataOutputStream(
                        connection.getOutputStream() );
      }
      catch ( IOException e ) {
         e.printStackTrace();         
      }

      outputThread = new Thread( this );
      outputThread.start();
   }

   // Control thread that allows continuous update of the
   // text area display.
   public void run()
   {      
      id.setText("No username yet");

      //Talk to Brian about this exception-to-game-over thing...

      // Receive messages sent to client
      boolean tryToRead = true;
      while ( tryToRead ) {
         try {
            String s = input.readUTF();
            processMessage( s );
         }
         catch ( IOException e ) {
            try {
            Thread.sleep(10000);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            
            System.err.println("Game Over!");
            tryToRead = false;
            System.exit(0);
         }
      }
   }

   // Process messages sent to client
   public void processMessage( String s )
   {
	   //Put the steps in here for blackjack game flow
	   
      //Need a common communication protocol with the Server
      //Snoop in the server file and see if Brian wrote something
      
	   //if server sent "this"
         //then update client with "this"
      //else if server sent "that"
         //then update client with "that"
      //else ...
      
      if(s.contains("chat: ")) {
         //Remove the "chat: " part and add to chatbox
         display.append(s.substring(6));
      }
      if(s.contains("Your turn")){
    	  try {
              Thread.sleep(5000);                 //1000 milliseconds is one second.
              } catch(InterruptedException ex) {
                  Thread.currentThread().interrupt();
              }
		   display.append("Would you like to hit?\n");
		   display.append("Other options here.....\n");
	   }

      display.setCaretPosition(
         display.getText().length() );
   }
   
   public void hitMe()
   {
      if( myTurn )
         try {
            output.writeUTF("turn: hitme");
            myTurn = false;
         }
         catch ( IOException ie ) {
            ie.printStackTrace();         
         }
   }
   
   public void stand()
   {
      if( myTurn )
         try {
            output.writeUTF("turn: stand");
            myTurn = false;
         }
         catch ( IOException ie ) {
            ie.printStackTrace();         
         }
   }
   
   public void chat(String message)
   {
         try {
            //Do not add username here (modded client could spoof it)
            //Server does it instead
            output.writeUTF("chat: " + message);
         }
         catch ( IOException ie ) {
            ie.printStackTrace();         
         }
   }
   
}

// Event Listeners for buttons

class HitMeListener extends MouseAdapter {
    private BlackJackClient applet;
    
    public HitMeListener( BlackJackClient t) {
        applet = t;
    }

    public void mouseReleased( MouseEvent e ) {
        applet.hitMe();
    }
}

class StandListener extends MouseAdapter {
    private BlackJackClient applet;
    
    public StandListener( BlackJackClient t) {
        applet = t;
    }

    public void mouseReleased( MouseEvent e ) {
        applet.stand();
    }
}

class SendChatListener extends MouseAdapter {
    private BlackJackClient applet;
    private JTextField chatField;
    
    public SendChatListener( BlackJackClient t, JTextField chatField) {
        applet = t;
        this.chatField = chatField;
    }

    public void mouseReleased( MouseEvent e ) {
        applet.chat(chatField.getText());
        chatField.setText("");
    }
}

class StatsListener extends MouseAdapter {
    private BlackJackClient applet;
    private JTextField statsField;
    
    public StatsListener( BlackJackClient t, JTextField statsField) {
        applet = t;
        this.statsField = statsField;
    }

    public void mouseReleased( MouseEvent e ) {
        applet.chat(statsField.getText());
        //Do not clear the statsField
        //The user may want to re-examine just one user's stats often
    }
}

/**************************************************************************
 * (C) Copyright 1999 by Deitel & Associates, Inc. and Prentice Hall.     *
 * All Rights Reserved.                                                   *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 *************************************************************************/
