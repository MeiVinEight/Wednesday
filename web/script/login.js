const loginBoxIcon = document.getElementById("login-box-head-icon");

async function login()
{
	const passwdInput = document.getElementById("input-box-input");
	let passwd = passwdInput.value;
	if (passwd === "")
	{
		popupWindow("请输入Token");
		return;
	}

	const token = calculateMD5(passwd);
	let response = await fetch("/api/v1/login", {
		method: "GET",
		headers: {
			"Authorization": token
		}
	});
	if (!response.ok)
	{
		popupWindow("登录失败: [" + response.status + "] " + response.statusText);
		return;
	}
	let body = await response.json();
	if (body.code !== 0)
	{
		popupWindow("登录失败: [" + body.code + "] " + body.message);
		return;
	}
	document.location.replace("/");
}

loginBoxIcon.addEventListener("mouseover", () => {loginBoxIcon.className = "rotation360"});
loginBoxIcon.addEventListener("animationend", () => {loginBoxIcon.className = ""});
//buttonFloating.addEventListener("mousedown", () => {buttonFloating.style.transform = "scale(0.95)"})
//buttonFloating.addEventListener("mouseup", () => {buttonFloating.style.transform = ""})