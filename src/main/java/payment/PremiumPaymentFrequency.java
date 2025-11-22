package payment;

public enum PremiumPaymentFrequency {
    ANNUAL,SEMI_ANNUAL,QUARTERLY,MONTHLY;

    public int getValueInMonths(){
        if (this == ANNUAL){
            return 12;
        }
        if (this == SEMI_ANNUAL){
            return 6;
        }
        if (this == QUARTERLY){
            return 3;
        }
        if(this == MONTHLY){
            return 1;
        }
        else{
            return 0;
        }
    }
}
