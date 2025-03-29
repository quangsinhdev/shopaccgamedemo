$(document).ready(function () {
    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            LOGIN: '/api/users/login',
            REGISTER: '/api/users/register',
            USER_ME: '/api/users/me',
            REFRESH_TOKEN: '/api/users/refresh-token'
        },
        PAGES: {
            HOME: '/',
            CLIENT_HOME: 'https://localhost:3000/'
        }
    };

    const MESSAGES = {
        vi: {
            NOT_LOGGED_IN: 'Bạn chưa đăng nhập. Vui lòng đăng nhập!',
            SESSION_EXPIRED: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!',
            LOGIN_FAILED: 'Tài khoản hoặc mật khẩu không đúng!',
            ACCOUNT_LOCKED: 'Tài khoản của bạn đã bị khóa!',
            SERVER_ERROR: 'Lỗi hệ thống. Vui lòng thử lại sau!',
            UNKNOWN_ERROR: 'Lỗi không xác định',
            REGISTRATION_SUCCESS: 'Đăng ký thành công!',
            REGISTRATION_FAILED: 'Đăng ký thất bại!',
            OAUTH2_SUCCESS: 'Đăng nhập OAuth2 thành công!',
            OAUTH2_LOCKED: 'Tài khoản của bạn đã bị khóa!',
            USERNAME_EMPTY: 'Tài khoản không được bỏ trống hoặc chứa khoảng trắng',
            USERNAME_LENGTH: 'Tài khoản có độ dài tối thiểu 8 ký tự và tối đa 50 ký tự',
            USERNAME_PATTERN: 'Tài khoản chỉ có thể chứa số và chữ cái',
            PASSWORD_EMPTY: 'Mật khẩu không được bỏ trống hoặc chỉ là khoảng trắng',
            PASSWORD_LENGTH: 'Mật khẩu có độ dài tối thiểu 8 ký tự và tối đa 100 ký tự',
            PASSWORD_PATTERN: 'Mật khẩu phải chứa ít nhất một chữ cái và một chữ số',
            WHITESPACE: 'Không được chứa khoảng trắng',
            FULLNAME_EMPTY: 'Họ tên hoặc nickname không được bỏ trống',
            FULLNAME_LENGTH: 'Họ tên hoặc nickname tối thiểu 4 ký tự, tối đa 80 ký tự',
            FULLNAME_PATTERN: 'Họ tên hoặc nickname không hợp lệ (không bắt đầu/kết thúc bằng khoảng trắng)',
            EMAIL_EMPTY: 'Email không được bỏ trống',
            EMAIL_LENGTH: 'Email phải từ 6 đến 80 ký tự',
            EMAIL_INVALID: 'Email không hợp lệ',
            CONFIRM_PASSWORD_MISMATCH: 'Mật khẩu xác nhận không khớp',
            CSRF_TOKEN_MISSING: 'Không nhận được CSRF token từ Server!'
        }
    };
    const currentLang = 'vi';

    let isProcessing = false;
    let isRefreshing = false;
    let currentTimeout = null;
    let unlockButtonTimeout = null;
    let isButtonLocked = false;

    const ajaxConfig = {
        xhrFields: { withCredentials: true },
        contentType: 'application/json',
        timeout: 5000
    };

    const showNotification = (message, type = 'error', duration = 3000) => {
        if (currentTimeout) {
            clearTimeout(currentTimeout);
            currentTimeout = null;
        }

        const $notification = $('#notification-login');
        const $text = $('#notification-login-text');
        if (!$notification.length || !$text.length) {
            alert(message);
            return;
        }

        $notification.stop(true, true).hide().removeClass('error success locked');
        $text.text(message);
        $notification.addClass(type).fadeIn(500);
        currentTimeout = setTimeout(() => {
            $notification.fadeOut(500, () => {
                $notification.removeClass(type);
                currentTimeout = null;
            });
        }, duration);
    };

    const errorHandlers = {
        400: (msg) => showNotification(msg || 'Dữ liệu không hợp lệ!', 'error'),
        401: (msg) => showNotification(msg || MESSAGES[currentLang].LOGIN_FAILED, 'error'),
        403: (msg, lockMinutes) => {
            showNotification(msg || MESSAGES[currentLang].ACCOUNT_LOCKED, 'error');
            if (lockMinutes) lockLoginButton(lockMinutes);
        },
        409: (msg) => showNotification(msg || 'Tài khoản đã tồn tại!', 'error'),
        422: (msg) => showNotification(msg || 'Dữ liệu không hợp lệ!', 'error'),
        500: (msg) => showNotification(msg || MESSAGES[currentLang].SERVER_ERROR, 'error'),
        default: (msg) => showNotification(msg || MESSAGES[currentLang].UNKNOWN_ERROR, 'error')
    };

    function handleAjaxError(xhr, defaultMsg) {
        let msg = defaultMsg;
        try {
            const response = JSON.parse(xhr.responseText || '{}');
            msg = response.message || response.errorMessage || defaultMsg; // Xóa response.error
            if (response.errorMessages?.length > 0) msg = response.errorMessages.join(', ');
        } catch (e) {
            msg = xhr.responseText || defaultMsg || MESSAGES[currentLang].UNKNOWN_ERROR;
        }
        const lockMatch = msg.match(/Thử lại sau: (\d+) phút/);
        const lockMinutes = lockMatch ? parseInt(lockMatch[1], 10) : null;
        (errorHandlers[xhr.status] || errorHandlers.default)(msg, lockMinutes);
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
            } else {
                showNotification(MESSAGES[currentLang].CSRF_TOKEN_MISSING, 'error');
            }
            isRefreshing = false;
            return true;
        } catch (xhr) {
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
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

    function lockLoginButton(minutes) {
        const $loginButton = $('#login-submit-button');
        isButtonLocked = true;
        $loginButton.prop('disabled', true).text(`Tạm khóa đăng nhập trong ${minutes} phút`);

        const lockDuration = minutes * 60 * 1000;
        if (unlockButtonTimeout) clearTimeout(unlockButtonTimeout);

        unlockButtonTimeout = setTimeout(() => {
            isButtonLocked = false;
            $loginButton.prop('disabled', false).text('Đăng nhập');
            showNotification('Tài khoản đã được mở khóa. Vui lòng thử lại!', 'success', 3000);
        }, lockDuration);
    }

    function updateDropdown(fullname, balance) {
        const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        $('#login-form').toggle(!isLoggedIn);
        $('#user-dropdown').toggle(isLoggedIn);
        $('#fullname-display').text(isLoggedIn ? fullname || 'N/A' : '');
        $('#balance-display').text(isLoggedIn ? (balance ? balance.toLocaleString('vi-VN') : '0') : '');
    }

    async function fetchUserDetails() {
        try {
            const user = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.USER_ME}`,
                method: 'GET',
                ...ajaxConfig
            });
            localStorage.setItem('isLoggedIn', 'true');
            return { success: true, fullname: user.fullname, balance: user.balance };
        } catch (xhr) {
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
            return { success: false, fullname: '', balance: 0 };
        }
    }

    function checkLoginStatus() {
        const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        if (isLoggedIn) {
            fetchUserDetails().then(({ success, fullname, balance }) => {
                updateDropdown(success ? fullname : '', success ? balance : 0);
            });
        } else {
            updateDropdown('', 0);
        }
    }

    function checkOAuth2Response() {
        const urlParams = new URLSearchParams(window.location.search);
        const locked = urlParams.get('locked');
        const token = urlParams.get('token');

        if (token) {
            localStorage.setItem('isLoggedIn', 'true');
            showNotification(MESSAGES[currentLang].OAUTH2_SUCCESS, 'success', 3000);
            setTimeout(() => window.location.href = CONFIG.PAGES.CLIENT_HOME, 3000);
        } else if (locked === 'accountlocked') {
            showNotification(MESSAGES[currentLang].OAUTH2_LOCKED, 'error', 4000);
        }
    }

    function validateLoginForm({ username, password }) {
        if (!username || username.trim() === '') return MESSAGES[currentLang].USERNAME_EMPTY;
        if (username.length < 8 || username.length > 50) return MESSAGES[currentLang].USERNAME_LENGTH;
        if (!/^[a-zA-Z0-9]+$/.test(username)) return MESSAGES[currentLang].USERNAME_PATTERN;
        if (/\s/.test(username)) return MESSAGES[currentLang].WHITESPACE;

        if (!password || password.trim() === '') return MESSAGES[currentLang].PASSWORD_EMPTY;
        if (password.length < 8 || password.length > 100) return MESSAGES[currentLang].PASSWORD_LENGTH;
        if (!/^(?=.*[a-zA-Z])(?=.*\d).+$/.test(password)) return MESSAGES[currentLang].PASSWORD_PATTERN;
        if (/\s/.test(password)) return MESSAGES[currentLang].WHITESPACE;

        return null;
    }

    function validateRegisterForm({ fullname, email, username, password, confirmPassword }) {
        if (!fullname || fullname.trim() === '') return MESSAGES[currentLang].FULLNAME_EMPTY;
        if (fullname.length < 4 || fullname.length > 80) return MESSAGES[currentLang].FULLNAME_LENGTH;
        if (!/^[^\s].*[^\s]$/.test(fullname) || !/[^\s\d]/.test(fullname)) {
            return MESSAGES[currentLang].FULLNAME_PATTERN;
        }

        if (!email || email.trim() === '') return MESSAGES[currentLang].EMAIL_EMPTY;
        if (email.length < 6 || email.length > 80) return MESSAGES[currentLang].EMAIL_LENGTH;
        if (!/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/.test(email)) return MESSAGES[currentLang].EMAIL_INVALID;

        if (!username || username.trim() === '') return MESSAGES[currentLang].USERNAME_EMPTY;
        if (username.length < 8 || username.length > 50) return MESSAGES[currentLang].USERNAME_LENGTH;
        if (!/^[a-zA-Z0-9]+$/.test(username)) return MESSAGES[currentLang].USERNAME_PATTERN;
        if (/\s/.test(username)) return MESSAGES[currentLang].WHITESPACE;

        if (!password || password.trim() === '') return MESSAGES[currentLang].PASSWORD_EMPTY;
        if (password.length < 8 || password.length > 100) return MESSAGES[currentLang].PASSWORD_LENGTH;
        if (!/^(?=.*[a-zA-Z])(?=.*\d).+$/.test(password)) return MESSAGES[currentLang].PASSWORD_PATTERN;
        if (/\s/.test(password)) return MESSAGES[currentLang].WHITESPACE;

        if (!confirmPassword || confirmPassword.trim() === '') return MESSAGES[currentLang].PASSWORD_EMPTY;
        if (confirmPassword.length < 8 || confirmPassword.length > 100) return MESSAGES[currentLang].PASSWORD_LENGTH;
        if (!/^(?=.*[a-zA-Z])(?=.*\d).+$/.test(confirmPassword)) return MESSAGES[currentLang].PASSWORD_PATTERN;
        if (password !== confirmPassword) return MESSAGES[currentLang].CONFIRM_PASSWORD_MISMATCH;

        return null;
    }

    $('#login-form').on('submit', async function (event) {
        event.preventDefault();
        if (isProcessing || isButtonLocked) return;

        isProcessing = true;
        const $loginButton = $('#login-submit-button');
        $loginButton.prop('disabled', true).text('Đang xử lý...');

        const loginData = {
            username: $('#login-username').val(),
            password: $('#login-password').val()
        };

        const loginError = validateLoginForm(loginData);
        if (loginError) {
            showNotification(loginError, 'error');
            isProcessing = false;
            $loginButton.prop('disabled', false).text('Đăng nhập');
            return;
        }

        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.LOGIN}`,
                method: 'POST',
                data: JSON.stringify(loginData),
                ...ajaxConfig
            });
            if (response.csrfToken) {
                localStorage.setItem('isLoggedIn', 'true');
                localStorage.setItem('csrfToken', response.csrfToken);
                const { success, fullname, balance } = await fetchUserDetails();
                if (success) {
                    updateDropdown(fullname, balance);
                    window.location.href = CONFIG.PAGES.HOME;
                }
            } else {
                showNotification(MESSAGES[currentLang].CSRF_TOKEN_MISSING, 'error');
            }
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].LOGIN_FAILED);
        } finally {
            isProcessing = false;
            if (!isButtonLocked) {
                $loginButton.prop('disabled', false).text('Đăng nhập');
            }
        }
    });

    $('#register-form').on('submit', async function (event) {
        event.preventDefault();
        if (isProcessing) return;

        isProcessing = true;
        const $submitButton = $('#submit-button');
        $submitButton.prop('disabled', true).text('Đang xử lý...');

        const formData = {
            fullname: $('#fullname').val(),
            email: $('#email').val(),
            username: $('#username').val(),
            password: $('#password').val(),
            confirmPassword: $('#confirmPassword').val()
        };

        const registerError = validateRegisterForm(formData);
        if (registerError) {
            showNotification(registerError, 'error');
            isProcessing = false;
            $submitButton.prop('disabled', false).text('Tạo tài khoản');
            return;
        }

        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.REGISTER}`,
                method: 'POST',
                data: JSON.stringify(formData),
                ...ajaxConfig
            });
            if (response.success) {
                showNotification(response.successMessage || MESSAGES[currentLang].REGISTRATION_SUCCESS, 'success', 3000);
                setTimeout(() => {
                    $('#register-modal').hide();
                    $('#login-form').show();
                }, 3000);
            } else {
                showNotification(response.errorMessage || MESSAGES[currentLang].REGISTRATION_FAILED, 'error');
            }
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].REGISTRATION_FAILED);
        } finally {
            isProcessing = false;
            $submitButton.prop('disabled', false).text('Tạo tài khoản');
        }
    });

    $('#show-register').on('click', (e) => {
        e.preventDefault();
        $('#login-form').hide();
        $('#register-modal').show();
    });

    $('#show-login').on('click', (e) => {
        e.preventDefault();
        $('#register-modal').hide();
        $('#login-form').show();
    });

    $('.info-icon').on('click', () => $('#modalOverlay').toggleClass('visible'));
    $('#closeModal').on('click', () => $('#modalOverlay').removeClass('visible'));

    checkLoginStatus();
    checkOAuth2Response();
});