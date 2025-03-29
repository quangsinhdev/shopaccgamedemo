$(document).ready(function () {
    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            PASSWORD_RECOVERY: '/api/users/password-recovery',
            USER_ME: '/api/users/me',
            REFRESH_TOKEN: '/api/users/refresh-token'
        },
        PAGES: {
            HOME: '/',
            LOGIN: '/pages/client/login.html'
        }
    };

    const NOTIFICATION_DURATION = {
        SUCCESS: 5000,
        ERROR: 3000,
        EXPIRED: 2000
    };

    const MESSAGES = {
        vi: {
            ALREADY_LOGGED_IN: 'Bạn đã đăng nhập. Chuyển về trang chủ!',
            SESSION_EXPIRED: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!',
            RECOVERY_SUCCESS: 'Yêu cầu khôi phục đã được gửi. Kiểm tra email của bạn!',
            RECOVERY_FAILED: 'Đã xảy ra lỗi khi khôi phục mật khẩu',
            INVALID_DATA: 'Dữ liệu không hợp lệ. Vui lòng kiểm tra lại!',
            NOT_FOUND: 'Không tìm thấy tài khoản với username và email này!',
            SERVER_ERROR: 'Lỗi máy chủ. Vui lòng thử lại sau!',
            UNKNOWN_ERROR: 'Lỗi không xác định. Vui lòng thử lại!',
            USERNAME_NOT_FOUND: 'Tài khoản không tồn tại!',
            EMAIL_MISMATCH: 'Email không khớp với tài khoản này!',
            USERNAME_EMPTY: 'Tài khoản không được bỏ trống hoặc chỉ chứa khoảng trắng',
            USERNAME_LENGTH: 'Tài khoản có độ dài tối thiểu 8 ký tự và tối đa 50 ký tự',
            USERNAME_PATTERN: 'Tài khoản chỉ có thể chứa số và chữ cái',
            USERNAME_WHITESPACE: 'Tài khoản không hợp lệ! (Không được chứa khoảng trắng)',
            EMAIL_EMPTY: 'Email không được bỏ trống hoặc chỉ chứa khoảng trắng',
            EMAIL_LENGTH: 'Email không hợp lệ. Vui lòng thử lại (độ dài từ 6 đến 80 ký tự)',
            EMAIL_PATTERN: 'Email không hợp lệ',
            EMAIL_WHITESPACE: 'Email không hợp lệ (không được chứa khoảng trắng)'
        }
    };
    const currentLang = 'vi';

    let isRefreshing = false;
    let notificationTimeout = null;

    const ajaxConfig = {
        xhrFields: { withCredentials: true },
        contentType: 'application/json',
        timeout: 5000
    };

    const showNotification = (message, type = 'error', duration = 3000, callback) => {
        try {
            const $notification = $('#notification');
            if (!$notification.length) {
                alert(message);
                if (callback) setTimeout(callback, duration);
                return;
            }

            if (notificationTimeout) {
                clearTimeout(notificationTimeout);
                $notification.stop().hide();
            }

            $notification.removeClass('error success').text(message).addClass(type).show();

            notificationTimeout = setTimeout(() => {
                $notification.hide();
                if (callback) callback();
                notificationTimeout = null;
            }, duration);
        } catch (error) {
            alert('Có lỗi xảy ra với thông báo: ' + message);
        }
    };

    const errorHandlers = {
        400: (errorCode, message) => {
            if (errorCode === 'USER_NOT_FOUND') {
                showNotification(message || MESSAGES[currentLang].USERNAME_NOT_FOUND, 'error', NOTIFICATION_DURATION.ERROR);
            } else if (errorCode === 'EMAIL_MISMATCH') {
                showNotification(message || MESSAGES[currentLang].EMAIL_MISMATCH, 'error', NOTIFICATION_DURATION.ERROR);
            } else {
                showNotification(message || MESSAGES[currentLang].INVALID_DATA, 'error', NOTIFICATION_DURATION.ERROR);
            }
        },
        401: () => showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error', NOTIFICATION_DURATION.EXPIRED, () => {
            localStorage.removeItem('isLoggedIn');
            window.location.href = CONFIG.PAGES.LOGIN;
        }),
        404: (msg) => showNotification(msg || MESSAGES[currentLang].NOT_FOUND, 'error', NOTIFICATION_DURATION.ERROR),
        500: (msg) => showNotification(msg || MESSAGES[currentLang].SERVER_ERROR, 'error', NOTIFICATION_DURATION.ERROR),
        default: (msg) => showNotification(msg || MESSAGES[currentLang].UNKNOWN_ERROR, 'error', NOTIFICATION_DURATION.ERROR)
    };

    function handleAjaxError(xhr, defaultMsg) {
        try {
            let msg = defaultMsg;
            const response = JSON.parse(xhr.responseText || '{}');
            const errorCode = response.errorCode || '';
            msg = response.message || response.error || defaultMsg;
            (errorHandlers[xhr.status] || errorHandlers.default)(errorCode, msg);
        } catch (e) {
            showNotification(MESSAGES[currentLang].UNKNOWN_ERROR, 'error', NOTIFICATION_DURATION.ERROR);
        }
    }

    async function refreshToken() {
        if (isRefreshing) return false;
        isRefreshing = true;
        try {
            await $.ajax({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.REFRESH_TOKEN}`,
                method: 'POST',
                ...ajaxConfig
            });
            localStorage.setItem('isLoggedIn', 'true');
            isRefreshing = false;
            return true;
        } catch (xhr) {
            localStorage.removeItem('isLoggedIn');
            isRefreshing = false;
            return false;
        }
    }

    async function ajaxWithRetry(settings) {
        try {
            return await $.ajax(settings);
        } catch (xhr) {
            if (xhr.status === 401 && await refreshToken()) {
                return await $.ajax(settings);
            }
            throw xhr;
        }
    }

    function checkLoginStatus() {
        const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        if (isLoggedIn) {
            showNotification(MESSAGES[currentLang].ALREADY_LOGGED_IN, 'success', NOTIFICATION_DURATION.SUCCESS, () => {
                window.location.href = CONFIG.PAGES.HOME;
            });
            return true;
        }
        return false;
    }

    function validateRecoveryForm({ username, email }) {
        try {
            if (!username || username.trim() === '') return MESSAGES[currentLang].USERNAME_EMPTY;
            if (/\s/.test(username)) return MESSAGES[currentLang].USERNAME_WHITESPACE;
            const trimmedUsername = username.trim();
            if (trimmedUsername.length < 8 || trimmedUsername.length > 50) return MESSAGES[currentLang].USERNAME_LENGTH;
            if (!/^[a-zA-Z0-9]+$/.test(trimmedUsername)) return MESSAGES[currentLang].USERNAME_PATTERN;

            if (!email || email.trim() === '') return MESSAGES[currentLang].EMAIL_EMPTY;
            if (/\s/.test(email)) return MESSAGES[currentLang].EMAIL_WHITESPACE;
            const trimmedEmail = email.trim();
            if (trimmedEmail.length < 6 || trimmedEmail.length > 80) return MESSAGES[currentLang].EMAIL_LENGTH;
            if (!/^[A-Za-z0-9]+@[A-Za-z0-9]+\.[A-Za-z]{2,}$/.test(trimmedEmail)) return MESSAGES[currentLang].EMAIL_PATTERN;

            return null;
        } catch (error) {
            return MESSAGES[currentLang].UNKNOWN_ERROR;
        }
    }

    $('#recovery-form').on('submit', async function (event) {
        event.preventDefault();
        if (checkLoginStatus()) return;

        const $submitButton = $('#submit-button');
        const originalButtonText = $submitButton.text();
        $submitButton.prop('disabled', true).text('Đang xử lý...');

        try {
            const rawUsername = $('#username').val() || '';
            const rawEmail = $('#email').val() || '';
            const recoveryFormData = {
                username: rawUsername.trim(),
                email: rawEmail.trim()
            };

            const errorMessage = validateRecoveryForm({ username: rawUsername, email: rawEmail });
            if (errorMessage) {
                showNotification(errorMessage, 'error', NOTIFICATION_DURATION.ERROR);
                $submitButton.prop('disabled', false).text(originalButtonText);
                return;
            }

            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.PASSWORD_RECOVERY}`,
                method: 'POST',
                data: JSON.stringify(recoveryFormData),
                ...ajaxConfig
            });
            showNotification(response.message || MESSAGES[currentLang].RECOVERY_SUCCESS, 'success', NOTIFICATION_DURATION.SUCCESS);
            $('#recovery-form')[0].reset();
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].RECOVERY_FAILED);
        } finally {
            $submitButton.prop('disabled', false).text(originalButtonText);
        }
    });

    checkLoginStatus();
});