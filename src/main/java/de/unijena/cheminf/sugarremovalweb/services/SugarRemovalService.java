package de.unijena.cheminf.sugarremovalweb.services;

/**
 * @author Jonas Schaub
 * @author Maria Sorokina
 */

import de.unijena.cheminf.sugarremovalweb.misc.MoleculeConnectivityChecker;
import de.unijena.cheminf.sugarremovalweb.model.ProcessedMolecule;
import de.unijena.cheminf.sugarremovalweb.model.SubmittedMoleculeData;
import de.unijena.cheminf.sugarremovalweb.readers.ReaderService;
import net.sf.jniinchi.INCHI_OPTION;
import org.apache.tomcat.jni.Proc;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.GraphUtil;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.isomorphism.DfPattern;
import org.openscience.cdk.isomorphism.Mappings;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.ringsearch.RingSearch;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerComparator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.BondManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.junit.Assert;

import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SugarRemovalService {


    public static enum StructuresToKeepMode {

        ALL (0),
        HEAVY_ATOM_COUNT (5),
        MOLECULAR_WEIGHT (60);
        private final int defaultThreshold;

        StructuresToKeepMode(int aDefaultValue) {
            this.defaultThreshold = aDefaultValue;
        }

        public int getDefaultThreshold() {
            return this.defaultThreshold;
        }
    }



    public static final String[] LINEAR_SUGARS_SMILES = {
            "C(C(C(C(C(C=O)O)O)O)O)O", //aldohexose
            "C(C(CC(C(CO)O)O)O)(O)=O", //3-deoxyhexonic acid
            "C(C(C(CC(=O)O)O)O)O", //2-deoxypentonic acid
            "C(C(C(C(C(CO)O)O)O)=O)O", //2-ketohexose
            "C(C(C(C(C(CO)O)O)O)O)O", //hexitol
            "C(C(C(C(CC=O)O)O)O)O", //2-deoxyhexose
            "CCCCC(O)C(=O)O", //2-hydroxyhexanoic acid TODO/discuss: Is this a sugar?
            "CC(=O)CC(=O)CCC(=O)O", //4,6-dioxoheptanoic acid TODO/discuss: Is this a sugar?
            "O=C(O)CC(O)CC(=O)O", //3-hydroxypentanedioic acid TODO/discuss: Is this a sugar?
            "O=C(O)C(=O)C(=O)C(O)C(O)CO", //hexo-2,3-diulosonic acid
            "O=C(O)CCC(O)C(=O)O", //2-hydroxypentanedioic acid TODO/discuss: Is this a sugar?
            "O=C(CO)C(O)C(O)CO" //ketopentose
    };
    public static final String [] RING_SUGARS_SMILES = {
            "C1CCOC1", //tetrahydrofuran to match all 5-membered sugar rings
            "C1CCOCC1", //tetrahydropyran to match all 6-membered sugar rings
            "C1CCCOCC1" //oxepane to match all 7-membered sugar rings
    };

    public static final boolean REMOVE_LINEAR_SUGARS_IN_RING_DEFAULT = false;
    public static final boolean DETECT_GLYCOSIDIC_BOND_DEFAULT = false;
    public static final boolean REMOVE_ONLY_TERMINAL_DEFAULT = true;
    public static final boolean INCLUDE_NR_OF_ATTACHED_OXYGEN_DEFAULT = true;
    public static final double ATTACHED_OXYGENS_TO_ATOMS_IN_RING_RATIO_THRESHOLD_DEFAULT = 0.5;
    public static final boolean SET_PROPERTY_OF_SUGAR_CONTAINING_MOLECULES_DEFAULT = true;
    public static final String INDEX_PROPERTY_KEY = "SUGAR_REMOVAL_UTILITY_INDEX";
    public static final String CONTAINS_LINEAR_SUGAR_PROPERTY_KEY = "CONTAINS_LINEAR_SUGAR";
    public static final String CONTAINS_SUGAR_PROPERTY_KEY = "CONTAINS_SUGAR";
    public static final String CONTAINS_CIRCULAR_SUGAR_PROPERTY_KEY = "CONTAINS_CIRCULAR_SUGAR";


    UniversalIsomorphismTester universalIsomorphismTester ;

    @Autowired
    MoleculeConnectivityChecker mcc;

    @Autowired
    ReaderService readerService;




    private List<IAtomContainer> ringSugars;
    private List<IAtomContainer> linearSugars;

    private List<DfPattern> linearSugarPatterns;

    private StructuresToKeepMode structuresToKeepMode;
    public static final StructuresToKeepMode STRUCTURES_TO_KEEP_MODE_DEFAULT = StructuresToKeepMode.HEAVY_ATOM_COUNT;
    private int structureToKeepModeThreshold;


    private boolean detectGlycosidicBond;
    private boolean removeOnlyTerminal;
    private boolean removeLinearSugarsInRing;
    private boolean includeNrOfAttachedOxygens;
    private double attachedOxygensToAtomsInRingRatioThreshold;


    private boolean setPropertyOfSugarContainingMolecules;




    ProcessedMolecule removeSugarsFromAtomContainer(IAtomContainer  moleculeToProcess, SubmittedMoleculeData submittedMoleculeData){


        ProcessedMolecule molecule = new ProcessedMolecule();

        SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Unique);

        List options = new ArrayList();
        options.add(INCHI_OPTION.SNon);
        options.add(INCHI_OPTION.ChiralFlagOFF);
        options.add(INCHI_OPTION.AuxNone);



        if(submittedMoleculeData.getDataString() != null && submittedMoleculeData.getDataString() != "") {
            molecule.setSmiles(submittedMoleculeData.getDataString());
        }else{
            try {
                molecule.setSmiles(smilesGenerator.create(moleculeToProcess));
            } catch (CDKException e) {
                e.printStackTrace();
                return null;
            }
        }
        //molecule.setMolecule(moleculeToProcess);
        molecule.sugarsToRemove = new ArrayList<>();
        molecule.deglycosylatedMoietiesSmiles = new ArrayList<>();

        InChIGenerator gen = null;
        try {
            gen = InChIGeneratorFactory.getInstance().getInChIGenerator(moleculeToProcess, options );

            molecule.setInchikey(gen.getInchiKey());


            //Removing sugars according to params
            if(!submittedMoleculeData.getSugarsToRemove().isEmpty()) {

                if (submittedMoleculeData.getSugarsToRemove().contains("allSugars")) {//remove all the sugars
                    molecule.sugarsToRemove.add("all");

                    setRemoveLinearSugarsInRing(false);
                    setPropertyOfSugarContainingMolecules(true);

                    if (submittedMoleculeData.getSugarsToRemove().contains("allSugarsWithGlyBonds")) {
                        setDetectGlycosidicBond(true);
                        molecule.sugarsToRemove.add("withGlyBonds");
                    }
                    else{
                        setDetectGlycosidicBond(false);
                    }

                    try {
                        moleculeToProcess = removeAllSugars(moleculeToProcess, false);
                        //the molecule to process can be in several parts: need to separate them
                        List<IAtomContainer> listAC = mcc.checkConnectivity(moleculeToProcess);
                        if(listAC.size()>1){
                            for(IAtomContainer moiety : listAC){
                                molecule.deglycosylatedMoietiesSmiles.add(smilesGenerator.create(moiety));
                            }

                        }else{
                            molecule.deglycosylatedMoietiesSmiles.add(smilesGenerator.create(moleculeToProcess));
                        }


                    } catch (CloneNotSupportedException | CDKException e) {
                        e.printStackTrace();
                        return null;
                    }

                } else { //do the removal à la carte

                    if (submittedMoleculeData.getSugarsToRemove().contains("ringSugars")) {

                        molecule.sugarsToRemove.add("ring");

                        setRemoveOnlyTerminalSugars(false);

                        setRemoveLinearSugarsInRing(false);
                        setPropertyOfSugarContainingMolecules(true);

                        if (submittedMoleculeData.getSugarsToRemove().contains("ringsWithGlyBonds")) {
                            setDetectGlycosidicBond(true);
                            molecule.sugarsToRemove.add("withGlyBonds");
                        }else{
                            setDetectGlycosidicBond(false);
                        }


                        try {
                            moleculeToProcess = removeCircularSugars(moleculeToProcess, false);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                            return null;
                        }


                    }
                    if (submittedMoleculeData.getSugarsToRemove().contains("terminalRingSugars")) {

                        molecule.sugarsToRemove.add("terminalRing");

                        setRemoveOnlyTerminalSugars(true);

                        setRemoveLinearSugarsInRing(false);
                        setPropertyOfSugarContainingMolecules(true);

                        if (submittedMoleculeData.getSugarsToRemove().contains("termRingsWithGlyBonds")) {
                            molecule.sugarsToRemove.add("withGlyBonds");
                            setDetectGlycosidicBond(true);
                        }


                        try {
                            moleculeToProcess = removeCircularSugars(moleculeToProcess, false);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                            return null;
                        }

                    }
                    if (submittedMoleculeData.getSugarsToRemove().contains("linearSugars")) {
                        molecule.sugarsToRemove.add("linear");
                        setRemoveOnlyTerminalSugars(false);
                        setRemoveLinearSugarsInRing(false);
                        setPropertyOfSugarContainingMolecules(true);

                        try {
                            moleculeToProcess = removeLinearSugars(moleculeToProcess, false);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                            return null;
                        }

                    }
                    if (submittedMoleculeData.getSugarsToRemove().contains("terminalLnearSugars")) {

                        molecule.sugarsToRemove.add("terminalLinear");
                        setRemoveOnlyTerminalSugars(true);
                        setRemoveLinearSugarsInRing(false);
                        setPropertyOfSugarContainingMolecules(true);

                        try {
                            moleculeToProcess = removeLinearSugars(moleculeToProcess, false);
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }


                    //add to the list to return
                    try {
                        //the molecule to process can be in several parts: need to separate them
                        List<IAtomContainer> listAC = mcc.checkConnectivity(moleculeToProcess);

                        if(listAC.size()>1){
                            for(IAtomContainer moiety : listAC){
                                molecule.deglycosylatedMoietiesSmiles.add(smilesGenerator.create(moiety));
                            }

                        }else{
                            molecule.deglycosylatedMoietiesSmiles.add(smilesGenerator.create(moleculeToProcess));
                        }

                        //check if any sugar was removed
                        if(!molecule.getSmiles().equals(molecule.deglycosylatedMoietiesSmiles.get(0))){
                            molecule.sugarWasRemoved=true;
                        }



                    } catch (CDKException e) {
                        e.printStackTrace();
                        return null;
                    }

                }
            }else{
                return null;
            }

        } catch (CDKException e) {
            e.printStackTrace();
            return null;
        }

        //add a nice way of dealing with long smiles


        molecule.displaySmiles = molecule.smiles.replaceAll("(.{40})", "$0 ").trim();





        return molecule;
    }




    /**
     * Processes the molecules submitted as a SMILES string or a draw (also SMILES)
     * @param submittedMoleculeData
     * @return
     */
    public ArrayList<ProcessedMolecule> doWork(SubmittedMoleculeData submittedMoleculeData){

        ArrayList<ProcessedMolecule> processedMolecules = new ArrayList<>();

        prepareSugars();

        SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer moleculeToProcess = null;

        //Reading SMILES
        try {
            //remove the weird characters
            String smilesToParse = submittedMoleculeData.getDataString();
            smilesToParse = smilesToParse.replace("\"", "");
            smilesToParse = smilesToParse.replace("\'", "");
            submittedMoleculeData.setDataString(smilesToParse);
            moleculeToProcess = smilesParser.parseSmiles(submittedMoleculeData.getDataString());

            ProcessedMolecule processedMolecule = removeSugarsFromAtomContainer(moleculeToProcess, submittedMoleculeData);


            if(processedMolecule != null) {
                processedMolecules.add(processedMolecule);
            }else{
                return processedMolecules;
            }

        } catch (CDKException e) {
            e.printStackTrace();
            return processedMolecules;
        }



        return processedMolecules;
    }


    /**
     * Processes the molecules submitted as a file
     * @param submittedMoleculeData
     * @param file
     * @return
     */
    public ArrayList<ProcessedMolecule> doWork(SubmittedMoleculeData submittedMoleculeData, String file){

        prepareSugars();

        ArrayList<ProcessedMolecule> processedMolecules = new ArrayList<>();

            if(readerService.startService(file)){
                //reader can start
                readerService.doWorkWithFile();

                ArrayList<IAtomContainer> readMolecules = readerService.getReadMolecules();

                for(IAtomContainer moleculeAC : readMolecules){


                    ProcessedMolecule processedMolecule = removeSugarsFromAtomContainer(moleculeAC, submittedMoleculeData);
                    if(processedMolecule != null) {
                        processedMolecules.add(processedMolecule);
                    }
                }

            }
            else{
                return processedMolecules;
            }


        return processedMolecules;

    }



    /******************** Actual sugar removal methods *****************************/

    private void prepareSugars(){
        universalIsomorphismTester = new UniversalIsomorphismTester();

        this.linearSugars = new ArrayList<>(LINEAR_SUGARS_SMILES.length);
        this.ringSugars = new ArrayList<>(RING_SUGARS_SMILES.length);
        this.linearSugarPatterns = new ArrayList<>(LINEAR_SUGARS_SMILES.length);

        SmilesParser tmpSmilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        //adding linear sugars to list
        for (String tmpSmiles : LINEAR_SUGARS_SMILES) {
            try {
                this.linearSugars.add(tmpSmilesParser.parseSmiles(tmpSmiles));
            } catch (InvalidSmilesException anInvalidSmilesException) {
                System.out.println("Problem with linear sugar template parsing for: "+tmpSmiles);
            }
        }
        //adding ring sugars to list
        for (String tmpSmiles : RING_SUGARS_SMILES) {
            try {
                this.ringSugars.add(tmpSmilesParser.parseSmiles(tmpSmiles));
            } catch (InvalidSmilesException anInvalidSmilesException) {
                System.out.println("Problem with ring sugar template parsing for: "+tmpSmiles);
            }
        }

        //parsing linear sugars into patterns
        for(IAtomContainer tmpSugarAC : this.linearSugars){
            this.linearSugarPatterns.add(DfPattern.findSubstructure(tmpSugarAC));
        }

        this.detectGlycosidicBond = DETECT_GLYCOSIDIC_BOND_DEFAULT;
        this.removeOnlyTerminal = REMOVE_ONLY_TERMINAL_DEFAULT;
        this.structuresToKeepMode = STRUCTURES_TO_KEEP_MODE_DEFAULT;
        this.structureToKeepModeThreshold = this.structuresToKeepMode.defaultThreshold;
        this.includeNrOfAttachedOxygens = INCLUDE_NR_OF_ATTACHED_OXYGEN_DEFAULT;
        this.attachedOxygensToAtomsInRingRatioThreshold = ATTACHED_OXYGENS_TO_ATOMS_IN_RING_RATIO_THRESHOLD_DEFAULT;
        this.removeLinearSugarsInRing = REMOVE_LINEAR_SUGARS_IN_RING_DEFAULT;
        this.setPropertyOfSugarContainingMolecules = SET_PROPERTY_OF_SUGAR_CONTAINING_MOLECULES_DEFAULT;

    }


    public List<String> getLinearSugars() {
        List<String> tmpSmilesList = new ArrayList<>(this.linearSugars.size());
        SmilesGenerator tmpSmilesGen = new SmilesGenerator(SmiFlavor.Unique);
        for (IAtomContainer tmpLinearSugar : this.linearSugars) {
            String tmpSmiles = null;
            try {
                tmpSmiles = tmpSmilesGen.create(tmpLinearSugar);
            } catch (CDKException aCDKException) {
                aCDKException.printStackTrace();
            }
            if (!Objects.isNull(tmpSmiles)) {
                tmpSmilesList.add(tmpSmiles);
            }
        }
        return tmpSmilesList;
    }

    public List<String> getCircularSugars() {
        List<String> tmpSmilesList = new ArrayList<>(this.ringSugars.size());
        SmilesGenerator tmpSmilesGen = new SmilesGenerator(SmiFlavor.Unique);
        for (IAtomContainer tmpRingSugar : this.ringSugars) {
            String tmpSmiles = null;
            try {
                tmpSmiles = tmpSmilesGen.create(tmpRingSugar);
            } catch (CDKException aCDKException) {
                aCDKException.printStackTrace();
            }
            if (!Objects.isNull(tmpSmiles)) {
                tmpSmilesList.add(tmpSmiles);
            }
        }
        return tmpSmilesList;
    }

    public boolean isGlycosidicBondDetected() {
        return this.detectGlycosidicBond;
    }

    public boolean areOnlyTerminalSugarsRemoved() {
        return this.removeOnlyTerminal;
    }

    public StructuresToKeepMode getStructuresToKeepMode() {
        return this.structuresToKeepMode;
    }

    public int getStructureToKeepModeThreshold() {
        return this.structureToKeepModeThreshold;
    }

    public boolean isNrOfAttachedOxygensIncluded() {
        return this.includeNrOfAttachedOxygens;
    }

    public double getAttachedOxygensToAtomsInRingRatioThreshold() {
        return this.attachedOxygensToAtomsInRingRatioThreshold;
    }

    public boolean areLinearSugarsInRingsRemoved() {
        return this.removeLinearSugarsInRing;
    }

    public boolean isPropertyOfSugarContainingMoleculesSet() {
        return this.setPropertyOfSugarContainingMolecules;
    }

    public void clearCircularSugars() {
        this.ringSugars.clear();
    }

    public void clearLinearSugars() {
        this.linearSugars.clear();
        this.linearSugarPatterns.clear();
    }

    public void setDetectGlycosidicBond(boolean aBoolean) {
        this.detectGlycosidicBond = aBoolean;
    }

    public void setRemoveOnlyTerminalSugars(boolean aBoolean) {
        this.removeOnlyTerminal = aBoolean;
    }

    public void setIncludeNrOfAttachedOxygens(boolean aBoolean) {
        this.includeNrOfAttachedOxygens = aBoolean;
    }

    public void setRemoveLinearSugarsInRing(boolean aBoolean) {
        this.removeLinearSugarsInRing = aBoolean;
    }

    public void setPropertyOfSugarContainingMolecules(boolean aBoolean) {
        this.setPropertyOfSugarContainingMolecules = aBoolean;
    }


    public boolean hasLinearSugars(IAtomContainer aMolecule) throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        if (aMolecule.isEmpty()) {
            return false;
        }
        aMolecule = this.setIndices(aMolecule);
        List<IAtomContainer> tmpSugarCandidates = this.getLinearSugarCandidates(aMolecule);
        boolean tmpContainsSugar = !tmpSugarCandidates.isEmpty();
        if (this.setPropertyOfSugarContainingMolecules) {
            aMolecule.setProperty(CONTAINS_LINEAR_SUGAR_PROPERTY_KEY, tmpContainsSugar);
            aMolecule.setProperty(CONTAINS_SUGAR_PROPERTY_KEY, tmpContainsSugar);
        }
        return tmpContainsSugar;
    }

    public boolean hasCircularSugars(IAtomContainer aMolecule) throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        if (aMolecule.isEmpty()) {
            return false;
        }
        aMolecule = this.setIndices(aMolecule);
        List<IAtomContainer> tmpSugarCandidates = this.getCircularSugarCandidates(aMolecule);
        boolean tmpContainsSugar = !tmpSugarCandidates.isEmpty();
        if (this.setPropertyOfSugarContainingMolecules) {
            aMolecule.setProperty(CONTAINS_CIRCULAR_SUGAR_PROPERTY_KEY, tmpContainsSugar);
            aMolecule.setProperty(CONTAINS_SUGAR_PROPERTY_KEY, tmpContainsSugar);
        }
        return tmpContainsSugar;
    }


    public boolean hasSugars(IAtomContainer aMolecule) throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        if (aMolecule.isEmpty()) {
            return false;
        }
        aMolecule = this.setIndices(aMolecule);
        List<IAtomContainer> tmpCircularSugarCandidates = this.getCircularSugarCandidates(aMolecule);
        boolean tmpContainsCircularSugar = !tmpCircularSugarCandidates.isEmpty();
        List<IAtomContainer> tmpLinearSugarCandidates = this.getLinearSugarCandidates(aMolecule);
        boolean tmpContainsLinearSugar = !tmpLinearSugarCandidates.isEmpty();
        boolean tmpContainsSugar = (tmpContainsCircularSugar || tmpContainsLinearSugar);
        if (this.setPropertyOfSugarContainingMolecules) {
            aMolecule.setProperty(CONTAINS_SUGAR_PROPERTY_KEY, tmpContainsSugar);
            aMolecule.setProperty(CONTAINS_CIRCULAR_SUGAR_PROPERTY_KEY, tmpContainsCircularSugar);
            aMolecule.setProperty(CONTAINS_LINEAR_SUGAR_PROPERTY_KEY, tmpContainsLinearSugar);
        }
        return tmpContainsSugar;
    }

    public IAtomContainer removeCircularSugars(IAtomContainer aMolecule, boolean aShouldBeCloned)
            throws NullPointerException, CloneNotSupportedException, IllegalArgumentException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        if (aMolecule.isEmpty()) {
            return aMolecule;
        }
        if (this.removeOnlyTerminal) {
            boolean tmpIsConnected = ConnectivityChecker.isConnected(aMolecule);
            if (!tmpIsConnected) {
                throw new IllegalArgumentException("Only terminal sugar moieties should be removed but the given atom" +
                        "container already contains multiple unconnected structures.");
            }
        }
        IAtomContainer tmpNewMolecule;
        if (aShouldBeCloned) {
            tmpNewMolecule = aMolecule.clone();
        } else {
            tmpNewMolecule = aMolecule;
        }
        tmpNewMolecule = this.setIndices(tmpNewMolecule);
        List<IAtomContainer> tmpSugarCandidates = this.getCircularSugarCandidates(tmpNewMolecule);
        /*note: this means that there are matches of the circular sugar patterns and that they adhere to most of
        the given settings. The exception is that they might not be terminal*/
        boolean tmpContainsSugar = !tmpSugarCandidates.isEmpty();
        if (this.setPropertyOfSugarContainingMolecules) {
            tmpNewMolecule.setProperty(CONTAINS_CIRCULAR_SUGAR_PROPERTY_KEY, tmpContainsSugar);
            tmpNewMolecule.setProperty(CONTAINS_SUGAR_PROPERTY_KEY, tmpContainsSugar);
        }
        if (tmpContainsSugar) {
            tmpNewMolecule = this.removeSugarCandidates(tmpNewMolecule, tmpSugarCandidates);
            tmpNewMolecule = this.postProcessAfterRemoval(tmpNewMolecule);
        }
        //May be empty and may be unconnected, based on the settings
        return tmpNewMolecule;
    }

    public IAtomContainer removeLinearSugars(IAtomContainer aMolecule, boolean aShouldBeCloned)
            throws NullPointerException, CloneNotSupportedException, IllegalArgumentException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        if (aMolecule.isEmpty()) {
            return aMolecule;
        }
        if (this.removeOnlyTerminal) {
            boolean tmpIsConnected = ConnectivityChecker.isConnected(aMolecule);
            if (!tmpIsConnected) {
                throw new IllegalArgumentException("Only terminal sugar moieties should be removed but the given atom" +
                        "container already contains multiple unconnected structures. This makes the determination" +
                        "of terminal and non-terminal sugar moieties and therefore the sugar removal under the given " +
                        "settings impossible!");
            }
        }
        IAtomContainer tmpNewMolecule;
        if (aShouldBeCloned) {
            tmpNewMolecule = aMolecule.clone();
        } else {
            tmpNewMolecule = aMolecule;
        }
        tmpNewMolecule = this.setIndices(tmpNewMolecule);
        List<IAtomContainer> tmpSugarCandidates = this.getLinearSugarCandidates(tmpNewMolecule);
        /*note: this means that there are matches of the linear sugar patterns and that they adhere to most of
        the given settings. The exception is that they might not be terminal*/
        boolean tmpContainsSugar = !tmpSugarCandidates.isEmpty();
        if (this.setPropertyOfSugarContainingMolecules) {
            tmpNewMolecule.setProperty(CONTAINS_LINEAR_SUGAR_PROPERTY_KEY, tmpContainsSugar);
            tmpNewMolecule.setProperty(CONTAINS_SUGAR_PROPERTY_KEY, tmpContainsSugar);
        }
        if (tmpContainsSugar) {
            tmpNewMolecule = this.removeSugarCandidates(tmpNewMolecule, tmpSugarCandidates);
            tmpNewMolecule = this.postProcessAfterRemoval(tmpNewMolecule);
        }
        //May be empty and may be unconnected, based on the settings
        return tmpNewMolecule;
    }

    public IAtomContainer removeAllSugars(IAtomContainer aMolecule, boolean aShouldBeCloned)
            throws NullPointerException, CloneNotSupportedException, IllegalArgumentException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        if (aMolecule.isEmpty()) {
            return aMolecule;
        }
        if (this.removeOnlyTerminal) {
            boolean tmpIsConnected = ConnectivityChecker.isConnected(aMolecule);
            if (!tmpIsConnected) {
                throw new IllegalArgumentException("Only terminal sugar moieties should be removed but the given atom" +
                        "container already contains multiple unconnected structures. This makes the determination" +
                        "of terminal and non-terminal sugar moieties and therefore the sugar removal under the given " +
                        "settings impossible!");
            }
        }
        IAtomContainer tmpNewMolecule;
        if (aShouldBeCloned) {
            tmpNewMolecule = aMolecule.clone();
        } else {
            tmpNewMolecule = aMolecule;
        }
        tmpNewMolecule = this.setIndices(tmpNewMolecule);
        //note: this has to be done stepwise because linear and circular sugar candidates can overlap
        List<IAtomContainer> tmpCircularSugarCandidates = this.getCircularSugarCandidates(tmpNewMolecule);
        boolean tmpContainsCircularSugars = !tmpCircularSugarCandidates.isEmpty();
        if (tmpContainsCircularSugars) {
            tmpNewMolecule = this.removeSugarCandidates(tmpNewMolecule, tmpCircularSugarCandidates);
            tmpNewMolecule = this.postProcessAfterRemoval(tmpNewMolecule);
        }
        //exit here if molecule is empty after removal
        if (tmpNewMolecule.isEmpty()) {
            if (this.setPropertyOfSugarContainingMolecules) {
                tmpNewMolecule.setProperty(CONTAINS_SUGAR_PROPERTY_KEY, tmpContainsCircularSugars);
                tmpNewMolecule.setProperty(CONTAINS_CIRCULAR_SUGAR_PROPERTY_KEY, tmpContainsCircularSugars);
                tmpNewMolecule.setProperty(CONTAINS_LINEAR_SUGAR_PROPERTY_KEY, false);
            }
            return tmpNewMolecule;
        }
        //note: if only terminal sugars are removed, the atom container should not be disconnected at this point
        List<IAtomContainer> tmpLinearSugarCandidates = this.getLinearSugarCandidates(tmpNewMolecule);
        boolean tmpContainsLinearSugars = !tmpLinearSugarCandidates.isEmpty();
        if (tmpContainsLinearSugars) {
            tmpNewMolecule = this.removeSugarCandidates(tmpNewMolecule, tmpLinearSugarCandidates);
            tmpNewMolecule = this.postProcessAfterRemoval(tmpNewMolecule);
        }
        boolean tmpContainsAnyTypeOfSugars = (tmpContainsCircularSugars || tmpContainsLinearSugars);
        if (this.setPropertyOfSugarContainingMolecules) {
            tmpNewMolecule.setProperty(CONTAINS_SUGAR_PROPERTY_KEY, tmpContainsAnyTypeOfSugars);
            tmpNewMolecule.setProperty(CONTAINS_CIRCULAR_SUGAR_PROPERTY_KEY, tmpContainsCircularSugars);
            tmpNewMolecule.setProperty(CONTAINS_LINEAR_SUGAR_PROPERTY_KEY, tmpContainsLinearSugars);
        }
        //May be empty and may be unconnected, based on the settings
        return tmpNewMolecule;
    }


    public static IAtomContainer selectBiggestUnconnectedFragment(IAtomContainer aMolecule) throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        if (aMolecule.isEmpty()) {
            return aMolecule;
        }
        boolean tmpIsConnected = ConnectivityChecker.isConnected(aMolecule);
        if (tmpIsConnected) {
            return aMolecule;
        }
        Map<Object, Object> tmpProperties = aMolecule.getProperties();
        IAtomContainerSet tmpUnconnectedFragments = ConnectivityChecker.partitionIntoMolecules(aMolecule);
        IAtomContainer tmpBiggestFragment;
        if(tmpUnconnectedFragments != null && tmpUnconnectedFragments.getAtomContainerCount() >= 1) {
            tmpBiggestFragment = tmpUnconnectedFragments.getAtomContainer(0);
            int tmpBiggestFragmentHeavyAtomCount = AtomContainerManipulator.getHeavyAtoms(tmpBiggestFragment).size();
            for(IAtomContainer tmpFragment : tmpUnconnectedFragments.atomContainers()){
                int tmpFragmentHeavyAtomCount = AtomContainerManipulator.getHeavyAtoms(tmpFragment).size();
                if(tmpFragmentHeavyAtomCount > tmpBiggestFragmentHeavyAtomCount){
                    tmpBiggestFragment = tmpFragment;
                    tmpBiggestFragmentHeavyAtomCount = tmpFragmentHeavyAtomCount;
                }
            }
        } else {
            throw new NullPointerException("Could not detect the unconnected structures of the given atom container.");
        }
        tmpBiggestFragment.setProperties(tmpProperties);
        return tmpBiggestFragment;
    }

    public static IAtomContainer selectHeaviestUnconnectedFragment(IAtomContainer aMolecule) throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        if (aMolecule.isEmpty()) {
            return aMolecule;
        }
        boolean tmpIsConnected = ConnectivityChecker.isConnected(aMolecule);
        if (tmpIsConnected) {
            return aMolecule;
        }
        Map<Object, Object> tmpProperties = aMolecule.getProperties();
        IAtomContainerSet tmpUnconnectedFragments = ConnectivityChecker.partitionIntoMolecules(aMolecule);
        IAtomContainer tmpHeaviestFragment;
        if(tmpUnconnectedFragments != null && tmpUnconnectedFragments.getAtomContainerCount() >= 1) {
            tmpHeaviestFragment = tmpUnconnectedFragments.getAtomContainer(0);
            double tmpHeaviestFragmentWeight = AtomContainerManipulator.getMass(tmpHeaviestFragment);
            for(IAtomContainer tmpFragment : tmpUnconnectedFragments.atomContainers()){
                double tmpFragmentWeight = AtomContainerManipulator.getMass(tmpFragment);
                if(tmpFragmentWeight > tmpHeaviestFragmentWeight){
                    tmpHeaviestFragment = tmpFragment;
                    tmpHeaviestFragmentWeight = tmpFragmentWeight;
                }
            }
        } else {
            //if something went wrong
            return null;
        }
        tmpHeaviestFragment.setProperties(tmpProperties);
        return tmpHeaviestFragment;
    }


    public static List<IAtomContainer> partitionAndSortUnconnectedFragments(IAtomContainer aMolecule)
            throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        boolean tmpIsEmpty = aMolecule.isEmpty();
        boolean tmpIsConnected = ConnectivityChecker.isConnected(aMolecule);
        if (tmpIsConnected || tmpIsEmpty) {
            ArrayList<IAtomContainer> tmpFragmentList = new ArrayList<>(1);
            tmpFragmentList.add(aMolecule);
            return tmpFragmentList;
        }
        IAtomContainerSet tmpUnconnectedFragments = ConnectivityChecker.partitionIntoMolecules(aMolecule);
        int tmpSize = tmpUnconnectedFragments.getAtomContainerCount();
        ArrayList<IAtomContainer> tmpSortedList = new ArrayList<>(tmpSize);
        for (IAtomContainer tmpFragment : tmpUnconnectedFragments.atomContainers()) {
            tmpSortedList.add(tmpFragment);
        }
        /*Compares two IAtomContainers for order with the following criteria with decreasing priority:
            Compare atom count
            Compare molecular weight (heavy atoms only)
            Compare bond count
            Compare sum of bond orders (heavy atoms only)
        If no difference can be found with the above criteria, the IAtomContainers are considered equal.*/
        AtomContainerComparator tmpComparator = new AtomContainerComparator();
        tmpSortedList.sort(tmpComparator);
        return tmpSortedList;
    }








    private IAtomContainer setIndices(IAtomContainer aMolecule) throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        int tmpIndex = 0;
        for (IAtom tmpAtom : aMolecule.atoms()) {
            tmpAtom.setProperty(INDEX_PROPERTY_KEY, tmpIndex);
            tmpIndex++;
        }
        return aMolecule;
    }


    private boolean areAllExocyclicBondsSingle(IAtomContainer aRingToTest, IAtomContainer anOriginalMolecule)
            throws NullPointerException {
        Objects.requireNonNull(aRingToTest, "Given ring atom container is 'null'");
        Objects.requireNonNull(anOriginalMolecule, "Given atom container representing the original molecule " +
                "is 'null'");
        int tmpAtomCountInRing = aRingToTest.getAtomCount();
        int tmpArrayListInitCapacity = tmpAtomCountInRing * 2;
        List<IBond> tmpExocyclicBondsList = new ArrayList<>(tmpArrayListInitCapacity);
        Iterable<IAtom> tmpRingAtoms = aRingToTest.atoms();
        for (IAtom tmpRingAtom : tmpRingAtoms) {
            if (!anOriginalMolecule.contains(tmpRingAtom)) {
                continue;
            }
            List<IBond> tmpConnectedBondsList = anOriginalMolecule.getConnectedBondsList(tmpRingAtom);
            for (IBond tmpBond : tmpConnectedBondsList) {
                boolean tmpIsInRing = aRingToTest.contains(tmpBond);
                if (!tmpIsInRing) {
                    tmpExocyclicBondsList.add(tmpBond);
                }
            }
        }
        return (BondManipulator.getMaximumBondOrder(tmpExocyclicBondsList) == IBond.Order.SINGLE);
    }


    private boolean hasGlycosidicBond(IAtomContainer aRingToTest, IAtomContainer anOriginalMolecule)
            throws NullPointerException {
        Objects.requireNonNull(aRingToTest, "Given ring atom container is 'null'");
        Objects.requireNonNull(anOriginalMolecule, "Given atom container representing the original molecule " +
                "is 'null'");
        Iterable<IAtom> tmpRingAtoms = aRingToTest.atoms();
        boolean tmpContainsGlycosidicBond = false;
        for (IAtom tmpRingAtom : tmpRingAtoms) {
            boolean tmpBreakOuterLoop = false;
            //check to avoid exceptions
            if (!anOriginalMolecule.contains(tmpRingAtom)) {
                continue;
            }
            List<IAtom> connectedAtomsList = anOriginalMolecule.getConnectedAtomsList(tmpRingAtom);
            for (IAtom tmpAtom : connectedAtomsList) {
                boolean tmpIsInRing = aRingToTest.contains(tmpAtom);
                if (!tmpIsInRing) {
                    String tmpSymbol = tmpAtom.getSymbol();
                    boolean tmpIsOxygen = (tmpSymbol.equals("O"));
                    if (tmpIsOxygen) {
                        List<IBond> tmpConnectedBondsList = anOriginalMolecule.getConnectedBondsList(tmpAtom);
                        boolean tmpHasOnlyTwoBonds = (tmpConnectedBondsList.size() == 2);
                        boolean tmpAllBondsAreSingle =
                                (BondManipulator.getMaximumBondOrder(tmpConnectedBondsList) == IBond.Order.SINGLE);
                        boolean tmpOneBondAtomIsHydrogen = false;
                        for (IBond tmpBond : tmpConnectedBondsList) {
                            for (IAtom tmpBondAtom : tmpBond.atoms()) {
                                if (tmpBondAtom.getSymbol().equals("H")) {
                                    tmpOneBondAtomIsHydrogen = true;
                                }
                            }
                        }
                        if ((tmpHasOnlyTwoBonds && tmpAllBondsAreSingle) && !tmpOneBondAtomIsHydrogen) {
                            tmpContainsGlycosidicBond = true;
                            tmpBreakOuterLoop = true;
                            break;
                        }
                    }
                }
            }
            if (tmpBreakOuterLoop) {
                break;
            }
        }
        return tmpContainsGlycosidicBond;
    }


    private int getAttachedOxygenAtomCount(IAtomContainer aRingToTest, IAtomContainer anOriginalMolecule)
            throws NullPointerException {
        Objects.requireNonNull(aRingToTest, "Given ring atom container is 'null'");
        Objects.requireNonNull(anOriginalMolecule, "Given atom container representing the original molecule " +
                "is 'null'");
        int tmpExocyclicOxygenCounter = 0;
        Iterable<IAtom> tmpRingAtoms = aRingToTest.atoms();
        for (IAtom tmpRingAtom : tmpRingAtoms) {
            //check to avoid exceptions
            if (!anOriginalMolecule.contains(tmpRingAtom)) {
                continue;
            }
            List<IAtom> tmpConnectedAtomsList = anOriginalMolecule.getConnectedAtomsList(tmpRingAtom);
            for (IAtom tmpConnectedAtom : tmpConnectedAtomsList) {
                String tmpSymbol = tmpConnectedAtom.getSymbol();
                boolean tmpIsOxygen = tmpSymbol.equals("O");
                boolean tmpIsInRing = aRingToTest.contains(tmpConnectedAtom);
                if (tmpIsOxygen && !tmpIsInRing) {
                    tmpExocyclicOxygenCounter++;
                }
            }
        }
        return tmpExocyclicOxygenCounter;
    }



    private IAtomContainer clearTooSmallStructures(IAtomContainer aMolecule) throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        if (this.structuresToKeepMode == StructuresToKeepMode.ALL) {
            return aMolecule;
        }
        IAtomContainerSet tmpComponents = ConnectivityChecker.partitionIntoMolecules(aMolecule);
        for (int i = 0; i < tmpComponents.getAtomContainerCount(); i++) {
            IAtomContainer tmpComponent = tmpComponents.getAtomContainer(i);
            boolean tmpIsTooSmall = this.isTooSmall(tmpComponent);
            if (tmpIsTooSmall) {
                //note: careful with removing things from sets/lists while iterating over it! But here it is ok because elements
                // are not removed from the same set that is iterated
                for (IAtom tmpAtom : tmpComponent.atoms()) {
                    //check to avoid exceptions
                    if (aMolecule.contains(tmpAtom)) {
                        aMolecule.removeAtom(tmpAtom);
                    }
                }
            }
        }
        //does not return null, but the atom container might be empty!
        return aMolecule;
    }



    private boolean isTooSmall(IAtomContainer aMolecule) throws NullPointerException, UnsupportedOperationException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        boolean tmpIsTooSmall;
        if (this.structuresToKeepMode == StructuresToKeepMode.ALL) {
            tmpIsTooSmall = false;
        } else if (this.structuresToKeepMode == StructuresToKeepMode.HEAVY_ATOM_COUNT) {
            int tmpHeavyAtomCount = AtomContainerManipulator.getHeavyAtoms(aMolecule).size();
            tmpIsTooSmall = tmpHeavyAtomCount < this.structureToKeepModeThreshold;
        } else if (this.structuresToKeepMode == StructuresToKeepMode.MOLECULAR_WEIGHT) {
            double tmpMolWeight = AtomContainerManipulator.getMass(aMolecule, AtomContainerManipulator.MolWeight);
            tmpIsTooSmall = tmpMolWeight < this.structureToKeepModeThreshold;
        } else {
            throw new UnsupportedOperationException("Undefined StructuresToKeepMode setting!");
        }
        return tmpIsTooSmall;
    }


    private boolean isTerminal(IAtomContainer aSubstructure, IAtomContainer aParentMolecule)
            throws NullPointerException, IllegalArgumentException, CloneNotSupportedException {
        //<editor-fold desc="Checks">
        Objects.requireNonNull(aSubstructure, "Given substructure is 'null'.");
        Objects.requireNonNull(aParentMolecule, "Given parent molecule is 'null'.");
        boolean tmpIsParent = true;
        for (IAtom tmpAtom : aSubstructure.atoms()) {
            if (!aParentMolecule.contains(tmpAtom)) {
                tmpIsParent = false;
                break;
            }
        }
        if (!tmpIsParent) {
            throw new IllegalArgumentException("Given substructure is not part of the given parent molecule.");
        }
        boolean tmpIsUnconnected = !ConnectivityChecker.isConnected(aParentMolecule);
        if (tmpIsUnconnected) {
            throw new IllegalArgumentException("Parent molecule is already unconnected.");
        }

        boolean tmpIsTerminal;
        IAtomContainer tmpMoleculeClone = aParentMolecule.clone();
        IAtomContainer tmpSubstructureClone = aSubstructure.clone();
        HashMap<Integer, IAtom> tmpIndexToAtomMap = new HashMap<>(tmpMoleculeClone.getAtomCount() + 1, 1);
        for (IAtom tmpAtom : tmpMoleculeClone.atoms()) {
            tmpIndexToAtomMap.put(tmpAtom.getProperty(INDEX_PROPERTY_KEY), tmpAtom);
        }
        for (IAtom tmpAtom : tmpSubstructureClone.atoms()) {
            tmpMoleculeClone.removeAtom(tmpIndexToAtomMap.get(tmpAtom.getProperty(INDEX_PROPERTY_KEY)));
        }
        boolean tmpIsConnected = ConnectivityChecker.isConnected(tmpMoleculeClone);
        if (this.structuresToKeepMode == StructuresToKeepMode.ALL) {
            tmpIsTerminal = tmpIsConnected;
        } else {
            if (tmpIsConnected) {
                tmpIsTerminal = true;
            } else {
                IAtomContainerSet tmpComponents = ConnectivityChecker.partitionIntoMolecules(tmpMoleculeClone);
                for (IAtomContainer tmpComponent : tmpComponents.atomContainers()) {
                    boolean tmpIsTooSmall = this.isTooSmall(tmpComponent);
                    if (tmpIsTooSmall) {
                        tmpMoleculeClone.remove(tmpComponent);
                    }
                }
                tmpIsTerminal = ConnectivityChecker.isConnected(tmpMoleculeClone);
            }
        }
        return tmpIsTerminal;
    }



    private IAtomContainer removeSugarCandidates(IAtomContainer aMolecule, List<IAtomContainer> aCandidateList)
            throws NullPointerException, IllegalArgumentException {
        //<editor-fold desc="Checks">
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'.");
        Objects.requireNonNull(aCandidateList, "Given list is 'null'.");
        if (aCandidateList.isEmpty()) {
            return aMolecule;
        }
        for (IAtomContainer tmpSubstructure : aCandidateList) {
            boolean tmpIsParent = true;
            for (IAtom tmpAtom : tmpSubstructure.atoms()) {
                if (!aMolecule.contains(tmpAtom)) {
                    tmpIsParent = false;
                    break;
                }
            }
            if (!tmpIsParent) {
                throw new IllegalArgumentException("At least one of the possible sugar-like substructures is not " +
                        "actually part of the given molecule.");
            }
        }
        //</editor-fold>
        IAtomContainer tmpNewMolecule = aMolecule;
        List<IAtomContainer> tmpSugarCandidates = aCandidateList;
        if (this.removeOnlyTerminal) {
            //Only terminal sugars should be removed
            //but the definition of terminal depends on the set structures to keep mode!
            //decisions based on this setting are made in the respective private method
            //No unconnected structures result at the end or at an intermediate step
            boolean tmpContainsNoTerminalSugar = false;
            while (!tmpContainsNoTerminalSugar) {
                boolean tmpSomethingWasRemoved = false;
                for (int i = 0; i < tmpSugarCandidates.size(); i++) {
                    IAtomContainer tmpCandidate = tmpSugarCandidates.get(i);
                    boolean tmpIsTerminal = false;
                    try {
                        tmpIsTerminal = this.isTerminal(tmpCandidate, tmpNewMolecule);
                    } catch (CloneNotSupportedException aCloneNotSupportedException) {
                        aCloneNotSupportedException.printStackTrace();
                        throw new IllegalArgumentException("Could not clone one candidate and therefore not determine " +
                                "whether it is terminal or not.");
                    }
                    if (tmpIsTerminal) {
                        for (IAtom tmpAtom : tmpCandidate.atoms()) {
                            if (tmpNewMolecule.contains(tmpAtom)) {
                                tmpNewMolecule.removeAtom(tmpAtom);
                            }
                        }
                        //TODO: Post-process after every removal? Formerly non-terminal sugars might have only attached
                        // oxygen atoms now instead of hydroxy groups. This might be a problem!
                        tmpSugarCandidates.remove(i);
                        //The removal shifts the remaining indices!
                        i = i - 1;
                        //to clear away leftover unconnected fragments that are not to be kept due to the settings
                        tmpNewMolecule = this.clearTooSmallStructures(tmpNewMolecule);
                        //atom container may be empty after that
                        if (tmpNewMolecule.isEmpty()) {
                            tmpContainsNoTerminalSugar = true;
                            break;
                        }
                        tmpSomethingWasRemoved = true;
                    }
                }
                if (!tmpSomethingWasRemoved) {
                    tmpContainsNoTerminalSugar = true;
                }
            }
        } else {
            //all sugar moieties are removed, may result in an unconnected atom container
            for (IAtomContainer tmpSugarCandidate : tmpSugarCandidates) {
                for (IAtom tmpAtom : tmpSugarCandidate.atoms()) {
                    if (tmpNewMolecule.contains(tmpAtom)) {
                        tmpNewMolecule.removeAtom(tmpAtom);
                    }
                }
            }
        }
        //To clear away unconnected, too small structures and generate valid valences, the post-processing method must
        // be called
        return tmpNewMolecule;
    }


    private IAtomContainer postProcessAfterRemoval(IAtomContainer aMolecule) throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'");
        IAtomContainer tmpNewMolecule = aMolecule;
        //if too small / too light, unconnected structures should be discarded, this is done now
        //otherwise, the possibly unconnected atom container is returned
        //Even if only terminal sugars are removed, the resulting, connected structure may still be too small to keep!
        if (this.structuresToKeepMode != StructuresToKeepMode.ALL) {
            tmpNewMolecule = this.clearTooSmallStructures(tmpNewMolecule);
        }
        if (!tmpNewMolecule.isEmpty()) {
            try {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(tmpNewMolecule);
                CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance()).addImplicitHydrogens(tmpNewMolecule);
            } catch (CDKException aCDKException) {
                aCDKException.printStackTrace();
            }
        }
        return tmpNewMolecule;
    }

    private List<IAtomContainer> getCircularSugarCandidates(IAtomContainer aMolecule) throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'");
        IAtomContainer tmpNewMolecule = aMolecule;
        int[][] tmpAdjList = GraphUtil.toAdjList(tmpNewMolecule);
        //efficient computation/partitioning of the ring systems
        RingSearch tmpRingSearch = new RingSearch(tmpNewMolecule, tmpAdjList);
        List<IAtomContainer> tmpIsolatedRings = tmpRingSearch.isolatedRingFragments();
        List<IAtomContainer> tmpSugarCandidates = new ArrayList<>(tmpIsolatedRings.size());
        for(IAtomContainer tmpReferenceRing : this.ringSugars){
            for(IAtomContainer tmpIsolatedRing : tmpIsolatedRings){
                boolean tmpIsIsomorph = false;
                try {
                    tmpIsIsomorph = universalIsomorphismTester.isIsomorph(tmpReferenceRing, tmpIsolatedRing);
                } catch (CDKException aCDKException) {
                    aCDKException.printStackTrace();
                    continue;
                }
                if (tmpIsIsomorph) {
                    /*note: another requirement of a suspected sugar ring is that it contains only single bonds.
                     * This is not tested here because all the structures in the reference rings do meet this criterion.
                     * But a structure that does not meet this criterion could be added to the references by the user.*/
                    //do not remove rings with non-single exocyclic bonds, they are not sugars (not an option!)
                    boolean tmpAreAllExocyclicBondsSingle = this.areAllExocyclicBondsSingle(tmpIsolatedRing, tmpNewMolecule);
                    if (!tmpAreAllExocyclicBondsSingle) {
                        continue;
                    }
                    //do not remove rings without an attached glycosidic bond if this option is set
                    if (this.detectGlycosidicBond) {
                        boolean tmpHasGlycosidicBond = this.hasGlycosidicBond(tmpIsolatedRing, tmpNewMolecule);
                        if (!tmpHasGlycosidicBond) {
                            continue;
                        }
                    }
                    //do not remove rings with 'too few' attached oxygens if this option is set
                    if (this.includeNrOfAttachedOxygens) {
                        int tmpExocyclicOxygenCount = this.getAttachedOxygenAtomCount(tmpIsolatedRing, tmpNewMolecule);
                        int tmpAtomsInRing = tmpIsolatedRing.getAtomCount();
                        boolean tmpAreEnoughOxygensAttached = this.doesRingHaveEnoughOxygenAtomsAttached(tmpAtomsInRing,
                                tmpExocyclicOxygenCount);
                        if (!tmpAreEnoughOxygensAttached) {
                            continue;
                        }
                    }
                    //if sugar ring has not been excluded yet, the molecule contains sugars, although they might not
                    // be terminal
                    tmpSugarCandidates.add(tmpIsolatedRing);
                }
            }
        }
        return tmpSugarCandidates;
    }


    private List<IAtomContainer> getLinearSugarCandidates(IAtomContainer aMolecule) throws NullPointerException {
        Objects.requireNonNull(aMolecule, "Given molecule is 'null'");
        IAtomContainer tmpNewMolecule = aMolecule;
        List<IAtomContainer> tmpSugarCandidates = new ArrayList<>(aMolecule.getAtomCount() / 2);
        for(DfPattern tmpLinearSugarPattern : this.linearSugarPatterns) {
            /*unique in this case means that the same match cannot be in this collection multiple times but they can
            still overlap! Overlapping atoms are removed in the following lines.*/
            Mappings tmpMappings = tmpLinearSugarPattern.matchAll(tmpNewMolecule);
            Mappings tmpUniqueMappings = tmpMappings.uniqueAtoms();
            Iterable<IAtomContainer> tmpUniqueSubstructureMappings = tmpUniqueMappings.toSubstructures();
            for (IAtomContainer tmpMatchedStructure : tmpUniqueSubstructureMappings) {
                tmpSugarCandidates.add(tmpMatchedStructure);
            }
        }
        //TODO/discuss: Is there a better way to get non-overlapping matches?
        HashSet<Integer> tmpSugarCandidateAtomsSet = new HashSet<>(aMolecule.getAtomCount() + 2, 1);
        for (int i = 0; i < tmpSugarCandidates.size(); i++) {
            IAtomContainer tmpCandidate = tmpSugarCandidates.get(i);
            for (int j = 0; j < tmpCandidate.getAtomCount(); j++) {
                IAtom tmpAtom = tmpCandidate.getAtom(j);
                int tmpAtomIndex = tmpAtom.getProperty(INDEX_PROPERTY_KEY);
                boolean tmpIsAtomAlreadyInCandidates = tmpSugarCandidateAtomsSet.contains(tmpAtomIndex);
                if (tmpIsAtomAlreadyInCandidates) {
                    tmpCandidate.removeAtom(tmpAtom);
                    //The removal shifts the remaining indices!
                    j = j - 1;
                } else {
                    tmpSugarCandidateAtomsSet.add(tmpAtomIndex);
                }
            }
            if (tmpCandidate.isEmpty()) {
                tmpSugarCandidates.remove(tmpCandidate);
                //The removal shifts the remaining indices!
                i = i - 1;
            }
        }
        if (!this.removeLinearSugarsInRing && !tmpSugarCandidates.isEmpty()) {
            int[][] tmpAdjList = GraphUtil.toAdjList(tmpNewMolecule);
            RingSearch tmpRingSearch = new RingSearch(tmpNewMolecule, tmpAdjList);
            for (int i = 0; i < tmpSugarCandidates.size(); i++) {
                IAtomContainer tmpCandidate = tmpSugarCandidates.get(i);
                for (int j = 0; j < tmpCandidate.getAtomCount(); j++) {
                    IAtom tmpAtom = tmpCandidate.getAtom(j);
                    if (tmpRingSearch.cyclic(tmpAtom)) {
                        //  *this is the routine to remove the whole candidate*
                        tmpSugarCandidates.remove(i);
                        //removal shifts the remaining indices
                        i = i - 1;
                        break;
                        //  *this would be the routine to remove only the cyclic atoms (see also add. part below)*
                        /*if (tmpCandidate.contains(tmpAtom)) {
                            tmpCandidate.removeAtom(tmpAtom);
                            //The removal shifts the remaining indices!
                            j = j - 1;
                        }*/
                    }
                }
                //  *this would be the routine to remove only the cyclic atoms (part 2)*
                /*if (tmpCandidate.isEmpty()) {
                    tmpSugarCandidates.remove(i);
                    //The removal shifts the remaining indices!
                    i = i - 1;
                }*/
            }
        }
        return tmpSugarCandidates;
    }




    private boolean doesRingHaveEnoughOxygenAtomsAttached(int aNumberOfAtomsInRing,
                                                          int aNumberOfAttachedExocyclicOxygenAtoms) {
        if (aNumberOfAtomsInRing == 0) {
            //better than throwing an exception here?
            return false;
        }
        double tmpAttachedOxygensToAtomsInRingRatio =
                ((double) aNumberOfAttachedExocyclicOxygenAtoms / (double) aNumberOfAtomsInRing);
        boolean tmpMeetsThreshold =
                (tmpAttachedOxygensToAtomsInRingRatio >= this.attachedOxygensToAtomsInRingRatioThreshold);
        return tmpMeetsThreshold;
    }



    public void setAttachedOxygensToAtomsInRingRatioThreshold(double aDouble) throws IllegalArgumentException {
        boolean tmpIsFinite = Double.isFinite(aDouble); //false for NaN and infinity arguments
        boolean tmpIsNegative = (aDouble < 0);
        if(!tmpIsFinite || tmpIsNegative) {
            throw new IllegalArgumentException("Given double is NaN, infinite or negative.");
        }
        if ((!this.includeNrOfAttachedOxygens)) {
            throw new IllegalArgumentException("The number of attached oxygen atoms is currently not included in the " +
                    "decision making process, so a ratio threshold makes no sense.");
        }
        this.attachedOxygensToAtomsInRingRatioThreshold = aDouble;
    }


    public void setStructuresToKeepMode(StructuresToKeepMode aMode) throws NullPointerException {
        Objects.requireNonNull(aMode, "Given mode is 'null'.");
        this.structuresToKeepMode = aMode;
        this.structureToKeepModeThreshold = this.structuresToKeepMode.getDefaultThreshold();
    }

    public void setStructuresToKeepThreshold(int aThreshold) throws IllegalArgumentException {
        if ((this.structuresToKeepMode == StructuresToKeepMode.ALL)) {
            throw new IllegalArgumentException("The mode is currently set to keep all structures, so a threshold " +
                    "makes no sense.");
        }
        if (aThreshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative.");
        }
        this.structureToKeepModeThreshold = aThreshold;
    }


}
