
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
        sugarRemovalParams.push("terminalRingSugars");
    }
    if(cbs[3].checked==true){
        sugarRemovalParams.push("linearSugars");
    }
    if(cbs[4].checked==true){
        sugarRemovalParams.push("terminalLnearSugars");
    }

    var allWithGlyBonds = document.getElementById("cbWithGlyBonds1");
    if(allWithGlyBonds.checked == true){
        sugarRemovalParams.push("allSugarsWithGlyBonds");
    }
    var ringWithGlyBonds = document.getElementById("cbWithGlyBonds2");
    if(ringWithGlyBonds.checked == true){
        sugarRemovalParams.push("ringsWithGlyBonds");
    }
    var termRingWithGlyBonds = document.getElementById("cbWithGlyBonds3");
    if(termRingWithGlyBonds.checked == true){
        sugarRemovalParams.push("termRingsWithGlyBonds");
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
function uncheckPickingBoxes(obj) { //Controls position 0
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


function uncheckAllAndTerminalRingCheckbox(obj){ //controls position 1
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


function unckeckAllAndRingCheckbox(obj){//Contols position 2
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



function uncheckAllAndTerminalLinearCheckbox(obj){//controls position 3
    sugarRemovalParams = [];
    var cbs = document.getElementsByClassName("cbSugarType");
    cbs[0].checked = false;
    cbs[4].checked = false;

    $(document.getElementById("glyBonds1")).slideUp();
    document.getElementById("cbWithGlyBonds1").checked = false;

    if(cbs[1].checked == false && cbs[2].checked==false){
        $(document.getElementById("glyBonds2")).slideUp();
        $(document.getElementById("glyBonds3")).slideUp();
        document.getElementById("cbWithGlyBonds2").checked = false;
        document.getElementById("cbWithGlyBonds3").checked = false;
    }
}

function uncheckAllAndLinearCheckbox(obj){//controls position 4
    sugarRemovalParams = [];
    var cbs = document.getElementsByClassName("cbSugarType");
    cbs[0].checked = false;
    cbs[3].checked = false;

    $(document.getElementById("glyBonds1")).slideUp();
    document.getElementById("cbWithGlyBonds1").checked = false;

    if(cbs[1].checked == false && cbs[2].checked==false){
        $(document.getElementById("glyBonds2")).slideUp();
        $(document.getElementById("glyBonds3")).slideUp();
        document.getElementById("cbWithGlyBonds2").checked = false;
        document.getElementById("cbWithGlyBonds3").checked = false;
    }
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
    console.log(submittedMoleculeData);


    if(smiles.trim().match(/^([^J][A-Za-z0-9%@+\-\[\]\(\)\\=#$]+)$/ig) && smiles !="" && smiles !=" "){

        $(document.getElementById("errorDivSmiles")).hide();
        $('#waitimg').show();

        var settings = {
            "url": "/molecule",
            "method": "POST",
            "timeout": 0,
            "headers": {
                "Content-Type": "application/json"
            },
            "data": JSON.stringify(submittedMoleculeData),

            success: function (processedMolecules) {
                $('#waitimg').hide();


                var resultsTableDiv = document.getElementById("resultList");
                resultsTableDiv.innerHTML = fillResultsTable(processedMolecules);

                //draw all molecules
                for(var i=0; i< processedMolecules.length; i++){
                    var structure = drawMoleculeFromSmiles(processedMolecules[i].smiles, processedMolecules[i].inchikey);
                    if(processedMolecules[i].deglycosylatedMoietiesSmiles == null || processedMolecules[i].deglycosylatedMoietiesSmiles.length==0){
                        drawMoleculeFromSmiles(processedMolecules[i].smiles, processedMolecules[i].inchikey+'-0');
                    }else{
                        for(var j=0; j<processedMolecules[i].deglycosylatedMoietiesSmiles.length; j++){
                            drawMoleculeFromSmiles(processedMolecules[i].deglycosylatedMoietiesSmiles[j], processedMolecules[i].inchikey+'-'+j);
                        }
                    }

                    //draw sugars
                    /*if(processedMolecules[i].sugarMoietiesRemovedSmiles != null && processedMolecules[i].sugarMoietiesRemovedSmiles.length >0) {
                        for (var j = 0; j < processedMolecules[i].sugarMoietiesRemovedSmiles.length; j++) {
                            drawMoleculeFromSmiles(processedMolecules[i].sugarMoietiesRemovedSmiles[j], processedMolecules[i].sugarMoietiesRemovedSmiles[j]);
                        }
                    }*/
                }

                $(document).ready( function () {
                    $('#filledTable').DataTable(
                        {
                            //
                            dom: '<"top"if>rt<"bottom"Bp>',


                            buttons: [
                                'colvis',
                                {
                                    extend: 'csv',
                                    filename: 'removed_sugars',
                                    text: 'Save results (csv)',
                                    exportOptions: {
                                        columns: [0, 3, 4 ]
                                    },
                                },
                                {
                                    extend: 'copy',
                                    text: 'Copy results to clipboard',
                                    exportOptions: {
                                        columns: [0, 3, 4 ]
                                    },
                                }
                            ],
                            "columnDefs": [

                                {
                                    "targets": [ 0 ], // submitted molecule smiles
                                    "visible": true
                                },
                                {
                                    "targets": [ 3 ], // deglycosylated moieties smiles
                                    "visible": false
                                },
                                {
                                    "targets": [ 4 ], // sugar was removed
                                    "visible": true,
                                }

                            ],
                            "columns": [
                                { "width": "250px"}, //submitted smiles
                                { "width": "250px"}, //submitted structure
                                { "width": "250px"}, //deglycosylated parts structure
                                { "max-width": "20%"}, //deglycosylated parts smiles
                                {"width": "75px"}
                                //{ "width": "250px"}, //sugars structures
                                //{ "max-width": "20%"} //sugars smiles
                            ]
                        }

                    );
                } );

                $(document.getElementById("resultList")).slideDown();
                $(document.getElementById("errorDivSmiles")).hide();

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
            if(response.status == 200 || response[0].smiles != null ){
                $('#waitimg').hide();
                $(document.getElementById("errorDivSmiles")).hide();
            }else{
                $('#waitimg').hide();
                $(document.getElementById("errorDivSmiles")).slideDown();

            }
        });
    }else{
        $('#waitimg').hide();
        $(document.getElementById("errorDivSmiles")).slideDown();
    }



};




function submitDraw(obj) {

    console.log(obj);

    var drawnMolecule = editor.getSmiles();
    submittedMoleculeData.dataString = drawnMolecule;
    submittedMoleculeData.sugarsToRemove = sugarRemovalParams;

    console.log(drawnMolecule);

    if (drawnMolecule !="" ) {
        $(document.getElementById("errorDivDraw")).hide();
        $('#waitimg').show();

        var settings = {
            "url": "/molecule",
            "method": "POST",
            "timeout": 0,
            "headers": {
                "Content-Type": "application/json"
            },
            "data": JSON.stringify(submittedMoleculeData),
            success: function (processedMolecules) {
                $('#waitimg').hide();

                var resultsTableDiv = document.getElementById("resultList");
                resultsTableDiv.innerHTML = fillResultsTable(processedMolecules);

                //draw all molecules
                for(var i=0; i< processedMolecules.length; i++){
                    var structure = drawMoleculeFromSmiles(processedMolecules[i].smiles, processedMolecules[i].inchikey);
                    if(processedMolecules[i].deglycosylatedMoietiesSmiles == null || processedMolecules[i].deglycosylatedMoietiesSmiles.length==0){
                        drawMoleculeFromSmiles(processedMolecules[i].smiles, processedMolecules[i].inchikey+'-0');
                    }else{
                        for(var j=0; j<processedMolecules[i].deglycosylatedMoietiesSmiles.length; j++){
                            drawMoleculeFromSmiles(processedMolecules[i].deglycosylatedMoietiesSmiles[j], processedMolecules[i].inchikey+'-'+j);
                        }
                    }

                    //draw sugars
                    /*if(processedMolecules[i].sugarMoietiesRemovedSmiles != null && processedMolecules[i].sugarMoietiesRemovedSmiles.length >0) {
                        for (var j = 0; j < processedMolecules[i].sugarMoietiesRemovedSmiles.length; j++) {
                            drawMoleculeFromSmiles(processedMolecules[i].sugarMoietiesRemovedSmiles[j], processedMolecules[i].sugarMoietiesRemovedSmiles[j]);
                        }
                    }*/
                }

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
                                        columns: [0, 3 ]
                                    },
                                },
                                {
                                    extend: 'copy',
                                    text: 'Copy results to clipboard',
                                    exportOptions: {
                                        columns: [0, 3 ]
                                    },
                                }
                            ],
                            "columnDefs": [

                                {
                                    "targets": [ 0 ], // submitted molecule smiles
                                    "visible": true
                                },
                                {
                                    "targets": [ 3 ], // deglycosylated moieties smiles
                                    "visible": false
                                },
                                {
                                    "targets": [ 4 ], // sugar was removed
                                    "visible": true,
                                }

                            ],
                            "columns": [
                                { "width": "250px"}, //submitted smiles
                                { "width": "250px"}, //submitted structure
                                { "width": "250px"}, //deglycosylated parts structure
                                { "max-width": "20%"}, //deglycosylated parts smiles
                                {"width": "75px"}
                                //{ "width": "250px"}, //sugars structures
                                //{ "max-width": "20%"} //sugars smiles
                            ]
                        }

                    );
                } );

                $(document.getElementById("resultList")).slideDown();
                $(document.getElementById("errorDivDraw")).hide();
                var offset = $(document.getElementById("resultList")).offset();
                offset.top -= 20;
                $('html, body').animate({
                    scrollTop: offset.top,
                });

                console.log("SUCCESS : ", processedMolecules);

            },
        };
        $.ajax(settings).done(function (response) {
            if(response.status == 200 || response[0].smiles != null ){
                $('#waitimg').hide();
                $(document.getElementById("errorDivDraw")).hide();
            }else{
                $('#waitimg').hide();
                $(document.getElementById("errorDivDraw")).slideDown();

            }
        });


    } else {
        $('#waitimg').hide();
        $(document.getElementById("errorDivDraw")).slideDown();
    }


};

