$(document).ready(function () {
    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            PURCHASE_HISTORY: '/api/users/transactions/gameaccounts',
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
            PURCHASE_HISTORY_FAILED: 'Không thể tải lịch sử mua hàng. Vui lòng thử lại sau!',
            NO_PERMISSION: 'Bạn không có quyền truy cập chức năng này!',
            NO_MORE_TRANSACTIONS: 'Không còn giao dịch để hiển thị.',
            CSRF_TOKEN_NOT_FOUND: 'CSRF token không tìm thấy. Vui lòng đăng nhập lại!'
        }
    };
    const currentLang = 'vi';

    let isRefreshing = false;
    let isFetching = false;
    let currentPage = 0;
    let pageSize = 10;
    let totalElements = 0;
    let loadedTransactions = 0;

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
        400: (msg) => showNotification(msg || 'Dữ liệu không hợp lệ!', 'error'),
        401: () => showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error', 2000, () => {
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
            window.location.href = CONFIG.PAGES.LOGIN;
        }),
        403: (msg) => showNotification(msg || MESSAGES[currentLang].NO_PERMISSION, 'error'),
        404: (msg) => showNotification(msg || 'Không tìm thấy dữ liệu!', 'error'),
        500: () => showNotification(MESSAGES[currentLang].SERVER_ERROR || 'Lỗi hệ thống. Vui lòng thử lại sau!', 'error'),
        default: (msg) => showNotification(msg || 'Lỗi không xác định!', 'error')
    };

    function handleAjaxError(xhr, defaultMsg) {
        let msg = defaultMsg;
        try {
            const response = JSON.parse(xhr.responseText || '{}');
            msg = response.message || response.error || defaultMsg;
        } catch (e) {
            msg = xhr.responseText || defaultMsg || 'Lỗi không xác định từ server!';
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
            showNotification(MESSAGES[currentLang].NOT_LOGGED_IN, 'error', 2000, () => {
                window.location.href = CONFIG.PAGES.LOGIN;
            });
            return false;
        }
        return true;
    }

    if (checkLoginStatus()) {
        fetchPurchaseHistory(0, pageSize, 'transactionDate,desc');
    }

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

    function fetchPurchaseHistory(page = 0, size = 10, sort = 'transactionDate,desc', append = false) {

        if (loadedTransactions >= totalElements && totalElements !== 0) {
            const $tbody = $('#purchase-history-body');
            if (!$tbody.find('.no-more-data').length) {
                $tbody.append('<tr><td colspan="9" class="text-center no-more-data">Không còn giao dịch để hiển thị.</td></tr>');
            }
            return;
        }

        isFetching = true;

        const $tbody = $('#purchase-history-body');
        if (!append) {
            $tbody.empty();
            loadedTransactions = 0;
            currentPage = 0;
        }
        $tbody.append('<tr><td colspan="9" class="text-center loading-text">Đang tải lịch sử mua hàng...</td></tr>');

        ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.PURCHASE_HISTORY}?page=${page}&size=${size}&sort=${sort}`,
            method: 'GET',
            ...ajaxConfig
        })
            .then(history => {
                renderPurchaseHistory(history, page, size, sort, append);
            })
            .catch(xhr => {
                handleAjaxError(xhr, MESSAGES[currentLang].PURCHASE_HISTORY_FAILED);
            })
            .finally(() => {
                isFetching = false;
                $tbody.find('.loading-text').remove();
            });
    }

    function renderPurchaseHistory(history, page, size, sort, append) {
        const $tbody = $('#purchase-history-body');
        if (!append) {
            $tbody.empty();
        }

        const historyData = history.content || [];

        if (!historyData || historyData.length === 0) {
            if (loadedTransactions === 0) {
                $tbody.append('<tr><td colspan="9" class="text-center">Không có lịch sử mua hàng</td></tr>');
            }
        } else {
            historyData.forEach(item => {
                const row = `
                    <tr>
                        <td>${item.gameAccountType || 'Không có thông tin'}</td>
                        <td>${(item.gameAccountType || 'Không có thông tin') + ' - ' + (item.accountId || 'Không có thông tin')}</td>
                        <td>${formatCurrency(item.price) || '0'}</td>
                        <td>${item.username || 'Không có thông tin'}</td>
                        <td>${item.password || 'Không có thông tin'}</td>
                        <td>${item.email || 'Không có thông tin'}</td>
                        <td>${item.phone || 'Không có thông tin'}</td>
                        <td>${item.transactionDate ? formatDateTime(item.transactionDate) : 'Không có thông tin'}</td>
                        <td>${item.description || 'Không có thông tin'}</td>
                    </tr>
                `;
                $tbody.append(row);
            });

            loadedTransactions += historyData.length;
        }

        const paginationData = history.page || {};
        currentPage = paginationData.number !== undefined ? paginationData.number : 0;
        pageSize = paginationData.size !== undefined ? paginationData.size : 10;
        totalElements = paginationData.totalElements !== undefined ? paginationData.totalElements : 0;

        $('#purchase-history-pagination').empty();
    }

    $(window).on('scroll', function () {
        if (isFetching || loadedTransactions >= totalElements) return;

        const scrollPosition = $(window).scrollTop() + $(window).height();
        const pageHeight = $(document).height();

        if (scrollPosition >= pageHeight - 100) {
            currentPage++;
            const sort = $('#purchase-history-sort').val();
            fetchPurchaseHistory(currentPage, pageSize, sort, true);
        }
    });

    $('#purchase-history-sort').on('change', function () {
        const sort = $(this).val();
        currentPage = 0;
        loadedTransactions = 0;
        totalElements = 0;
        fetchPurchaseHistory(0, pageSize, sort);
    });

    function formatDateTime(timestamp) {
        if (!timestamp) return 'Không có thông tin';
        const date = new Date(timestamp);
        if (isNaN(date.getTime())) return 'Không có thông tin';
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${hours}:${minutes} ${day}/${month}/${year}`;
    }

    function formatCurrency(amount) {
        if (amount == null || isNaN(amount)) return '0';
        return amount.toLocaleString('vi-VN');
    }
});