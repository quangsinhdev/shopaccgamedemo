$(document).ready(function () {
    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            USER_ME: '/api/users/me',
            CHANGE_PASSWORD: '/api/users/password',
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
            PASSWORD_MISMATCH: 'Mật khẩu mới và xác nhận mật khẩu không khớp!',
            CHANGE_PASSWORD_SUCCESS: 'Đã đổi mật khẩu thành công. Vui lòng đợi vài giây...',
            SESSION_EXPIRED: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!',
            LOGOUT_FAILED: 'Đăng xuất thất bại',
            CHANGE_PASSWORD_FAILED: 'Đã xảy ra lỗi khi đổi mật khẩu',
            USER_FETCH_FAILED: 'Không thể tải thông tin người dùng',
            CURRENT_PASSWORD_INVALID: 'Mật khẩu hiện tại không đúng. Vui lòng kiểm tra lại!',
            NEW_PASSWORD_EMPTY: 'Mật khẩu mới không được bỏ trống hoặc chỉ là khoảng trắng',
            NEW_PASSWORD_LENGTH: 'Mật khẩu mới có độ dài tối thiểu là 8 ký tự và tối đa 100 ký tự',
            NEW_PASSWORD_PATTERN: 'Mật khẩu mới phải chứa ít nhất một chữ cái và một chữ số',
            NEW_PASSWORD_WHITESPACE: 'Mật khẩu mới không được chứa khoảng trắng',
            CONFIRM_PASSWORD_EMPTY: 'Mật khẩu xác nhận không được bỏ trống hoặc chỉ là khoảng trắng',
            CONFIRM_PASSWORD_LENGTH: 'Mật khẩu xác nhận có độ dài tối thiểu là 8 ký tự và tối đa 100 ký tự',
            CONFIRM_PASSWORD_PATTERN: 'Mật khẩu xác nhận phải chứa ít nhất một chữ cái và một chữ số',
            SERVER_ERROR: 'Lỗi server. Vui lòng thử lại sau!',
            FORBIDDEN: 'Tài khoản của bạn đã bị khóa hoặc bạn không có quyền!',
            BAD_REQUEST: 'Dữ liệu không hợp lệ!',
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
        if (!$notification.length) return;
        $notification.removeClass('error success show').hide();
        $('#notification-text').text(message);
        $notification.addClass(`${type} show`).show();
        setTimeout(() => {
            $notification.removeClass('show').hide();
            if (callback) callback();
        }, duration);
    };

    const errorHandlers = {
        400: (msg) => showNotification(msg || MESSAGES[currentLang].BAD_REQUEST, 'error'),
        401: () => showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error', 2000, () => {
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
            window.location.href = CONFIG.PAGES.LOGIN;
        }),
        403: (msg) => showNotification(msg || MESSAGES[currentLang].FORBIDDEN, 'error'),
        500: () => showNotification(MESSAGES[currentLang].SERVER_ERROR, 'error'),
        default: (msg) => showNotification(msg || MESSAGES[currentLang].CHANGE_PASSWORD_FAILED, 'error')
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
        } else {
            fetchUserDetails();
        }
    }

    checkLoginStatus();

    function validatePassword(password, fieldPrefix = 'PASSWORD') {
        const messages = {
            CURRENT_PASSWORD: {
                EMPTY: MESSAGES[currentLang].CURRENT_PASSWORD_INVALID,
                LENGTH: MESSAGES[currentLang].CURRENT_PASSWORD_INVALID,
                PATTERN: MESSAGES[currentLang].CURRENT_PASSWORD_INVALID,
                WHITESPACE: MESSAGES[currentLang].CURRENT_PASSWORD_INVALID
            },
            NEW_PASSWORD: {
                EMPTY: MESSAGES[currentLang].NEW_PASSWORD_EMPTY,
                LENGTH: MESSAGES[currentLang].NEW_PASSWORD_LENGTH,
                PATTERN: MESSAGES[currentLang].NEW_PASSWORD_PATTERN,
                WHITESPACE: MESSAGES[currentLang].NEW_PASSWORD_WHITESPACE
            },
            CONFIRM_PASSWORD: {
                EMPTY: MESSAGES[currentLang].CONFIRM_PASSWORD_EMPTY,
                LENGTH: MESSAGES[currentLang].CONFIRM_PASSWORD_LENGTH,
                PATTERN: MESSAGES[currentLang].CONFIRM_PASSWORD_PATTERN,
                WHITESPACE: MESSAGES[currentLang].NEW_PASSWORD_WHITESPACE
            }
        };
        const msg = messages[fieldPrefix];

        if (!password || password.trim() === '') return msg.EMPTY;
        if (password.length < 8 || password.length > 100) return msg.LENGTH;
        if (/\s/.test(password)) return msg.WHITESPACE;
        if (!/^(?=.*[a-zA-Z])(?=.*\d).+$/.test(password)) return msg.PATTERN;
        return null;
    }

    $('#change-password-form').on('submit', async function (event) {
        event.preventDefault();
        const $form = $(this);
        const $submitButton = $form.find('button[type="submit"]');
        const originalButtonText = $submitButton.text();

        const currentPassword = $('#currentPassword').val();
        const newPassword = $('#newPassword').val();
        const confirmNewPassword = $('#confirmPassword').val();

        let error = validatePassword(currentPassword, 'CURRENT_PASSWORD');
        if (error) {
            showNotification(error, 'error');
            return;
        }
        error = validatePassword(newPassword, 'NEW_PASSWORD');
        if (error) {
            showNotification(error, 'error');
            return;
        }
        error = validatePassword(confirmNewPassword, 'CONFIRM_PASSWORD');
        if (error) {
            showNotification(error, 'error');
            return;
        }

        if (newPassword !== confirmNewPassword) {
            showNotification(MESSAGES[currentLang].PASSWORD_MISMATCH, 'error');
            return;
        }

        $submitButton.text('Đang xử lý...').prop('disabled', true);

        const formData = {
            currentPassword: currentPassword,
            newPassword: newPassword,
            confirmNewPassword: confirmNewPassword
        };

        try {
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.CHANGE_PASSWORD}`,
                method: 'PATCH',
                data: JSON.stringify(formData),
                ...ajaxConfig
            });
            showNotification(MESSAGES[currentLang].CHANGE_PASSWORD_SUCCESS, 'success', 4000, () => {
                localStorage.removeItem('isLoggedIn');
                localStorage.removeItem('csrfToken');
                window.location.href = CONFIG.PAGES.LOGIN;
            });
            $form[0].reset();
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].CHANGE_PASSWORD_FAILED);
        } finally {
            $submitButton.text(originalButtonText).prop('disabled', false);
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
            .then(response => {
                if (response?.id) {
                    $('#fullname-display').text(response.fullname || 'Không có thông tin');
                    $('#username-display').text(response.username || 'Không có thông tin');
                    $('#email-display').text(response.email || 'Không có thông tin');
                    $('#status-display').text(getStatusDisplay(response.userStatus));
                    $('#created-at-display').text(
                        response.timeCreateAt ? new Date(response.timeCreateAt).toLocaleString() : 'Không có thông tin'
                    );
                    $('#totaldeposit-display').text(response.totaldeposit || '0');
                } else {
                    showNotification('Dữ liệu người dùng không hợp lệ', 'error');
                }
            })
            .catch(xhr => {
                handleAjaxError(xhr, MESSAGES[currentLang].USER_FETCH_FAILED);
            });
    }

    const statusMap = { 'ACTIVE': 'Đang hoạt động', 'LOCKED': 'Đang khóa' };
    const roleMap = { 'USER': 'Người dùng', 'AGENCY': 'Agency', 'ADMIN': 'Admin' };

    function getStatusDisplay(userStatus) {
        return statusMap[userStatus] || 'Không có thông tin';
    }

    function getRoleDisplay(role) {
        return roleMap[role] || 'Không có thông tin';
    }
});