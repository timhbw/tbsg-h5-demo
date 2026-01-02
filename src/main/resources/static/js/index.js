/* ============================================
   淘宝闪购渠道H5 - 首页脚本
   ============================================ */

// 工具函数：切换导航
function switchTab(target) {
    const navItems = document.querySelectorAll('.nav-item');
    const sections = document.querySelectorAll('.section');

    // 先移除所有激活状态
    navItems.forEach(item => item.classList.remove('active'));
    sections.forEach(section => section.classList.remove('active-section'));

    // 使用 requestAnimationFrame 确保 DOM 更新
    requestAnimationFrame(() => {
        // 添加新的激活状态
        navItems.forEach(item => {
            if (item.dataset.target === target) {
                item.classList.add('active');
            }
        });

        sections.forEach(section => {
            if (section.id === target) {
                section.classList.add('active-section');
                // 确保新激活的 section 滚动到顶部
                section.scrollTop = 0;
            }
        });

        Storage.setCurrentPage(target);
    });
}

// 检查登录状态
function checkLoginStatus() {
    const jwt = Storage.getJWT();
    const mobile = Storage.getMobile();
    const loginForm = document.getElementById('loginForm');
    const profileInfo = document.getElementById('profileInfo');
    const menuList = document.getElementById('menuList');
    const logoutArea = document.getElementById('logoutArea');
    const profilePhone = document.getElementById('profilePhone');

    if (jwt && mobile) {
        loginForm.style.display = 'none';
        profileInfo.style.display = 'block';
        menuList.style.display = 'block';
        logoutArea.style.display = 'block';
        profilePhone.textContent = mobile;
    } else {
        loginForm.style.display = 'block';
        profileInfo.style.display = 'none';
        menuList.style.display = 'none';
        logoutArea.style.display = 'none';
    }
}

// 登录功能
function handleLogin() {
    const loginButton = document.getElementById('loginButton');
    const mobileInput = document.getElementById('mobileInput');

    loginButton.addEventListener('click', async () => {
        const mobile = mobileInput.value.trim();
        if (!mobile) {
            showAlert('请输入手机号码', 'error');
            return;
        }

        // 显示加载状态
        const originalText = loginButton.textContent;
        loginButton.innerHTML = '<span class="loading"></span> 登录中...';
        loginButton.disabled = true;

        try {
            const response = await fetch(API_CONFIG.getUrl(API_CONFIG.ENDPOINTS.LOGIN), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ mobile: mobile })
            });

            const data = await response.json();

            if (data.status === 'SUCCESS' && data.jwt) {
                Storage.setJWT(data.jwt);
                Storage.setMobile(mobile);
                showAlert('登录成功');
                checkLoginStatus();
            } else {
                showAlert(data.errMessage || '登录失败，请重试', 'error');
            }
        } catch (error) {
            console.error('Error:', error);
            showAlert('登录失败，请检查网络连接', 'error');
        } finally {
            loginButton.textContent = originalText;
            loginButton.disabled = false;
        }
    });
}

// 退出登录功能
function handleLogout() {
    const logoutButton = document.getElementById('logoutButton');

    logoutButton.addEventListener('click', () => {
        Storage.clearAuth();
        showAlert('退出登录成功');
        checkLoginStatus();
    });
}

// 访问淘宝闪购H5页面（生产环境）
function handleTbsgButton() {
    const tbsgButton = document.getElementById('tbsgButton');

    tbsgButton.addEventListener('click', async () => {
        const jwt = Storage.getJWT();
        if (!jwt) {
            showAlert('请先登录', 'error');
            switchTab('mine');
            return;
        }

        // 显示加载状态
        const originalText = tbsgButton.textContent;
        tbsgButton.innerHTML = '<span class="loading"></span> 加载中...';
        tbsgButton.disabled = true;

        try {
            const response = await fetch(API_CONFIG.getUrl(API_CONFIG.ENDPOINTS.GET_TBSG_URL), {
                method: 'POST',
                headers: {
                    'Authorization': jwt,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ env: "PROD" })
            });

            const data = await response.json();

            if (data.status === 'SUCCESS' && data.tbsgH5Url) {
                // 更新 token（如果有新的）
                if (data.jwt) {
                    Storage.setJWT(data.jwt);
                }

                showAlert('即将跳转到淘宝闪购H5页面');
                setTimeout(() => {
                    if (DeviceDetector.isiOS) {
                        window.location.href = data.tbsgH5Url;
                    } else {
                        window.open(data.tbsgH5Url, '_blank');
                    }
                }, 1500);
            } else {
                if (data.errMessage && data.errMessage.includes('过期')) {
                    Storage.clearAuth();
                    showAlert('登录已过期，请重新登录', 'error');
                    checkLoginStatus();
                } else {
                    showAlert(data.errMessage || '获取URL失败', 'error');
                }
            }
        } catch (error) {
            console.error('Error:', error);
            showAlert('获取URL失败，请重试', 'error');
        } finally {
            tbsgButton.textContent = originalText;
            tbsgButton.disabled = false;
        }
    });
}

// 访问淘宝闪购H5页面（预发环境）
function handleTbsgButtonPPE() {
    const tbsgButtonPPE = document.getElementById('tbsgButtonPPE');

    tbsgButtonPPE.addEventListener('click', async () => {
        const jwt = Storage.getJWT();
        if (!jwt) {
            showAlert('请先登录', 'error');
            switchTab('mine');
            return;
        }

        // 显示加载状态
        const originalText = tbsgButtonPPE.textContent;
        tbsgButtonPPE.innerHTML = '<span class="loading"></span> 加载中...';
        tbsgButtonPPE.disabled = true;

        try {
            const response = await fetch(API_CONFIG.getUrl(API_CONFIG.ENDPOINTS.GET_TBSG_URL), {
                method: 'POST',
                headers: {
                    'Authorization': jwt,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ env: "PPE" })
            });

            const data = await response.json();

            if (data.status === 'SUCCESS' && data.tbsgH5Url) {
                // 更新 token（如果有新的）
                if (data.jwt) {
                    Storage.setJWT(data.jwt);
                }

                showAlert('即将跳转到淘宝闪购H5页面（预发）');
                setTimeout(() => {
                    if (DeviceDetector.isiOS) {
                        window.location.href = data.tbsgH5Url;
                    } else {
                        window.open(data.tbsgH5Url, '_blank');
                    }
                }, 1500);
            } else {
                if (data.errMessage && data.errMessage.includes('过期')) {
                    Storage.clearAuth();
                    showAlert('登录已过期，请重新登录', 'error');
                    checkLoginStatus();
                } else {
                    showAlert(data.errMessage || '获取URL失败', 'error');
                }
            }
        } catch (error) {
            console.error('Error:', error);
            showAlert('获取URL失败，请重试', 'error');
        } finally {
            tbsgButtonPPE.textContent = originalText;
            tbsgButtonPPE.disabled = false;
        }
    });
}

// 导航切换
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const target = item.dataset.target;
            switchTab(target);
        });
    });
}

// 页面初始化
document.addEventListener('DOMContentLoaded', () => {
    console.log('Index page loaded');
    
    // 初始化导航
    initNavigation();

    // 检查登录状态
    checkLoginStatus();

    // 绑定事件
    handleLogin();
    handleLogout();
    handleTbsgButton();
    handleTbsgButtonPPE();

    // 恢复上次的页面状态
    const currentPage = Storage.getCurrentPage();
    switchTab(currentPage);
});
