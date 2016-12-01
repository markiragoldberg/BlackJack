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
   public java.util.ArrayList<Integer> playersNotDone;
   DeckofCards deck = new DeckofCards();

   public BlackJackServer()
   {
      super( "BlackJack Server" );

      players = new Player[ 3 ];
      currentPlayer = -1;
 
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
     // output.append(deck.getDeck().toString() + "\n");

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
      
      try {
         Thread.sleep(3000);
      }
      catch (InterruptedException e) {
         e.printStackTrace();
         System.exit(1);
      }
      
      
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
            players[i].output.writeUTF("Your hand value is: " + getValueOfHand(players[i].hand) + "\n\n");
         } catch (Exception e) {
            e.printStackTrace();
         }
         sendMessageToOtherPlayers("Player " + Integer.toString(i+1) + " received cards: " 
               + players[i].hand.get(0) + ", " + players[i].hand.get(1) + "\n", i);
         //internal messages to get clients to show new cards, with server as 0 and 1 as 1
         sendMessageToAllPlayers("newcard: " + Integer.toString(i+1));
         sendMessageToAllPlayers("newvalue: " + players[i].hand.get(0).getValue());
         sendMessageToAllPlayers("newsuit: " + players[i].hand.get(0).getSuit());
         sendMessageToAllPlayers("newcard: " + Integer.toString(i+1));
         sendMessageToAllPlayers("newvalue: " + players[i].hand.get(1).getValue());
         sendMessageToAllPlayers("newsuit: " + players[i].hand.get(1).getSuit());
         display("Player: " + Integer.toString(i+1) + " received card: \n" + players[i].hand.get(0) + "\n");
         display("Player: " + Integer.toString(i+1) + " received card: \n" + players[i].hand.get(1) + "\n");
         display("Player " + Integer.toString(i+1) + " hand value is " + getValueOfHand(players[i].hand));
         sendMessageToOtherPlayers("Player " + Integer.toString(i+1) + "\'s hand value is " + getValueOfHand(players[i].hand) + "\n\n", i);
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
         
         sendMessageToAllPlayers("newcard: " + Integer.toString(0));
         sendMessageToAllPlayers("newvalue: " + serverHand.get(serverHandIndex).getValue());
         sendMessageToAllPlayers("newsuit: " + serverHand.get(serverHandIndex).getSuit());
         
         incrementIndex();
         serverHandIndex += 1;
      }
      
      sendMessageToAllPlayers("The servers hand is: " + displayHand() + "\n");
      sendMessageToAllPlayers("The servers hand value is: " + getValueOfHand(serverHand) + "\n");
      display("The servers hand value is: " + getValueOfHand(serverHand) + "\n");
      
      playersNotDone = new java.util.ArrayList<Integer>();
      for( int i = 0; i < players.length; ++i) {
         playersNotDone.add(i);
      }
      
      currentPlayer = 0;
      try {
         players[currentPlayer].output.writeUTF("It's your turn. Hit or stand?\n");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   //give current player a card
   //bust them if appropriate
   //transfer turn to next player and alert them
   public synchronized void playerHit() {
      players[currentPlayer].hand.add(deck.cards.get(getDeckIndex()));
      //transitions deck to the next top card
      incrementIndex();
      try {
         players[currentPlayer].output.writeUTF( "You have been dealt:\n" );
         players[currentPlayer].output.writeUTF(
            "Card: \n" + players[currentPlayer].hand.get(players[currentPlayer].hand.size()-1) + "\n");
         players[currentPlayer].output.writeUTF(
            "Your hand value is: " + getValueOfHand(players[currentPlayer].hand) + "\n");
         sendMessageToOtherPlayers(
            "Player " + Integer.toString(currentPlayer+1) + " was dealt the " + 
            players[currentPlayer].hand.get(players[currentPlayer].hand.size()-1) + ".\n" ,currentPlayer);
            
         sendMessageToAllPlayers("newcard: " + Integer.toString(currentPlayer + 1));
         sendMessageToAllPlayers("newvalue: " + players[currentPlayer].hand.get(players[currentPlayer].hand.size()-1).getValue());
         sendMessageToAllPlayers("newsuit: " + players[currentPlayer].hand.get(players[currentPlayer].hand.size()-1).getSuit());
            
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      //Get index of player[currentPlayer] in playersNotDone
      int pNdIndex = 0;
      for(pNdIndex = 0; pNdIndex < playersNotDone.size(); ++pNdIndex) {
         if(playersNotDone.get(pNdIndex) == currentPlayer) {
            break;
         }
      }
      
      //if player busted, remove from set of unfinished players
      //regardless, get next player or end game if the last one finished
      if(getValueOfHand(players[currentPlayer].hand) > 21) {
         playersNotDone.set(pNdIndex, -1);
         try {
            players[currentPlayer].output.writeUTF("You busted!\n");
            sendMessageToOtherPlayers("Player " + Integer.toString(currentPlayer + 1) + " went bust!\n", currentPlayer);
         } catch (IOException ie) {
            ie.printStackTrace();
         }
         boolean allDone = true;
         for(int p : playersNotDone) {
            if(p != -1) {
               allDone = false;
            }
         }
         if(allDone) {
            endGame();
         }
         else {
            do {
               currentPlayer = playersNotDone.get( ((++pNdIndex) % playersNotDone.size()) );
            } while(currentPlayer == -1);
            try {
               display("Player " + Integer.toString(currentPlayer + 1) + " has the turn\n");
               players[currentPlayer].output.writeUTF("\nIt's your turn. Hit or stand?\n");
               sendMessageToOtherPlayers("\nIt's Player " + Integer.toString(currentPlayer + 1) + "'s turn.\n", currentPlayer);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }
      else {
            do {
               currentPlayer = playersNotDone.get( ((++pNdIndex) % playersNotDone.size()) );
            } while(currentPlayer == -1);
         try {
            display("Player " + Integer.toString(currentPlayer + 1) + " has the turn\n");
            players[currentPlayer].output.writeUTF("\nIt's your turn. Hit or stand?\n");
            sendMessageToOtherPlayers("\nIt's Player " + Integer.toString(currentPlayer + 1) + "'s turn.\n", currentPlayer);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
   
   public synchronized void playerStood() {
      sendMessageToAllPlayers("Player " + Integer.toString(currentPlayer + 1) + " stood.\n");
      if(playersNotDone.size() <= 1) {
         endGame();
      }
      else {
         //Get index of player[currentPlayer] in playersNotDone
         int pNdIndex = 0;
         for(pNdIndex = 0; pNdIndex < playersNotDone.size(); ++pNdIndex) {
            if(playersNotDone.get(pNdIndex) == currentPlayer) {
               break;
            }
         }
         playersNotDone.set(pNdIndex, -1);
         boolean allDone = true;
         for(int p : playersNotDone) {
            if(p != -1) {
               allDone = false;
            }
         }
         if(allDone) {
            endGame();
         }
         else {
            do {
               currentPlayer = playersNotDone.get( ((++pNdIndex) % playersNotDone.size()) );
            } while(currentPlayer == -1);
         }
         try {
            display("Player " + Integer.toString(currentPlayer + 1) + " has the turn\n");
            players[currentPlayer].output.writeUTF("It's your turn. Hit or stand?\n");
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
   
   public synchronized void endGame() {
      // All players have stood or busted
      // If at least one player stood, server takes cards
      boolean allPlayersBusted = true;
      for(Player p : players) {
         if(getValueOfHand(p.hand) <= 21) {
            allPlayersBusted = false;
            break;
         }
      }
      if(!allPlayersBusted) {
            sendMessageToAllPlayers("\nServer now drawing to 17 or bust\n");
            while(getValueOfHand(serverHand) < 17) {
               serverHand.add(deck.cards.get(getDeckIndex()));
               String card = serverHand.get(serverHandIndex).toString();
               display("Server received card: " + card + "\n");
               sendMessageToAllPlayers("Server received card: " + card + "\n");
               incrementIndex();
               serverHandIndex += 1;
            }
         sendMessageToAllPlayers("The server\'s hand is: " + displayHand() + "\n");
         sendMessageToAllPlayers("The server\'s hand value is: " + getValueOfHand(serverHand) + "\n\n");
         display("The servers hand value is: " + getValueOfHand(serverHand) + "\n");
         if(getValueOfHand(serverHand) > 21) {
            sendMessageToAllPlayers("The server went bust!\n");
            display("The server went bust!\n");
         }
      }
      else {
         display("Everyone busted so the server doesn't draw\n");
      }
      
      //Go through all players and tell everyone who won, pushed, and lost
      try {
         for(int p = 0; p < players.length; ++p) {
            if(getValueOfHand(players[p].hand) <= 21) {
               if(getValueOfHand(players[p].hand) > getValueOfHand(serverHand) ||
                  getValueOfHand(serverHand) > 21) {
                     players[p].output.writeUTF("You won!\n");
                     sendMessageToOtherPlayers("Player " + Integer.toString(p+1) + " won!\n", p);
                     continue;
                  }
               else if(getValueOfHand(players[p].hand) == getValueOfHand(serverHand)) {
                  //player pushed
                  players[p].output.writeUTF("You pushed.\n");
                  sendMessageToOtherPlayers("Player " + Integer.toString(p+1) + " pushed.\n", p);
                  continue;
               }
            }
            //player busted
            players[p].output.writeUTF("You lost...\n");
            sendMessageToOtherPlayers("Player " + Integer.toString(p+1) + " lost...\n", p);
         }
      } catch (IOException ie) {
         ie.printStackTrace();
      }
      
      //Don't keep playing after the game ends
      currentPlayer = -1;
      
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
    
    public synchronized int getCurrentPlayer() {
       return currentPlayer;
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
            output.writeUTF( "Waiting for more players\n\n" );
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
            if(message.contains("turn: hitme") && control.getCurrentPlayer() == number){
               control.display("Player " + Integer.toString(number + 1) + " attempting turn when it is Player " + Integer.toString(control.getCurrentPlayer() + 1) + "'s turn\n");
            	control.playerHit();
            }else if(message.contains("turn: stand") && control.getCurrentPlayer() == number){
            	control.playerStood();
            }else if(message.contains("chat:")){
            	message = message.substring(6);
               control.sendMessageToAllPlayers("chat: " + "Player " + Integer.toString(number + 1) + ": " + message);
            }else if(message.contains("stats:")){
               //TODO: do stats, no stats yet
            	//message = message.substring(7);
            }
            else if(message.contains("quit:")) {
               //TODO: graceful exit
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
