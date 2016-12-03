public class Card {
 
       //This is a simple card class consisting of a card that has two values: suit and value
	   //The cards will be put into the deck
	
		//Instance Variables
        private String suit;
        private String value;
       
       //Constructor to create card
        public Card(String suit, String value){
                this.suit = suit;
                this.value = value;
        }
        
        public Card(){}
        
        //Getters and Setters
        public String getSuit(){
                return suit;
        }
        public void setSuit(String suit){
                this.suit = suit;
        }
        public String getValue(){
                return value;
        }
        public void setValue(String value){
                this.value = value;
        }
       
        //To String methods
        @Override
        public String toString(){
                return "\n"+value + " of "+ suit;
        }
        public String toString2(){
            return (value + " of "+ suit);
    }
}
