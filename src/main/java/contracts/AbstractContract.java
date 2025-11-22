package contracts;
import company.InsuranceCompany;
import objects.*;
import payment.*;

public abstract class AbstractContract
{
    private final String contractNumber;
    protected final InsuranceCompany insurer;
    protected final Person policyHolder;
    protected final ContractPaymentData contractPaymentData;
    protected int coverageAmount;
    protected boolean isActive;


    public AbstractContract(String contractNumber, InsuranceCompany insurer, Person policyHolder, ContractPaymentData contractPaymentData, int coverageAmount){
        if(contractNumber == null|| contractNumber.isEmpty()){
            throw new IllegalArgumentException("Invalid input arguments");
        }

        if(insurer == null){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        if(policyHolder ==null){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        if(coverageAmount<0){
            throw new IllegalArgumentException("Invalid input arguments");
        }

        this.contractNumber = contractNumber;
        this.insurer = insurer;
        this.policyHolder = policyHolder;
        this.contractPaymentData = contractPaymentData;
        this.coverageAmount = coverageAmount;
        this.isActive =true;

    }

    public String getContractNumber() {
        return contractNumber;
    }

    public Person getPolicyHolder(){
        return policyHolder;
    }

    public InsuranceCompany getInsurer() {
        return insurer;
    }

    public int getCoverageAmount() {
        return coverageAmount;
    }


    public boolean isActive(){
        return isActive;
    };
    public void setInactive(){
        this.isActive = false;
    };

    public void setCoverageAmount(int coverageAmount) {
        if(coverageAmount<0){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        this.coverageAmount = coverageAmount;
    }

    public ContractPaymentData getContractPaymentData() {

        return contractPaymentData;
    }

    public void pay(int amount) {

        insurer.getHandler().pay(this, amount);
    }

    public void updateBalance(){

        insurer.chargePremiumOnContract(this);
    };
}
