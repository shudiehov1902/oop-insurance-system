package contracts;
import java.util.LinkedHashSet;
import java.util.Set;
import company.InsuranceCompany;
import objects.*;


public class MasterVehicleContract extends AbstractVehicleContract{
    private final Set<SingleVehicleContract> childContracts;
    public MasterVehicleContract(String contractNumber, InsuranceCompany insurer, Person beneficiary, Person policyHolder){
        super(contractNumber, insurer, beneficiary, policyHolder,null,0);
        if (policyHolder.getLegalForm() != LegalForm.LEGAL) {
            throw new IllegalArgumentException("Policy holder must be legal person");
        }
        this.childContracts = new LinkedHashSet<>();
    }

    public Set<SingleVehicleContract> getChildContracts() {
        return childContracts;
    }

    public void requestAdditionOfChildContract(SingleVehicleContract contract){

        childContracts.add(contract);
    }
    @Override
    public boolean isActive() {
        if (childContracts.isEmpty()) return super.isActive();
        return childContracts.stream().anyMatch(SingleVehicleContract::isActive);
    }


    @Override
    public void setInactive() {
        super.setInactive();
        childContracts.forEach(SingleVehicleContract::setInactive);
    }

    @Override
    public void pay(int amount) {
        getInsurer().getHandler().pay(this, amount);
    }

    @Override
    public void updateBalance(){
        insurer.chargePremiumOnContract(this);
    };
}
