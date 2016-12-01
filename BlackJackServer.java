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
   public int serverHandIndex = 0;
   public int handValue;
   public java.util.ArrayList<Card> serverHand = new java.util.ArrayList<Card>();
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
      output.append(deck.getDeck().toString() + "\n");

      setSize( 500, 1000 );
      setVisible(true);
   }
   
   public synchronized void incrementIndex(){
	   deckIndex = deckIndex + 1;
   }
   
   public synchronized int getDeckIndex(){
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
      
      try {
          Thread.sleep(10000);                 //1000 milliseconds is one second.
          } catch(InterruptedException ex) {
              Thread.currentThread().interrupt();
          }
      
      serverHand.add(deck.cards.get(getDeckIndex()));
      String card = serverHand.get(serverHandIndex).toString();
      display("Server received card: " + card + "\n");
      sendMessageToOtherPlayer("Server received card: " + card + "\n", 0);
      sendMessageToOtherPlayer("Server received card: " + card + "\n", 1);
      incrementIndex();
      serverHandIndex += 1;
      
      serverHand.add(deck.cards.get(getDeckIndex()));
      card = serverHand.get(serverHandIndex).toString();
      display("Server received card: " + card + "\n");
      sendMessageToOtherPlayer("Server received card: " + card + "\n", 0);
      sendMessageToOtherPlayer("Server received card: " + card + "\n", 1);
      incrementIndex();
      serverHandIndex += 1;
      sendMessageToOtherPlayer("The servers hand is: " + displayHand() + "\n", 0);
      sendMessageToOtherPlayer("The servers hand is: " + displayHand() + "\n", 1);
      getValueOfCard(serverHand.get(serverHandIndex - 2));
      getValueOfCard(serverHand.get(serverHandIndex - 1));
      sendMessageToOtherPlayer("The servers hand value is: " + handValue + "\n", 0);
      sendMessageToOtherPlayer("The servers hand value is: " + handValue + "\n", 1);
      
   }
   
   
   public String displayHand(){
	   String theHand = "";
	   
	   for(int i = 0; i < serverHand.size(); i++){
           Card card = serverHand.get(i);
           if(i == serverHand.size() - 1){
		   theHand += card.getValue();
           }
           else {theHand += card.getValue() + ", ";}
	   }
	   return theHand;
   }
   
