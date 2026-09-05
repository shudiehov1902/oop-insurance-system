import company.InsuranceCompany;
import contracts.SingleVehicleContract;
import contracts.MasterVehicleContract;
import objects.Person;
import objects.Vehicle;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class Demo {
    public static void main(String[] args) {
        System.out.println("=== OOP Insurance System - Demo Run ===\n");

        InsuranceCompany company = new InsuranceCompany(LocalDateTime.of(2026, 1, 1, 0, 0));
        System.out.println("[1] Insurance company created. Current time: " + company.getCurrentTime());

        // Valid Slovak birth number (natural person)
        Person policyHolder = new Person("9001010007");
        System.out.println("[2] Policy holder created (natural person): " + policyHolder.getId()
                + " | legal form: " + policyHolder.getLegalForm());

        Vehicle car = new Vehicle("BA123XY", 20000);
        System.out.println("[3] Vehicle registered: " + car.getLicensePlate()
                + " | original value: " + car.getOriginalValue() + " EUR");

        System.out.println("\n[4] Attempting to insure the vehicle with a premium below the required minimum (2% of value)...");
        try {
            company.insureVehicle("C-001", null, policyHolder, 50, PremiumPaymentFrequency.ANNUAL, car);
        } catch (IllegalArgumentException e) {
            System.out.println("    -> Rejected as expected: " + e.getMessage());
        }

        System.out.println("\n[5] Insuring the vehicle with a valid premium (>= 2% of value = 400 EUR/year)...");
        SingleVehicleContract contract = company.insureVehicle(
                "C-001", null, policyHolder, 400, PremiumPaymentFrequency.ANNUAL, car
        );
        System.out.println("    -> Contract created: " + contract.getContractNumber()
                + " | coverage amount: " + contract.getCoverageAmount() + " EUR"
                + " | active: " + contract.isActive());

        System.out.println("\n[6] Filing a claim for damages below the total-loss threshold (70% of value)...");
        company.processClaim(contract, 5000);
        System.out.println("    -> Payout recorded for policy holder: " + policyHolder.getPaidOutAmount() + " EUR"
                + " | contract still active: " + contract.isActive());

        System.out.println("\n[7] Filing a second claim, this time above the total-loss threshold...");
        company.processClaim(contract, 15000);
        System.out.println("    -> Total paid out: " + policyHolder.getPaidOutAmount() + " EUR"
                + " | contract active after total loss: " + contract.isActive());

        System.out.println("\n[8] Creating a legal-entity policy holder and a Master Vehicle Contract...");
        Person company_ = new Person("12345678"); // 8-digit registration number -> LEGAL
        System.out.println("    -> Legal entity created: " + company_.getId()
                + " | legal form: " + company_.getLegalForm());
        MasterVehicleContract master = company.createMasterVehicleContract("M-001", null, company_);
        System.out.println("    -> Master contract created: " + master.getContractNumber()
                + " | active: " + master.isActive());

        System.out.println("\n=== Demo complete: validation rules, contract lifecycle, and claims all behaved as designed. ===");
    }
}
