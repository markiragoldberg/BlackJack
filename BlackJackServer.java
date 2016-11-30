// Fig. 21.7: TicTacToeServer.java
// This class maintains a game of Tic-Tac-Toe for two
// client applets.
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import java.util.List;

public class BlackJackServer extends JFrame {
   private byte board[];
   private boolean xMove;
   private JTextArea output;
   private Player players[];
   private ServerSocket server;
   private int currentPlayer;
   public int deckIndex = 0;
   DeckofCards deck = new DeckofCards();

   public BlackJackServer()
   {
      super( "BlackJack Server" );

      board = new byte[ 9 ];
      xMove = true;
      players = new Player[ 2 ];
      currentPlayer = 0;
 
      // set up ServerSocket
      try {
         server = new ServerSocket( 5000, 2 );
      }
      catch( IOException e ) {
         e.printStackTrace();
         System.exit( 1 );
      }

      output = new JTextArea();
      getContentPane().add(new JScrollPane(output), BorderLayout.CENTER );
      output.setText( "Server awaiting connections\n" );
      output.append("The deck has been created and shuffled for this game...\n");
      output.append(deck.getDeck().toString());

      setSize( 500, 1000 );
      setVisible(true);
   }
   
   public void incrementIndex(){
	   deckIndex = deckIndex + 1;
   }
   
   public int getDeckIndex(){
	   return deckIndex;
   }

   // wait for two connections so game can be played
   public void execute()
   {
      for ( int i = 0; i < players.length; i++ ) {
         try {
            players[ i ] =
               new Player( server.accept(), this, i );
            players[ i ].start();
         }
         catch( IOException e ) {
            e.printStackTrace();
            System.exit( 1 );
         }
      }

      // Player X is suspended until Player O connects.
      // Resume player X now.          
      synchronized ( players[ 0 ] ) {
         players[ 0 ].threadSuspended = false;   
         players[ 0 ].notify();
      }
  
   }
   
   public void display( String s )
   {
      output.append( s + "\n" );
   }
 
   // Determine if a move is valid.
   // This method is synchronized because only one move can be
   // made at a time.
   public synchronized boolean validMove( int loc,
                                          int player )
   {
      boolean moveDone = false;

      while ( player != currentPlayer ) {
         try {
            wait();
         }
         catch( InterruptedException e ) {
            e.printStackTrace();
         }
      }

      if ( !isOccupied( loc ) ) {
         board[ loc ] =
            (byte) ( currentPlayer == 0 ? 'X' : 'O' );
         //Race condition might be solved by using something similar to this code
         currentPlayer = ( currentPlayer + 1 ) % 2;
         players[ currentPlayer ].otherPlayerMoved( loc );
         notify();    // tell waiting player to continue
         return true;
      }
      else 
         return false;
   }
   
   public void alertOtherPlayerGameOver(){
       try{
       players[currentPlayer].output.writeUTF("Gameover, you loose!");
       }catch( IOException e ) { 
       e.printStackTrace();
       }
   }

   public void sendMessageToOtherPlayer(String message, int number){
	   int otherPlayer;
       try{
       if(number == 0){
    	   otherPlayer = 1;
       }else{
    	   otherPlayer = 0;
       }
       players[otherPlayer].output.writeUTF(message);
       }catch( IOException e ) { 
       e.printStackTrace();
       }
   }
   
   
   public boolean isOccupied( int loc )
   {
      if ( board[ loc ] == 'X' || board [ loc ] == 'O' )
          return true;
      else
          return false;
   }

