let advancedOpen = false;
let advancedSection = document.getElementById("advanced")
advancedSection.style.display = 'none';

function advancedToggle(event){
    event.preventDefault();
    if(advancedOpen){
        advancedSection.style.display = 'none';
        advancedOpen = false
    }
    else{
        advancedSection.style.display = 'block';
        advancedOpen = true
    }
}