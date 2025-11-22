package payment;
import java.util.TreeSet;
import company.InsuranceCompany;
import contracts.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PaymentHandler {
    private final Map<AbstractContract,Set<PaymentInstance>> paymentHistory;
    private final InsuranceCompany  insurer;

    public PaymentHandler(InsuranceCompany insurer){
        if(insurer == null){
            throw new IllegalArgumentException("Invalid input arguments");
        }

        this.insurer = insurer;
        paymentHistory = new LinkedHashMap<>();
    }

    public Map<AbstractContract, Set<PaymentInstance>> getPaymentHistory() {
        return paymentHistory;
    }

    public void pay(MasterVehicleContract contract, int amount){
        if(contract ==null || amount<=0){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        if(!contract.isActive() || contract.getInsurer()!= this.insurer|| contract.getChildContracts().isEmpty()){
            throw new InvalidContractException("Contract is inactive or belongs to a different insurer or don't have any ChildContracts ");
        }
        int remainingAmount = amount;

        for (SingleVehicleContract child : contract.getChildContracts()) {
            if (!child.isActive()) continue;

            ContractPaymentData data = child.getContractPaymentData();
            int debt = data.getOutstandingBalance();

            if (debt > 0) {
                if (remainingAmount >= debt) {
                    remainingAmount -= debt;
                    data.setOutstandingBalance(0);
                } else {
                    data.setOutstandingBalance(debt - remainingAmount);
                    remainingAmount = 0;
                    break;
                }
            }
        }


        while (remainingAmount > 0) {
            boolean paidInThisRound = false;

            for (SingleVehicleContract child : contract.getChildContracts()) {
                if (!child.isActive()) continue;

                ContractPaymentData data = child.getContractPaymentData();
                int premium = data.getPremium();

                if (remainingAmount >= premium) {
                    data.setOutstandingBalance(data.getOutstandingBalance() - premium);
                    remainingAmount -= premium;
                    paidInThisRound = true;
                } else if (premium > 0) {
                    data.setOutstandingBalance(data.getOutstandingBalance() - remainingAmount);
                    remainingAmount = 0;
                    paidInThisRound = true;
                    break;
                }
            }

            if (!paidInThisRound) break;
        }

        int paidAmount = amount - remainingAmount;
        PaymentInstance payment = new PaymentInstance(insurer.getCurrentTime(), paidAmount);
        paymentHistory.computeIfAbsent(contract, c -> new TreeSet<>()).add(payment);
    }
    public void pay(AbstractContract contract, int amount){
        if(contract ==null || amount<=0){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        if(!contract.isActive() || contract.getInsurer()!= this.insurer){
            throw new InvalidContractException("Contract is inactive or belongs to a different insurer");
        }

        ContractPaymentData data = contract.getContractPaymentData();
        data.setOutstandingBalance(data.getOutstandingBalance() - amount);

        PaymentInstance payment = new PaymentInstance(insurer.getCurrentTime(), amount);

        Set<PaymentInstance> history = paymentHistory.computeIfAbsent(contract, c -> new TreeSet<>());history.add(payment);

    }



}
