package payment;


import java.time.LocalDateTime;

public class ContractPaymentData {
    private int premium;
    private PremiumPaymentFrequency premiumPaymentFrequency;
    private LocalDateTime nextPaymentTime;
    private int outstandingBalance;

    public ContractPaymentData(int premium,PremiumPaymentFrequency premiumPaymentFrequency, LocalDateTime nextPaymentTime, int outstandingBalance){
        if(premium<=0){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        if (premiumPaymentFrequency ==null|| premiumPaymentFrequency.getValueInMonths() == 0){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        if (nextPaymentTime ==null){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        this.premium = premium;
        this.premiumPaymentFrequency = premiumPaymentFrequency;
        this.nextPaymentTime = nextPaymentTime;
        this.outstandingBalance = outstandingBalance;
    }

    public int getPremium(){
        return this.premium;
    }

    public void setPremium(int premium){
        if(premium <=0){
            throw new IllegalArgumentException("Invalid input arguments");
        }

        this.premium = premium;
    }

    public void setOutstandingBalance(int outstandingBalance){
        this.outstandingBalance = outstandingBalance;
    }

    public int getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setPremiumPaymentFrequency(PremiumPaymentFrequency premiumPaymentFrequency) {
        if(premiumPaymentFrequency == null|| premiumPaymentFrequency.getValueInMonths() ==0) {
            throw new IllegalArgumentException("Invalid input arguments");
        }
        this.premiumPaymentFrequency = premiumPaymentFrequency;
    }

    public PremiumPaymentFrequency getPremiumPaymentFrequency() {
        return premiumPaymentFrequency;
    }

    public LocalDateTime getNextPaymentTime() {
        return nextPaymentTime;
    }
    public void updateNextPaymentTime(){
        this.nextPaymentTime = nextPaymentTime.plusMonths(premiumPaymentFrequency.getValueInMonths());
    }

}
