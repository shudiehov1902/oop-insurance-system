# OOP Insurance System

Semestrálny projekt z predmetu B-OOP (FEI STU, 2025) — jednoduchý model poisťovacieho
systému pre malé poisťovne, implementovaný v Jave podľa zadania `OOP_semestralne_zadanie.pdf`.

## Popis

Systém modeluje poisťovňu (`InsuranceCompany`), ktorá uzatvára a spravuje tri typy
poistných zmlúv:

- **SingleVehicleContract** — povinné zmluvné poistenie (PZP) pre jedno vozidlo
- **MasterVehicleContract** — rámcová (súborová) PZP zmluva pre flotilu vozidiel,
  zložená z jednotlivých `SingleVehicleContract`
- **TravelContract** — cestovné poistenie skupiny osôb

Poistenými objektmi môžu byť osoby (`Person`, fyzické aj právnické) alebo vozidlá
(`Vehicle`). Platby na zmluvách spravuje `PaymentHandler`, ktorý vykonáva úhrady
a ukladá ich históriu (`PaymentInstance`). Poisťovňa priebežne aktualizuje nedoplatky
zmlúv a spracúva poistné udalosti (výplatu poistného plnenia).

## Štruktúra balíčkov

```
src/main/java/
├── company/    InsuranceCompany
├── contracts/  AbstractContract, AbstractVehicleContract, SingleVehicleContract,
│               MasterVehicleContract, TravelContract, InvalidContractException
├── objects/    Person, Vehicle, LegalForm
└── payment/    ContractPaymentData, PaymentHandler, PaymentInstance,
                PremiumPaymentFrequency
```

## Build a testy

Projekt používa Maven (Java 23, JUnit 5).

```bash
mvn test
```

Testy sa nachádzajú v `src/test/java` (`RequiredTests.java` — zadané testy,
`AiBasedTests.java` — doplnkové testy).

## Opravené chyby

- **Surefire nespúšťal žiadne testy** — `pom.xml` obmedzoval testy na vzor
  `**/*Test.java`, ktorému nevyhovuje ani `RequiredTests.java`, ani
  `AiBasedTests.java` (obe končia na `Tests.java`). Testy sa teda nikdy
  nespúšťali. Obmedzenie bolo odstránené, takže Surefire teraz používa
  štandardné (širšie) vzory a všetky testy sa spustia.
- **`Person.contracts` obsahovala aj zmluvy, kde je osoba len oprávnenou osobou
  (beneficiary)** — podľa zadania má táto množina obsahovať iba zmluvy, na
  ktorých je osoba uvedená ako `policyHolder`. V `InsuranceCompany.insureVehicle`
  a `InsuranceCompany.createMasterVehicleContract` sa navyše volalo aj
  `beneficiary.addContract(contract)`, čo bolo v rozpore so zadaním; tieto
  volania boli odstránené.
