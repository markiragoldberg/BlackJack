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
   //enormous load of UI stuff
   //login page stuff
   private JTextField loginField;
   private JPasswordField passwordField;
   private JButton loginButton;
   
   // game page stuff
   private JTextField id;
   private JTextArea chatbox;
   
   private JPanel infoPanel;
   private JTextArea handinfo;
   private JTextArea statsinfo;
   
   private JPanel inputPanel;
   
   private JPanel inputTopRow;
   private JButton hitMeButton;
   private JButton standButton;
   private JTextField statsField;
   private JButton statsButton;
   
   private JPanel inputCenterRow;
   private JTextField chatField;
   private JButton chatButton;
   
   private JPanel inputBottomRow;
   private JButton quitButton;
   private JButton playAgainButton;
   
   //Networking stuff
   
   private Socket connection;
   private DataInputStream input;
   private DataOutputStream output;
   private Thread outputThread;
   private boolean loggedIn;
   
   //Game logic stuff
   private String[] players;
   
   public void init()
   {
      loggedIn = false;
      
      loginField = new JTextField("Enter username...", 20);
      passwordField = new JPasswordField(20);
      loginButton = new JButton("Login");
      loginButton.addMouseListener(
        new LoginListener(this, loginField, passwordField));
      
      getContentPane().add(loginField, BorderLayout.WEST);
      getContentPane().add(passwordField, BorderLayout.EAST);
      getContentPane().add(loginButton, BorderLayout.SOUTH);
      
      //TODO implement logging in
      login("Welcome to BlackJack!");
      
      setSize(800, 600);
   }

   // Set up user-interface and board
   public void login(String username)
   {
      //get rid of the login screen stuff
      getContentPane().removeAll();
      
      //messages from chat, server, game updates
      chatbox = new JTextArea();
      chatbox.setEditable( false );
      getContentPane().add( new JScrollPane( chatbox ),
                            BorderLayout.CENTER );
                       
      infoPanel = new JPanel();
      infoPanel.setLayout(new BorderLayout());
      //current players + their hands + server's up card
      handinfo = new JTextArea();
      handinfo.setEditable(false);
      infoPanel.add(new JScrollPane(handinfo),
                           BorderLayout.CENTER);
      //stats readout for stats request
      statsinfo = new JTextArea( 8, 25);
      statsinfo.setEditable(false);
      infoPanel.add(statsinfo,
                     BorderLayout.SOUTH);
      getContentPane().add(new JScrollPane(infoPanel),
                           BorderLayout.EAST);

      //panel with all inputs
      inputPanel = new JPanel();
      inputPanel.setLayout(new BorderLayout());
      
      inputTopRow = new JPanel();
      hitMeButton = new JButton("Hit Me");
      hitMeButton.addMouseListener(
        new HitMeListener(this));
      inputTopRow.add(hitMeButton);
      standButton = new JButton("Stand");
      standButton.addMouseListener(
        new StandListener(this));
      inputTopRow.add(standButton);
      statsField = new JTextField("Enter username...", 20);
      inputTopRow.add(statsField);
      statsButton = new JButton("Get Stats");
      standButton.addMouseListener(
        new StatsListener(this, statsField));
      inputTopRow.add(statsButton);
      inputPanel.add(inputTopRow, BorderLayout.NORTH);
      
      inputCenterRow = new JPanel();
      chatField = new JTextField("Say something...", 35);
      inputCenterRow.add(chatField);
      chatButton = new JButton("Chat");
      chatButton.addMouseListener(
        new ChatListener(this, chatField));
      inputCenterRow.add(chatButton);
      inputPanel.add(inputCenterRow, BorderLayout.CENTER);
      
      inputBottomRow = new JPanel();
      quitButton = new JButton("Quit");
      quitButton.addMouseListener(
        new QuitListener(this));
      inputBottomRow.add(quitButton);
      playAgainButton = new JButton("Play Again");
      playAgainButton.addMouseListener(
        new PlayAgainListener(this));
      playAgainButton.setEnabled(false);
      inputBottomRow.add(playAgainButton);
      inputPanel.add(inputBottomRow, BorderLayout.SOUTH);
      
      getContentPane().add(inputPanel,
                           BorderLayout.SOUTH);

      //player username
      //arguably ought to remove this?
      id = new JTextField();
      id.setEditable( false );
      
      id.setText(username);
      
      getContentPane().add( id, BorderLayout.NORTH );
      
      setSize(800, 600);
      getContentPane().revalidate();
      getContentPane().repaint();
      
      loggedIn = true;
      //Disable buttons until server tells us to go
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
      //Talk to Brian about this exception-to-game-over thing...

      // Receive messages sent to client
      boolean tryToRead = true;
      while ( true ) {
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
            tryToRead = false;
            System.exit(0);
         }
      }
   }

   // Process messages sent to client
   public void processMessage( String s )
   {
      if(s.contains("loggedin: ")) {
         if(!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  login(s.substring(10));
               }
            });
         } else {
            login(s.substring(10));
         }
      }
      else if(s.contains("chat: ")) {
         //Remove the "chat: " part and add to the chatbox
         handinfo.append(s.substring(6) + "\n");
      }
      else if(s.contains("stats: ")) {
         //IMO we can just dump stats in the chatbox too
         chatbox.append(s.substring(7));
      }
      else if(s.contains("Your turn")){
    	  try {
              Thread.sleep(5000);                 //1000 milliseconds is one second.
              } catch(InterruptedException ex) {
                  Thread.currentThread().interrupt();
              }
		   chatbox.append("It's your turn. Hit or stand?\n");
	   }
      else {
         chatbox.append(s);
      }

      chatbox.setCaretPosition(
         chatbox.getText().length() );
   }
   
   public void hitMe()
   {
      try {
         output.writeUTF("turn: hitme");
      }
      catch ( IOException ie ) {
         ie.printStackTrace();         
      }
   }
   
   public void stand()
   {
      try {
         output.writeUTF("turn: stand");
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
   
   public void attemptToLogin()
   {
      try {
         output.writeUTF("loginpw: " + 
            loginField.getText() + 
            new String(passwordField.getPassword()));
      } catch (IOException ie) {
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

class ChatListener extends MouseAdapter {
    private BlackJackClient applet;
    private JTextField chatField;
    
    public ChatListener( BlackJackClient t, JTextField chatField) {
        applet = t;
        this.chatField = chatField;
    }

    public void mouseReleased( MouseEvent e ) {
        if(!chatField.getText().isEmpty()) {
            applet.chat(chatField.getText());
            chatField.setText("");
        }
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

class PlayAgainListener extends MouseAdapter {
    private BlackJackClient applet;
    
    public PlayAgainListener( BlackJackClient t) {
        applet = t;
    }

    public void mouseReleased( MouseEvent e ) {
        //tell server you want to play again...
    }
}

class QuitListener extends MouseAdapter {
    private BlackJackClient applet;
    
    public QuitListener(BlackJackClient t) {
        applet = t;
    }

    public void mouseReleased( MouseEvent e ) {
         //TODO: Tell the server you're quitting
         System.exit(0);
    }
}

class LoginListener extends MouseAdapter {
   private BlackJackClient applet;
   private JTextField loginField;
   private JPasswordField passwordField;
   
   public LoginListener(BlackJackClient t, 
         JTextField loginField, JPasswordField passwordField) {
      applet = t;
      this.loginField = loginField;
      this.passwordField = passwordField;
   }

   public void mouseReleased( MouseEvent e ) {
      // attempt to log in...
      
      //send login & pw to server...
      //run() should get login confirm from server...
      //run() should invokeAndWait() on login()...
    
    //  applet.attemptToLogin();
    
    //TODO remove this call, doesn't belong here
    applet.login("DEBUG");
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
