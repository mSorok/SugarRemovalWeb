package de.unijena.cheminf.sugarremovalweb.model;

import java.util.ArrayList;

public class SubmittedMoleculeData {

    ArrayList<String> sugarsToRemove;
    String submittedDataType;
    String dataString;

    public ArrayList<String> getSugarsToRemove() {
        return sugarsToRemove;
    }

    public void setSugarsToRemove(ArrayList<String> sugarsToRemove) {
        this.sugarsToRemove = sugarsToRemove;
    }

    public String getSubmittedDataType() {
        return submittedDataType;
    }

    public void setSubmittedDataType(String submittedDataType) {
        this.submittedDataType = submittedDataType;
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }
}
