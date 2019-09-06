// 函数中的参数为 要获取的cookie键的名称,查
function getCookie(c_name) {
	if(document.cookie.length > 0) {
		c_start = document.cookie.indexOf(c_name + "=");
		if(c_start != -1) {
			c_start = c_start + c_name.length + 1;
			c_end = document.cookie.indexOf(";", c_start);
			if(c_end == -1) {
				c_end = document.cookie.length;
			}

			return unescape(document.cookie.substring(c_start, c_end));
		}
	}
	return "";
}
// 函数中的参数分别为 cookie的名称、值以及过期年数，增，改
function setCookie(c_name, value, expireyears) {
	var exdate = new Date();
	exdate.setDate(exdate.getDate() + expireyears * 365);
	document.cookie = c_name + "=" + escape(value) +
		((expireyears == null) ? "" : ";expires=" + exdate.toGMTString())
}
/**
 * iframe高度自适应
 */
function iframeResize() {
	var obj = parent.document.getElementById("main_iframe");
	if(obj) {
		var iframeWin = obj.contentWindow || obj.contentDocument.parentWindow;
		if(iframeWin.document.body) {
			obj.height = iframeWin.document.body.scrollHeight || iframeWin.document.documentElement.scrollHeight;
			obj.height = String(parseInt(obj.height) + 60);
		}
	}
}
/**
 * 获取url中的参数name的参数值
 * @param {Object} name
 */
function GetQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
	var r = window.location.search.substr(1).match(reg);
//	console.log("GetQueryString" + ":" + name + "+" + r);
	//search,查询？后面的参数，并匹配正则
	if(r != null) return decodeURIComponent(r[2]);
	return null;
}