   public String gameOver()
   {
      // Place code here to test for a winner of the game
      if((board[0] == 'X' && board[1] == 'X' && board[2] == 'X') || (board[0] == 'O' && board[1] == 'O' && board[2] == 'O') 
      || (board[3] == 'X' && board[4] == 'X' && board[5] == 'X')   || (board[3] == 'O' && board[4] == 'O' && board[5] == 'O')
      || (board[6] == 'X' && board[7] == 'X' && board[8] == 'X')   || (board[6] == 'O' && board[7] == 'O' && board[8] == 'O')
      || (board[0] == 'X' && board[3] == 'X' && board[6] == 'X')   || (board[0] == 'O' && board[3] == 'O' && board[6] == 'O')
      || (board[1] == 'X' && board[4] == 'X' && board[7] == 'X')   || (board[1] == 'O' && board[4] == 'O' && board[7] == 'O')
      || (board[2] == 'X' && board[5] == 'X' && board[8] == 'X')   || (board[2] == 'O' && board[5] == 'O' && board[8] == 'O')
      || (board[0] == 'X' && board[4] == 'X' && board[8] == 'X')   || (board[0] == 'O' && board[4] == 'O' && board[8] == 'O')
      || (board[2] == 'X' && board[4] == 'X' && board[6] == 'X')   || (board[2] == 'O' && board[4] == 'O' && board[6] == 'O')){
          display("Gameover, player " + currentPlayer + " won!");
      return "Gameover!";
      }else return "";
  
   }

   public static void main( String args[] )
   {
      BlackJackServer game = new BlackJackServer();

      game.addWindowListener( new WindowAdapter() {
        public void windowClosing( WindowEvent e )
            {
               System.exit( 0 );
            }
         }
      );

      game.execute();
   }
}

// Player class to manage each Player as a thread
class Player extends Thread {
   private Socket connection;
   private DataInputStream input;
   public DataOutputStream output;
   private BlackJackServer control;
   private int number;
   private char mark;
   protected boolean threadSuspended = true;
   public int playerHandIndex = 0;
   public java.util.ArrayList<Card> hand = new java.util.ArrayList<Card>();

   public Player( Socket s, BlackJackServer t, int num )
   {
      mark = ( num == 0 ? 'X' : 'O' );

      connection = s;
      
      try {
         input = new DataInputStream(
                    connection.getInputStream() );
         output = new DataOutputStream(
                    connection.getOutputStream() );
      }
      catch( IOException e ) {
         e.printStackTrace();
         System.exit( 1 );
      }

      control = t;
      number = num;
      hand.add(control.deck.cards.get(control.getDeckIndex()));
      control.incrementIndex();
      playerHandIndex++;
      hand.add(control.deck.cards.get(control.getDeckIndex()));
      control.incrementIndex();
      playerHandIndex++;
   }

   public void otherPlayerMoved( int loc )
   {
      try {
         output.writeUTF( "Opponent moved" );
         output.writeInt( loc );
      }
      catch ( IOException e ) { e.printStackTrace(); }
   }

   public void run()
   {
      boolean done = false;
     
      try {
         control.display( "Player " +
            ( number == 0 ? 'X' : 'O' ) + " connected" );
         output.writeChar( mark );
         output.writeUTF( "Player " +
            ( number == 0 ? "X connected\n" :
                            "O connected, please wait\n" ) );

         // wait for another player to arrive
         if ( mark == 'X' ) {
            output.writeUTF( "Waiting for another player" );

            try {
               synchronized( this ) {   
                  while ( threadSuspended )
                     wait();  
               }
            } 
            catch ( InterruptedException e ) {
               e.printStackTrace();
            }

            output.writeUTF(
               "Other player connected. Your turn." );
            
         }
         
         //RACE CONDITION NEEDS TO BE FIXED
         try {
             Thread.sleep(2000);                 //1000 milliseconds is one second.
             } catch(InterruptedException ex) {
                 Thread.currentThread().interrupt();
             }
         output.writeUTF( "You have been dealt cards..." );
         String card = hand.get(playerHandIndex - 2).toString();
         output.writeUTF("Card: " + card);
         control.sendMessageToOtherPlayer("The other player received card: " + card, number);
         card = hand.get(playerHandIndex - 1).toString();
         output.writeUTF("Card: " + card);
         control.sendMessageToOtherPlayer("The other player received card: " + card, number);
         
         // Play game
         while ( !done ) {
            int location = input.readInt();
            
            if ( control.validMove( location, number ) ) {
               control.display( "loc: " + location );
               output.writeUTF( "Valid move." );
            }
            else 
               output.writeUTF( "Invalid move, try again" );

            if ( control.gameOver().equals("Gameover!") )
               done = true;
         }         
         output.writeUTF( "Gameover, you win!" );
         control.alertOtherPlayerGameOver();
         connection.close();
      }
      catch( IOException e ) {
         e.printStackTrace();
         System.exit( 1 );
      }
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
