package de.unijena.cheminf.sugarremovalweb.readers;


import org.openscience.cdk.interfaces.IAtomContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

@Service
public class ReaderService {



    public ArrayList<IAtomContainer> readMolecules;

    public File molecularFile;

    private String submittedFileFormat ;



    public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException
    {
        File convFile = new File( multipart.getOriginalFilename());
        multipart.transferTo(convFile);
        return convFile;
    }




    public boolean startService(String file){
        this.molecularFile = new File(file);

        System.out.println("\n\n Working on: "+this.molecularFile.getAbsolutePath() + "\n\n");

        boolean acceptFileFormat = acceptFile(molecularFile.getName());

        if(acceptFileFormat){
            return true;
        }
        else{
            return false;
        }
    }





    private boolean acceptFile(String filename) {
        filename = filename.toLowerCase();
        if (filename.endsWith("sdf") || filename.toLowerCase().contains("sdf".toLowerCase())) {
            this.submittedFileFormat="sdf";
            return true;
        } else if (filename.endsWith("smi")  ||
                filename.toLowerCase().contains("smi".toLowerCase()) ||
                filename.toLowerCase().contains("smiles".toLowerCase()) ||
                filename.toLowerCase().contains("smile".toLowerCase())) {
            this.submittedFileFormat="smi";
            return true;
        } else if (filename.endsWith("json")) {
            return false;
        }
        else if (filename.endsWith("mol")  ||
                filename.toLowerCase().contains("mol".toLowerCase())
                || filename.toLowerCase().contains("molfile".toLowerCase())) {
            this.submittedFileFormat="mol";
            return true;
        }


        return false;
    }


    public void doWorkWithFile(){

        IReader reader = null;
        if(this.submittedFileFormat.equals("mol")){
            reader = new MOLReader();
        }
        else if(this.submittedFileFormat.equals("sdf")){
            reader = new SDFReader();
        }
        else if(this.submittedFileFormat.equals("smi")){
            reader = new SMILESReader();
        }

        this.readMolecules = reader.readMoleculesFromFile(this.molecularFile);

    }



    public ArrayList<IAtomContainer> getReadMolecules(){return readMolecules; };
}
