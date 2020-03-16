package de.unijena.cheminf.sugarremovalweb.readers;

import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.File;
import java.util.Hashtable;

public class MOLReader implements IReader {
    @Override
    public Hashtable<String, IAtomContainer> readMoleculesFromFile(File file) {
        return null;
    }
}