public int getValueOfCard(Card card){
	   
	   switch(card.getValue()){
	   case "2": handValue += 2;
	   break;
	   case "3": handValue += 3;
	   break;
	   case "4": handValue += 4;
	   break;
	   case "5": handValue += 5;
	   break;
	   case "6": handValue += 6;
	   break;
	   case "7": handValue += 7;
	   break;
	   case "8": handValue += 8;
	   break;
	   case "9": handValue += 9;
	   break;
	   case "10": handValue += 10;
	   break;
	   case "A": handValue += 1;
	   break;
	   case "Jack": handValue += 10;
	   break;
	   case "Queen": handValue += 10;
	   break;
	   case "King": handValue += 10;
	   break;
	   }
	   return handValue;
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
            (byte) ( currentPlayer == 0 ? '1' : '2' );
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
      if ( board[ loc ] == '1' || board [ loc ] == '2' )
          return true;
      else
          return false;
   }

   public String gameOver()
   {
      // Place code here to test for a winner of the game
      if(false){
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
   int handValue = 0;
   private int number;
   private String mark;
   protected boolean threadSuspended = true;
   public int playerHandIndex = 0;
   public java.util.ArrayList<Card> hand = new java.util.ArrayList<Card>();

   public Player( Socket s, BlackJackServer t, int num )
   {
      mark = ( num == 0 ? "1" : "2" );

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
         output.writeUTF( "Opponent moved\n" );
         output.writeInt( loc );
      }
      catch ( IOException e ) { e.printStackTrace(); }
   }

   public void run()
   {
      boolean done = false;
     
      try {
         control.display( "Player " +
            ( number == 0 ? '1' : '2' ) + " connected" );
         if(number == 0){
        	 control.display("Waiting for other player to connect...");
         }
         output.writeUTF( "Player " +
            ( number == 0 ? "1 connected\n" :
                            "2 connected, please wait\n" ) );

         // wait for another player to arrive
         if ( mark == "1" ) {
            output.writeUTF( "Waiting for another player\n" );

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
               "Other player connected. Your turn.\n" );
            
         }
         
         //RACE CONDITION NEEDS TO BE FIXED
         try {
             Thread.sleep(5000);                 //1000 milliseconds is one second.
             } catch(InterruptedException ex) {
                 Thread.currentThread().interrupt();
             }
         output.writeUTF( "You have been dealt cards...\n" );
         String card = hand.get(playerHandIndex - 2).toString();
         output.writeUTF("Card: " + card + "\n");
         getValueOfCard(hand.get(playerHandIndex - 2));
         output.writeUTF("Your hand value is: " + handValue + "\n");
         control.sendMessageToOtherPlayer("The other player received card: " + card + "\n", number);
         control.display("Player: " + mark + " received card: " + card + "\n");
         control.display("Player " + mark + " hand value is " + Integer.toString(handValue));
         control.sendMessageToOtherPlayer("Player " + mark + " hand value is " + Integer.toString(handValue) + "\n", number);
         card = hand.get(playerHandIndex - 1).toString();
         output.writeUTF("Card: " + card + "\n");
         getValueOfCard(hand.get(playerHandIndex - 1));
         output.writeUTF("Your hand value is: " + handValue + "\n");
         output.writeUTF("Your full hand is: " + displayHand() + "\n");
         control.display("Player: " + mark + " received card: " + card + "\n");
         control.display("Player " + mark + " hand value is " + Integer.toString(handValue));
         control.display("Player " + mark + " complete hand is " + displayHand() + "\n");
         control.sendMessageToOtherPlayer("The other player received card: " + card + "\n", number);
         control.sendMessageToOtherPlayer("Player " + mark + " hand value is " + Integer.toString(handValue) + "\n", number);
         control.sendMessageToOtherPlayer("Player " + mark + " complete hand is " + displayHand() + "\n", number);
         
         
         
         // Play game
         while ( !done ) {
            int location = input.readInt();
            
            if ( control.validMove( location, number ) ) {
               control.display( "loc: " + location );
               output.writeUTF( "Valid move.\n" );
            }
            else 
               output.writeUTF( "Invalid move, try again\n" );

            if ( control.gameOver().equals("Gameover!") )
               done = true;
         }         
         output.writeUTF( "Gameover, you win!\n" );
         control.alertOtherPlayerGameOver();
         connection.close();
      }
      catch( IOException e ) {
         e.printStackTrace();
         System.exit( 1 );
      }
   }
   
   public int getValueOfCard(Card card){
	   
	   switch(card.getValue()){
	   case "2": handValue += 2;
	   break;
	   case "3": handValue += 3;
	   break;
	   case "4": handValue += 4;
	   break;
	   case "5": handValue += 5;
	   break;
	   case "6": handValue += 6;
	   break;
	   case "7": handValue += 7;
	   break;
	   case "8": handValue += 8;
	   break;
	   case "9": handValue += 9;
	   break;
	   case "10": handValue += 10;
	   break;
	   case "A": handValue += 1;
	   break;
	   case "Jack": handValue += 10;
	   break;
	   case "Queen": handValue += 10;
	   break;
	   case "King": handValue += 10;
	   break;
	   }
	   return handValue;
   }
   
   public String displayHand(){
	   String theHand = "";
	   
	   for(int i = 0; i < hand.size(); i++){
           Card card = hand.get(i);
           if(i == hand.size() - 1){
		   theHand += card.getValue();
           }
           else {theHand += card.getValue() + ", ";}
	   }
	   return theHand;
   }
}                                                            