function cleanOCLEditor(obj){
    console.log("trying to clean editor");
    editor.setSmiles('C');
}






function submitFile(){

    submittedMoleculeData.sugarsToRemove = sugarRemovalParams;

    var fileData = new FormData();

    fileData.append("file", document.forms["fileUpload"].file.files[0]);
    fileData.append('submittedMoleculeData', new Blob([JSON.stringify(submittedMoleculeData)], {
        type: "application/json"
    }));


    var fileName = document.forms["fileUpload"].file.files[0].name;
    console.log(fileName);

    if(fileName.toLowerCase().endsWith("sdf") || fileName.toLowerCase().endsWith("mol") || fileName.toLowerCase().endsWith("smi")
        || fileName.toLowerCase().endsWith("smiles")){
        $('#waitimg').show();





        fetch('/molecule', {
            method: 'post',
            body: fileData,

        }).then((resp) => resp.json()).then(function (data) {


            console.log(data);
            $(document.getElementById("errorDivFile")).hide();

            var processedMolecules = data;

            var resultsTableDiv = document.getElementById("resultList");
            resultsTableDiv.innerHTML = fillResultsTable(processedMolecules);

            //draw all molecules
            for(var i=0; i< processedMolecules.length; i++){
                var structure = drawMoleculeFromSmiles(processedMolecules[i].smiles, processedMolecules[i].inchikey);
                if(processedMolecules[i].deglycosylatedMoietiesSmiles == null || processedMolecules[i].deglycosylatedMoietiesSmiles.length==0){
                    drawMoleculeFromSmiles(processedMolecules[i].smiles, processedMolecules[i].inchikey+'-0');
                }else{
                    for(var j=0; j<processedMolecules[i].deglycosylatedMoietiesSmiles.length; j++){
                        drawMoleculeFromSmiles(processedMolecules[i].deglycosylatedMoietiesSmiles[j], processedMolecules[i].inchikey+'-'+j);
                    }
                }

                //draw sugars
                /*if(processedMolecules[i].sugarMoietiesRemovedSmiles != null && processedMolecules[i].sugarMoietiesRemovedSmiles.length >0) {
                    for (var j = 0; j < processedMolecules[i].sugarMoietiesRemovedSmiles.length; j++) {
                        drawMoleculeFromSmiles(processedMolecules[i].sugarMoietiesRemovedSmiles[j], processedMolecules[i].sugarMoietiesRemovedSmiles[j]);
                    }
                }*/

            }

            $(document).ready( function () {
                $('#waitimg').hide();

                $(document.getElementById("errorDivFile")).slideUp();


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
                                    columns: [0, 3]
                                },
                            },
                            {
                                extend: 'copy',
                                text: 'Copy results to clipboard',
                                exportOptions: {
                                    columns: [0, 3 ]
                                },
                            }
                        ],
                        "columnDefs": [

                            {
                                "targets": [ 0 ], // submitted molecule smiles
                                "visible": true
                            },
                            {
                                "targets": [ 3 ], // deglycosylated moieties smiles
                                "visible": false
                            },
                            {
                                "targets": [ 4 ], // sugar was removed
                                "visible": true,
                            }

                        ],
                        "columns": [
                            { "width": "250px"}, //submitted smiles
                            { "width": "250px"}, //submitted structure
                            { "width": "250px"}, //deglycosylated parts structure
                            { "max-width": "20%"}, //deglycosylated parts smiles
                            {"width": "75px"}
                            //{ "width": "250px"}, //sugars structures
                            //{ "max-width": "20%"} //sugars smiles
                        ]
                    }

                );
            } );

            $(document.getElementById("resultList")).slideDown();
            $(document.getElementById("errorDivFile")).hide();
            var offset = $(document.getElementById("resultList")).offset();
            offset.top -= 20;
            $('html, body').animate({
                scrollTop: offset.top,
            });

            console.log("SUCCESS : ", processedMolecules);



        }).catch(function(err) {
            $('#waitimg').hide();
            $(document.getElementById("errorDivFile")).slideDown();
        });






    }else{
        $('#waitimg').hide();
        $(document.getElementById("errorDivFileFormat")).slideDown();
    }


}




