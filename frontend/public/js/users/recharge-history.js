$(document).ready(function () {
    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            TRANSACTIONS_CARD: '/api/users/transactions/card-deposits',
            TRANSACTIONS_VNPAY: '/api/users/transactions/vnpay',
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
            LOGOUT_SUCCESS: 'Đăng xuất thành công!',
            LOGOUT_FAILED: 'Đăng xuất thất bại',
            TRANSACTIONS_FAILED: 'Không thể tải lịch sử nạp tiền. Vui lòng thử lại sau!',
            NO_PERMISSION: 'Bạn không có quyền truy cập chức năng này!',
            REFRESH_SUCCESS: 'Làm mới thành công!',
            NO_MORE_TRANSACTIONS: 'Không còn giao dịch để hiển thị.',
            CSRF_TOKEN_NOT_FOUND: 'CSRF token không tìm thấy. Vui lòng đăng nhập lại!'
        }
    };
    const currentLang = 'vi';

    let isRefreshing = false;
    let isFetching = false;
    let filters = {};

    const tabStates = {
        card: {
            currentPage: 0,
            pageSize: 10,
            loadedTransactions: 0,
            totalElements: 0
        },
        vnpay: {
            currentPage: 0,
            pageSize: 10,
            loadedTransactions: 0,
            totalElements: 0
        }
    };

    const ajaxConfig = {
        xhrFields: { withCredentials: true },
        contentType: 'application/json',
        timeout: 10000
    };

    const showNotification = (message, type, duration = 3000, callback) => {
        $('.notification').remove();
        const $notification = $('<div>', { id: 'notification', class: 'notification' });
        const icon = type === 'success' ? '✔' : '✖';
        $notification.html(`<span class="icon">${icon}</span><span>${message}</span>`);
        $('body').append($notification);
        $notification.addClass(type === 'success' ? 'success' : 'error').addClass('show');
        setTimeout(() => {
            $notification.removeClass('show').addClass('hidden');
            setTimeout(() => {
                $notification.remove();
                if (callback) callback();
            }, 500);
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
        500: () => showNotification('Lỗi hệ thống. Vui lòng thử lại sau!', 'error'),
        default: (msg) => showNotification(msg || MESSAGES[currentLang].TRANSACTIONS_FAILED, 'error')
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
        const isSafeMethod = ['GET', 'HEAD', 'OPTIONS'].includes(settings.method.toUpperCase());
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
        return true;
    }

    function normalizeNetworkProvider(provider) {
        const normalized = (provider || '').toUpperCase();
        const providerMap = {
            'VIETTEL': 'Viettel',
            'MOBIFONE': 'Mobifone',
            'VINAPHONE': 'Vinaphone'
        };
        return providerMap[normalized] || provider || 'Không có thông tin';
    }

    function normalizeStatus(status) {
        const normalizedStatus = (status || '').toLowerCase();
        const statusMap = {
            'pending': 'Chờ duyệt',
            'in_progress': 'Chờ duyệt',
            'success': 'Thành công',
            'successful': 'Thành công',
            'rejected': 'Thất bại',
            'failed': 'Thất bại'
        };
        return statusMap[normalizedStatus] || status || 'Không có thông tin';
    }

    function formatDateTime(date) {
        if (!date) return 'Không có thông tin';
        const d = new Date(date);
        const hours = String(d.getHours()).padStart(2, '0');
        const minutes = String(d.getMinutes()).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const year = d.getFullYear();
        return `${hours}:${minutes} ${day}/${month}/${year}`;
    }

    function formatCurrency(amount) {
        if (amount == null || isNaN(amount)) return '0';
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }

    function getStatusClass(status) {
        const normalizedStatus = (status || '').toLowerCase();
        const statusClassMap = {
            'success': 'status-success',
            'successful': 'status-success',
            'pending': 'status-pending',
            'in_progress': 'status-pending',
            'rejected': 'status-rejected',
            'failed': 'status-rejected'
        };
        return statusClassMap[normalizedStatus] || '';
    }

    async function loadRechargeHistory(tab, page = 0, size = 10, sort = 'timeOfDepositing,desc', append = false) {
        if (!checkLoginStatus()) return;
    
        const state = tabStates[tab];
        if (state.loadedTransactions >= state.totalElements && state.totalElements !== 0) {
            const $tbody = $(`#${tab} .purchase-history tbody`);
            if (!$tbody.find('.no-more-data').length) {
                const colSpan = tab === 'card' ? 7 : 4;
                $tbody.append(`<tr><td colspan="${colSpan}" class="text-center no-more-data">${MESSAGES[currentLang].NO_MORE_TRANSACTIONS}</td></tr>`);
            }
            return;
        }
    
        isFetching = true;
    
        const $table = $(`#${tab} .purchase-history`);
        const $tbody = $(`#${tab} .purchase-history tbody`);
        const $loadingContainer = $(`#${tab} .loading-container`);
        $table.addClass('loading');
    
        if (!append) {
            $tbody.empty();
            state.loadedTransactions = 0;
            state.currentPage = 0;
        }
        $loadingContainer.html('Đang tải lịch sử nạp tiền...').addClass('show');
    
        const endpoint = tab === 'card' ? CONFIG.API_ENDPOINTS.TRANSACTIONS_CARD : CONFIG.API_ENDPOINTS.TRANSACTIONS_VNPAY;
        if (!endpoint) {
            isFetching = false;
            $table.removeClass('loading');
            $loadingContainer.removeClass('show');
            return;
        }
    
        let url = `${CONFIG.BASE_URL}${endpoint}?page=${page}&size=${size}&sort=${sort}`;
        if (filters.status) url += `&status=${filters.status}`;
        if (filters.startDate) url += `&startDate=${filters.startDate}`;
        if (filters.endDate) url += `&endDate=${filters.endDate}`;
        if (filters.minValue) url += `&minValue=${filters.minValue}`;
        if (filters.maxValue) url += `&maxValue=${filters.maxValue}`;
    
        try {
            const startTime = Date.now();
            const response = await ajaxWithRetry({
                url: url,
                method: 'GET',
                ...ajaxConfig
            });
            const endTime = Date.now();
    
            if (!response || !response.content) {
                throw new Error('Dữ liệu không hợp lệ từ server');
            }
    
            const mockResponse = {
                content: response.content,
                totalPages: 3,
                number: page,
                last: page === 2,
                totalElements: 25
            };
            displayRechargeHistory(mockResponse, tab, page, size, sort, append);
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].TRANSACTIONS_FAILED);
        } finally {
            isFetching = false;
            $table.removeClass('loading');
            $loadingContainer.removeClass('show');
        }
    }

    function displayRechargeHistory(response, tab, page, size, sort, append) {
        const $tbody = $(`#${tab} .purchase-history tbody`);
        if (!append) {
            $tbody.empty();
        }
    
        const historyData = response.content || [];
        const state = tabStates[tab];
    
        if (!historyData || historyData.length === 0) {
            if (state.loadedTransactions === 0) {
                const colSpan = tab === 'card' ? 7 : 4;
                $tbody.html(`<tr><td colspan="${colSpan}" class="text-center">Không có dữ liệu lịch sử nạp tiền</td></tr>`);
            }
            if (append && historyData.length === 0) {
                state.totalElements = state.loadedTransactions;
            }
        } else {
            const batchSize = 5;
            let index = 0;
    
            function renderBatch() {
                const startTime = Date.now();
                const end = Math.min(index + batchSize, historyData.length);
                for (let i = index; i < end; i++) {
                    const item = historyData[i];
                    const row = $('<tr>');
                    if (tab === 'card') {
                        row.append(
                            $('<td>').text(normalizeNetworkProvider(item.depositCardNetworkProvider)),
                            $('<td>').text(item.serial || 'Không có thông tin'),
                            $('<td>').text(item.code || 'Không có thông tin'),
                            $('<td>').text(formatCurrency(item.value)),
                            $('<td>').text(formatCurrency(item.actuallyReceive)),
                            $('<td>').text(formatDateTime(item.timeOfDepositing)),
                            $('<td>').addClass(getStatusClass(item.cardDepositStatus)).text(normalizeStatus(item.cardDepositStatus))
                        );
                    } else if (tab === 'vnpay') {
                        const transactionId = item.transactionId || 'Không có thông tin';
                        const shortTransactionId = transactionId.length > 20 ? transactionId.substring(0, 17) + '...' : transactionId;
                        row.append(
                            $('<td>').text(formatCurrency(item.amount)),
                            $('<td>').text(shortTransactionId),
                            $('<td>').text(formatDateTime(item.timeOfDepositing)),
                            $('<td>').addClass(getStatusClass(item.status)).text(normalizeStatus(item.status))
                        );
                    }
                    $tbody.append(row);
                }
                const endTime = Date.now();
                index = end;
                if (index < historyData.length) {
                    setTimeout(renderBatch, 0);
                } else {
                    state.loadedTransactions += historyData.length;
                }
            }
    
            renderBatch();
        }
    
        state.currentPage = response.number !== undefined ? response.number : 0;
        state.pageSize = size;
        state.totalElements = response.totalElements !== undefined ? response.totalElements : 0;
    
        $(`#${tab}-pagination`).empty();
    }

    function debounce(func, wait) {
        let timeout;
        return function (...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    $(window).on('scroll', debounce(function () {
        if (isFetching) return;

        const activeTab = $('.tab-btn.active').data('tab');
        const state = tabStates[activeTab];

        const scrollPosition = $(window).scrollTop() + $(window).height();
        const pageHeight = $(document).height();

        if (scrollPosition >= pageHeight - 100) {
            state.currentPage++;
            const sort = $(`#${activeTab}-sort`).val();
            loadRechargeHistory(activeTab, state.currentPage, state.pageSize, sort, true);
        }
    }, 200));

    $('.sort-select').on('change', function () {
        const tab = $('.tab-btn.active').data('tab');
        const sort = $(this).val();
        const state = tabStates[tab];
        state.currentPage = 0;
        state.loadedTransactions = 0;
        state.totalElements = 0;
        loadRechargeHistory(tab, 0, state.pageSize, sort);
    });

    $('.tab-btn').on('click', function () {
        const tab = $(this).data('tab');
        $('.tab-btn').removeClass('active');
        $(this).addClass('active');
        $('.tab-pane').removeClass('active');
        $(`#${tab}`).addClass('active');

        const state = tabStates[tab];
        state.currentPage = 0;
        state.loadedTransactions = 0;
        state.totalElements = 0;
        const sort = $(`#${tab}-sort`).val();
        loadRechargeHistory(tab, 0, state.pageSize, sort);
    });

    $('.refresh-btn').on('click', async function () {
        const $button = $(this);
        const activeTab = $('.tab-btn.active').data('tab');

        if (!activeTab || !['card', 'vnpay'].includes(activeTab)) {
            showNotification('Không tìm thấy tab để làm mới!', 'error');
            return;
        }

        $button.prop('disabled', true).text('Đang làm mới...');
        try {
            const state = tabStates[activeTab];
            state.currentPage = 0;
            state.loadedTransactions = 0;
            state.totalElements = 0;
            const sort = $(`#${activeTab}-sort`).val();
            await loadRechargeHistory(activeTab, 0, state.pageSize, sort);
            showNotification(MESSAGES[currentLang].REFRESH_SUCCESS, 'success');
        } catch (error) {
            handleAjaxError(error, MESSAGES[currentLang].TRANSACTIONS_FAILED);
        } finally {
            $button.prop('disabled', false).text('Làm mới');
        }
    });

    $('#logout-btn').on('click', async function (e) {
        e.preventDefault();
        try {
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.LOGOUT}`,
                method: 'POST',
                ...ajaxConfig
            });
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
            showNotification(MESSAGES[currentLang].LOGOUT_SUCCESS, 'success', 2000, () => {
                window.location.href = CONFIG.PAGES.LOGIN;
            });
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].LOGOUT_FAILED);
        }
    });

    $('#apply-filters').on('click', function () {
        filters = {
            status: $('#status-filter').val(),
            startDate: $('#start-date').val(),
            endDate: $('#end-date').val(),
            minValue: $('#min-value').val() ? parseInt($('#min-value').val()) : null,
            maxValue: $('#max-value').val() ? parseInt($('#max-value').val()) : null
        };
        const tab = $('.tab-btn.active').data('tab');
        const state = tabStates[tab];
        state.currentPage = 0;
        state.loadedTransactions = 0;
        state.totalElements = 0;
        const sort = $(`#${tab}-sort`).val();
        loadRechargeHistory(tab, 0, state.pageSize, sort);
    });

    $('#clear-filters').on('click', function () {
        $('#status-filter').val('');
        $('#start-date').val('');
        $('#end-date').val('');
        $('#min-value').val('');
        $('#max-value').val('');
        filters = {};
        const tab = $('.tab-btn.active').data('tab');
        const state = tabStates[tab];
        state.currentPage = 0;
        state.loadedTransactions = 0;
        state.totalElements = 0;
        const sort = $(`#${tab}-sort`).val();
        loadRechargeHistory(tab, 0, state.pageSize, sort);
    });

    checkLoginStatus() && loadRechargeHistory('card', 0, 10, 'timeOfDepositing,desc');
});