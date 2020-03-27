
var sugarRemovalParams = [];

var selectedInputType = null;


var submittedMoleculeData = {
    "sugarsToRemove" : [],
    "submittedDataType": "",
    "dataString":""
}

function fillSugarRemovalParams(obj){
    var cbs = document.getElementsByClassName("cbSugarType");
    if(cbs[0].checked==true){
        sugarRemovalParams.push("allSugars");
    }
    if(cbs[1].checked==true){
        sugarRemovalParams.push("ringSugars");
    }
    if(cbs[2].checked==true){
        sugarRemovalParams.push("linearSugars");
    }
    if(cbs[3].checked==true){
        sugarRemovalParams.push("terminalRingSugars");
    }

    console.log(sugarRemovalParams);

    var x = document.getElementById("step2");
    var y = document.getElementById("chooseSugarRemovalTypeFirst");


    if(sugarRemovalParams.length>0) {

        if (x.style.display === "none") {
            $(x).slideDown();
            y.style.display = "none";

            var offset = $(x).offset();
            offset.top -= 20;
            $('html, body').animate({
                scrollTop: offset.top,
            });
        }
        sugarRemovalParams = [];

    }else{
        $(x).slideUp();
        $(y).slideDown();

        var offset = $(y).offset();
        offset.top -= 20;
        $('html, body').animate({
            scrollTop: offset.top,
        });
    }
}





/* Control sugar removal type CheckBoxes */
function uncheckPickingBoxes(obj) {
    sugarRemovalParams = [];
    var cbs = document.getElementsByClassName("cbSugarType");

    if(cbs[0].checked ==true){
        for (var i = 1; i < cbs.length; i++) {
            cbs[i].checked = false;
        }
    }

}

function uncheckAllCheckbox(obj) {
    sugarRemovalParams = [];
    var cbs = document.getElementsByClassName("cbSugarType");
    cbs[0].checked = false;



}


function uncheckAllAndTerminalRingCheckbox(obj){
    sugarRemovalParams = [];
    var cbs = document.getElementsByClassName("cbSugarType");
    cbs[0].checked = false;
    cbs[3].checked = false;

}

function unckeckAllAndRingCheckbox(obj){
    sugarRemovalParams = [];
    var cbs = document.getElementsByClassName("cbSugarType");
    cbs[0].checked = false;
    cbs[1].checked = false;

}


function showFileInputDiv(obj){
    /*hide all others*/
    $(document.getElementById("smilesSubmission")).slideUp();
    $(document.getElementById("oclContainer")).slideUp();

    /* show this one*/
    $(document.getElementById("submitFile")).slideToggle();

    var offset = $(document.getElementById("submitFile")).offset();
    offset.top -= 20;
    $('html, body').animate({
        scrollTop: offset.top,
    });

    selectedInputType = "file";
    submittedMoleculeData.submittedDataType = "file";
    console.log(submittedMoleculeData);
}

function showSmilesPasteDiv(obj){
    /*hide all others*/
    $(document.getElementById("submitFile")).slideUp();
    $(document.getElementById("oclContainer")).slideUp();

    /* show this one*/
    $(document.getElementById("smilesSubmission")).slideToggle();

    var offset = $(document.getElementById("smilesSubmission")).offset();
    offset.top -= 20;
    $('html, body').animate({
        scrollTop: offset.top,
    });

    selectedInputType = "smiles";
    submittedMoleculeData.submittedDataType = "smiles";;
    console.log(submittedMoleculeData);

}

function showDrawMoleculeDiv(obj){

    /*hide all others*/
    $(document.getElementById("submitFile")).slideUp();
    $(document.getElementById("smilesSubmission")).slideUp();

    /* show this one*/
    $(document.getElementById("oclContainer")).slideToggle();

    var offset = $(document.getElementById("oclContainer")).offset();
    offset.top -= 20;
    $('html, body').animate({
        scrollTop: offset.top,
    });

    selectedInputType = "draw";
    submittedMoleculeData.submittedDataType = "draw";;
    console.log(submittedMoleculeData);

}



$('.input-choice-button').click(function(){
    if($(this).hasClass('active')){
        $(this).removeClass('active')
    } else {
        $(this).addClass('active')
    }
});