function drawMoleculeFromSmiles(smiles, inchikey) {
    var options = {
        noImplicitAtomLabelColors: true,
        noStereoProblem: true,
        suppressChiralText: true
    };

    var canvas = document.getElementById(inchikey);


    try {
        var molecule = OCL.Molecule.fromSmiles(smiles);
        OCL.StructureView.drawMolecule(canvas, molecule, options);
        var dataURL = canvas.toDataURL();

    } catch(e) {
        console.log(e.name + " in OpenChemLib: " + e.message);
    }
    return canvas;
}





function fillResultsTable(processedMolecules){
    //processed Molecules is a list of objects processedMolecule

    var htmlText;

    // tableheader
    htmlText = "<h2>Results</h2><table id='filledTable' class='display'>";
    htmlText+= "<thead><tr><th>Submitted molecule</th><th>Submitted structure</th><th>Deglycosylated moieties</th><th>Deglycosylated moieties SMILES</th><th>Had sugar</th></tr></thead>";

    //table body
    htmlText+="<tbody>";

    for(var i = 0; i< processedMolecules.length; i++){
        htmlText += '<tr><td>';//col 0
        htmlText += processedMolecules[i].displaySmiles;
        htmlText += '</td><td style="text-align:center;">';
        htmlText += "<canvas id='"+processedMolecules[i].inchikey+"' width='250' height='250'></canvas>";
        htmlText +="</td>"; //col1 over

        htmlText +="<td style='text-align:center;'>";//col2
        htmlText += createDeglycosylatedMoeitiesPics(processedMolecules[i].deglycosylatedMoietiesSmiles, processedMolecules[i]);
        htmlText += "</td>";//col2 over

        htmlText += "<td>";//col3
        htmlText += createDeglycosylatedMoeitiesList(processedMolecules[i].deglycosylatedMoietiesSmiles, processedMolecules[i]);
        htmlText += "</td>" ;//col3 over
        htmlText += "<td>";//col4
        htmlText += processedMolecules[i].sugarWasRemoved ;
        htmlText += "</td>";// col4 over
        //htmlText += "<td  style='text-align:center;'>"; //col4
        //htmlText += createSugarMoeitiesPics(processedMolecules[i].sugarMoietiesRemovedSmiles);
        //htmlText += "</td>";//col4 over
       //htmlText += "<td>"; //col5
        //htmlText += createSugarMoeitiesList(processedMolecules[i].sugarMoietiesRemovedSmiles);
        //htmlText += "</td>";
        htmlText += "</tr>";
    }

    htmlText += "</tbody>";
    htmlText += "</table>";
    htmlText += "<div style='height: 145px; float: left; width: 100%;'><p>Note that results can be saved as a csv file or copied to the clipboard.</p>";
    htmlText += "<p>The visible columns can be triggered by clicking on the 'Column visibility' button.</p></div>";


    return htmlText;
}



