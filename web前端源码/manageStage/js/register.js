window.onload = function(){
	var flag = new Array(0, 0, 0, 0, 0, 0);
	var contentlist = document.getElementsByClassName("san-form-item-content");
	
	var time = 59;
	var t_interval;
	/**
	 * 用于定时
	 */
	function myclock() {
		console.log("123");
		emailbtn.disabled=true;
		if(time > 0){
			emailbtn.innerText = time;
			time--;
		}
		else if(time == 0){
			emailbtn.disabled=false;
			emailbtn.innerText="获取验证码";
			window.clearInterval(t_interval);
		}
	}
	function OnOffButton(flag1, flag2, flag3, flag4, flag5) {
		var emailbtn = contentlist[4].getElementsByTagName("button")[0];
		if(flag1 == 1 && flag2 == 1 && flag3 == 1 && flag4 == 1) {
			emailbtn.disabled = false;
		} else {
			emailbtn.disabled = true;
		}
		var submitbtn = contentlist[6].getElementsByTagName("button")[0];
		if(flag1 == 1 && flag2 == 1 && flag3 == 1 && flag4 == 1 && flag5 == 1) {
			submitbtn.disabled = false;
		} else {
			submitbtn.disabled = true;
		}
	
	}
	var username = contentlist[0].getElementsByTagName("input")[0];
	username.onchange = function() {
		if(username.value.length > 0) {
			flag[0] = 1;
			console.log(flag[0]);
		} else {
			flag[0] = 0;
			console.log(flag[0]);
		}
	}
	
	var psword = contentlist[1].getElementsByTagName("input")[0];
	/*
	psword.onclick = function(){
	//	var info = document.createTextNode("密码长度要在8-16之间");
		var parr = psword.parentNode;
		var info = parr.getElementsByTagName("label")[0];
		
		info.style.visibility="visible";
	}*/
	psword.onchange = function() {
		var parr = psword.parentNode;
		var info = parr.getElementsByTagName("label")[0];
		if(psword.value.length >= 8 && psword.value.length <= 16) {
			info.style.visibility = "hidden";
			flag[1] = 1;
			console.log(flag[1]);
			OnOffButton(flag[0], flag[1], flag[2], flag[3], flag[4]);
		} else {
			info.style.visibility = "visible";
			flag[1] = 0;
			console.log(flag[1]);
			OnOffButton(flag[0], flag[1], flag[2], flag[3], flag[4]);
		}
	}
	
	var psword2 = contentlist[2].getElementsByTagName("input")[0];
	psword2.onchange = function() {
		var parr = psword2.parentNode;
		var info = parr.getElementsByTagName("label")[0];
		if(psword2.value == psword.value) {
			info.style.visibility = "hidden";
			flag[2] = 1;
			console.log(flag[2]);
			OnOffButton(flag[0], flag[1], flag[2], flag[3], flag[4]);
		} else {
			info.style.visibility = "visible";
			flag[2] = 0;
			console.log(flag[2]);
			OnOffButton(flag[0], flag[1], flag[2], flag[3], flag[4]);
		}
	}
	var email = contentlist[3].getElementsByTagName("input")[0];
	email.onchange = function() {
		var parr = email.parentNode;
		var info = parr.getElementsByTagName("label")[0];
	
		var reg = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
	
		if(reg.test(email.value)) {
			info.style.visibility = "hidden";
			flag[3] = 1;
			console.log(flag[3]);
			OnOffButton(flag[0], flag[1], flag[2], flag[3], flag[4]);
		} else {
			info.style.visibility = "visible";
			flag[3] = 0;
			console.log(flag[3]);
			OnOffButton(flag[0], flag[1], flag[2], flag[3], flag[4]);
	
		}
	}

	var emailbtn = contentlist[4].getElementsByTagName("button")[0];
	emailbtn.onclick = email_btn_click();
/*	emailbtn.onclick = function() {
		console.log(email.value);
		var info = {};
		info['email'] = email.value;
		info['type'] = "register";
		jQuery.ajax({
			type: "POST",
			url: "http://192.168.100.32:8036/quest/code",
			xhrFields: {
				withCredentials: true
			},
			data: info
		});
	//	alert("已发送至邮箱，请注意查收！");
		t_interval = window.setInterval("myclock()",1000);
	}*/
	
	function email_btn_click(){
		var info = {};
		info['email'] = email.value;
		info['type'] = "register";
		jQuery.ajax({
			type: "POST",
			url: "http://192.168.100.32:8036/quest/code",
			xhrFields: {
				withCredentials: true
			},
			data: info
		});
		alert("已发送至邮箱，请注意查收！");
		t_interval = window.setInterval("myclock()",1000);
	}

	var vercode = contentlist[4].getElementsByTagName("input")[0];
	vercode.onchange = function() {
		if(vercode.value.length == 6) {
			flag[4] = 1;
			OnOffButton(flag[0], flag[1], flag[2], flag[3], flag[4]);
		} else {
			flag[4] = 0;
			OnOffButton(flag[0], flag[1], flag[2], flag[3], flag[4]);
		}
	
	}
	var submitbtn = contentlist[6].getElementsByTagName("button")[0];
	submitbtn.onclick = function() {
		var hash = MD5.createMD5String(psword.value);
		/*	var obj_info = {
				name:username.value,
				password:hash,
				avatar:"https://is1-ssl.mzstatic.com/image/thumb/Purple123/v4/d7/dd/a5/d7dda57c-2d4f-c387-f482-3b4f4c633cfa/AppIcon-0-1x_U007emarketing-0-0-85-220-0-7.png/246x0w.jpg",
				email:email.value,
				code:vercode.value,
				type:"register"
			};*/
		//	var info = JSON.stringify(obj_info);
		var info = {};
		info['name'] = username.value;
		info['password'] = hash;
		info['avatar'] = "https://is1-ssl.mzstatic.com/image/thumb/Purple123/v4/d7/dd/a5/d7dda57c-2d4f-c387-f482-3b4f4c633cfa/AppIcon-0-1x_U007emarketing-0-0-85-220-0-7.png/246x0w.jpg";
		info['email'] = email.value;
		info['code'] = vercode.value;
		info['type'] = "register";
		jQuery.ajax({
			type: "POST",
			url: "http://192.168.100.32:8036/user/register",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				console.log(result);
				if(result.content == "success")
				{
					alert("注册成功");
					window.location.href = "login.html";
					
				}
				else if(result.content == "codeError")
				{
					alert("验证码错误");
				}
				else if(result.content == "repeat")
				{
					alert("邮箱已被注册");
				}
			},
			error: function() {
				console.log("error");
			}
		})
	
	}
}