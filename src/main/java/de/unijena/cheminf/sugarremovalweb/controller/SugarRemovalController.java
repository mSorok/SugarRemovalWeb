package de.unijena.cheminf.sugarremovalweb.controller;

import de.unijena.cheminf.sugarremovalweb.misc.SessionCleaner;
import de.unijena.cheminf.sugarremovalweb.readers.ReaderService;
import de.unijena.cheminf.sugarremovalweb.readers.UserInputMoleculeReaderService;
import de.unijena.cheminf.sugarremovalweb.storage.StorageFileNotFoundException;
import de.unijena.cheminf.sugarremovalweb.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.exceptions.TemplateInputException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author mSorok
 */
@Controller
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
                //create new SubmittedMolecule object with all the parameters

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


    /**
     *
     * @param mol String
     * @param redirectAttributes
     * @return
     */
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
     * Cast multipart spring file to conventional file
     * @param multipart
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException
    {
        File convFile = new File( multipart.getOriginalFilename());
        multipart.transferTo(convFile);
        return convFile;
    }







}
