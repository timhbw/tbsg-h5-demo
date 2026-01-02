/* ============================================
   淘宝闪购渠道H5 - 公共脚本文件
   ============================================ */

// 设备检测
const DeviceDetector = {
    userAgent: navigator.userAgent,
    
    get isAndroid() {
        return this.userAgent.indexOf("Android") > -1 || this.userAgent.indexOf("Adr") > -1;
    },
    
    get isiOS() {
        return !!this.userAgent.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/);
    },
    
    get isMobile() {
        return this.isAndroid || this.isiOS;
    }
};

// API 配置
const API_CONFIG = {
    BASE_URL: window.location.origin + '/api/tbsg/channel',
    
    ENDPOINTS: {
        LOGIN: '/login',
        GET_TBSG_URL: '/getTbsgH5Url',
        PAY_CALLBACK: '/notify/paycallback'
    },
    
    getUrl(endpoint) {
        return this.BASE_URL + endpoint;
    }
};

// 本地存储管理
const Storage = {
    KEYS: {
        JWT: 'tbsgH5JWT',
        MOBILE: 'tbsgMobile',
        CURRENT_PAGE: 'currentPage'
    },
    
    getJWT() {
        return localStorage.getItem(this.KEYS.JWT);
    },
    
    setJWT(jwt) {
        localStorage.setItem(this.KEYS.JWT, jwt);
    },
    
    getMobile() {
        return localStorage.getItem(this.KEYS.MOBILE);
    },
    
    setMobile(mobile) {
        localStorage.setItem(this.KEYS.MOBILE, mobile);
    },
    
    getCurrentPage() {
        return localStorage.getItem(this.KEYS.CURRENT_PAGE) || 'home';
    },
    
    setCurrentPage(page) {
        localStorage.setItem(this.KEYS.CURRENT_PAGE, page);
    },
    
    clearAuth() {
        localStorage.removeItem(this.KEYS.JWT);
        localStorage.removeItem(this.KEYS.MOBILE);
    },
    
    clearAll() {
        localStorage.clear();
    }
};

// 工具函数：显示提示
function showAlert(message, type = 'success') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert ${type}`;
    alertDiv.textContent = message;
    document.body.appendChild(alertDiv);

    setTimeout(() => {
        if (document.body.contains(alertDiv)) {
            document.body.removeChild(alertDiv);
        }
    }, 2000);
}

// 工具函数：解析 URL 参数
function getQueryParams() {
    const params = {};
    const search = window.location.search.substring(1);
    
    if (search) {
        const pairs = search.split('&');
        for (let i = 0; i < pairs.length; i++) {
            const pair = pairs[i].split('=');
            if (pair.length === 2) {
                params[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1]);
            }
        }
    }
    
    return params;
}

// 工具函数：带认证的 fetch 请求
async function fetchWithAuth(url, options = {}) {
    const jwt = Storage.getJWT();
    
    const defaultOptions = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...(jwt && { 'Authorization': jwt })
        }
    };
    
    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...(options.headers || {})
        }
    };
    
    try {
        const response = await fetch(url, mergedOptions);
        const data = await response.json();
        
        // 如果返回了新的 JWT，更新存储
        if (data.jwt) {
            Storage.setJWT(data.jwt);
        }
        
        return data;
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    }
}

// 工具函数：显示加载状态
function showLoading(show = true) {
    const loadingElement = document.getElementById('loading');
    if (loadingElement) {
        if (show) {
            loadingElement.classList.add('active');
        } else {
            loadingElement.classList.remove('active');
        }
    }
}

// 工具函数：防抖
function debounce(func, wait = 300) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 工具函数：节流
function throttle(func, limit = 300) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// 工具函数：格式化金额（分转元）
function formatAmount(amountInCents) {
    return (parseInt(amountInCents) / 100).toFixed(2);
}

// 工具函数：验证手机号
function validateMobile(mobile) {
    const regex = /^1[3-9]\d{9}$/;
    return regex.test(mobile);
}

// 页面加载完成后的初始化
document.addEventListener('DOMContentLoaded', () => {
    console.log('Common.js loaded');
    console.log('Device:', DeviceDetector.isMobile ? 'Mobile' : 'Desktop');
    console.log('Platform:', DeviceDetector.isAndroid ? 'Android' : DeviceDetector.isiOS ? 'iOS' : 'Other');
});
