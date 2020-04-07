package de.unijena.cheminf.sugarremovalweb.model;

import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;

public class ProcessedMolecule {

    ArrayList<String> sugarsToRemove;

    IAtomContainer molecule;

    public String smiles;

    ArrayList<IAtomContainer> sugarMoietiesRemoved;

    public ArrayList<String> sugarMoietiesRemovedSmiles;

    ArrayList<IAtomContainer> deglycosylatedMoieties;

    public ArrayList<String> deglycosylatedMoietiesSmiles;

    String submittedDataType;


    public ArrayList<String> getSugarsToRemove() {
        return sugarsToRemove;
    }

    public void setSugarsToRemove(ArrayList<String> sugarsToRemove) {
        this.sugarsToRemove = sugarsToRemove;
    }

    public IAtomContainer getMolecule() {
        return molecule;
    }

    public void setMolecule(IAtomContainer molecule) {
        this.molecule = molecule;
    }

    public String getSmiles() {
        return smiles;
    }

    public void setSmiles(String smiles) {
        this.smiles = smiles;
    }

    public ArrayList<IAtomContainer> getSugarMoietiesRemoved() {
        return sugarMoietiesRemoved;
    }

    public void setSugarMoietiesRemoved(ArrayList<IAtomContainer> sugarMoietiesRemoved) {
        this.sugarMoietiesRemoved = sugarMoietiesRemoved;
    }

    public ArrayList<IAtomContainer> getDeglycosylatedMoieties() {
        return deglycosylatedMoieties;
    }

    public void setDeglycosylatedMoieties(ArrayList<IAtomContainer> deglycosylatedMoieties) {
        this.deglycosylatedMoieties = deglycosylatedMoieties;
    }

    public String getSubmittedDataType() {
        return submittedDataType;
    }

    public void setSubmittedDataType(String submittedDataType) {
        this.submittedDataType = submittedDataType;
    }

    public ArrayList<String> getSugarMoietiesRemovedSmiles() {
        return sugarMoietiesRemovedSmiles;
    }

    public void setSugarMoietiesRemovedSmiles(ArrayList<String> sugarMoietiesRemovedSmiles) {
        this.sugarMoietiesRemovedSmiles = sugarMoietiesRemovedSmiles;
    }

    public ArrayList<String> getDeglycosylatedMoietiesSmiles() {
        return deglycosylatedMoietiesSmiles;
    }

    public void setDeglycosylatedMoietiesSmiles(ArrayList<String> deglycosylatedMoietiesSmiles) {
        this.deglycosylatedMoietiesSmiles = deglycosylatedMoietiesSmiles;
    }
}