/*
function createSugarMoeitiesPics(listOfSugars){

    if(listOfSugars == null){
        return "<p style='color: red;'>No sugar to remove</p>";
    }else{
        var verticalTableString="<table>";
        for(var i =0; i< listOfSugars.length;i++){
            verticalTableString += "<tr><td style='text-align:center;'>";
            verticalTableString += "<canvas id='"+listOfSugars[i]+"' width='250' height='150'></canvas>";
            verticalTableString += "</tr></td>";
        }
        verticalTableString +="</table>";
        return verticalTableString;
    }
}

function createSugarMoeitiesList(listOfSugars){

    if(listOfSugars == null){
        return "<p style='color: red;'>No sugar to remove</p>";
    }else{
        var smilesList="<ul>";
        for(var i =0; i< listOfSugars.length;i++){
            smilesList += listOfSugars[i];
            smilesList += "</li>";
        }
        smilesList += "</ul>";
        return smilesList;
    }
}*/

function createDeglycosylatedMoeitiesPics(listOfMoieties, processedMolecule){

    if(listOfMoieties == null || listOfMoieties.length==0){

        var canvasText = "<canvas id='"+processedMolecule.inchikey+"-0' width='250' height='250'></canvas>";
        return canvasText;
    }else{
        var verticalTableString="<table>";
        for(var i =0; i< listOfMoieties.length;i++){
            verticalTableString += "<tr><td style='text-align:center;'>";
            verticalTableString += "<canvas id='"+processedMolecule.inchikey+"-"+i+"' width='250' height='150'></canvas>";
            verticalTableString += "</tr></td>";
        }
        verticalTableString +="</table>";
        return verticalTableString;

    }
}


function createDeglycosylatedMoeitiesList(listOfMoieties, processedMolecule){
    if(listOfMoieties == null){
        return "<p style='color: red;'>processedMolecule.smiles</p>";
    }else{
        var smilesList="<ul>";
        for(var i =0; i< listOfMoieties.length;i++){
            smilesList += "<li>";
            smilesList += listOfMoieties[i]+"  ";
            smilesList += "</li>";
        }
        smilesList += "</ul>";
        return smilesList;

    }
}

