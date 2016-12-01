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
   private JTextArea output;
   public Player players[];
   private ServerSocket server;
   private int currentPlayer;
   public int deckIndex = 0;
   public int serverHandIndex = 0;
   public java.util.ArrayList<Card> serverHand = new java.util.ArrayList<Card>();
   DeckofCards deck = new DeckofCards();

   public BlackJackServer()
   {
      super( "BlackJack Server" );

      players = new Player[ 3 ];
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

      /*
      // Player X is suspended until Player O connects.
      // Resume player X now.
      for(int p = 0; p < players.length-2; p++) {
         synchronized ( players[ 0 ] ) {
            players[ 0 ].threadSuspended = false;   
            players[ 0 ].notify();
         }
      }*/
      
      
      //Server should deal out the cards here!
      for ( int i = 0; i < players.length; i++ ) {
         players[i].hand.add(deck.cards.get(getDeckIndex()));
         incrementIndex();
         players[i].hand.add(deck.cards.get(getDeckIndex()));
         incrementIndex();
         try {
            players[i].output.writeUTF( "You have been dealt cards...\n" );
            players[i].output.writeUTF("Card: " + players[i].hand.get(0) + "\n");
            players[i].output.writeUTF("Card: " + players[i].hand.get(1) + "\n");
            players[i].output.writeUTF("Your hand value is: " + getValueOfHand(players[i].hand) + "\n");
         } catch (Exception e) {
            e.printStackTrace();
         }
         sendMessageToOtherPlayers("Player " + Integer.toString(i+1) + " received cards: " 
               + players[i].hand.get(0) + ", " + players[i].hand.get(1) + "\n", i);
         display("Player: " + Integer.toString(i+1) + " received card: " + players[i].hand.get(0) + "\n");
         display("Player: " + Integer.toString(i+1) + " received card: " + players[i].hand.get(1) + "\n");
         display("Player " + Integer.toString(i+1) + " hand value is " + getValueOfHand(players[i].hand));
         sendMessageToOtherPlayers("Player " + Integer.toString(i+1) + "\'s hand value is " + getValueOfHand(players[i].hand) + "\n", i);
         try {
             Thread.sleep(2000);                 //1000 milliseconds is one second.
          } catch(InterruptedException ex) {
              Thread.currentThread().interrupt();
          }
      }
      
      for(int i = 0; i < 2; ++i) {
         serverHand.add(deck.cards.get(getDeckIndex()));
         String card = serverHand.get(serverHandIndex).toString();
         display("Server received card: " + card + "\n");
         sendMessageToAllPlayers("Server received card: " + card + "\n");
         incrementIndex();
         serverHandIndex += 1;
      }
      
      sendMessageToAllPlayers("The servers hand is: " + displayHand() + "\n");
      sendMessageToAllPlayers("The servers hand value is: " + getValueOfHand(serverHand) + "\n");
      display("The servers hand value is: " + getValueOfHand(serverHand) + "\n");
      
      java.util.ArrayList<Integer> playersNotDone = new java.util.ArrayList<Integer>();
      for( int i = 0; i < players.length; ++i) {
         playersNotDone.add(i);
      }
      
      int i = 0;
      while(!playersNotDone.isEmpty()) {
         //give the player a turn
         int p = playersNotDone.get(i);
      
         try {
            players[p].output.writeUTF("It's your turn. Hit or stand?\n");
         } catch (Exception e) {
            e.printStackTrace();
         }
      
         //have server wait for the input
         
         
         //if player hit
         if(true) {
            players[p].hand.add(deck.cards.get(getDeckIndex()));
            incrementIndex();
            //if player busted, remove from set of unfinished players
            try {
               players[p].output.writeUTF( "You have been dealt:\n" );
               players[p].output.writeUTF("Card: " + players[p].hand.get(players[p].hand.size()-1) + "\n");
               players[p].output.writeUTF("Your hand value is: " + getValueOfHand(players[p].hand) + "\n");
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
         
         //player stood or busted, remove them
         if(getValueOfHand(players[p].hand) > 21) { // OR IF PLAYER STOOD
            playersNotDone.remove(i);
         }
         
         //get next player in queue
         if(!playersNotDone.isEmpty()) {
            i = (i + 1) % playersNotDone.size();
         }
      }
      
      boolean allPlayersBusted = true;
      for(Player p : players) {
         if(getValueOfHand(p.hand) <= 21) {
            allPlayersBusted = false;
            break;
         }
      }
      
      if(!allPlayersBusted) {
         sendMessageToAllPlayers("\nServer now drawing to 17 or bust");
         while(getValueOfHand(serverHand) < 17) {
            serverHand.add(deck.cards.get(getDeckIndex()));
            String card = serverHand.get(serverHandIndex).toString();
            display("Server received card: " + card + "\n");
            sendMessageToAllPlayers("Server received card: " + card + "\n");
            incrementIndex();
            serverHandIndex += 1;
         }
      }
      sendMessageToAllPlayers("The servers hand is: " + displayHand() + "\n");
      sendMessageToAllPlayers("The servers hand value is: " + getValueOfHand(serverHand) + "\n");
      display("The servers hand value is: " + getValueOfHand(serverHand) + "\n");
      if(getValueOfHand(serverHand) > 21) {
         sendMessageToAllPlayers("The server went bust!\n");
         display("The server went bust!\n");
      }
      else {
         display("Everyone busted so the server doesn't draw\n");
      }
      
      
      //game loop:
      // server goes through all players repeatedly until all stood or busted
      //server draws to 17 or bust
      //server determines winners and losers and outputs them      
      try {
         for(int p = 0; p < players.length; ++p) {
            if(getValueOfHand(players[p].hand) <= 21) {
               if(getValueOfHand(players[p].hand) == getValueOfHand(serverHand) ||
                  getValueOfHand(serverHand) > 21) {
                     players[p].output.writeUTF("You won!\n");
                     sendMessageToOtherPlayers("Player " + Integer.toString(p+1) + " won!\n", p);
                  }
               else {
                  //player pushed
                  players[p].output.writeUTF("You pushed.\n");
                  sendMessageToOtherPlayers("Player " + Integer.toString(p+1) + " pushed.\n", p);
               }
            }
            //player busted
            players[p].output.writeUTF("You lost...\n");
            sendMessageToOtherPlayers("Player " + Integer.toString(p+1) + " lost...\n", p);
         }
      } catch (IOException ie) {
         ie.printStackTrace();
      }
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
	   case "2": return 2;
	   case "3": return 3;
	   case "4": return 4;
	   case "5": return 5;
	   case "6": return 6;
	   case "7": return 7;
	   case "8": return 8;
	   case "9": return 9;
	   case "10": return 10;
	   case "A": return 1;
	   case "Jack": return 10;
	   case "Queen": return 10;
	   case "King": return 10;
	   }
	   return 0;
   }

	public int getValueOfHand(java.util.ArrayList<Card> hand) {
	    int value = 0;
	    int ace = 0;
	    for(int i = 0; i < hand.size(); ++i) {
	       value += getValueOfCard(hand.get(i));
	       if(getValueOfCard(hand.get(i)) == 1) {
	          ace++;
	       }
	    }
	    if(value < 12 && ace > 0) {
	       value += 10;
	    }
	    return value;
	 }
   
   public void display( String s )
   {
      output.append( s + "\n" );
   }
   
   public void alertOtherPlayerGameOver(){
       try{
       players[currentPlayer].output.writeUTF("Gameover, you loose!");
       }catch( IOException e ) { 
       e.printStackTrace();
       }
   }

   public void sendMessageToOtherPlayers(String message, int self){
       try{
          for(int p = 0; p < players.length; ++p) {
             if(players[p] != null && p != self) {
               players[p].output.writeUTF(message);
             }
          }
       }catch( IOException e ) { 
         e.printStackTrace();
       }
   }
   
   public void sendMessageToAllPlayers(String message) {
       try{
          for(int p = 0; p < players.length; ++p) {
             if(players[p] != null) {
               players[p].output.writeUTF(message);
             }
          }
       }catch( IOException e ) { 
         e.printStackTrace();
       }
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
   private int number;
   private String mark;
   protected boolean threadSuspended = true;
   public java.util.ArrayList<Card> hand = new java.util.ArrayList<Card>();
   public boolean playerTurn = false;
   

   public Player( Socket s, BlackJackServer t, int num )
   {
      mark = ( Integer.toString(num+1));

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
            Integer.toString(number + 1) + " connected" );
         if(number == 0){
        	 control.display("Waiting for other player to connect...");
         }
         output.writeUTF( "Player " +
            ( Integer.toString(number + 1) + " connected, please wait\n"));

         // wait for another player to arrive
         if ( number != (control.players.length-1) ) {
            output.writeUTF( "Waiting for more players\n" );

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
         
         
         // Play game
         while ( !done ) {
            String message = input.readUTF();
            
            if(message.contains("hitme")){
            	
            }else if(message.contains("stand")){
            	
            }else if(message.contains("chat:")){
            	message = message.substring(6);
            }else if(message.contains("stats:")){
            	message = message.substring(7);
            }
            

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
   
   public int getValueOfHand(java.util.ArrayList<Card> hand) {
      int value = 0;
      int ace = 0;
      for(int i = 0; i < hand.size(); ++i) {
         value += getValueOfCard(hand.get(i));
         if(getValueOfCard(hand.get(i)) == 1) {
            ace++;
         }
      }
      if(value < 12 && ace > 0) {
         value += 10;
      }
      return value;
   }
   
   public int getValueOfCard(Card card){
	   
	   switch(card.getValue()){
	   case "2": return 2;
	   case "3": return 3;
	   case "4": return 4;
	   case "5": return 5;
	   case "6": return 6;
	   case "7": return 7;
	   case "8": return 8;
	   case "9": return 9;
	   case "10": return 10;
	   case "A": return 1;
	   case "Jack": return 10;
	   case "Queen": return 10;
	   case "King": return 10;
	   }
	   return 0;
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
