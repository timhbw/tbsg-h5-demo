/* ============================================
   淘宝闪购渠道H5 - 支付成功跳转页脚本
   ============================================ */

// 从 URL 参数中获取 redirectUrl（使用 common.js 中的函数）
const urlParams = getQueryParams();
const redirectUrl = urlParams.redirectUrl;

let countdown = 3;
const countdownElement = document.getElementById('countdown');
const manualLink = document.getElementById('manualLink');

// 设置手动跳转链接
if (redirectUrl) {
    manualLink.href = redirectUrl;
}

// 倒计时
const timer = setInterval(() => {
    countdown--;
    countdownElement.textContent = countdown;
    
    if (countdown <= 0) {
        clearInterval(timer);
        if (redirectUrl) {
            window.location.href = redirectUrl;
        } else {
            alert('跳转地址不存在');
        }
    }
}, 1000);

// 手动跳转
manualLink.addEventListener('click', (e) => {
    e.preventDefault();
    clearInterval(timer);
    if (redirectUrl) {
        window.location.href = redirectUrl;
    } else {
        alert('跳转地址不存在');
    }
});

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    console.log('Redirect page loaded');
});
