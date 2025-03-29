$(document).ready(function () {
    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            PASSWORD_RECOVERY: '/api/users/password-recovery',
            VERIFY_RECOVERY_TOKEN: '/api/users/verify-recovery-token',
            USER_ME: '/api/users/me',
            REFRESH_TOKEN: '/api/users/refresh-token'
        },
        PAGES: {
            RECOVERYPAGE: '/pages/client/recovery.html',
            LOGIN: '/pages/client/login.html'
        }
    };

    const NOTIFICATION_DURATION = {
        SUCCESS: 4000,
        ERROR: 3500,
        EXPIRED: 2000
    };

    const MESSAGES = {
        vi: {
            ALREADY_LOGGED_IN: 'Bạn đã đăng nhập. Chuyển về trang chủ!',
            SESSION_EXPIRED: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!',
            UPDATE_SUCCESS: 'Đổi mật khẩu thành công!',
            UPDATE_FAILED: 'Đã xảy ra lỗi khi đổi mật khẩu',
            INVALID_DATA: 'Dữ liệu không hợp lệ. Vui lòng kiểm tra lại!',
            NOT_FOUND: 'Không tìm thấy người dùng với token khôi phục này',
            TOKEN_INVALID: 'Token khôi phục không hợp lệ hoặc đã hết hạn',
            TOKEN_MISMATCH: 'Token không khớp với tài khoản này!',
            SERVER_ERROR: 'Lỗi máy chủ. Vui lòng thử lại sau!',
            UNKNOWN_ERROR: 'Lỗi không xác định. Vui lòng thử lại!',
            ACCESS_DENIED: 'Bạn không có quyền truy cập trang này!',
            TOKEN_EMPTY: 'Token không được bỏ trống hoặc chỉ là khoảng trắng',
            USERNAME_EMPTY: 'Tài khoản không được bỏ trống hoặc chỉ chứa khoảng trắng',
            NEW_PASSWORD_EMPTY: 'Mật khẩu mới không được bỏ trống hoặc chỉ chứa khoảng trắng',
            NEW_PASSWORD_LENGTH: 'Mật khẩu mới phải có độ dài từ 8 đến 100 ký tự',
            NEW_PASSWORD_WHITESPACE: 'Mật khẩu mới không được chứa khoảng trắng',
            NEW_PASSWORD_PATTERN: 'Mật khẩu mới phải chứa ít nhất một chữ cái và một chữ số',
            CONFIRM_PASSWORD_EMPTY: 'Xác nhận mật khẩu không được bỏ trống hoặc chỉ chứa khoảng trắng',
            CONFIRM_PASSWORD_LENGTH: 'Xác nhận mật khẩu phải có độ dài từ 8 đến 100 ký tự',
            CONFIRM_PASSWORD_PATTERN: 'Xác nhận mật khẩu phải chứa ít nhất một chữ cái và một chữ số',
            PASSWORD_MISMATCH: 'Mật khẩu mới và xác nhận mật khẩu không khớp',
            USERNAME_PASSWORD_SAME: 'Tài khoản và mật khẩu không được trùng khớp!',
            PASSWORD_CURRENT_SAME: 'Mật khẩu mới và mật khẩu hiện tại không được trùng nhau!'
        }
    };
    const currentLang = 'vi';

    let isRefreshing = false;
    let notificationTimeout = null;

    const ajaxConfig = {
        xhrFields: { withCredentials: true },
        contentType: 'application/json',
        timeout: 2000
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
            let redirect = false;
            let msg = message || MESSAGES[currentLang].INVALID_DATA;
            if (errorCode === 'TOKEN_NOT_FOUND_OR_EXPIRED') {
                msg = message || MESSAGES[currentLang].TOKEN_INVALID;
                redirect = true;
            } else if (errorCode === 'TOKEN_MISMATCH') {
                msg = message || MESSAGES[currentLang].TOKEN_MISMATCH;
                redirect = true;
            } else if (errorCode === 'USER_NOT_FOUND') {
                msg = message || MESSAGES[currentLang].NOT_FOUND;
                redirect = true;
            } else if (errorCode === 'USERNAME_PASSWORD_SAME') {
                msg = message || MESSAGES[currentLang].USERNAME_PASSWORD_SAME;
            } else if (errorCode === 'PASSWORD_CURRENT_SAME') {
                msg = message || MESSAGES[currentLang].PASSWORD_CURRENT_SAME;
            } else if (errorCode === 'PASSWORD_MISMATCH') {
                msg = message || MESSAGES[currentLang].PASSWORD_MISMATCH;
            }

            showNotification(msg, 'error', NOTIFICATION_DURATION.ERROR, redirect ? () => {
                window.location.href = CONFIG.PAGES.RECOVERYPAGE;
            } : null);
        },
        401: () => showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error', NOTIFICATION_DURATION.EXPIRED, () => {
            localStorage.removeItem('isLoggedIn');
            window.location.href = CONFIG.PAGES.LOGIN;
        }),
        403: (msg) => showNotification(msg || MESSAGES[currentLang].ACCESS_DENIED, 'error', NOTIFICATION_DURATION.ERROR, () => {
            window.location.href = CONFIG.PAGES.RECOVERYPAGE;
        }),
        404: (msg) => showNotification(msg || MESSAGES[currentLang].NOT_FOUND, 'error', NOTIFICATION_DURATION.ERROR, () => {
            window.location.href = CONFIG.PAGES.RECOVERYPAGE;
        }),
        422: (msg) => showNotification(msg || MESSAGES[currentLang].TOKEN_INVALID, 'error', NOTIFICATION_DURATION.ERROR, () => {
            window.location.href = CONFIG.PAGES.RECOVERYPAGE;
        }),
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
            window.location.href = CONFIG.PAGES.RECOVERYPAGE;
            return true;
        }
        return false;
    }

    async function verifyRecoveryToken(username, recoveryToken) {
        if (!username || !recoveryToken || username.trim() === '' || recoveryToken.trim() === '') {
            window.location.href = CONFIG.PAGES.RECOVERYPAGE;
            return false;
        }

        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.VERIFY_RECOVERY_TOKEN}`,
                method: 'GET',
                data: { username, recoveryToken },
                dataType: 'json',
                ...ajaxConfig
            });
            if (response === true) {
                return true;
            } else {
                window.location.href = CONFIG.PAGES.RECOVERYPAGE;
                return false;
            }
        } catch (xhr) {
            window.location.href = CONFIG.PAGES.RECOVERYPAGE;
            return false;
        }
    }

    function validateUpdatePasswordForm({ username, recoveryToken, newPassword, confirmNewPassword }) {
        try {
            if (!username || username.trim() === '') return MESSAGES[currentLang].USERNAME_EMPTY;

            if (!recoveryToken || recoveryToken.trim() === '') return MESSAGES[currentLang].TOKEN_EMPTY;

            if (!newPassword || newPassword.trim() === '') return MESSAGES[currentLang].NEW_PASSWORD_EMPTY;
            if (/\s/.test(newPassword)) return MESSAGES[currentLang].NEW_PASSWORD_WHITESPACE;
            const trimmedNewPassword = newPassword.trim();
            if (trimmedNewPassword.length < 8 || trimmedNewPassword.length > 100) return MESSAGES[currentLang].NEW_PASSWORD_LENGTH;
            if (!/^(?=.*[a-zA-Z])(?=.*\d).+$/.test(trimmedNewPassword)) return MESSAGES[currentLang].NEW_PASSWORD_PATTERN;

            if (!confirmNewPassword || confirmNewPassword.trim() === '') return MESSAGES[currentLang].CONFIRM_PASSWORD_EMPTY;
            if (/\s/.test(confirmNewPassword)) return MESSAGES[currentLang].NEW_PASSWORD_WHITESPACE;
            const trimmedConfirmPassword = confirmNewPassword.trim();
            if (trimmedConfirmPassword.length < 8 || trimmedConfirmPassword.length > 100) return MESSAGES[currentLang].CONFIRM_PASSWORD_LENGTH;
            if (!/^(?=.*[a-zA-Z])(?=.*\d).+$/.test(trimmedConfirmPassword)) return MESSAGES[currentLang].CONFIRM_PASSWORD_PATTERN;

            if (trimmedNewPassword !== trimmedConfirmPassword) return MESSAGES[currentLang].PASSWORD_MISMATCH;

            return null;
        } catch (error) {
            return MESSAGES[currentLang].UNKNOWN_ERROR;
        }
    }

    async function init() {
        if (checkLoginStatus()) return;

        const urlParams = new URLSearchParams(window.location.search);
        const rawUsername = urlParams.get('username') || '';
        const rawRecoveryToken = urlParams.get('recoveryToken') || '';

        const isValid = await verifyRecoveryToken(rawUsername, rawRecoveryToken);
        if (!isValid) {
            return;
        }

        $('#updatenewpassword-form').on('submit', async function (event) {
            event.preventDefault();

            const $submitButton = $('#submit-button');
            const originalButtonText = $submitButton.text();
            $submitButton.prop('disabled', true).text('Đang xử lý...');

            try {
                const rawNewPassword = $('#newPassword').val() || '';
                const rawConfirmNewPassword = $('#confirmNewPassword').val() || '';

                const updatePasswordData = {
                    username: rawUsername.trim(),
                    recoveryToken: rawRecoveryToken.trim(),
                    newPassword: rawNewPassword.trim(),
                    confirmNewPassword: rawConfirmNewPassword.trim()
                };

                const errorMessage = validateUpdatePasswordForm({
                    username: rawUsername,
                    recoveryToken: rawRecoveryToken,
                    newPassword: rawNewPassword,
                    confirmNewPassword: rawConfirmNewPassword
                });
                if (errorMessage) {
                    showNotification(errorMessage, 'error', NOTIFICATION_DURATION.ERROR);
                    $submitButton.prop('disabled', false).text(originalButtonText);
                    return;
                }

                const response = await ajaxWithRetry({
                    url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.PASSWORD_RECOVERY}`,
                    method: 'PATCH',
                    data: JSON.stringify(updatePasswordData),
                    ...ajaxConfig
                });
                showNotification(response?.message || MESSAGES[currentLang].UPDATE_SUCCESS, 'success', NOTIFICATION_DURATION.SUCCESS, () => {
                    window.location.href = CONFIG.PAGES.LOGIN;
                });
                $('#updatenewpassword-form')[0].reset();
            } catch (xhr) {
                handleAjaxError(xhr, MESSAGES[currentLang].UPDATE_FAILED);
            } finally {
                $submitButton.prop('disabled', false).text(originalButtonText);
            }
        });
    }

    init();
});