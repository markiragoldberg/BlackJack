import java.util.*;
 
public class DeckofCards {
        ArrayList<Card> cards = new ArrayList<Card>();
       
        String[] values = {"A","2","3","4","5","6","7","8","9","10","Jack","Queen","King"};
        String[] suit = {"Clubs", "Spades", "Diamonds", "Hearts"};
       
        static boolean firstThread = true;
        public DeckofCards(){
                for (int i = 0; i<suit.length; i++) {
                        for(int j=0; j<values.length; j++){
                                this.cards.add(new Card(suit[i],values[j]));
                        }
                }
                //shuffle the deck when its created
                Collections.shuffle(this.cards);
               
        }
       
        public ArrayList<Card> getDeck(){
                return cards;
        }
       
       // public static void main(String[] args){
             //   DeckofCards deck = new DeckofCards();
               
                //print out the deck.
           //     System.out.println(deck.getDeck());
       // }
       
}
 
 
