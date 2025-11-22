package objects;

import contracts.AbstractContract;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;



public class Person {
    private final String id;
    private final LegalForm legalForm;
    private int paidOutAmount;
    private final Set<AbstractContract> contracts;

    public Person (String id){
        if(id == null || id.isEmpty() || (!isValidBirthNumber(id) && !isValidRegistrationNumber(id))){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        this.id = id;

        if(isValidBirthNumber(id)){
            this.legalForm = LegalForm.NATURAL;
        }
        else{
            this.legalForm = LegalForm.LEGAL;
        }
        this.paidOutAmount  = 0;
        contracts = new LinkedHashSet<>();
    }
    public static boolean isValidBirthNumber(String birthNumber){
        if(birthNumber == null || (birthNumber.length()!=9 && birthNumber.length()!=10 ) || !birthNumber.matches("\\d+")){
            return false;
        }
        int dd = Integer.parseInt(birthNumber.substring(4, 6));
        int mm = Integer.parseInt(birthNumber.substring(2, 4));
        int rr = Integer.parseInt(birthNumber.substring(0, 2));
        if(mm<1 || (mm>12 && mm<51) || mm>62){
            return false;
        }
        int year = 0;
        if(birthNumber.matches("\\d{9}$")){
            if(rr>53){
                return false;
            }
            year = 1900 + rr;
        }
        if(birthNumber.matches("\\d{10}$")) {
            int digit = 0;
            int sum =0;
            for(int i = 0; i<10;i++){
                digit = Character.getNumericValue(birthNumber.toCharArray()[i]);
                sum += (int) Math.pow((-1),i) * digit;
            }
            if (sum%11 != 0){
               return false;
            }
            year =(rr >= 54) ? 1900 + rr : 2000 + rr;

        }
        int month = 0;
        if(mm>=51){
            month = mm - 50;
        }
        else {
            month = mm;
        }

        try {
            LocalDate.of(year, month, dd);
        } catch (DateTimeException e) {
            return false;
        }

        return true;


    }

    public static boolean isValidRegistrationNumber(String registrationNumber){
        if(registrationNumber == null ||(registrationNumber.length()!=6 && registrationNumber.length()!=8 ) || !registrationNumber.matches("\\d+")){
            return false;
        }
        return true;
    }

    public String getId() {
        return id;
    }

    public int getPaidOutAmount() {
        return paidOutAmount;
    }

    public LegalForm getLegalForm() {
        return legalForm;
    }

    public Set<AbstractContract> getContracts() {
        return contracts;
    }

    public void addContract(AbstractContract contract){
        if(contract == null){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        contracts.add(contract);
    }

    public void payout(int paidOutAmount){
        if(paidOutAmount<=0){
            throw new IllegalArgumentException("Invalid input arguments");
        }
        this.paidOutAmount += paidOutAmount;
    }


}
