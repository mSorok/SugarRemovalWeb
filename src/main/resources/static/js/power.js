
var sugarRemovalParams = [];

var selectedInputType = null;

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
    var y = document.getElementById("chooseSugarRemovalTypeFirst");
    var x = document.getElementById("step2");

    if(sugarRemovalParams.length>0) {

        if (x.style.display === "none") {
            $(x).slideDown();
            y.style.display = "none";
        }

        $.ajax
        ({
            url: '/sugarParameters',
            data: JSON.stringify(sugarRemovalParams.join("zzz")),
            type: 'POST',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            /*error: function(){alert("nope!") },*/
            //error: function(xhr, status, error) {
            //  alert();
            //},
            //success: function () {alert("yes!")}
        });

        sugarRemovalParams = [];
    }else{
        $(x).slideUp();
        $(y).slideDown();
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

    selectedInputType = "file";
}

function showSmilesPasteDiv(obj){
    /*hide all others*/
    $(document.getElementById("submitFile")).slideUp();
    $(document.getElementById("oclContainer")).slideUp();

    /* show this one*/
    $(document.getElementById("smilesSubmission")).slideToggle();

    selectedInputType = "smiles";

}

function showDrawMoleculeDiv(obj){
    /*hide all others*/
    $(document.getElementById("submitFile")).slideUp();
    $(document.getElementById("smilesSubmission")).slideUp();

    /* show this one*/
    $(document.getElementById("oclContainer")).slideToggle();

    selectedInputType = "draw";

}



$('.input-choice-button').click(function(){
    if($(this).hasClass('active')){
        $(this).removeClass('active')
    } else {
        $(this).addClass('active')
    }
});


$('.submit-mol-button-ocl').click(function(){

    $.ajax
    ({
        url: '/drawing',
        data: JSON.stringify(editor.getMolFile() +"---"+sugarRemovalParams.concat("zzz") ) ,
        type: 'POST',
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        error: function(){window.location.href = "/results"; },
        //error: function(xhr, status, error) {
        //  alert();
        //},
        success: function () {
            alert("yes!")
        }
    });

});

