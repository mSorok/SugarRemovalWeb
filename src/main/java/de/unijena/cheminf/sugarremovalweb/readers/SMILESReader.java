package de.unijena.cheminf.sugarremovalweb.readers;

import de.unijena.cheminf.sugarremovalweb.misc.BeanUtil;
import de.unijena.cheminf.sugarremovalweb.misc.MoleculeChecker;
import net.sf.jniinchi.INCHI_OPTION;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

/**
 * @author mSorok
 * Reads SMILES files, processes the molecules and inserts in database
 */
public class SMILESReader implements IReader {
    ArrayList<IAtomContainer> molecules;
    MoleculeChecker moleculeChecker;

    public SMILESReader(){
        this.molecules = new ArrayList<>();
        moleculeChecker = BeanUtil.getBean(MoleculeChecker.class);
    }

    @Override
    public ArrayList<IAtomContainer> readMoleculesFromFile(File file) {
        int count = 1;
        String line;
        MolecularFormulaManipulator mfm = new MolecularFormulaManipulator();
        try {
            LineNumberReader smilesReader = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
            System.out.println("SMILES reader creation");

            while ((line = smilesReader.readLine()) != null  && count <= 1000) {
                String smiles_names = line;
                if(!line.contains("smiles")) {
                    try {

                        String smiles;
                        String id;

                        if (line.contains("\\s")) {
                            String[] splitted = smiles_names.split("\\s+"); //splitting the canonical smiles format: SMILES \s mol name
                            smiles =splitted[0];
                            id = splitted[1];
                        }
                        else{
                            smiles = line;
                            smiles = smiles.replace("\n", "");
                            id="";
                        }
                        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

                        IAtomContainer molecule = null;
                        try {
                            molecule = sp.parseSmiles(smiles);
                            molecule.setProperty("MOL_NUMBER_IN_FILE", file.getName()+" " + Integer.toString(count));
                            molecule.setProperty("ID", id);
                            molecule.setID(id);
                            molecule.setProperty("FILE_ORIGIN", file.getName().replace(".smi", ""));
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                            LocalDate localDate = LocalDate.now();
                            molecule.setProperty("ACQUISITION_DATE", dtf.format(localDate));

                            // ID workaround
                            if (molecule.getID() == "" || molecule.getID() == null) {
                                for (Object p : molecule.getProperties().keySet()) {
                                    if (p.toString().toLowerCase().contains("id")) {
                                        molecule.setID(molecule.getProperty(p.toString()));
                                    }
                                }
                                if (molecule.getID() == "" || molecule.getID() == null) {
                                    //molecule.setID("nb"+molecule.getProperty("MOL_NUMBER_IN_FILE"));
                                    UUID uidlong = UUID.randomUUID();
                                    id= uidlong.toString().substring(0,3).toUpperCase()+"-(" + mfm.getString(mfm.getMolecularFormula(molecule) )+")";
                                    molecule.setID(id);
                                }
                            }
                            molecule = moleculeChecker.checkMolecule(molecule);
                            if(molecule != null) {
                                try {
                                    List options = new ArrayList();
                                    options.add(INCHI_OPTION.SNon);
                                    options.add(INCHI_OPTION.ChiralFlagOFF);
                                    options.add(INCHI_OPTION.AuxNone);
                                    InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule, options );
                                    molecule.setProperty("INCHIKEY", gen.getInchiKey());
                                } catch (CDKException e) {
                                    Integer totalBonds = molecule.getBondCount();
                                    Integer ib = 0;
                                    while (ib < totalBonds) {
                                        IBond b = molecule.getBond(ib);
                                        if (b.getOrder() == IBond.Order.UNSET) {
                                            b.setOrder(IBond.Order.SINGLE);
                                        }
                                        ib++;
                                    }
                                    List options = new ArrayList();
                                    options.add(INCHI_OPTION.SNon);
                                    options.add(INCHI_OPTION.ChiralFlagOFF);
                                    options.add(INCHI_OPTION.AuxNone);
                                    InChIGenerator gen = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule, options );
                                    molecule.setProperty("INCHIKEY", gen.getInchiKey());



                                }
                                this.molecules.add(molecule);
                            }

                        } catch (InvalidSmilesException e) {
                            e.printStackTrace();
                            smilesReader.skip(count - 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    count++;
                }
            }
            smilesReader.close();
        } catch (IOException ex) {
            System.out.println("Oops ! File not found. Please check if the -in file or -out directory is correct");
            ex.printStackTrace();
        }
        System.out.println("Number of molecules in file : "+this.molecules.size());
        return this.molecules;
    }
}
