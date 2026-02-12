const lock = new Uint8Array(1);
lock[0] = 0;

function popupWindow(msg)
{
	const templateValue = document.getElementById("popup-window-temp");
	let newVal = templateValue.cloneNode(true);
	newVal.children[1].innerHTML = msg;
	newVal.id = "popup-window-temp0";
	popupWindow0(newVal);
}
function popupWindowOK(msg)
{
	const templateValue = document.getElementById("popup-window-temp");
	let newVal = templateValue.cloneNode(true);
	newVal.children[1].innerHTML = msg;
	newVal.id = "popup-window-temp0";
	newVal.children[0].className = "popup-icon1";
	popupWindow0(newVal);
}
function popupWindow0(newVal)
{
	const popupContainer = document.getElementById("popup-container");
	popupContainer.prepend(newVal);
	Atomics.compareExchange(lock, 0, 0, 1);
	for (let i = 1; i < popupContainer.children.length; i++)
	{
		popupContainer.children[i].style.transition = "all 0s";
		popupContainer.children[i].style.top = "-50px";
	}
	void popupContainer.offsetWidth;
	//setTimeout(popupWindowLoaded0, 1, newVal)
	for (let i = 0; i < popupContainer.children.length; i++)
	{
		popupContainer.children[i].style.transition = "all 0.4s cubic-bezier(0.25, 1.25, 0.75, 1)";
		popupContainer.children[i].style.opacity = "1";
		popupContainer.children[i].style.top = "0";
		if (popupContainer.children[i].id === "popup-window-temp0")
			popupContainer.children[i].style.transform = "scale(1)";
	}
	setTimeout(popupWindowLoaded1, 2200, newVal);
	lock[0] = 0;
}
function popupWindowLoaded1(element)
{
	element.id = "popup-window-temp1";
	element.style.opacity = "0";
	element.style.transform = "scale(0.5) translateY(-50px)";
	setTimeout(popupWindowLoaded2, 200, element);
}
function popupWindowLoaded2(element)
{
	element.remove();
}