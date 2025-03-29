$(document).ready(function () {
    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            USER_ME: '/api/users/me',
            ACTIVATE_GIFTCODE: '/api/users/giftcodes/activate',
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
            ACTIVATE_GIFTCODE_SUCCESS: 'Kích hoạt Giftcode thành công!',
            ACTIVATE_GIFTCODE_FAILED: 'Đã xảy ra lỗi khi kích hoạt giftcode',
            USER_FETCH_FAILED: 'Không thể tải thông tin người dùng',
            GIFTCODE_EMPTY: 'Mã Giftcode không được để trống!',
            GIFTCODE_LENGTH: 'Mã Giftcode phải có độ dài từ 4 đến 30 ký tự!',
            GIFTCODE_PATTERN: 'Mã Giftcode chỉ được chứa chữ cái và số!',
            BAD_REQUEST: 'Dữ liệu không hợp lệ!',
            GIFTCODE_NOT_FOUND: 'Giftcode không tồn tại hoặc không khả dụng!',
            FORBIDDEN: 'Tài khoản của bạn đã bị khóa, không thể kích hoạt giftcode!',
            SERVER_ERROR: 'Lỗi hệ thống. Vui lòng thử lại sau!',
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
        const $icon = $('#notification-icon');
        if (!$notification.length) return;

        $notification.removeClass('success error show hidden').hide();
        $('#notification-text').text(message);

        $notification.addClass(type === 'success' ? 'success' : 'error');
        $icon.html(type === 'success' ? '<i class="fas fa-check-circle"></i>' : '<i class="fas fa-exclamation-circle"></i>');

        $notification.addClass('show').show();
        setTimeout(() => {
            $notification.removeClass('show').addClass('hidden');
            if (callback) callback();
        }, duration);
    };

    const errorHandlers = {
        400: (msg) => showNotification(msg === 'Giftcode không tồn tại hoặc không khả dụng!' ? MESSAGES[currentLang].GIFTCODE_NOT_FOUND : (msg || MESSAGES[currentLang].BAD_REQUEST), 'error'),
        401: () => showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error', 2000, () => {
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
            window.location.href = CONFIG.PAGES.LOGIN;
        }),
        403: (msg) => showNotification(msg || MESSAGES[currentLang].FORBIDDEN, 'error'),
        404: (msg) => showNotification(msg || 'Không tìm thấy tài nguyên!', 'error'),
        500: () => showNotification(MESSAGES[currentLang].SERVER_ERROR, 'error'),
        default: (msg) => showNotification(msg, 'error')
    };

    function handleAjaxError(xhr, defaultMsg) {
        let msg = defaultMsg;
        try {
            const response = JSON.parse(xhr.responseText);
            msg = response.message || response.messageResponse || defaultMsg;
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

    function validateGiftcode(giftcode) {
        if (!giftcode || /^\s*$/.test(giftcode)) {
            showNotification(MESSAGES[currentLang].GIFTCODE_EMPTY, 'error');
            return false;
        }
        if (giftcode.length < 4 || giftcode.length > 30) {
            showNotification(MESSAGES[currentLang].GIFTCODE_LENGTH, 'error');
            return false;
        }
        if (!/^[a-zA-Z0-9]+$/.test(giftcode)) {
            showNotification(MESSAGES[currentLang].GIFTCODE_PATTERN, 'error');
            return false;
        }
        return true;
    }

    $('#giftcode-form').on('submit', async function (event) {
        event.preventDefault();

        if (!checkLoginStatus()) return;

        const giftcode = $('#giftcode').val();
        if (!validateGiftcode(giftcode)) return;

        const requestData = { code: giftcode };

        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.ACTIVATE_GIFTCODE}`,
                method: 'POST',
                data: JSON.stringify(requestData),
                ...ajaxConfig
            });
            showNotification(response.messageResponse || MESSAGES[currentLang].ACTIVATE_GIFTCODE_SUCCESS, 'success');
            fetchUserDetails();
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].ACTIVATE_GIFTCODE_FAILED);
        }
    });

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
                    $('#fullname-display').text(user.fullname || 'N/A');
                    $('#email-display').text(user.email || 'N/A');
                    $('#role-display').text(getRoleDisplay(user.role));
                    $('#status-display').text(getStatusDisplay(user.userStatus));
                    $('#balance-display').text(formatCurrency(user.balance) || '0');
                } else {
                    showNotification('Dữ liệu người dùng không hợp lệ', 'error');
                }
            })
            .catch(xhr => {
                handleAjaxError(xhr, MESSAGES[currentLang].USER_FETCH_FAILED);
            });
    }

    function formatCurrency(amount) {
        return amount.toLocaleString('vi-VN');
    }

    function getStatusDisplay(userStatus) {
        const statusMap = { 'ACTIVE': 'Đang hoạt động', 'LOCKED': 'Đang khóa' };
        return statusMap[userStatus] || 'N/A';
    }

    function getRoleDisplay(role) {
        const roleMap = { 'USER': 'Người dùng', 'AGENCY': 'Agency', 'ADMIN': 'Admin' };
        return roleMap[role] || 'N/A';
    }
});