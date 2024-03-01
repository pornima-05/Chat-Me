package com.example.testapplication2.Model;

public class PaymentDetails {

    private String cardName;
    private String cardPrice;

    public PaymentDetails() {
    }

    public PaymentDetails( String cardName, String cardPrice) {
        this.cardName = cardName;
        this.cardPrice = cardPrice;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }


    public String getCardPrice() {
        return cardPrice;
    }

    public void setCardPrice(String cardPrice) {
        this.cardPrice = cardPrice;
    }

}
