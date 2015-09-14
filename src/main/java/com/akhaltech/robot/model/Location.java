package com.akhaltech.robot.model;

/**
 * Created by vince on 2015-09-08.
 */
public class Location {

    private String addressSummary;
    private String contactSummary;
    private String electoralDistrict;
    private String corporationName;
    private String corporationIssuedDate;
    private String medicalLicensesInOtherJurisdictions;

    public String getAddressSummary() {
        return addressSummary;
    }

    public void setAddressSummary(String addressSummary) {
        this.addressSummary = addressSummary;
    }

    public String getContactSummary() {
        return contactSummary;
    }

    public void setContactSummary(String contactSummary) {
        this.contactSummary = contactSummary;
    }

    public String getElectoralDistrict() {
        return electoralDistrict;
    }

    public void setElectoralDistrict(String electoralDistrict) {
        this.electoralDistrict = electoralDistrict;
    }

    public String getCorporationName() {
        return corporationName;
    }

    public void setCorporationName(String corporationName) {
        this.corporationName = corporationName;
    }

    public String getCorporationIssuedDate() {
        return corporationIssuedDate;
    }

    public void setCorporationIssuedDate(String corporationIssuedDate) {
        this.corporationIssuedDate = corporationIssuedDate;
    }

    public String getMedicalLicensesInOtherJurisdictions() {
        return medicalLicensesInOtherJurisdictions;
    }

    public void setMedicalLicensesInOtherJurisdictions(String medicalLicensesInOtherJurisdictions) {
        this.medicalLicensesInOtherJurisdictions = medicalLicensesInOtherJurisdictions;
    }
}
