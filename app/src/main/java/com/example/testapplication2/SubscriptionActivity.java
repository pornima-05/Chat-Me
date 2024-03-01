package com.example.testapplication2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

public class SubscriptionActivity extends AppCompatActivity implements PaymentResultListener {
    private CardView selectedCardView;
    private String selectedCardName;
    private String selectedCardPrice;
    private boolean isCardSelected = false;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        final Button makePaymentButton = findViewById(R.id.makepayement);
        makePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCardSelected) {
                    startRazorpayPayment(selectedCardName, selectedCardPrice);
                } else {
                    Toast.makeText(SubscriptionActivity.this, "Please select a subscription plan", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Set click listeners for card views
        setCardClickListener(R.id.premium, "Premium", "1500");
        setCardClickListener(R.id.profile, "Profile", "1000");
        setCardClickListener(R.id.theme, "Theme", "900");
        setCardClickListener(R.id.wallpaper, "Wallpaper", "900");
        setCardClickListener(R.id.secure, "Secure", "2500");
        setCardClickListener(R.id.membership, "Membership", "700");


    }

    private void setCardClickListener(int cardViewId, final String cardName, final String cardPrice) {
        final CardView cardView = findViewById(cardViewId);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Check if the user has already purchased the selected plan
                if (isPlanPurchased(cardName)) {
                    Toast.makeText(SubscriptionActivity.this, "You already have this subscription plan", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Reset color for previously selected card
                if (selectedCardView != null) {
                    selectedCardView.setCardBackgroundColor(Color.WHITE);
                }

                // Set color for the selected card
                cardView.setCardBackgroundColor((Color.parseColor("#89E251")));
                selectedCardView = cardView;
                selectedCardName = cardName;
                selectedCardPrice = cardPrice;
                isCardSelected = true;

            }
        });
    }
    private boolean isPlanPurchased(String planName) {
        // Retrieve the list of purchased plans from SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String purchasedPlans = sharedPreferences.getString("purchased_plans", "");

        // Check if the selected plan is in the list of purchased plans
        return purchasedPlans.contains(planName);
    }

    private void markPlanAsPurchased(String planName) {
        // Retrieve the list of purchased plans from SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String purchasedPlans = sharedPreferences.getString("purchased_plans", "");

        // Append the selected plan to the list of purchased plans
        purchasedPlans += planName + ",";

        // Save the updated list of purchased plans to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("purchased_plans", purchasedPlans);
        editor.apply();
    }
    private void startRazorpayPayment(String cardName, String cardPrice) {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_Wcsnm0OWQD4Xcr"); // Replace with your Razorpay Key ID
        checkout.setImage(R.drawable.logo);

        try {
            JSONObject options = new JSONObject();
            options.put("name",  selectedCardName);
            options.put("description", selectedCardName + " Subscription");
            options.put("currency", "INR");
            options.put("amount", Integer.parseInt(selectedCardPrice) * 100); // Amount in paise
            options.put("prefill", new JSONObject().put("email", "customer@example.com"));

            // Add custom fields
            JSONObject customFields = new JSONObject();
            customFields.put("cardName", cardName);
            customFields.put("cardImage", getCardImageResource(cardName)); // Replace with actual method to get image resource
            options.put("customFields", customFields);

            // Mark the purchased plan as purchased
            markPlanAsPurchased(cardName);

            // Reset the background color of the selected card to white
            resetSelectedCardBackgroundColor();


            checkout.open(this, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetSelectedCardBackgroundColor() {
        if (selectedCardView != null) {
            selectedCardView.setCardBackgroundColor(Color.WHITE);
            selectedCardView = null;  // Set selectedCardView to null to indicate no card is selected
            selectedCardName = null;
            selectedCardPrice = null;
            isCardSelected = false;  // Reset the flag to indicate no card is selected
        }
    }

    // Method to get the image resource based on card name (replace with your actual implementation)
    private int getCardImageResource(String cardName) {
        switch (cardName) {
            case "Premium":
                return R.drawable.premium; // Replace with your actual drawable resource
            case "Profile":
                return R.drawable.profile;
            case "Theme":
                return R.drawable.theme;
            case "Wallpaper":
                return R.drawable.wallpaper;
            case "Secure":
                return R.drawable.secure;
            case "Membership":
                return R.drawable.membership;
            // Add cases for other card names
            default:
                return 0; // Return 0 or default image resource if not found
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        // Retrieve additional information from customFields
        try {
            JSONObject customFields = new JSONObject(s).optJSONObject("customFields");
            if (customFields != null) {
                String cardName = customFields.optString("cardName");
                int cardImageResource = customFields.optInt("cardImage");

                // Now you can use cardName and cardImageResource as needed
                Toast.makeText(this, "Payment Successful for " + cardName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Failed: " + s, Toast.LENGTH_SHORT).show();

    }

}