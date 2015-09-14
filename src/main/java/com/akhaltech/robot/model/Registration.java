package com.akhaltech.robot.model;

import java.util.List;

/**
 * Created by vince on 2015-09-08.
 */
public class Registration {
    private String registrationClass;
    private String certificateIssued;
    private String registrationStatus;
    private String effectiveFrom;
    private String expiryDate;
    private String graduatedFrom;
    private String yearOfGraduation;
    private String termsAndConditions;
    private List<RegistrationHistory> historyList;
    private List<PostgraduateTraining> trainingList;

    public String getRegistrationClass() {
        return registrationClass;
    }

    public void setRegistrationClass(String registrationClass) {
        this.registrationClass = registrationClass;
    }

    public String getCertificateIssued() {
        return certificateIssued;
    }

    public void setCertificateIssued(String certificateIssued) {
        this.certificateIssued = certificateIssued;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public String getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(String effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public String getGraduatedFrom() {
        return graduatedFrom;
    }

    public void setGraduatedFrom(String graduatedFrom) {
        this.graduatedFrom = graduatedFrom;
    }

    public String getYearOfGraduation() {
        return yearOfGraduation;
    }

    public void setYearOfGraduation(String yearOfGraduation) {
        this.yearOfGraduation = yearOfGraduation;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public List<RegistrationHistory> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<RegistrationHistory> historyList) {
        this.historyList = historyList;
    }

    public List<PostgraduateTraining> getTrainingList() {
        return trainingList;
    }

    public void setTrainingList(List<PostgraduateTraining> trainingList) {
        this.trainingList = trainingList;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }
}
