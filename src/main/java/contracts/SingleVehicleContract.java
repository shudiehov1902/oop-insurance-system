package contracts;

import company.*;
import objects.Person;
import objects.Vehicle;
import payment.ContractPaymentData;

public class SingleVehicleContract extends AbstractVehicleContract{
    private final Vehicle insuredVehicle;

    public SingleVehicleContract(String contractNumber, InsuranceCompany insurer, Person beneficiary, Person policyHolder, ContractPaymentData contractPaymentData, int coverageAmount, Vehicle vehicleToInsure){
        super(contractNumber, insurer,beneficiary, policyHolder, contractPaymentData, coverageAmount);
        if(vehicleToInsure ==null || contractPaymentData ==null){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        insuredVehicle = vehicleToInsure;


    }

    public Vehicle getInsuredVehicle() {

        return insuredVehicle;
    }
}
