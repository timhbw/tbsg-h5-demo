/* ============================================
   淘宝闪购渠道H5 - 支付页脚本
   ============================================ */

// 解析 URL 参数（使用 common.js 中的函数）
const params = getQueryParams();

// 填充页面数据
if (params.payAmount) {
    const amountYuan = formatAmount(params.payAmount);
    document.getElementById('payAmountYuan').innerText = amountYuan;
}

if (params.transactionId) {
    document.getElementById('transactionId').innerText = params.transactionId;
}

if (params.subject) {
    document.getElementById('subject').innerText = params.subject;
}

if (params.uid) {
    document.getElementById('uid').innerText = params.uid;
    document.getElementById('uidRow').classList.remove('hidden');
}

if (params.body) {
    document.getElementById('body').innerText = params.body;
    document.getElementById('bodyRow').classList.remove('hidden');
}

// 支付处理逻辑
function handlePay() {
    // 禁用按钮
    const button = document.querySelector('.pay-button');
    button.disabled = true;
    button.style.opacity = '0.6';
    button.style.cursor = 'not-allowed';

    // 显示加载动画
    showLoading(true);

    const contextPath = params.contextPath || '';

    // 模拟支付处理（实际应该调用支付接口）
    setTimeout(() => {
        // 构建回调参数
        const callbackData = {
            status: 'SUCCESS',
            transactionId: params.transactionId,
            payAmount: params.payAmount,
            notifyUrl: params.notifyUrl,
            redirectUrl: params.redirectUrl
        };

        // 调用机构的回调接口
        const apiUrl = API_CONFIG.getUrl(API_CONFIG.ENDPOINTS.PAY_CALLBACK);

        fetch(apiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(callbackData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.redirectUrl) {
                // 跳转到中间页
                window.location.href = data.redirectUrl;
            } else {
                alert('支付失败：' + (data.message || '未知错误'));
                button.disabled = false;
                button.style.opacity = '1';
                button.style.cursor = 'pointer';
                showLoading(false);
            }
        })
        .catch(error => {
            console.error('支付失败:', error);
            alert('支付失败，请重试');
            button.disabled = false;
            button.style.opacity = '1';
            button.style.cursor = 'pointer';
            showLoading(false);
        });
    }, 1000);
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    console.log('Pay page loaded');

    // 绑定支付按钮事件
    const payButton = document.getElementById('payButton');
    if (payButton) {
        payButton.addEventListener('click', handlePay);
    }
});
