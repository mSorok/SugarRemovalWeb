package de.unijena.cheminf.sugarremovalweb.readers;


import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

public interface IReader {

    ArrayList<IAtomContainer> readMoleculesFromFile(File file);
}
