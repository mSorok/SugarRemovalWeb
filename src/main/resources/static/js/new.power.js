
var sugarRemovalParams = [];

var selectedInputType = null;

var editor;


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

    var withGlyBond = document.getElementsByClassName("classWithGlyBonds");
    if(withGlyBond.checked == true){
        sugarRemovalParams.push("withGlyBonds");
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
        $(document.getElementById("glyBonds2")).slideUp();
        $(document.getElementById("glyBonds3")).slideUp();
        document.getElementById("cbWithGlyBonds2").checked = false;
        document.getElementById("cbWithGlyBonds3").checked = false;

        $(document.getElementById("glyBonds1")).slideDown();
    }else{
        $(document.getElementById("glyBonds1")).slideUp();
        document.getElementById("cbWithGlyBonds1").checked = false;
    }
}

function uncheckAllAndTerminalRingCheckbox(obj){
    sugarRemovalParams = [];
    var cbs = document.getElementsByClassName("cbSugarType");
    cbs[0].checked = false; //all
    cbs[2].checked = false; //terminal ring sugars

    $(document.getElementById("glyBonds1")).slideUp();
    $(document.getElementById("glyBonds3")).slideUp();
    document.getElementById("cbWithGlyBonds1").checked = false;
    document.getElementById("cbWithGlyBonds3").checked = false;

    if(cbs[1].checked == true){
        $(document.getElementById("glyBonds2")).slideDown();
    }else{
        $(document.getElementById("glyBonds2")).slideUp();
        document.getElementById("cbWithGlyBonds2").checked = false;
    }

}


function unckeckAllAndRingCheckbox(obj){
    sugarRemovalParams = [];
    var cbs = document.getElementsByClassName("cbSugarType");
    cbs[0].checked = false;
    cbs[1].checked = false;

    $(document.getElementById("glyBonds1")).slideUp();
    $(document.getElementById("glyBonds2")).slideUp();
    document.getElementById("cbWithGlyBonds1").checked = false;
    document.getElementById("cbWithGlyBonds2").checked = false;

    if(cbs[2].checked == true){
        $(document.getElementById("glyBonds3")).slideDown();
    }else{
        $(document.getElementById("glyBonds3")).slideUp();
        document.getElementById("cbWithGlyBonds3").checked = false;
    }

}

function uncheckAllAndTerminalLinearCheckbox(obj){
    sugarRemovalParams = [];
    var cbs = document.getElementsByClassName("cbSugarType");
    cbs[0].checked = false;
    cbs[4].checked = false;

    if(cbs[1].checked == false && cbs[2].checked==false){
        $(document.getElementById("glyBonds2")).slideUp();
        $(document.getElementById("glyBonds3")).slideUp();
        document.getElementById("cbWithGlyBonds2").checked = false;
        document.getElementById("cbWithGlyBonds3").checked = false;
    }
}

function uncheckAllAndLinearCheckbox(obj){
    sugarRemovalParams = [];
    var cbs = document.getElementsByClassName("cbSugarType");
    cbs[0].checked = false;
    cbs[3].checked = false;
    if(cbs[1].checked == false && cbs[2].checked==false){
        $(document.getElementById("glyBonds2")).slideUp();
        $(document.getElementById("glyBonds3")).slideUp();
        document.getElementById("cbWithGlyBonds2").checked = false;
        document.getElementById("cbWithGlyBonds3").checked = false;
    }
}


function checkedWithGlyBonds(obj){
    //TODO
}



function showFileInputDiv(obj){
    /*hide all others*/
    $(document.getElementById("smilesSubmission")).slideUp();
    $(document.getElementById("oclContainer")).slideUp();

    /* show this one*/
    $(document.getElementById("submitFile")).slideDown();

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
    $(document.getElementById("smilesSubmission")).slideDown();

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
    $(document.getElementById("oclContainer")).show();

    document.getElementById("superEditor").innerHTML = '<div id="editor" style="width: 500px; height:400px; border: solid; border-width:1px; position:relative;"></div>';

    editor = OCL.StructureEditor.createEditor("editor");



    var offset = $(document.getElementById("oclContainer")).offset();
    offset.top -= 20;
    $('html, body').animate({
        scrollTop: offset.top,
    });

    selectedInputType = "draw";
    submittedMoleculeData.submittedDataType = "draw";;
    console.log(submittedMoleculeData);

}


