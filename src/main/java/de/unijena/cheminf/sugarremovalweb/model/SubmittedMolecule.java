package de.unijena.cheminf.sugarremovalweb.model;

import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;

public class SubmittedMolecule {

    ArrayList<String> sugarsToRemove;

    IAtomContainer molecule;

    String smiles;

    ArrayList<IAtomContainer> sugarMoietiesRemoved;

    ArrayList<IAtomContainer> deglycosylatedMoieties;

    String submittedDataType;

}
