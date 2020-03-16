package de.unijena.cheminf.sugarremovalweb.readers;


import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.File;
import java.util.Hashtable;

public interface IReader {

    Hashtable<String, IAtomContainer> readMoleculesFromFile(File file);
}
