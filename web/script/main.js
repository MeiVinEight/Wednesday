const navigations = document.getElementById("navigation-buttons");
const selectTableContaner = document.getElementById("select-table-container");
let selectedTable = 1;
let currentScroll = [undefined, undefined, undefined];
currentScroll[0] = document.getElementById("select-table-4").children[0].children[0].children[0];

async function changeToken()
{
	const input = document.getElementById("change-password-input");
	let value = input.value;
	if (value === "")
		return popupWindow("请输入Token");
	value = calculateMD5(value);
	const response = await fetch("/api/v1/system/token", {
		method: "POST",
		headers: {
			"Authorization": value
		}
	})
	if (!response.ok)
		return popupWindow("密码修改失败: [" + response.status + "] " + response.statusText);
	let body = await response.json();
	if (body.code !== 0)
		popupWindow("密码修改失败: [" + body.code + "] " + body.message);
	popupWindowOK("修改成功");
}
function selectTable(event, id)
{
	const tableInselect = document.getElementById("select-table-" + id);
	if (id === selectedTable)
	{
		tableInselect.style.display = "block";
		return;
	}

	const tableUnselect = document.getElementById("select-table-" + selectedTable);
	const buttonInselect = navigations.children[id - 1];
	const buttonUnselect = navigations.children[selectedTable - 1];
	const oldId = selectedTable;
	selectedTable = id;

	buttonInselect.className = "navigation-button navigation-button-selected";
	buttonInselect.children[2].style.backgroundColor = "#0AA";
	buttonUnselect.className = "navigation-button navigation-button-nonselected";
	buttonUnselect.children[2].style.backgroundColor = "#5DD";

	if (oldId > id)
	{
		tableInselect.style.top = "-100%";
		tableUnselect.style.top = "-100%";
	}
	tableInselect.style.display = "block";
	void selectTableContaner.offsetWidth;
	tableInselect.style.transitionProperty = "all";
	tableUnselect.style.transitionProperty = "all";
	if (oldId < id)
	{
		tableInselect.style.top = "-100%";
		tableUnselect.style.top = "-100%";
	}
	else
	{
		tableInselect.style.top = "0";
		tableUnselect.style.top = "0";
	}
	tableInselect.style.opacity = "1";
	tableUnselect.style.opacity = "0";
	setTimeout(() =>
	{
		tableInselect.style.transitionProperty = "none";
		tableUnselect.style.transitionProperty = "none";
		tableUnselect.style.display = "none";
		tableInselect.style.top = "0";
		tableUnselect.style.top = "0";
		void selectTableContaner.offsetWidth;
	}, 500);

	const headBarScrollder = document.getElementById("head-bar-scroller");
	const pos = - (id - 1) * 40;
	headBarScrollder.style.top = pos + "px";
/*
	const headBarContainer = document.getElementById("head-bar");
	const navgBtnContainer = document.getElementById("navigation-buttons");
	const oldVal = headBarContainer.children[0];
	const newVal = document.createElement("div");
	newVal.className = "navigation-button";
	newVal.style.position = "relative";
	newVal.style.opacity = "0";
	newVal.append(navgBtnContainer.children[id - 1].children[0].cloneNode(true));
	newVal.append(navgBtnContainer.children[id - 1].children[1].cloneNode(true));
	if (id > oldId)
	{
		headBarContainer.append(newVal);
		oldVal.style.top = "25%";
		newVal.style.top = "25%";
		void headBarContainer.offsetWidth;
		oldVal.style.transition = "all 0.5s cubic-bezier(0.3, 1, 1, 1)";
		newVal.style.transition = "all 0.5s cubic-bezier(0.3, 1, 1, 1)";
		oldVal.style.top = "-25%";
		newVal.style.top = "-25%";
	}
	else
	{
		headBarContainer.prepend(newVal);
		oldVal.style.top = "-25%";
		newVal.style.top = "-25%";
		void headBarContainer.offsetWidth;
		oldVal.style.transition = "all 0.5s cubic-bezier(0.3, 1, 1, 1)";
		newVal.style.transition = "all 0.5s cubic-bezier(0.3, 1, 1, 1)";
		oldVal.style.top = "25%";
		newVal.style.top = "25%";
	}
	oldVal.style.opacity = "0";
	newVal.style.opacity = "1";
	setTimeout(() =>
	{
		oldVal.style.transition = "";
		newVal.style.transition = "";
		oldVal.remove();
		newVal.style.top = "0";
	}, 500);
*/
}
function connectionEdit(event)
{
	const connectionLine = event.target.parentNode.parentNode.parentNode;
	const inputs = connectionLine.children[0];
	const saveds = connectionLine.children[2];

	let inEdit = !inputs.children[0].children[0].children[0].disabled;
	if (inEdit)
	{
		let connName = saveds.children[0].innerHTML;
		let connPath = saveds.children[1].innerHTML;
		let connTokn = saveds.children[2].innerHTML;

		inputs.children[0].children[0].children[0].disabled = true;
		inputs.children[1].children[0].children[0].disabled = true;
		inputs.children[2].children[0].children[0].disabled = true;
		inputs.children[0].children[0].children[0].value = connName;
		inputs.children[1].children[0].children[0].value = connPath;
		inputs.children[2].children[0].children[0].value = connTokn;
		inputs.children[0].children[0].children[0].setAttribute("placeholder", "");
		inputs.children[1].children[0].children[0].setAttribute("placeholder", "");
		inputs.children[2].children[0].children[0].setAttribute("placeholder", "");
		inputs.children[0].setAttribute("style", "background: none; box-shadow: none;");
		inputs.children[1].setAttribute("style", "background: none; box-shadow: none;");
		inputs.children[2].setAttribute("style", "background: none; box-shadow: none;");
	}
	else
	{
		inputs.children[0].children[0].children[0].disabled = false;
		inputs.children[1].children[0].children[0].disabled = false;
		inputs.children[2].children[0].children[0].disabled = false;
		inputs.children[0].children[0].children[0].setAttribute("placeholder", "输入连接名称");
		inputs.children[1].children[0].children[0].setAttribute("placeholder", "输入连接地址");
		inputs.children[2].children[0].children[0].setAttribute("placeholder", "输入连接令牌");
		inputs.children[0].setAttribute("style", "");
		inputs.children[1].setAttribute("style", "");
		inputs.children[2].setAttribute("style", "");
	}
}
async function connectionSave(event)
{
	const connectionLine = event.target.parentNode.parentNode.parentNode;
	const inputs = connectionLine.children[0];
	let connName = inputs.children[0].children[0].children[0].value;
	let connPath = inputs.children[1].children[0].children[0].value;
	let connTokn = inputs.children[2].children[0].children[0].value;
	if (connName === "" || connPath === "")
	{
		popupWindow("连接缺少名称 / 地址");
		if (connName === "")
			inputs.children[0].classList.add("input-box-error");
		if (connPath === "")
			inputs.children[1].classList.add("input-box-error");
		return;
	}

	const saveds = connectionLine.children[2];
	let resp;
	try
	{
		resp = await fetch("/api/v1/net/conn", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify({
				name: connName,
				url: connPath,
				token: connTokn
			})
		})
	}
	catch (err)
	{
		return popupWindow("保存失败: " + err);
	}
	if (!resp.ok)
		return popupWindow("保存失败: [" + resp.status + "] " + resp.statusText);
	let body = await resp.json();
	if (body.code !== 0)
		return popupWindow("保存失败: " + body.message);
	popupWindowOK("保存成功");
	saveds.children[0].innerHTML = connName;
	saveds.children[1].innerHTML = connPath;
	saveds.children[2].innerHTML = connTokn;
	event.target = event.target.parentNode.children[0];
	connectionEdit(event);
}
function connectionConf(event)
{
	const connectionLine = event.target.parentNode.parentNode.parentNode;
	const configBox = connectionLine.children[1];
	const show = !configBox.hasAttribute("data-show");
	if (show)
	{
		const innerBox = configBox.children[0];
		const offHei = innerBox.offsetHeight;
		configBox.setAttribute("data-show", "");
		configBox.style.opacity = "1";
		connectionLine.style.height = (64 + offHei) + "px";
	}
	else
	{
		configBox.removeAttribute("data-show");
		configBox.style.opacity = "0";
		connectionLine.style.height = "80px";
	}
}
function connectionDelete(event)
{
	const connectionLine = event.target.parentNode.parentNode.parentNode;
	connectionLine.style.height = "0";
	connectionLine.style.marginBottom = "0";
	connectionLine.style.transform = "translateX(-100%)";
	setTimeout(() => connectionLine.remove(), 500);
}
function onConnect(event)
{
	const button = event.target;
	const connectionLine = button.parentNode.parentNode.parentNode.parentNode.parentNode;
	const connected = connectionLine.hasAttribute("data-connected");
	if (connected)
	{
		button.children[0].innerHTML = "连接";
		connectionLine.removeAttribute("data-connected", "");
	}
	else
	{
		button.children[0].innerHTML = "断开";
		connectionLine.setAttribute("data-connected", "");
	}
	/*
	const popover = document.getElementById("popover0")
	popover.style.opacity = "1";
	popover.style.pointerEvents = "";
	*/
}
function serviceScroll(event, id0)
{
	/*
	for (let i = 0; i < scrollTable.children.length; i++)
	{
		let button = scrollTable.children[i];
		button.style.backgroundColor = "";
	}
	*/

	if (currentScroll[id0] !== undefined)
		currentScroll[id0].style.backgroundColor = "";
	currentScroll[id0] = event.target;
	event.target.style.backgroundColor = "#DDD";

	//scrollDelete(event.target);
	//const added = document.getElementById("conn-user-contact-template");
	//scrollAdd(id0, added);

	//const selectTable4 = document.getElementById("select-table-4");
	//const scrollTable = selectTable4.children[0];
	//const scroll = scrollTable.children[id0];
	//for (let i = 0; i < scroll.children.length; i++)
	//	scrollDelete(scroll.children[i]);
}
function scrollDelete(removed)
{
	removed.style.height = "0";
	removed.style.padding = "0";
	removed.style.transform = "translateX(-100%)";
	setTimeout(() => removed.remove(), 500);
}
function scrollAdd(scrollId, added)
{
	const selectTable4 = document.getElementById("select-table-4");
	const scrollTable = selectTable4.children[0];
	const scroll = scrollTable.children[scrollId];

	added = added.cloneNode(true);
	added.id = "";
	added.setAttribute("style", "transition: none; transform: translateX(100%);");
	added.setAttribute("onclick", "serviceScroll(event, " + scrollId + ")")
	scroll.append(added);
	void scroll.offsetWidth;
	added.setAttribute("style", "");
}
function switchServiceEnable(event)
{
	const switchbox = event.target.parentNode;
	const enabled = switchbox.children[0].checked;
}
function selectConfigTable(id)
{
	const selectConfig = document.getElementById("select-config-table");
	const topOffset = -(selectConfig.offsetHeight + 16) * id;
	for (let i = 0; i < selectConfig.children.length; i++)
	{
		let configTable = selectConfig.children[i];
		if (i === id)
		{
			configTable.style.opacity = "1";
			configTable.style.pointerEvents = "";
		}
		else
		{
			configTable.style.opacity = "0";
			configTable.style.pointerEvents = "none";
		}
		configTable.style.top = topOffset + "px";
	}
}