/*
$('.input-choice-button').click(function(){
    if($(this).hasClass('active')){
        $(this).removeClass('active')
    } else {
        $(this).addClass('active')
    }
});
*/


function submitSMILES(obj){
    console.log("pressed smiles button");
    console.log(submittedMoleculeData);
    var smiles = document.getElementById("smilesTextArea").value;
    submittedMoleculeData.dataString = smiles;
    submittedMoleculeData.sugarsToRemove = sugarRemovalParams;
    sugarRemovalParams = [];
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

    console.log(obj);

    var drawnMolecule = editor.getSmiles();
    submittedMoleculeData.dataString = drawnMolecule;
    submittedMoleculeData.sugarsToRemove = sugarRemovalParams;
    sugarRemovalParams = [];

    console.log(drawnMolecule);

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
                            ],
                            "columns": [
                                { "max-width": "350px"},
                                null,
                                { "max-width": "350px"},
                                null,
                                null

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

    submittedMoleculeData.sugarsToRemove = sugarRemovalParams;
    sugarRemovalParams = [];


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

                console.log(response);

                var processedMolecules = response;

                var resultsTableDiv = document.getElementById("resultList");
                resultsTableDiv.innerHTML = fillResultsTable(processedMolecules);

                $(document).ready( function () {
                    $('#filledTable').DataTable(
                        {
                            dom: '<"top"if>rt<"bottom"Bp>',
                            buttons: [
                                'csv', 'copy'
                            ],
                            "columns": [
                                { "max-width": "350px"},
                                null,
                                { "max-width": "350px"},
                                null,
                                null

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


            }
        }).catch(function(err) {
            $(document.getElementById("errorDivFile")).slideDown();
        });

    }else{
        $(document.getElementById("errorDivFileFormat")).slideDown();
    }


}






function drawMoleculeBySmiles(smiles, size) {
    var molecule = OCL.Molecule.fromSmiles(smiles);

    var docW = $(document).width();
    return OCL.SVGRenderer.renderMolecule(molecule.getIDCode(), docW/size, docW/size);
}


function fillResultsTable(processedMolecules){
    //processed Molecules is a list of objects processedMolecule

    var htmlText= '<table id="filledTable" class="display">';
    htmlText+= '<thead><tr><th>Submitted molecule</th><th>Structure</th><th>Deglycosylated moieties</th><th>Deglycosylated structures</th><th>Removed sugars</th></tr></thead>';

    //table body
    htmlText+="<tbody>";

    console.log(htmlText);
    for(var i = 0; i< processedMolecules.length; i++){

        console.log(processedMolecules[i]);
        htmlText += '<tr><td>';
        console.log(htmlText);
        htmlText += processedMolecules[i].smiles;
        console.log(htmlText);
        htmlText += '</td><td style="text-align:center;"><svg style="text-align:center;" xmlns="http://www.w3.org/2000/svg" >';
        htmlText += drawMoleculeBySmiles(processedMolecules[i].smiles, 8);
        console.log(htmlText);
        htmlText += "</svg></td>";
        console.log(htmlText);
        console.log("just before");
        console.log(htmlText);
        htmlText += "<td style='text-align:center;'>";
        console.log("just before2");
        console.log(htmlText);
        console.log(processedMolecules[i].deglycosylatedMoietiesSmiles.toString());
        htmlText =+ processedMolecules[i].deglycosylatedMoietiesSmiles.toString();
        console.log(htmlText);
        htmlText =+ "</td>";
        console.log(htmlText);

        console.log(htmlText);
        console.log("just after");
        console.log(htmlText);
        htmlText += "<td style='text-align:center;'>";
        console.log(htmlText);
        htmlText += createDeglycosylatedMoeitiesList(processedMolecules[i].deglycosylatedMoietiesSmiles, processedMolecules[i]);
        htmlText += "</td><td  style='text-align:center;'>";
        console.log(htmlText);
        htmlText += createSugarMoeitiesList(processedMolecules[i].sugarMoietiesRemovedSmiles);
        htmlText += "</td></tr>";
        console.log(htmlText);
    }
    htmlText += "</tbody>";
    htmlText += "</table>";

    console.log(htmlText);


    return htmlText;
}




function createSugarMoeitiesList(listOfSugars){

    if(listOfSugars == null){
        return "<p style='color: red;'>No sugar was removed</p>";
    }else{
        var verticalTableString="<table>";
        for(var i =0; i< listOfSugars.length;i++){
            verticalTableString += "<tr><td style='text-align:center;'>";
            verticalTableString += '<svg style="text-align:center;" xmlns="http://www.w3.org/2000/svg" >';
            verticalTableString += drawMoleculeBySmiles(listOfSugars[i],10);
            verticalTableString += "</svg>"
            verticalTableString += "</tr></td>";
        }
        verticalTableString +="</table>";
        return verticalTableString;
    }
}


function createDeglycosylatedMoeitiesList(listOfMoieties, processedMolecule){

    if(listOfMoieties == null){
        return '<svg style="text-align:center; color: red;" xmlns="http://www.w3.org/2000/svg" >' + drawMoleculeBySmiles(processedMolecule.smiles,8) + "</svg>"; //return the molecule structure itself
    }else{
        var verticalTableString="<table>";
        for(var i =0; i< listOfMoieties.length;i++){
            verticalTableString += "<tr><td style='text-align:center;'>";
            verticalTableString += '<svg style="text-align:center;" xmlns="http://www.w3.org/2000/svg" >';
            verticalTableString += drawMoleculeBySmiles(listOfMoieties[i],10);
            verticalTableString += "</svg>"
            verticalTableString += "</tr></td>";
        }
        verticalTableString +="</table>";
        return verticalTableString;

    }


}



//POUBELLE


fetch('/molecule', {
    method: 'post',
    body: fileData
}).then(function(response){
    if (response.status !== 200) {
        $('#waitimg').hide();
        $(document.getElementById("errorDivFile")).slideDown();
    }else if(response.status==200){

        $(document.getElementById("errorDivFile")).slideUp();

        console.log(response);


        var resultsTableDiv = document.getElementById("resultList");
        resultsTableDiv.innerHTML = fillResultsTable(processedMolecules);

        $(document).ready( function () {
            $('#filledTable').DataTable(
                {
                    dom: '<"top"if>rt<"bottom"Bp>',
                    buttons: [
                        'colvis',
                        {
                            extend: 'csv',
                            filename: 'removed_sugars',
                            text: 'Save results (csv)',
                            exportOptions: {
                                columns: [0, 3, 5 ]
                            },
                        },
                        {
                            extend: 'copy',
                            text: 'Copy results to clipboard',
                            exportOptions: {
                                columns: [0, 3, 5 ]
                            },
                        }
                    ],
                    "columnDefs": [
                        {
                            "targets": [ 0 ], // submitted molecule smiles
                            "visible": false
                        },
                        {
                            "targets": [ 3 ], // deglycosylated moieties smiles
                            "visible": false
                        },
                        {
                            "targets": [ 5 ], // sugar smiles
                            "visible": false
                        }

                    ],
                    "columns": [
                        { "width": "18%", "max-width":"20%"}, //submitted smiles
                        { "max-width": "20%"}, //submitted structure
                        { "max-width": "20%"}, //deglycosylated parts structure
                        { "max-width": "20%"}, //deglycosylated parts smiles
                        { "max-width": "20%"}, //sugars structures
                        { "max-width": "20%"} //sugars smiles
                    ]
                }

            );
        } );

        $(document.getElementById("resultList")).slideDown();
        $('#waitimg').hide();
        var offset = $(document.getElementById("resultList")).offset();
        offset.top -= 20;
        $('html, body').animate({
            scrollTop: offset.top,
        });

        console.log("SUCCESS : ", processedMolecules);


    }
}).catch(function(err) {
    $('#waitimg').hide();
    $(document.getElementById("errorDivFile")).slideDown();
});

