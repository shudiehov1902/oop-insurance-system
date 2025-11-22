package contracts;

import java.util.Set;
import objects.*;
import company.*;
import payment.ContractPaymentData;

public class TravelContract extends AbstractContract{
    private final Set<Person> insuredPersons;

    public TravelContract(String contractNumber, InsuranceCompany insurer, Person policyHolder, ContractPaymentData contractPaymentData, int coverageAmount, Set<Person> personsToInsure){
        super(contractNumber, insurer,policyHolder,contractPaymentData,coverageAmount);
        if(personsToInsure==null|| personsToInsure.isEmpty() || contractPaymentData == null){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        for (Person person : personsToInsure) {
            if (person == null || person.getLegalForm() != LegalForm.NATURAL) {
                throw new IllegalArgumentException("Only natural persons can be insured and they can not be null");
            }
        }

        insuredPersons = personsToInsure;
    }

    public Set<Person> getInsuredPersons() {
        return insuredPersons;
    }
}
