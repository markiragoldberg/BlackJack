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
   private JTextArea chatbox;
   
   private JPanel infoPanel;
   private JTextArea handinfo;
   private JTextArea statsinfo;
   
   private JPanel inputPanel;
   //input buttons, fields go here
   
   private Socket connection;
   private DataInputStream input;
   private DataOutputStream output;
   private Thread outputThread;
   
   private boolean myTurn;
   private String[] players;

   // Set up user-interface and board
   public void init()
   {
      //messages from chat, server, game updates
      chatbox = new JTextArea( 20, 30 );
      chatbox.setEditable( false );
      getContentPane().add( new JScrollPane( chatbox ),
                            BorderLayout.WEST );
                       
      infoPanel = new JPanel();
      //current players + their hands + server's up card
      handinfo = new JTextArea( 20, 22 );
      handinfo.setEditable(false);
      infoPanel.add(new JScrollPane(handinfo),
                           BorderLayout.NORTH);
      //stats readout for stats request
      statsinfo = new JTextArea( 20, 8);
      statsinfo.setEditable(false);
      infoPanel.add(statsinfo,
                     BorderLayout.SOUTH);

      //panel with all inputs
      inputPanel = new JPanel();
      inputPanel.setLayout(new GridBagLayout());
      //add a bunch of inputs here:
         //hit button
         //stand button
         //quit button
         //chat field + button
         //stats field (for username statted) + button
            //might use JComboBox...
         //play again button (for when game's over)
         //observe button (same, but for not being dealt in)
      
      getContentPane().add(inputPanel,
                           BorderLayout.SOUTH);

      //player username
      //arguably ought to remove this?
      id = new JTextField();
      id.setEditable( false );
      
      id.setText("your username");
      
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
         //Remove the "chat: " part and add to the chatbox
         chatbox.append(s.substring(6));
      }
      if(s.contains("stats: ")) {
         //IMO we can just dump stats in the chatbox too
         chatbox.append(s.substring(7));
      }
      if(s.contains("Your turn")){
    	  try {
              Thread.sleep(5000);                 //1000 milliseconds is one second.
              } catch(InterruptedException ex) {
                  Thread.currentThread().interrupt();
              }
		   chatbox.append("It's your turn. Hit or stand?\n");
	   }

      chatbox.setCaretPosition(
         chatbox.getText().length() );
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
   
   public void stats(String username)
   {
      try {
         output.writeUTF("stats: " + username);
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
        applet.stats(statsField.getText());
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
