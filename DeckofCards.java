mport java.util.*;
 
public class DeckofCards {
		//52 Cards are kept in a array list
        ArrayList<Card> cards = new ArrayList<Card>();
       //Each card is assigned values and a suit
        String[] values = {"A","2","3","4","5","6","7","8","9","10","Jack","Queen","King"};
        String[] suit = {"Clubs", "Spades", "Diamonds", "Hearts"};
       
        //Constructor to create the deck of cards
        public DeckofCards(){
        	//Making use a nested for loop to assign suit each value
                for (int i = 0; i<suit.length; i++) {
                        for(int j=0; j<values.length; j++){
                        	//Card being created and saved to the ArrayList of cards.
                                this.cards.add(new Card(suit[i],values[j]));
                        }
                }
                //Shuffle the deck when its created
                Collections.shuffle(this.cards);
               
        }
       
        //Returns the deck of cards
        public ArrayList<Card> getDeck(){
                return cards;
        }   
}
