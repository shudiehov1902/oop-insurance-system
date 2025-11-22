package contracts;
import company.*;
import objects.*;
import payment.*;

public abstract  class AbstractVehicleContract extends AbstractContract {
    protected Person beneficiary;

    public AbstractVehicleContract(String contractNumber, InsuranceCompany insurer, Person beneficiary, Person policyHolder, ContractPaymentData contractPaymentData, int coverageAmount) {
        super(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount);
        if (beneficiary != null && beneficiary.equals(policyHolder)) {
            throw new IllegalArgumentException("Invalid input arguments");
        }
        this.beneficiary = beneficiary;
    }

    public void setBeneficiary(Person beneficiary) {
        if (beneficiary != null && beneficiary.equals(policyHolder)) {
            throw new IllegalArgumentException("Invalid input arguments");
        }
        this.beneficiary = beneficiary;
    }

    public Person getBeneficiary() {
        return beneficiary;
    }
}
