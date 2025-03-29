$(document).ready(function () {
    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            USER_ME: '/api/users/me',
            DEPOSIT_CARD: '/api/users/transactions/card-deposits',
            DEPOSIT_VNPAY: '/api/users/transactions/vnpay',
            VNPAY_RETURN: '/api/users/transactions/vnpay/return',
            LOGOUT: '/api/users/logout',
            REFRESH_TOKEN: '/api/users/refresh-token',
            DEPOSIT_PAYMENT_INFO: '/api/users/deposit-payment-info'
        },
        PAGES: {
            LOGIN: '/pages/client/login.html',
            RECHARGE: '/pages/client/member/recharge.html'
        },
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

    const NOTIFICATION_DURATION = {
        SUCCESS: 4000,
        ERROR: 3500,
        EXPIRED: 2000
    };

    const MESSAGES = {
        vi: {
            NOT_LOGGED_IN: 'Bạn chưa đăng nhập. Vui lòng đăng nhập!',
            SESSION_EXPIRED: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!',
            LOGOUT_FAILED: 'Đăng xuất thất bại',
            INVALID_DATA: 'Dữ liệu không hợp lệ',
            CARD_EXISTS: 'Thẻ đã tồn tại trên hệ thống',
            SERVER_ERROR: 'Lỗi hệ thống. Vui lòng thử lại sau!',
            UNKNOWN_ERROR: 'Lỗi không xác định',
            CONNECTION_ERROR: 'Lỗi kết nối, vui lòng kiểm tra mạng',
            PAYMENT_CREATION_FAILED: 'Không thể tạo link thanh toán VNPay',
            RECHARGE_FAILED: 'Nạp tiền thất bại!',
            INVALID_RESPONSE: 'Phản hồi từ server không hợp lệ',
            FETCH_DEPOSIT_INFO_FAILED: 'Không thể tải thông tin chi phí giao dịch',
            CARD_PROVIDER_INVALID: 'Loại thẻ cào không hợp lệ',
            CARD_SERIAL_EMPTY: 'Serial thẻ không được bỏ trống',
            CARD_SERIAL_LENGTH: 'Serial thẻ cào không đúng. Vui lòng kiểm tra lại (5-20 ký tự)',
            CARD_SERIAL_PATTERN: 'Serial thẻ chỉ được phép chứa chữ cái và số, không chứa khoảng trắng',
            CARD_CODE_EMPTY: 'Mã thẻ không được bỏ trống',
            CARD_CODE_LENGTH: 'Mã thẻ không đúng. Vui lòng kiểm tra lại (5-20 ký tự)',
            CARD_CODE_PATTERN: 'Mã thẻ chỉ được phép chứa chữ cái và số, không chứa khoảng trắng',
            CARD_VALUE_RANGE: 'Mệnh giá thẻ cào phải từ 10.000 đến 2.000.000 VND',
            VNPAY_AMOUNT_EMPTY: 'Số tiền cần nạp không được bỏ trống',
            VNPAY_AMOUNT_RANGE: 'Số tiền cần nạp phải từ 10.000 đến 1.000.000.000 VND',
            VNPAY_ORDERINFO_EMPTY: 'Mô tả nạp tiền VNPay không được bỏ trống',
            VNPAY_ORDERINFO_PATTERN: 'Mô tả đơn hàng nạp tiền VNPay không đúng (chỉ chứa chữ cái, số và khoảng trắng)',
            REFRESH_TOKEN_INVALID: 'Không thể làm mới phiên đăng nhập. Vui lòng đăng nhập lại!'
        }
    };
    const currentLang = 'vi';

    let isRefreshing = false;

    const ajaxConfig = {
        xhrFields: { withCredentials: true },
        contentType: 'application/json',
        timeout: 10000
    };

    const showNotification = (message, type = 'error', duration = 3000, callback) => {
        const $notification = $('#notification');
        if (!$notification.length) {
            alert(message);
            if (callback) setTimeout(callback, duration);
            return;
        }
        const $icon = $('#notification-icon');
        const $text = $('#notification-text');
        const config = {
            success: { icon: '<i class="fa-solid fa-check-circle"></i>', class: 'success' },
            error: { icon: '<i class="fa-solid fa-exclamation-circle"></i>', class: 'error' }
        };
        const safeType = config[type] ? type : 'error';
        $notification.removeClass('success error show hidden').hide();
        $icon.html(config[safeType].icon);
        $text.text(message);
        $notification.addClass(config[safeType].class).addClass('show').show();
        setTimeout(() => {
            $notification.removeClass('show').addClass('hidden');
            if (callback) callback();
        }, duration);
    };

    const errorHandlers = {
        400: (msg) => showNotification(msg || MESSAGES[currentLang].INVALID_DATA, 'error', NOTIFICATION_DURATION.ERROR),
        401: (msg) => showNotification(msg || MESSAGES[currentLang].SESSION_EXPIRED, 'error', NOTIFICATION_DURATION.EXPIRED, () => {
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
            window.location.href = CONFIG.PAGES.LOGIN;
        }),
        403: (msg) => showNotification(msg || 'Bạn không có quyền thực hiện hành động này!', 'error', NOTIFICATION_DURATION.ERROR),
        409: (msg) => showNotification(msg || MESSAGES[currentLang].CARD_EXISTS, 'error', NOTIFICATION_DURATION.ERROR),
        500: (msg) => showNotification(msg || MESSAGES[currentLang].SERVER_ERROR, 'error', NOTIFICATION_DURATION.ERROR),
        default: (msg) => showNotification(msg || MESSAGES[currentLang].UNKNOWN_ERROR, 'error', NOTIFICATION_DURATION.ERROR)
    };

    function handleAjaxError(xhr, defaultMsg) {
        let msg = defaultMsg;
        if (xhr instanceof Error) {
            msg = xhr.message === 'timeout' ? MESSAGES[currentLang].CONNECTION_ERROR : `Lỗi: ${xhr.message}`;
            showNotification(msg, 'error', NOTIFICATION_DURATION.ERROR);
            return false;
        }
        try {
            const response = xhr.responseText ? JSON.parse(xhr.responseText) : {};
            msg = response.message || response.error || defaultMsg || MESSAGES[currentLang].INVALID_RESPONSE;
        } catch (e) {
            msg = defaultMsg || MESSAGES[currentLang].INVALID_RESPONSE;
        }
        const handler = errorHandlers[xhr.status] || errorHandlers.default;
        handler(msg);
        return false;
    }

    function checkTokenExists(tokenName) {
        const cookies = document.cookie.split(';');
        return cookies.some(cookie => cookie.trim().startsWith(`${tokenName}=`));
    }

    async function refreshToken() {
        if (isRefreshing) return false;
        isRefreshing = true;
        try {
            const response = await $.ajax({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.REFRESH_TOKEN}`,
                method: 'POST',
                ...ajaxConfig,
                beforeSend: function(xhr) {
                }
            });
            localStorage.setItem('isLoggedIn', 'true');
            if (response.csrfToken) {
                localStorage.setItem('csrfToken', response.csrfToken);
            }
            isRefreshing = false;
            return true;
        } catch (xhr) {
            isRefreshing = false;
            return false;
        }
    }

    async function ajaxWithRetry(settings) {
        const isSafeMethod = ['GET', 'HEAD', 'OPTIONS'].includes(settings.method.toUpperCase());
        const isExcluded = CONFIG.CSRF_EXCLUDED_PATHS.some(path => settings.url.includes(path));
    
    
        if (!isSafeMethod && !isExcluded) {
            const csrfToken = localStorage.getItem('csrfToken');
            if (!csrfToken) {
                showNotification('CSRF token không tìm thấy. Vui lòng đăng nhập lại!', 'error', NOTIFICATION_DURATION.ERROR, () => {
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
    
        try {
            return await $.ajax(settings);
        } catch (xhr) {
            if (xhr.status === 401) {

                const refreshed = await refreshToken();
                if (refreshed) {
                    if (!isSafeMethod && !isExcluded) {
                        const newCsrfToken = localStorage.getItem('csrfToken');
                        if (!newCsrfToken) {
                            showNotification('CSRF token không tìm thấy sau khi làm mới. Vui lòng đăng nhập lại!', 'error', NOTIFICATION_DURATION.ERROR, () => {
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
                    showNotification(MESSAGES[currentLang].REFRESH_TOKEN_INVALID, 'error', NOTIFICATION_DURATION.EXPIRED, () => {
                        localStorage.removeItem('isLoggedIn');
                        localStorage.removeItem('csrfToken');
                        window.location.href = CONFIG.PAGES.LOGIN;
                    });
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
        return true;
    }

    async function fetchUserDetails() {
        if (!checkLoginStatus()) return false;

        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.USER_ME}`,
                method: 'GET',
                ...ajaxConfig
            });
            if (!response?.id) throw new Error('Invalid user data');
            $('#balance-display').text(response.balance?.toLocaleString('vi-VN') || '0');
            return true;
        } catch (xhr) {
            return handleAjaxError(xhr, 'Không thể tải thông tin người dùng');
        }
    }

    async function fetchDepositPaymentInfo() {
        if (!checkLoginStatus()) return false;

        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.DEPOSIT_PAYMENT_INFO}`,
                method: 'GET',
                ...ajaxConfig
            });
            if (!response) throw new Error('Invalid deposit payment info');
            displayDepositPaymentInfo(response);
            return true;
        } catch (xhr) {
            return handleAjaxError(xhr, MESSAGES[currentLang].FETCH_DEPOSIT_INFO_FAILED);
        }
    }

    function displayDepositPaymentInfo(data) {
        const $viettelFee = $('.viettel-fee p');
        const $mobifoneFee = $('.mobifone-fee p');
        const $vinaphoneFee = $('.vinaphone-fee p');

        if ($viettelFee.length) {
            $viettelFee.text(`Chiết khấu: ${data.viettelTradeCost}%`);
        }
        if ($mobifoneFee.length) {
            $mobifoneFee.text(`Chiết khấu: ${data.mobifoneTradeCost}%`);
        }
        if ($vinaphoneFee.length) {
            $vinaphoneFee.text(`Chiết khấu: ${data.vinaphoneTradeCost}%`);
        }

        const $momoQrCode = $('.qr-momo .qr-code img');
        const $viettelPayQrCode = $('.qr-viettel-money .qr-code img');

        if ($momoQrCode.length) {
            if (data.qrCodeMomo && data.qrCodeMomo.trim() !== "") {
                $momoQrCode.attr('src', data.qrCodeMomo);
            } else {
                $momoQrCode.replaceWith('<p>Không có QR Code Momo</p>');
            }
        }
        if ($viettelPayQrCode.length) {
            if (data.qrCodeViettelPay && data.qrCodeViettelPay.trim() !== "") {
                $viettelPayQrCode.attr('src', data.qrCodeViettelPay);
            } else {
                $viettelPayQrCode.replaceWith('<p>Không có QR Code ViettelPay</p>');
            }
        }
    }

    function validateCardForm({ depositCardNetworkProvider, serial, code, value }) {
        const validProviders = ['VIETTEL', 'VINAPHONE', 'MOBIFONE'];
        if (!depositCardNetworkProvider || !validProviders.includes(depositCardNetworkProvider)) {
            return MESSAGES[currentLang].CARD_PROVIDER_INVALID;
        }
        if (!serial || serial.trim() === '') return MESSAGES[currentLang].CARD_SERIAL_EMPTY;
        if (serial.length < 5 || serial.length > 20) return MESSAGES[currentLang].CARD_SERIAL_LENGTH;
        if (!/^[a-zA-Z0-9]+$/.test(serial)) return MESSAGES[currentLang].CARD_SERIAL_PATTERN;

        if (!code || code.trim() === '') return MESSAGES[currentLang].CARD_CODE_EMPTY;
        if (code.length < 5 || code.length > 20) return MESSAGES[currentLang].CARD_CODE_LENGTH;
        if (!/^[a-zA-Z0-9]+$/.test(code)) return MESSAGES[currentLang].CARD_CODE_PATTERN;

        if (isNaN(value) || value < 10000 || value > 2000000) return MESSAGES[currentLang].CARD_VALUE_RANGE;

        return null;
    }

    function validateVNPayForm({ amount, orderInfo }) {
        if (!amount || isNaN(amount)) return MESSAGES[currentLang].VNPAY_AMOUNT_EMPTY;
        if (amount < 10000 || amount > 1000000000) return MESSAGES[currentLang].VNPAY_AMOUNT_RANGE;

        if (!orderInfo || orderInfo.trim() === '') return MESSAGES[currentLang].VNPAY_ORDERINFO_EMPTY;
        if (!/^[a-zA-Z0-9\s]+$/.test(orderInfo)) return MESSAGES[currentLang].VNPAY_ORDERINFO_PATTERN;

        return null;
    }

    async function handleCardRecharge(event) {
        event.preventDefault();
        if (!checkLoginStatus()) return;

        const formData = {
            depositCardNetworkProvider: $('#card-type').val()?.toUpperCase(),
            serial: $('#card-serial').val()?.trim(),
            code: $('#card-pin').val()?.trim(),
            value: parseInt($('#card-value').val())
        };

        const errorMessage = validateCardForm(formData);
        if (errorMessage) {
            showNotification(errorMessage, 'error', NOTIFICATION_DURATION.ERROR);
            return;
        }

        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.DEPOSIT_CARD}`,
                method: 'POST',
                data: JSON.stringify(formData),
                ...ajaxConfig
            });
            showNotification('Gửi yêu cầu nạp thẻ thành công, vui lòng chờ xử lý!', 'success', NOTIFICATION_DURATION.SUCCESS);
            $('#card-recharge-form')[0].reset();
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].RECHARGE_FAILED);
        }
    }

    async function handleVNPayRecharge(event) {
        event.preventDefault();
        if (!checkLoginStatus()) return;

        const amount = parseInt($('#vnpay-amount').val());
        const orderInfo = `Nap tien ${amount} VND`;
        const formData = { amount, orderInfo };

        const errorMessage = validateVNPayForm(formData);
        if (errorMessage) {
            showNotification(errorMessage, 'error', NOTIFICATION_DURATION.ERROR);
            return;
        }

        try {
            if (!(await fetchUserDetails())) return;
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.DEPOSIT_VNPAY}`,
                method: 'POST',
                data: JSON.stringify(formData),
                ...ajaxConfig
            });
            if (response.paymentUrl) {
                showNotification('Đang chuyển hướng tới VNPay...', 'success', NOTIFICATION_DURATION.SUCCESS, () => {
                    window.location.href = response.paymentUrl;
                });
            } else {
                showNotification(MESSAGES[currentLang].PAYMENT_CREATION_FAILED, 'error', NOTIFICATION_DURATION.ERROR);
            }
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].RECHARGE_FAILED);
        }
    }

    async function handleVNPayReturn() {
        const urlParams = new URLSearchParams(window.location.search);
        if (!urlParams.has('vnp_TxnRef')) return;

        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.VNPAY_RETURN}${window.location.search}`,
                method: 'GET',
                ...ajaxConfig
            });
            if (response.success) {
                showNotification(`Nạp tiền thành công! Mã giao dịch: ${response.transactionId}`, 'success', NOTIFICATION_DURATION.SUCCESS);
            } else {
                showNotification(MESSAGES[currentLang].RECHARGE_FAILED, 'error', NOTIFICATION_DURATION.ERROR);
            }
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].RECHARGE_FAILED);
        }
        window.history.replaceState({}, document.title, CONFIG.PAGES.RECHARGE);
    }

    $('#logout-btn').on('click', async function (event) {
        event.preventDefault();
        const $button = $(this);
        const originalText = $button.text();
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
            $button.text(originalText).prop('disabled', false);
        }
    });

    async function init() {
        if (!checkLoginStatus()) return;

        const userDetailsFetched = await fetchUserDetails();
        if (!userDetailsFetched) return;

        const depositInfoFetched = await fetchDepositPaymentInfo();
        if (!depositInfoFetched) return;

        handleVNPayReturn();

        $('#user-avatar').on('click', () => $('#avatar-dropdown').toggleClass('show'));
        $('#card-recharge-form').on('submit', handleCardRecharge);
        $('#vnpay-recharge-form').on('submit', handleVNPayRecharge);
    }

    init();
});