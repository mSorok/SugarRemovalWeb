package de.unijena.cheminf.sugarremovalweb.controller;

import de.unijena.cheminf.sugarremovalweb.misc.SessionCleaner;
import de.unijena.cheminf.sugarremovalweb.model.ProcessedMolecule;
import de.unijena.cheminf.sugarremovalweb.model.SubmittedMoleculeData;
import de.unijena.cheminf.sugarremovalweb.readers.ReaderService;
import de.unijena.cheminf.sugarremovalweb.readers.UserInputMoleculeReaderService;
import de.unijena.cheminf.sugarremovalweb.storage.StorageFileNotFoundException;
import de.unijena.cheminf.sugarremovalweb.storage.StorageService;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author mSorok
 */
@RestController
@RequestMapping("molecule")
public class SugarRemovalController {

    @Autowired
    HttpServletRequest request;

    private final StorageService storageService;

    public ArrayList<String> sugarRemovalParameters;


    @Autowired
    ReaderService readerService;

    @Autowired
    SessionCleaner sessionCleaner;


    @Autowired
    UserInputMoleculeReaderService userInputMoleculeReaderService;

    @Autowired
    public SugarRemovalController(StorageService storageService) {
        this.storageService = storageService;
    }


    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ArrayList<ProcessedMolecule>> checkMoleculeAndParameters(){

        ArrayList<ProcessedMolecule> processedMolecules = new ArrayList<>();
        //TODO add molecules to the list

        ResponseEntity<ArrayList<ProcessedMolecule>> re = new ResponseEntity(processedMolecules, HttpStatus.OK );
        return re;
    }



    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ArrayList<ProcessedMolecule>> catchMoleculeAndParameters(@RequestBody SubmittedMoleculeData submittedMoleculeData){


        if (submittedMoleculeData.getSubmittedDataType().equals("smiles")){
            System.out.println("process smiles");


        } else if (submittedMoleculeData.getSubmittedDataType().equals("draw")){
            System.out.println("process draw");

        }

        System.out.println(submittedMoleculeData.getDataString());
        System.out.println(submittedMoleculeData.getSugarsToRemove());

        ProcessedMolecule processedMolecule = processReceivedMolecule(submittedMoleculeData.getDataString(), submittedMoleculeData.getSugarsToRemove());




        //TODO process molecule:


        ArrayList<ProcessedMolecule> processedMolecules = new ArrayList<>();
        //TODO add molecules to the list if more than one

        ResponseEntity<ArrayList<ProcessedMolecule>> re = new ResponseEntity(processedMolecules, HttpStatus.OK );
        return re;
    }



    @PostMapping(consumes = { "multipart/form-data" })
    public void catchUploadedFileAndParameters(@RequestPart("submittedMoleculeData") SubmittedMoleculeData submittedMoleculeData,
                       @RequestPart("file") MultipartFile file) {

        if(!file.isEmpty()) {
            storageService.store(file);
            String loadedFile = "upload-dir/" + file.getOriginalFilename();
            System.out.println(loadedFile);
        }

    }



    @PutMapping
    public String updateMoleculeAndParameters(){
        return "update function called";
    }

    @DeleteMapping
    public String deleteMoleculeAndParameters(){
        return "deleted molecules called";
    }




    public ProcessedMolecule processReceivedMolecule(String smiles, ArrayList<String> sugarsToRemove){
        ProcessedMolecule processedMolecule = new ProcessedMolecule();
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        IAtomContainer molecule = null;
        try {
            molecule = sp.parseSmiles(smiles);
            processedMolecule.setMolecule(molecule);

            //TODO remove sugars as in sugarsToRemove


        }catch(CDKException e){
            e.printStackTrace();
        }
        return processedMolecule;
    }

/******** FILE HANDLING ********/




        /**
         * Handles file not found and file not uploaded situations
         * @param exc
         * @return
         */
    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    /**
     *
     * @param filename
     * @return
     *
     * from page / when files have been submitted, serves the files (loads)
     */
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }





    /*

    @PostMapping(value="/sugarParameters", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public String setSugarRemovalParameters(@RequestBody  String paramString){

        sugarRemovalParameters = new ArrayList<>();
        String [] splittedParams = paramString.split("zzz");
        for(String p : splittedParams){
            sugarRemovalParameters.add(p);
        }

        System.out.println(sugarRemovalParameters);

        return "redirect:/";
    }


    @PostMapping(value="/smiles", consumes = {MediaType.TEXT_PLAIN_VALUE} )
    public String readMoleculeFromSMILES(@RequestBody String smiles) {

        try {

            smiles = smiles.replace("\n", "").replace("\r", "");


            smiles = smiles.split("smiles=")[1];


            boolean acceptMolecule = userInputMoleculeReaderService.verifySMILES(smiles);

            if(acceptMolecule){

                String smifile = userInputMoleculeReaderService.transformToSMI(smiles);

                //TODO
                //create new ProcessedMolecule object with all the parameters

            }
            else{
                //TODO NO SMILES - return error
                // make the divs visible!
                //in the inxex, for every div, if th: someting is not null - show, otherwise, hide
            }


            return "redirect:/results";


        } catch (ArrayIndexOutOfBoundsException exception) {
            return "redirect:/";
        }



    }


    *//**
     *
     * @param mol String
     * @param redirectAttributes
     * @return
     *//*
    @PostMapping(value="/drawing", consumes = {MediaType.APPLICATION_JSON_VALUE} )
    public String readMoleculeFromSketcher(@RequestBody String mol, RedirectAttributes redirectAttributes) {
        System.out.println("in drawn function");

        System.out.println(mol);
        //do things on molecule
        //save as molfile

        String molfile = userInputMoleculeReaderService.transformToMOL(mol);


        boolean acceptMolecule = true; //accept molecule function to write

        return "";
    }




    @GetMapping("/results")
    public String showResults(Model model){


        return "results";


    }


            */






}
