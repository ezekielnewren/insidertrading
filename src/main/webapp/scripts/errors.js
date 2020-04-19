document.getElementsByTagName("head")[0].innerHTML += `
<link href="./styles/errors.css" rel="stylesheet" type="text/css" />
`
document.getElementsByTagName("body")[0].innerHTML += `
<div id="errors">
<!-- Errors -->
</div>
`
const parent = document.getElementById("errors");
let timeOuts = []

function fadeOut(id){
    var elm = document.getElementById(id);
    elm.style.opacity = 0
    timeOuts.push(setTimeout(() => elm.remove(), 1000))
}

function showError(message){
    rand = Math.random().toString(36).slice(2); 
    parent.innerHTML += `
    <div id="${rand}" class="error fade-in">${message}</div>
    `
    timeOuts.push(setTimeout(fadeOut, 7500, rand))
}

