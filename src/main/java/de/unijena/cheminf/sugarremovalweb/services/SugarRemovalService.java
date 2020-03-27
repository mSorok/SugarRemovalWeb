package de.unijena.cheminf.sugarremovalweb.services;

import de.unijena.cheminf.sugarremovalweb.misc.MoleculeConnectivityChecker;
import de.unijena.cheminf.sugarremovalweb.model.ProcessedMolecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.DfPattern;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SugarRemovalService {


    public List<IAtomContainer> linearSugars;
    public List<IAtomContainer> ringSugars;
    public List<DfPattern> patternListLinearSugars;

    UniversalIsomorphismTester universalIsomorphismTester = new UniversalIsomorphismTester();
    MoleculeConnectivityChecker mcc;


    public static final String[] LINEAR_SUGARS_SMILES = {
            "C(C(C(C(C(C=O)O)O)O)O)O",
            "C(C(CC(C(CO)O)O)O)(O)=O",
            "C(C(C(CC(=O)O)O)O)O",
            "C(C(C(C(C(CO)O)O)O)=O)O",
            "C(C(C(C(C(CO)O)O)O)O)O",
            "C(C(C(C(CC=O)O)O)O)O",
            "OCC(O)C(O)C(O)C(O)CO",
            "O=CC(O)C(O)C(O)C(O)CO",
            "CCCCC(O)C(=O)O", //TODO: Is this a sugar?
            "CC(=O)CC(=O)CCC(=O)O", //TODO: Is this a sugar?
            "O=C(O)CC(O)CC(=O)O", //TODO: Is this a sugar?
            "O=C(O)C(=O)C(=O)C(O)C(O)CO",
            "O=C(O)CCC(O)C(=O)O", //TODO: Is this a sugar?
            "O=CC(O)C(O)C(O)C(O)CO",
            "O=C(CO)C(O)C(O)CO"};

    public static final String [] RING_SUGARS_SMILES = {
            "C1CCOC1",
            "C1CCOCC1",
            "C1CCCOCC1"};






    public ArrayList<ProcessedMolecule> doWork(ArrayList<ProcessedMolecule> processedMolecules){


        for(ProcessedMolecule molecule : processedMolecules){
            //TODO detect sugar removal type
            //TODO remove sugars
        }


        return processedMolecules;
    }
}
