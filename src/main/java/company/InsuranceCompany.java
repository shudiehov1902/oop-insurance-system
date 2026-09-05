package company;

import contracts.*;
import objects.*;
import payment.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class InsuranceCompany
{
    private final Set<AbstractContract> contracts;
    private final PaymentHandler handler;
    private LocalDateTime currentTime;

    public InsuranceCompany(LocalDateTime currentTime){
        if(currentTime ==null){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        this.currentTime = currentTime;
        this.contracts = new LinkedHashSet<>();
        this.handler = new PaymentHandler(this);
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(LocalDateTime currentTime) {
        if(currentTime ==null){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        this.currentTime = currentTime;
    }

    public Set<AbstractContract> getContracts() {
        return contracts;
    }

    public PaymentHandler getHandler() {
        return handler;
    }

    public SingleVehicleContract insureVehicle(String contractNumber, Person beneficiary, Person policyHolder, int proposedPremium, PremiumPaymentFrequency proposedPaymentFrequency, Vehicle vehicleToInsure){
        if (contractNumber == null || contractNumber.isEmpty() || policyHolder == null || proposedPaymentFrequency == null || vehicleToInsure == null || proposedPremium <= 0) {
            throw new IllegalArgumentException("Invalid input arguments");
        }


        for (AbstractContract contract : contracts) {
            if (contract.getContractNumber().equals(contractNumber)) {
                throw new IllegalArgumentException("Contract number already exists");
            }
        }


        int annualPremium = proposedPremium * (12 / proposedPaymentFrequency.getValueInMonths());
        int minRequired = vehicleToInsure.getOriginalValue() * 2/100;
        if (annualPremium < minRequired) {
            throw new IllegalArgumentException("Annual premium must be be at least 2% of Vehicle price");
        }



        ContractPaymentData paymentData = new ContractPaymentData(
                proposedPremium,
                proposedPaymentFrequency,
                currentTime,
                0
        );


        int coverageAmount = vehicleToInsure.getOriginalValue() / 2;


        SingleVehicleContract contract = new SingleVehicleContract(contractNumber, this, beneficiary, policyHolder,paymentData,coverageAmount,vehicleToInsure);

        chargePremiumOnContract(contract);

        contracts.add(contract);
        policyHolder.addContract(contract);

        return contract;

    }

    public TravelContract insurePersons(String contractNumber, Person policyHolder,  int proposedPremium, PremiumPaymentFrequency proposedPaymentFrequency , Set<Person> personsToInsure){
        if (contractNumber == null || contractNumber.isEmpty() || policyHolder == null ||
                personsToInsure == null || personsToInsure.isEmpty() ||
                proposedPaymentFrequency == null || proposedPremium <= 0) {
            throw new IllegalArgumentException("Invalid input arguments");
        }

        for (AbstractContract c : contracts) {
            if (c.getContractNumber().equals(contractNumber)) {
                throw new IllegalArgumentException("Contract number already exists");
            }
        }

        int annualPremium = proposedPremium * (12 / proposedPaymentFrequency.getValueInMonths());
        if (annualPremium < personsToInsure.size() * 5) {
            throw new IllegalArgumentException("Annual premium must be at least 5x number of insured persons");
        }

        int coverageAmount = personsToInsure.size() * 10;

        ContractPaymentData contractPaymentData = new ContractPaymentData(
                proposedPremium,
                proposedPaymentFrequency,
                currentTime,
                0
        );

        TravelContract contract = new TravelContract(
                contractNumber,
                this,
                policyHolder,
                contractPaymentData,
                coverageAmount,
                personsToInsure
        );

        chargePremiumOnContract(contract);
        contracts.add(contract);
        policyHolder.addContract(contract);

        return contract;
    }

    public MasterVehicleContract createMasterVehicleContract(String contractNumber, Person beneficiary, Person policyHolder){
        if (contractNumber == null || contractNumber.isEmpty() || policyHolder == null) {
            throw new IllegalArgumentException("Invalid input arguments");
        }
        if (policyHolder.getLegalForm() != LegalForm.LEGAL) {
            throw new IllegalArgumentException("Policy holder must be legal person");
        }
        for (AbstractContract c : contracts) {
            if (c.getContractNumber().equals(contractNumber)) {
                throw new IllegalArgumentException("Contract number already exists");
            }
        }
        MasterVehicleContract contract = new MasterVehicleContract(contractNumber, this, beneficiary, policyHolder);
        contracts.add(contract);
        policyHolder.addContract(contract);
        return contract;

    }


    public void moveSingleVehicleContractToMasterVehicleContract( MasterVehicleContract masterVehicleContract, SingleVehicleContract singleVehicleContract) {


        if (singleVehicleContract == null || masterVehicleContract == null) {
            throw new IllegalArgumentException("Invalid input arguments");
        }


        if (!singleVehicleContract.isActive() || !masterVehicleContract.isActive()) {
            throw new InvalidContractException("Contract is not active");
        }


        if (singleVehicleContract.getInsurer() != this || masterVehicleContract.getInsurer() != this) {
            throw new InvalidContractException("Contract not issued by this insurer");
        }


        if (!singleVehicleContract.getPolicyHolder().equals(masterVehicleContract.getPolicyHolder())) {
            throw new InvalidContractException("Policy holders do not match");
        }


        this.contracts.remove(singleVehicleContract);
        singleVehicleContract.getPolicyHolder().getContracts().remove(singleVehicleContract);


        masterVehicleContract.requestAdditionOfChildContract(singleVehicleContract);
    }

    public void chargePremiumsOnContracts() {
        for (AbstractContract contract : contracts) {
            if (contract.isActive()) {
                contract.updateBalance();
            }
        }
    }

    public void chargePremiumOnContract(MasterVehicleContract contract) {
        for (SingleVehicleContract child : contract.getChildContracts()) {
            chargePremiumOnContract(child);
        }
    }

    public void chargePremiumOnContract(AbstractContract contract) {
        ContractPaymentData data = contract.getContractPaymentData();

        while (!currentTime.isBefore(data.getNextPaymentTime())) {
            data.setOutstandingBalance(data.getOutstandingBalance() + data.getPremium());
            data.updateNextPaymentTime();
        }
    }


    public void processClaim(TravelContract contract, Set<Person> affectedPersons) {
        if (contract == null || affectedPersons == null || affectedPersons.isEmpty()) {
            throw new IllegalArgumentException("Invalid input arguments");
        }

        if (!contract.getInsuredPersons().containsAll(affectedPersons)) {
            throw new IllegalArgumentException("Invalid input arguments");
        }

        if (!contract.isActive()) {
            throw new InvalidContractException("Contract must be active");
        }


        int payoutAmount = contract.getCoverageAmount() / affectedPersons.size();
        for (Person person : affectedPersons) {
            person.payout(payoutAmount);
        }

        contract.setInactive();
    }
    public void processClaim(SingleVehicleContract contract, int expectedDamages) {
        if (contract == null || expectedDamages <= 0) {
            throw new IllegalArgumentException("Invalid input arguments");
        }

        if (!contract.isActive()) {
            throw new InvalidContractException("Contract must be active");
        }

        Person payoutRecipient = contract.getBeneficiary();
        if (payoutRecipient == null) {
            payoutRecipient = contract.getPolicyHolder();
        }

        payoutRecipient.payout(contract.getCoverageAmount());

        int threshold = contract.getInsuredVehicle().getOriginalValue() * 70/100;
        if (expectedDamages >= threshold) {
            contract.setInactive();
        }


    }




}