function submitSMILES(obj){
    console.log("pressed smiles button");
    console.log(submittedMoleculeData);
    var smiles = document.getElementById("smilesTextArea").value;
    submittedMoleculeData.dataString = smiles;
    submittedMoleculeData.sugarsToRemove = sugarRemovalParams;
    console.log(submittedMoleculeData);


    if(smiles.trim().match(/^([^J][A-Za-z0-9@+\-\[\]\(\)\\=#$]+)$/ig) && smiles !="" && smiles !=" "){

        $(document.getElementById("errorDivSmiles")).slideUp();

        var settings = {
            "url": "/molecule",
            "method": "POST",
            "timeout": 0,
            "headers": {
                "Content-Type": "application/json"
            },
            "data": JSON.stringify(submittedMoleculeData),

            success: function (processedMolecules) {


                var resultsTableDiv = document.getElementById("resultList");
                resultsTableDiv.innerHTML = fillResultsTable(processedMolecules);

                $(document).ready( function () {
                    $('#filledTable').DataTable(
                        {
                            dom: '<"top"if>rt<"bottom"Bp>',
                            buttons: [
                                'csv', 'copy'
                            ]
                        }

                    );
                } );

                $(document.getElementById("resultList")).slideDown();

                var offset = $(document.getElementById("resultList")).offset();
                offset.top -= 20;
                $('html, body').animate({
                    scrollTop: offset.top,
                });

                console.log("SUCCESS : ", processedMolecules[0].smiles);

            },
        };


        $.ajax(
            settings
        ).done(function (response) {
            console.log(response);
        });
    }else{
        $(document.getElementById("errorDivSmiles")).slideDown();
    }



};




function submitDraw(obj) {

    var drawnMolecule = editor.getSmiles();
    submittedMoleculeData.dataString = drawnMolecule;
    submittedMoleculeData.sugarsToRemove = sugarRemovalParams;

    if (drawnMolecule !="" ) {
        $(document.getElementById("errorDivDraw")).slideUp();
        var settings = {
            "url": "/molecule",
            "method": "POST",
            "timeout": 0,
            "headers": {
                "Content-Type": "application/json"
            },
            "data": JSON.stringify(submittedMoleculeData),
            success: function (processedMolecules) {

                var resultsTableDiv = document.getElementById("resultList");
                resultsTableDiv.innerHTML = fillResultsTable(processedMolecules);

                $(document).ready( function () {
                    $('#filledTable').DataTable(
                        {
                            dom: '<"top"if>rt<"bottom"Bp>',
                            buttons: [
                                'csv', 'copy'
                            ]
                        }

                    );
                } );

                $(document.getElementById("resultList")).slideDown();
                var offset = $(document.getElementById("resultList")).offset();
                offset.top -= 20;
                $('html, body').animate({
                    scrollTop: offset.top,
                });

                console.log("SUCCESS : ", processedMolecules);

            },
        };
        $.ajax(settings).done(function (response) {
            console.log(response);
        });


    } else {
        $(document.getElementById("errorDivDraw")).slideDown();
    }


};

function cleanOCLEditor(obj){
    console.log("trying to clean editor");
    editor.setSmiles('C');
}






function submitFile(){

    var fileData = new FormData();

    fileData.append("file", document.forms["fileUpload"].file.files[0]);
    fileData.append('submittedMoleculeData', new Blob([JSON.stringify(submittedMoleculeData)], {
        type: "application/json"
    }));


    var fileName = document.forms["fileUpload"].file.files[0].name;
    console.log(fileName);

    if(fileName.toLowerCase().endsWith("sdf") || fileName.toLowerCase().endsWith("mol") || fileName.toLowerCase().endsWith("smi")
    || fileName.toLowerCase().endsWith("smiles")){
        fetch('/molecule', {
            method: 'post',
            body: fileData
        }).then(function(response){
            if (response.status !== 200) {
                $(document.getElementById("errorDivFile")).slideDown();
            }else if(response.status==200){
                var processedMolecules = response;
                var json = JSON.stringify(processedMolecules);
                $('#resultList').html("<h2>"+json+"</h2>");
                console.log("SUCCESS : ", processedMolecules);
            }
        }).catch(function(err) {
            $(document.getElementById("errorDivFile")).slideDown();
        });

    }else{
        $(document.getElementById("errorDivFileFormat")).slideDown();
    }


}






function drawMoleculeBySmiles(smiles) {
    var molecule = OCL.Molecule.fromSmiles(smiles);

    var docW = $(document).width();
    return OCL.SVGRenderer.renderMolecule(molecule.getIDCode(), docW/8, docW/8);
}


function fillResultsTable(processedMolecules){
    //processed Molecules is a list of objects processedMolecule

    var htmlText;

    // tableheader
    htmlText = "<table id='filledTable' class='display'>";
    htmlText+= "<thead><tr><th>Submitted molecule</th><th>Structure</th><th>Deglycosylated moieties</th><th>Removed sugars</th></tr></thead>";

    //table body
    htmlText+="<tbody>";

    for(var i = 0; i< processedMolecules.length; i++){
        htmlText += '<tr><td>' + processedMolecules[i].smiles + '</td><td style="text-align:center;"><svg style="text-align:center;" xmlns="http://www.w3.org/2000/svg" >'+drawMoleculeBySmiles(processedMolecules[i].smiles)+"<svg/></td>" +
            "<td>"+processedMolecules[i].deglycosylatedMoietiesSmiles+"</td><td>"+processedMolecules[i].sugarMoietiesRemovedSmiles+"</td></tr>";
    }
    htmlText += "</tbody>";
    htmlText += "</table>";


    return htmlText;
}





