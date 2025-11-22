import company.InsuranceCompany;
import contracts.*;
import objects.LegalForm;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PaymentInstance;
import payment.PremiumPaymentFrequency;
import payment.PaymentHandler;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AiBasedTests {

    private InsuranceCompany insuranceCompany;
    private Person naturalPerson1;
    private Person legalPerson1;
    private Vehicle vehicle1;
    private Vehicle vehicle2;
    private ContractPaymentData paymentData;
    private ContractPaymentData paymentData2;

    @BeforeEach
    void setUp() {
        insuranceCompany = new InsuranceCompany(LocalDateTime.of(2025, 4, 15, 12, 0));
        naturalPerson1 = new Person("8351068242"); // Valid rodné číslo
        legalPerson1 = new Person("12345678"); // Valid IČO
        vehicle1 = new Vehicle("AA111AA", 15000);
        vehicle2 = new Vehicle("MATAPAT", 10000);
        paymentData = new ContractPaymentData(150, PremiumPaymentFrequency.QUARTERLY, insuranceCompany.getCurrentTime(), 0);
        paymentData2 = new ContractPaymentData(150, PremiumPaymentFrequency.QUARTERLY, insuranceCompany.getCurrentTime(), 0);
    }

    // Tests for Person (Section 5.3.1)
    @Test
    void testPersonConstructorInvalidId() {
        // Tests requirement 5.3.1: Constructor throws IllegalArgumentException for invalid rodné číslo or IČO
        assertThrows(IllegalArgumentException.class, () -> new Person(null), "Null ID should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Person(""), "Empty ID should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Person("123"), "Invalid IČO length should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Person("123456789"), "Invalid rodné číslo length should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Person("9913331234"), "Invalid month in rodné číslo should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Person("0402114912"), "Invalid control sum for 10-digit rodné číslo should throw IllegalArgumentException");
    }

    @Test
    void testPersonPayout() {
        // Tests requirement 5.3.1: payout method updates paidOutAmount and throws IllegalArgumentException for non-positive amounts
        Person person = new Person("8351068242");
        person.payout(1000);
        assertEquals(1000, person.getPaidOutAmount(), "payout should update paidOutAmount");
        person.payout(500);
        assertEquals(1500, person.getPaidOutAmount(), "Multiple payouts should accumulate");
        assertThrows(IllegalArgumentException.class, () -> person.payout(0), "Zero payout should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> person.payout(-100), "Negative payout should throw IllegalArgumentException");
    }

    @Test
    void testPersonAddContract() {
        // Tests requirement 5.3.1: addContract throws IllegalArgumentException for null and maintains insertion order
        Person person = new Person("8351068242");
        SingleVehicleContract c1 = new SingleVehicleContract("c1", insuranceCompany, null, person, paymentData, 5000, vehicle1);
        SingleVehicleContract c2 = new SingleVehicleContract("c2", insuranceCompany, null, person, paymentData2, 5000, vehicle1);
        person.addContract(c1);
        person.addContract(c2);
        assertEquals(Arrays.asList(c1, c2), new ArrayList<>(person.getContracts()), "Contracts should maintain insertion order");
        assertThrows(IllegalArgumentException.class, () -> person.addContract(null), "Null contract should throw IllegalArgumentException");
    }

    // Tests for Vehicle (Section 5.3.2)
    @Test
    void testVehicleConstructor() {
        // Tests requirement 5.3.2: Constructor validates licensePlate and originalValue
        assertThrows(IllegalArgumentException.class, () -> new Vehicle(null, 1000), "Null licensePlate should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("AA11AA", 1000), "Invalid licensePlate length should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("AA111A!", 1000), "Invalid licensePlate characters should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("AA111AA", 0), "Non-positive originalValue should throw IllegalArgumentException");
        Vehicle vehicle = new Vehicle("AA111AA", 15000);
        assertEquals("AA111AA", vehicle.getLicensePlate());
        assertEquals(15000, vehicle.getOriginalValue());
    }

    // Tests for ContractPaymentData (Section 5.4.1)
    @Test
    void testContractPaymentDataConstructorAndSetters() {
        // Tests requirement 5.4.1: Constructor and setters validate inputs
        LocalDateTime time = LocalDateTime.of(2025, 4, 15, 12, 0);
        assertThrows(IllegalArgumentException.class, () -> new ContractPaymentData(0, PremiumPaymentFrequency.ANNUAL, time, 0), "Non-positive premium should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new ContractPaymentData(100, null, time, 0), "Null frequency should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, null, 0), "Null nextPaymentTime should throw IllegalArgumentException");
        ContractPaymentData data = new ContractPaymentData(100, PremiumPaymentFrequency.SEMI_ANNUAL, time, 50);
        assertThrows(IllegalArgumentException.class, () -> data.setPremium(0), "setPremium with non-positive value should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> data.setPremiumPaymentFrequency(null), "setPremiumPaymentFrequency with null should throw IllegalArgumentException");
        data.updateNextPaymentTime();
        assertEquals(time.plusMonths(6), data.getNextPaymentTime(), "updateNextPaymentTime should advance by frequency");
    }

    // Tests for TravelContract (Section 5.2.2)
    @Test
    void testTravelContractConstructor() {
        // Tests requirement 5.2.2: Constructor validates personsToInsure and contractPaymentData
        Set<Person> persons = new HashSet<>(Arrays.asList(naturalPerson1));
        assertThrows(IllegalArgumentException.class, () -> new TravelContract("t1", insuranceCompany, naturalPerson1, null, 1000, persons), "Null contractPaymentData should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new TravelContract("t1", insuranceCompany, naturalPerson1, paymentData, 1000, null), "Null personsToInsure should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new TravelContract("t1", insuranceCompany, naturalPerson1, paymentData, 1000, new HashSet<>()), "Empty personsToInsure should throw IllegalArgumentException");
        TravelContract contract = new TravelContract("t1", insuranceCompany, legalPerson1, paymentData, 1000, persons);
        assertEquals(persons, contract.getInsuredPersons());
        assertTrue(contract.isActive());
    }

    @Test
    void testTravelContractConstructorWithLegalPersonInPersonsToInsure() {
        // Tests requirement 5.2.2: Constructor throws IllegalArgumentException if personsToInsure contains a legal person
        Set<Person> personsWithLegal = new HashSet<>(Arrays.asList(naturalPerson1, legalPerson1));
        assertThrows(IllegalArgumentException.class, () ->
                        new TravelContract("t2", insuranceCompany, legalPerson1, paymentData, 1000, personsWithLegal),
                "personsToInsure containing legal person should throw IllegalArgumentException");
    }

    // Tests for AbstractVehicleContract (Section 5.2.3)
    @Test
    void testAbstractVehicleContractBeneficiary() {
        // Tests requirement 5.2.3: setBeneficiary and beneficiary logic
        SingleVehicleContract contract = new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, paymentData, 5000, vehicle1);
        contract.setBeneficiary(legalPerson1);
        assertEquals(legalPerson1, contract.getBeneficiary());
        contract.setBeneficiary(null);
        assertNull(contract.getBeneficiary());
        assertThrows(IllegalArgumentException.class, () -> new SingleVehicleContract("c2", insuranceCompany, naturalPerson1, naturalPerson1, paymentData2, 5000, vehicle2), "Beneficiary same as policyHolder should throw IllegalArgumentException");
    }

    // Tests for MasterVehicleContract (Section 5.2.5)
    @Test
    void testMasterVehicleContractActivity() {
        // Tests requirement 5.2.5: isActive and setInactive behavior
        MasterVehicleContract master = new MasterVehicleContract("m1", insuranceCompany, null, legalPerson1);
        assertTrue(master.isActive(), "Empty MasterVehicleContract should be active");
        SingleVehicleContract c1 = new SingleVehicleContract("c1", insuranceCompany, null, legalPerson1, paymentData, 5000, vehicle1);
        SingleVehicleContract c2 = new SingleVehicleContract("c2", insuranceCompany, null, legalPerson1, paymentData2, 5000, vehicle2);
        master.getChildContracts().add(c1);
        master.getChildContracts().add(c2);
        assertTrue(master.isActive(), "MasterVehicleContract with active children should be active");
        c1.setInactive();
        assertTrue(master.isActive(), "MasterVehicleContract with some active children should be active");
        c2.setInactive();
        assertFalse(master.isActive(), "MasterVehicleContract with no active children should be inactive");
        master.setInactive();
        assertFalse(master.isActive());
        assertFalse(c1.isActive());
        assertFalse(c2.isActive());
    }

    // Tests for PaymentInstance (Section 5.4.2)
    @Test
    void testPaymentInstanceConstructor() {
        // Tests requirement 5.4.2: Constructor validates paymentTime and paymentAmount
        LocalDateTime time = LocalDateTime.of(2025, 4, 15, 12, 0);
        assertThrows(IllegalArgumentException.class, () -> new PaymentInstance(null, 100), "Null paymentTime should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new PaymentInstance(time, 0), "Non-positive paymentAmount should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new PaymentInstance(time, -100), "Negative paymentAmount should throw IllegalArgumentException");
        PaymentInstance instance = new PaymentInstance(time, 100);
        assertEquals(time, instance.getPaymentTime());
        assertEquals(100, instance.getPaymentAmount());
    }

    // Tests for PremiumPaymentFrequency (Section 5.4.1)
    @Test
    void testPremiumPaymentFrequencyValues() {
        // Tests requirement 5.4.1: getValueInMonths returns correct values for each enum
        assertEquals(12, PremiumPaymentFrequency.ANNUAL.getValueInMonths(), "ANNUAL should return 12 months");
        assertEquals(6, PremiumPaymentFrequency.SEMI_ANNUAL.getValueInMonths(), "SEMI_ANNUAL should return 6 months");
        assertEquals(3, PremiumPaymentFrequency.QUARTERLY.getValueInMonths(), "QUARTERLY should return 3 months");
        assertEquals(1, PremiumPaymentFrequency.MONTHLY.getValueInMonths(), "MONTHLY should return 1 month");
    }

    // Tests for MasterVehicleContract (Section 5.2.5)
    @Test
    void testMasterVehicleContractConstructorInvalidPolicyHolder() {
        // Tests requirement 5.2.5: Constructor validates that policyHolder is a legal person
        assertThrows(IllegalArgumentException.class, () -> new MasterVehicleContract("m1", insuranceCompany, null, naturalPerson1), "Natural person as policyHolder should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new MasterVehicleContract(null, insuranceCompany, null, legalPerson1), "Null contractNumber should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new MasterVehicleContract("", insuranceCompany, null, legalPerson1), "Empty contractNumber should throw IllegalArgumentException");
        MasterVehicleContract master = new MasterVehicleContract("m1", insuranceCompany, null, legalPerson1);
        assertNull(master.getContractPaymentData(), "contractPaymentData should be null");
        assertEquals(0, master.getCoverageAmount(), "coverageAmount should be 0");
    }

    @Test
    void testMasterVehicleContractPayEmptyChildContracts() {
        // Tests requirement 5.4.2: pay(MasterVehicleContract, int) throws InvalidContractException for empty childContracts
        MasterVehicleContract master = new MasterVehicleContract("m1", insuranceCompany, null, legalPerson1);
        PaymentHandler handler = insuranceCompany.getHandler();
        assertThrows(InvalidContractException.class, () -> handler.pay(master, 100), "Pay on MasterVehicleContract with no child contracts should throw InvalidContractException");
    }

    @Test
    void testMasterVehicleContractPayInsufficientAmount() {
        // Tests requirement 5.4.2: pay(MasterVehicleContract, int) handles partial payment distribution
        SingleVehicleContract c1 = new SingleVehicleContract("c1", insuranceCompany, null, legalPerson1, paymentData, 5000, vehicle1);
        SingleVehicleContract c2 = new SingleVehicleContract("c2", insuranceCompany, null, legalPerson1, paymentData2, 5000, vehicle2);
        c1.getContractPaymentData().setPremium(100);
        c2.getContractPaymentData().setPremium(200);
        c1.getContractPaymentData().setOutstandingBalance(100);
        c2.getContractPaymentData().setOutstandingBalance(200);
        MasterVehicleContract master = new MasterVehicleContract("m1", insuranceCompany, null, legalPerson1);
        master.getChildContracts().add(c1);
        master.getChildContracts().add(c2);
        insuranceCompany.getContracts().add(master);
        PaymentHandler handler = insuranceCompany.getHandler();
        handler.pay(master, 150);
        assertEquals(0, c1.getContractPaymentData().getOutstandingBalance(), "First contract should be fully paid");
        assertEquals(150, c2.getContractPaymentData().getOutstandingBalance(), "Second contract should have remaining balance");
        assertEquals(1, handler.getPaymentHistory().get(master).size());
        PaymentInstance instance = handler.getPaymentHistory().get(master).iterator().next();
        assertEquals(150, instance.getPaymentAmount());
    }

    // Tests for AbstractContract (Section 5.2.1)
    @Test
    void testAbstractContractPayDispatch() {
        // Tests requirement 5.2.6: pay method dispatches to PaymentHandler correctly
        SingleVehicleContract contract = new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, paymentData, 5000, vehicle1);
        contract.getContractPaymentData().setOutstandingBalance(200);
        contract.pay(100);
        assertEquals(100, contract.getContractPaymentData().getOutstandingBalance(), "pay should reduce outstandingBalance via PaymentHandler");
        assertEquals(1, insuranceCompany.getHandler().getPaymentHistory().get(contract).size(), "PaymentHandler should record payment");
    }

    @Test
    void testAbstractContractUpdateBalanceDispatch() {
        // Tests requirement 5.2.6: updateBalance dispatches to InsuranceCompany correctly
        SingleVehicleContract contract = new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, paymentData, 5000, vehicle1);
        contract.getContractPaymentData().setPremium(150);
        insuranceCompany.setCurrentTime(insuranceCompany.getCurrentTime().plusMonths(3));
        contract.updateBalance();
        assertEquals(300, contract.getContractPaymentData().getOutstandingBalance(), "updateBalance should increase outstandingBalance via InsuranceCompany");
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(3), contract.getContractPaymentData().getNextPaymentTime(), "Next payment time should advance by two QUARTERLY periods");
    }

    @Test
    void testAbstractContractUpdateBalanceNoCharge() {
        // Tests requirement 5.2.6: updateBalance does not charge if currentTime is before nextPaymentTime
        ContractPaymentData futureData = new ContractPaymentData(150, PremiumPaymentFrequency.QUARTERLY, insuranceCompany.getCurrentTime().plusMonths(3), 0);
        SingleVehicleContract contract = new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, futureData, 5000, vehicle1);
        contract.getContractPaymentData().setPremium(150);
        // currentTime = 2025-04-15, nextPaymentTime = 2025-07-15, no charge expected
        contract.updateBalance();
        assertEquals(0, contract.getContractPaymentData().getOutstandingBalance(), "No premium should be charged if currentTime is before nextPaymentTime");
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(3), contract.getContractPaymentData().getNextPaymentTime(), "nextPaymentTime should not change");
    }

    @Test
    void testAbstractContractUpdateBalanceEqualTime() {
        // Tests requirement 5.2.6: updateBalance charges premium if currentTime equals nextPaymentTime
        SingleVehicleContract contract = new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, paymentData, 5000, vehicle1);
        contract.getContractPaymentData().setPremium(150);
        // currentTime = 2025-04-15, nextPaymentTime = 2025-04-15, one charge expected
        contract.updateBalance();
        assertEquals(150, contract.getContractPaymentData().getOutstandingBalance(), "One premium should be charged if currentTime equals nextPaymentTime");
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(3), contract.getContractPaymentData().getNextPaymentTime(), "nextPaymentTime should advance by one QUARTERLY period");
    }

    @Test
    void testAbstractContractUpdateBalanceTravelContract() {
        // Tests requirement 5.2.6: updateBalance works for TravelContract
        Set<Person> persons = new HashSet<>(Arrays.asList(naturalPerson1));
        TravelContract contract = new TravelContract("t1", insuranceCompany, naturalPerson1, paymentData, 1000, persons);
        contract.getContractPaymentData().setPremium(100);
        insuranceCompany.setCurrentTime(insuranceCompany.getCurrentTime().plusMonths(6));
        contract.updateBalance();
        assertEquals(300, contract.getContractPaymentData().getOutstandingBalance(), "Three premiums should be charged for 6 months with QUARTERLY frequency");
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(3), contract.getContractPaymentData().getNextPaymentTime(), "nextPaymentTime should advance by three QUARTERLY periods");
    }

    @Test
    void testAbstractContractUpdateBalanceMultipleFrequencies() {
        // Tests requirement 5.2.6: updateBalance handles different premiumPaymentFrequencies
        // MONTHLY
        ContractPaymentData monthlyData = new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY, insuranceCompany.getCurrentTime(), 0);
        SingleVehicleContract monthlyContract = new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, monthlyData, 5000, vehicle1);
        insuranceCompany.setCurrentTime(insuranceCompany.getCurrentTime().plusMonths(2));
        monthlyContract.updateBalance();
        assertEquals(300, monthlyContract.getContractPaymentData().getOutstandingBalance(), "Three premiums should be charged for 2 months with MONTHLY frequency");
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(1), monthlyContract.getContractPaymentData().getNextPaymentTime(), "nextPaymentTime should advance by three MONTHLY periods");

        // SEMI_ANNUAL
        ContractPaymentData semiAnnualData = new ContractPaymentData(200, PremiumPaymentFrequency.SEMI_ANNUAL, insuranceCompany.getCurrentTime().minusMonths(2), 0);
        SingleVehicleContract semiAnnualContract = new SingleVehicleContract("c2", insuranceCompany, null, naturalPerson1, semiAnnualData, 5000, vehicle1);
        semiAnnualContract.updateBalance();
        assertEquals(200, semiAnnualContract.getContractPaymentData().getOutstandingBalance(), "One premium should be charged for 6 months with SEMI_ANNUAL frequency");
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(4), semiAnnualContract.getContractPaymentData().getNextPaymentTime(), "nextPaymentTime should advance by one SEMI_ANNUAL period");

        // ANNUAL
        ContractPaymentData annualData = new ContractPaymentData(300, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime().minusMonths(12), 0);
        SingleVehicleContract annualContract = new SingleVehicleContract("c3", insuranceCompany, null, naturalPerson1, annualData, 5000, vehicle1);
        annualContract.updateBalance();
        assertEquals(600, annualContract.getContractPaymentData().getOutstandingBalance(), "Two premiums should be charged for 12 months with ANNUAL frequency");
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(12), annualContract.getContractPaymentData().getNextPaymentTime(), "nextPaymentTime should advance by two ANNUAL periods");
    }

    @Test
    void testAbstractContractUpdateBalanceNonZeroInitialBalance() {
        // Tests requirement 5.2.6: updateBalance handles non-zero initial outstandingBalance
        ContractPaymentData data = new ContractPaymentData(150, PremiumPaymentFrequency.QUARTERLY, insuranceCompany.getCurrentTime(), -50); // Overpayment
        SingleVehicleContract contract = new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, data, 5000, vehicle1);
        insuranceCompany.setCurrentTime(insuranceCompany.getCurrentTime().plusMonths(3));
        contract.updateBalance();
        assertEquals(250, contract.getContractPaymentData().getOutstandingBalance(), "Two premiums should be added to initial negative balance");
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(3), contract.getContractPaymentData().getNextPaymentTime(), "nextPaymentTime should advance by two QUARTERLY periods");
    }

    @Test
    void testMasterVehicleContractUpdateBalance() {
        // Tests requirement 5.2.6: updateBalance for MasterVehicleContract iterates over child contracts
        ContractPaymentData paymentData1 = new ContractPaymentData(100, PremiumPaymentFrequency.QUARTERLY, insuranceCompany.getCurrentTime(), 0);
        ContractPaymentData paymentData2 = new ContractPaymentData(200, PremiumPaymentFrequency.QUARTERLY, insuranceCompany.getCurrentTime(), 0);
        SingleVehicleContract c1 = new SingleVehicleContract("c1", insuranceCompany, null, legalPerson1, paymentData1, 5000, vehicle1);
        SingleVehicleContract c2 = new SingleVehicleContract("c2", insuranceCompany, null, legalPerson1, paymentData2, 5000, vehicle2);
        c1.getContractPaymentData().setPremium(100);
        c2.getContractPaymentData().setPremium(200);
        MasterVehicleContract master = new MasterVehicleContract("m1", insuranceCompany, null, legalPerson1);
        insuranceCompany.getContracts().add(master);
        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(master, c1);
        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(master, c2);
        insuranceCompany.setCurrentTime(insuranceCompany.getCurrentTime().plusMonths(3));
        master.updateBalance();
        assertEquals(200, c1.getContractPaymentData().getOutstandingBalance(), "Child contract 1 should have two premiums charged");
        assertEquals(400, c2.getContractPaymentData().getOutstandingBalance(), "Child contract 2 should have two premiums charged");
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(3), c1.getContractPaymentData().getNextPaymentTime(), "Child contract 1 nextPaymentTime should advance by two QUARTERLY periods");
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(3), c2.getContractPaymentData().getNextPaymentTime(), "Child contract 2 nextPaymentTime should advance by two QUARTERLY periods");
    }

    @Test
    void testMasterVehicleContractUpdateBalanceEmptyChildContracts() {
        // Tests requirement 5.2.6: updateBalance for MasterVehicleContract with empty childContracts
        MasterVehicleContract master = new MasterVehicleContract("m1", insuranceCompany, null, legalPerson1);
        insuranceCompany.getContracts().add(master);
        insuranceCompany.setCurrentTime(insuranceCompany.getCurrentTime().plusMonths(3));
        master.updateBalance(); // Should not throw exception
        assertTrue(master.getChildContracts().isEmpty(), "childContracts should remain empty");
    }

    // Tests for ContractPaymentData (Section 5.4.1)
    @Test
    void testContractPaymentDataBalanceUpdates() {
        // Tests requirement 5.4.1: outstandingBalance handling for overpayment and underpayment
        ContractPaymentData data = new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY, insuranceCompany.getCurrentTime(), 50);
        data.setOutstandingBalance(-50); // Overpayment
        assertEquals(-50, data.getOutstandingBalance(), "Negative outstandingBalance should indicate overpayment");
        data.setOutstandingBalance(200); // Underpayment
        assertEquals(200, data.getOutstandingBalance(), "Positive outstandingBalance should indicate underpayment");
    }

    // Tests for InsuranceCompany (Section 5.5)
    @Test
    void testInsureVehicleInvalidPremium() {
        // Tests requirement 5.5.1: insureVehicle validates annual premium >= 2% of vehicle value
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insureVehicle("c1", null, naturalPerson1, 200, PremiumPaymentFrequency.ANNUAL, vehicle1), "Premium < 2% of vehicle value should throw IllegalArgumentException");
        SingleVehicleContract contract = insuranceCompany.insureVehicle("c1", null, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);
        assertEquals(7500, contract.getCoverageAmount(), "Coverage should be half of vehicle value");
        assertEquals(300, contract.getContractPaymentData().getOutstandingBalance(), "Outstanding balance should include initial premium");
    }

    @Test
    void testChargePremiumOnMasterVehicleContractEmptyChildContracts() {
        // Tests requirement 5.5.2: chargePremiumOnContract(MasterVehicleContract) with empty childContracts
        MasterVehicleContract master = insuranceCompany.createMasterVehicleContract("m1", null, legalPerson1);
        insuranceCompany.getContracts().add(master);
        insuranceCompany.setCurrentTime(insuranceCompany.getCurrentTime().plusMonths(3));
        insuranceCompany.chargePremiumOnContract(master); // Should not throw exception
        assertTrue(master.getChildContracts().isEmpty(), "childContracts should remain empty");
    }

    @Test
    void testProcessClaimTravelContractInvalidPersons() {
        // Tests requirement 5.5.3: processClaim(TravelContract, Set<Person>) validates affectedPersons
        Set<Person> persons = new HashSet<>(Arrays.asList(naturalPerson1));
        TravelContract contract = insuranceCompany.insurePersons("t1", naturalPerson1, 5, PremiumPaymentFrequency.ANNUAL, persons);
        Person unrelatedPerson = new Person("0402114911");
        Set<Person> invalidPersons = new HashSet<>(Arrays.asList(unrelatedPerson));
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(contract, invalidPersons), "affectedPersons not subset of insuredPersons should throw IllegalArgumentException");
    }
}