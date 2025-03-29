$(document).ready(function () {
    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            USER_ME: '/api/users/me',
            LOGOUT: '/api/users/logout',
            REFRESH_TOKEN: '/api/users/refresh-token'
        },
        PAGES: { LOGIN: '/pages/client/login.html' },
        CSRF_EXCLUDED_PATHS: [
            '/api/users/login',
            '/api/users/logout',
            '/api/users/refresh-token',
            '/api/users/register',
            '/api/users/verify-recovery-token',
            '/api/users/password-recovery',
            '/oauth2/authorization/google',
            '/login?oauth2error=true'
        ]
    };

    const MESSAGES = {
        vi: {
            NOT_LOGGED_IN: 'Bạn chưa đăng nhập. Vui lòng đăng nhập!',
            SESSION_EXPIRED: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!',
            LOGOUT_FAILED: 'Đăng xuất thất bại',
            USER_FETCH_FAILED: 'Không thể tải thông tin người dùng. Vui lòng thử lại sau!',
            CSRF_TOKEN_NOT_FOUND: 'CSRF token không tìm thấy. Vui lòng đăng nhập lại!'
        }
    };
    const currentLang = 'vi';

    let isRefreshing = false;

    const ajaxConfig = {
        xhrFields: { withCredentials: true },
        contentType: 'application/json',
        timeout: 5000
    };

    const showNotification = (message, type, duration = 3000, callback) => {
        const $notification = $('#notification');
        if (!$notification.length) {
            alert(message);
            if (callback) setTimeout(callback, duration);
            return;
        }
        $notification.removeClass('success error show hidden').hide();
        $('#notification-text').text(message);
        $notification.addClass(type === 'success' ? 'success' : 'error').addClass('show').show();
        setTimeout(() => {
            $notification.removeClass('show').addClass('hidden');
            if (callback) callback();
        }, duration);
    };

    const errorHandlers = {
        401: () => showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error', 2000, () => {
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
            window.location.href = CONFIG.PAGES.LOGIN;
        }),
        default: (msg) => showNotification(msg, 'error')
    };

    function handleAjaxError(xhr, defaultMsg) {
        let msg = defaultMsg;
        try {
            const response = JSON.parse(xhr.responseText);
            msg = response.message || response.error || defaultMsg;
        } catch {
            msg = xhr.responseText || defaultMsg;
        }
        (errorHandlers[xhr.status] || errorHandlers.default)(msg);
    }

    async function refreshToken() {
        if (isRefreshing) return false;
        isRefreshing = true;
        try {
            const response = await $.ajax({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.REFRESH_TOKEN}`,
                method: 'POST',
                ...ajaxConfig
            });
            localStorage.setItem('isLoggedIn', 'true');
            if (response.csrfToken) {
                localStorage.setItem('csrfToken', response.csrfToken);
            }
            isRefreshing = false;
            return true;
        } catch {
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
            window.location.href = CONFIG.PAGES.LOGIN;
            isRefreshing = false;
            return false;
        }
    }

    async function ajaxWithRetry(settings) {
        const method = settings.method || settings.type || 'GET';
        const isSafeMethod = ['GET', 'HEAD', 'OPTIONS'].includes(method.toUpperCase());
        const isExcluded = CONFIG.CSRF_EXCLUDED_PATHS.some(path => settings.url.includes(path));

        if (!isSafeMethod && !isExcluded) {
            const csrfToken = localStorage.getItem('csrfToken');
            if (!csrfToken) {
                showNotification(MESSAGES[currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                    localStorage.removeItem('isLoggedIn');
                    localStorage.removeItem('csrfToken');
                    window.location.href = CONFIG.PAGES.LOGIN;
                });
                throw new Error('CSRF token not found');
            }
            settings.headers = {
                ...settings.headers,
                'X-XSRF-TOKEN': csrfToken
            };
        }

        settings.method = method;
        delete settings.type;

        try {
            return await $.ajax(settings);
        } catch (xhr) {
            if (xhr.status === 401) {
                const refreshed = await refreshToken();
                if (refreshed) {
                    if (!isSafeMethod && !isExcluded) {
                        const newCsrfToken = localStorage.getItem('csrfToken');
                        if (!newCsrfToken) {
                            showNotification(MESSAGES[currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                                localStorage.removeItem('isLoggedIn');
                                localStorage.removeItem('csrfToken');
                                window.location.href = CONFIG.PAGES.LOGIN;
                            });
                            throw new Error('CSRF token not found after refresh');
                        }
                        settings.headers = {
                            ...settings.headers,
                            'X-XSRF-TOKEN': newCsrfToken
                        };
                    }
                    return await $.ajax(settings);
                } else {
                    throw new Error('Failed to refresh token');
                }
            }
            throw xhr;
        }
    }

    function checkLoginStatus() {
        if (localStorage.getItem('isLoggedIn') !== 'true') {
            window.location.href = CONFIG.PAGES.LOGIN;
            return false;
        }
        fetchUserDetails();
        return true;
    }

    checkLoginStatus();

    $('#logout-btn').on('click', async function (event) {
        event.preventDefault();
        const $button = $(this);
        const originalButtonText = $button.text();

        $button.text('Đang đăng xuất...').prop('disabled', true);

        try {
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.LOGOUT}`,
                method: 'POST',
                ...ajaxConfig
            });
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
            window.location.href = CONFIG.PAGES.LOGIN;
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].LOGOUT_FAILED);
            setTimeout(() => {
                localStorage.removeItem('isLoggedIn');
                localStorage.removeItem('csrfToken');
                window.location.href = CONFIG.PAGES.LOGIN;
            }, 1500);
        } finally {
            $button.text(originalButtonText).prop('disabled', false);
        }
    });

    function fetchUserDetails() {
        ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.USER_ME}`,
            method: 'GET',
            ...ajaxConfig
        })
            .then(user => {
                if (user?.id) {
                    $('#fullname-display').text(user.fullname || 'Không có thông tin');
                    $('#username-display').text(user.username || 'Không có thông tin');
                    $('#email-display').text(user.email || 'Không có thông tin');
                    $('#role-display').text(getRoleDisplay(user.role));
                    $('#status-display').text(getStatusDisplay(user.userStatus));
                    $('#balance-display').text(formatCurrency(user.balance) || '0');
                    $('#created-at-display').text(user.timeCreateAt ? formatDateTime(user.timeCreateAt) : 'Không có thông tin');
                    $('#totaldeposit-display').text(formatCurrency(user.totaldeposit) || '0');
                } else {
                    showNotification('Dữ liệu người dùng không hợp lệ', 'error');
                }
            })
            .catch(xhr => {
                handleAjaxError(xhr, MESSAGES[currentLang].USER_FETCH_FAILED);
            });
    }

    function formatDateTime(timestamp) {
        const date = new Date(timestamp);
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${hours}:${minutes} ${day}/${month}/${year}`;
    }

    function getStatusDisplay(userStatus) {
        const statusMap = { 'ACTIVE': 'Đang hoạt động', 'LOCKED': 'Đang khóa' };
        return statusMap[userStatus] || 'Không có thông tin';
    }

    function getRoleDisplay(role) {
        const roleMap = { 'USER': 'Người dùng', 'AGENCY': 'Agency', 'ADMIN': 'Admin' };
        return roleMap[role] || 'Không có thông tin';
    }

    function formatCurrency(amount) {
        return amount.toLocaleString('vi-VN');
    }
});