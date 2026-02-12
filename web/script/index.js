function calculateMD5(value)
{
	let hash = CryptoJS.MD5(value);
	return hash.toString();
}
function focusInputBox(event)
{
	event.target.parentNode.parentNode.className = "input-box input-box-focus";
}
function blurInputBox(event)
{
	event.target.parentNode.parentNode.className = "input-box input-box-color";
}
function mousedownFloatingButton(event)
{
	event.target.style.transform = "scale(0.95)";
}
function mouseupFloatingButton(event)
{
	event.target.style.transform = "";
}

