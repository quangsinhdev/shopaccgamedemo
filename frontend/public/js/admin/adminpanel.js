const CONFIG = {
    BASE_URL: 'https://localhost:8443',
    API_ENDPOINTS: {
        LOGIN: '/api/users/login',
        LOGOUT: '/api/users/logout',
        REFRESH_TOKEN: '/api/users/refresh-token',
        GAME_ACCOUNTS: '/api/admin/gameaccounts',
        VOUCHERS: '/api/admin/vouchers',
        GIFTCODES: '/api/admin/giftcodes',
        USERS: '/api/admin/users',
        TRANSACTIONS_ACCOUNTS: '/api/admin/transactions/accounts',
        DEPOSITS_CARD: '/api/admin/deposits/card',
        DEPOSITS_CARD_SUCCESS: '/api/admin/deposits/card/success',
        DEPOSITS_VNPAY: '/api/admin/deposits/vnpay',
        DEPOSIT_PAYMENT_INFO: '/api/admin/deposit-payment-info',
        DEPOSIT_APPROVAL: '/api/admin/deposits/card'
    },
    PAGES: { LOGIN: '/pages/client/login.html' },
    CSRF_EXCLUDED_PATHS: [
        '/api/users/login',
        '/api/users/logout',
        '/api/users/refresh-token'
    ]
};

const MESSAGES = {
    vi: {
        NOT_LOGGED_IN: 'Bạn chưa đăng nhập. Vui lòng đăng nhập!',
        SESSION_EXPIRED: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!',
        LOGOUT_FAILED: 'Đăng xuất thất bại',
        FETCH_FAILED: 'Không thể tải dữ liệu. Vui lòng thử lại sau!',
        POST_FAILED: 'Không thể tạo mới. Vui lòng thử lại!',
        UPDATE_FAILED: 'Không thể cập nhật. Vui lòng thử lại!',
        DELETE_FAILED: 'Không thể xóa. Vui lòng thử lại!',
        SUCCESS_POST: 'Tạo mới thành công!',
        SUCCESS_UPDATE: 'Cập nhật thành công!',
        SUCCESS_DELETE: 'Xóa thành công!',
        ACCOUNT_LOCKED: 'Tài khoản bị khóa do đăng nhập sai quá nhiều. Vui lòng thử lại sau!',
        LOGIN_FAILED: 'Thông tin đăng nhập không đúng!',
        REFRESH_SUCCESS: 'Làm mới thành công!',
        CSRF_TOKEN_NOT_FOUND: 'CSRF token không tìm thấy. Vui lòng đăng nhập lại!'
    }
};

const currentLang = 'vi';

const ajaxConfig = {
    xhrFields: { withCredentials: true },
    contentType: 'application/json',
    timeout: 10000
};

let currentUserPage = 0;
let isUserLoading = false;
let userHasMore = true;
const userPageSize = 20;
let userFilters = {
    userStatus: '',
    role: ''
};

let loadedAccounts = 0;
let observer = null;
let isLoading = false;
let accountQueue = [];
let isFullyLoaded = false;

let isRefreshing = false;

const transactionState = {
    accounts: { page: 0, hasMore: true, isLoading: false },
    vnpay: { page: 0, hasMore: true, isLoading: false },
    card: { page: 0, hasMore: true, isLoading: false }
};


let transactionObserver = null;

let refreshSubscribers = [];

function formatDate(timestamp) {
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
    if (amount == null || isNaN(amount)) {
        return '0';
    }
    return amount.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
}

function getGameTypeDisplay(gameType) {
    const gameTypeMap = {
        'LOL': 'Liên Minh Huyền Thoại',
        'LQ': 'Liên Quân Mobile',
        'FIFA': 'Fifa Online 4'
    };
    return gameTypeMap[gameType] || gameType;
}

function getStatusDisplay(userStatus) {
    const statusMap = { 'ACTIVE': 'Đang hoạt động', 'LOCKED': 'Đang khóa' };
    return statusMap[userStatus] || 'Không có thông tin';
}

function getRoleDisplay(role) {
    const roleMap = { 'USER': 'Người dùng', 'AGENCY': 'Agency', 'ADMIN': 'Admin' };
    return roleMap[role] || 'Không có thông tin';
}

function getNetworkProviderDisplay(depositCardNetworkProvider) {
    const NetworkProviderMap = { 'VIETTEL': 'Viettel', 'MOBIFONE': 'Mobifone', 'VINAPHONE': 'Vinaphone' };
    return NetworkProviderMap[depositCardNetworkProvider] || 'Không có thông tin';
}
function getCardDepositStatusDisplay(cardDepositStatus) {
    const statusMap = { 'SUCCESS': 'Thành công', 'REJECTED': 'Thất bại', 'PENDING': 'Chờ duyệt' };
    return statusMap[cardDepositStatus] || 'Không có thông tin';
}


function formToJson($form) {
    return Object.fromEntries($form.serializeArray().map(item => [item.name, item.value]));
}

function debounce(func, wait) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

function debounceObserver(func, wait) {
    let timeout;
    return function (...args) {
        if (timeout) {
            clearTimeout(timeout);
        }
        timeout = setTimeout(() => {
            func.apply(this, args);
        }, wait);
    };
}

const errorHandlers = {
    401: () => window.showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error', 2000, () => {
        localStorage.removeItem('jwtToken');
        showLoginModal();
    }),
    403: () => window.showNotification('Bạn không có quyền thực hiện hành động này!', 'error'),
    default: (msg) => window.showNotification(msg, 'error')
};

function handleAjaxError(xhr, defaultMsg) {
    let msg = defaultMsg;
    try {
        const response = JSON.parse(xhr.responseText);
        msg = response.errorMessage || response.message || defaultMsg;
    } catch {
        msg = xhr.responseText || defaultMsg;
    }
    (errorHandlers[xhr.status] || errorHandlers.default)(msg);
}

function onTokenRefreshed(success) {
    refreshSubscribers.forEach(callback => callback(success));
    refreshSubscribers = [];
}

function subscribeTokenRefresh(callback) {
    refreshSubscribers.push(callback);
}

async function refreshToken() {
    if (isRefreshing) {
        return new Promise(resolve => {
            subscribeTokenRefresh(success => resolve(success));
        });
    }

    isRefreshing = true;

    try {
        const response = await $.ajax({
            url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.REFRESH_TOKEN}`,
            method: 'POST',
            ...ajaxConfig
        });


        if (response.csrfToken) {
            localStorage.setItem('csrfToken', response.csrfToken);
        } else {
            window.showNotification('Không thể làm mới CSRF token. Vui lòng đăng nhập lại!', 'error', 2000, () => {
                isAuthenticated = false;
                showLoginModal();
            });
            throw new Error('No CSRF token returned');
        }

        isRefreshing = false;
        onTokenRefreshed(true);
        return true;
    } catch (xhr) {
        if (xhr.status === 401) {
            window.showNotification('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!', 'error', 2000, () => {
                isAuthenticated = false;
                showLoginModal();
            });
        } else if (xhr.readyState === 0) {
            window.showNotification('Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng!', 'error');
        } else {
            window.showNotification('Lỗi không xác định khi làm mới token. Vui lòng thử lại!', 'error', 2000);
        }
        isRefreshing = false;
        onTokenRefreshed(false);
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
            window.showNotification(MESSAGES[currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                localStorage.removeItem('csrfToken');
                isAuthenticated = false;
                showLoginModal();
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
        const response = await $.ajax(settings);
        return response;
    } catch (xhr) {
        if (xhr.status === 401) {
            const success = await refreshToken();
            if (success) {
                if (!isSafeMethod && !isExcluded) {
                    const newCsrfToken = localStorage.getItem('csrfToken');
                    if (!newCsrfToken) {
                        window.showNotification(MESSAGES[currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                            localStorage.removeItem('csrfToken');
                            isAuthenticated = false;
                            showLoginModal();
                        });
                        throw new Error('CSRF token not found after refresh');
                    }
                    settings.headers = {
                        ...settings.headers,
                        'X-XSRF-TOKEN': newCsrfToken
                    };
                }
                try {
                    return await $.ajax(settings);
                } catch (retryXhr) {
                    if (retryXhr.status === 403) {
                        window.showNotification('CSRF token không hợp lệ sau khi làm mới. Vui lòng đăng nhập lại!', 'error', 2000, () => {
                            localStorage.removeItem('csrfToken');
                            isAuthenticated = false;
                            showLoginModal();
                        });
                    }
                    throw retryXhr;
                }
            } else {
                throw xhr;
            }
        } else if (xhr.status === 403) {
            const success = await refreshToken();
            if (success) {
                if (!isSafeMethod && !isExcluded) {
                    const newCsrfToken = localStorage.getItem('csrfToken');
                    if (!newCsrfToken) {
                        window.showNotification(MESSAGES[currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                            localStorage.removeItem('csrfToken');
                            isAuthenticated = false;
                            showLoginModal();
                        });
                        throw new Error('CSRF token not found after refresh');
                    }
                    settings.headers = {
                        ...settings.headers,
                        'X-XSRF-TOKEN': newCsrfToken
                    };
                }
                try {
                    return await $.ajax(settings);
                } catch (retryXhr) {
                    if (retryXhr.status === 403) {
                        window.showNotification('CSRF token không hợp lệ sau khi làm mới. Vui lòng đăng nhập lại!', 'error', 2000, () => {
                            localStorage.removeItem('csrfToken');
                            isAuthenticated = false;
                            showLoginModal();
                        });
                    }
                    throw retryXhr;
                }
            } else {
                window.showNotification('CSRF token không hợp lệ. Vui lòng đăng nhập lại!', 'error', 2000, () => {
                    localStorage.removeItem('csrfToken');
                    isAuthenticated = false;
                    showLoginModal();
                });
                throw new Error('CSRF validation failed');
            }
        }
        throw xhr;
    }
}

function showLoginModal() {
    const $modal = $('#login-modal');
    const $modalContent = $modal.find('.modal-content');
    $modal.addClass('show').show();
    $('.main-content').removeClass('active');
}

function logout() {
    ajaxWithRetry({
        url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.LOGOUT}`,
        method: 'POST',
        ...ajaxConfig
    })
        .then(() => {
            localStorage.removeItem('csrfToken');
            showLoginModal();
            window.showNotification('Đã đăng xuất!', 'success');
        })
        .catch(xhr => {
            handleAjaxError(xhr, MESSAGES[currentLang].LOGOUT_FAILED);
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('csrfToken');
            showLoginModal();
        });
}

async function loadUsers(append = false) {

    if (isUserLoading) {
        return;
    }

    if (!append) {
        currentUserPage = 0;
        userHasMore = true;
    } else if (!userHasMore) {
        return;
    }

    isUserLoading = true;

    try {
        if (!CONFIG || !CONFIG.BASE_URL || !CONFIG.API_ENDPOINTS.USERS) {
            throw new Error('CONFIG is not properly defined');
        }

        const baseUrl = CONFIG.BASE_URL;
        const endpoint = CONFIG.API_ENDPOINTS.USERS;
        const fullUrl = `${baseUrl}${endpoint}`;

        try {
            new URL(fullUrl);
        } catch (e) {
            throw new Error(`Invalid URL: ${fullUrl}`);
        }

        const url = new URL(fullUrl);
        url.searchParams.append('page', currentUserPage);
        url.searchParams.append('size', userPageSize);

        if (userFilters.userStatus) {
            url.searchParams.append('userStatus', userFilters.userStatus);
        }
        if (userFilters.role) {
            url.searchParams.append('role', userFilters.role);
        }

        const response = await ajaxWithRetry({
            url: url.toString(),
            method: 'GET',
            ...ajaxConfig
        });

        const users = response.content || [];

        const $tableBody = $('#user-table');
        if (!$tableBody.length) {
            return;
        }

        if (!append) {
            $tableBody.empty();
        }

        if (users.length === 0 && !append) {
            $tableBody.append('<tr><td colspan="6" class="text-center">Không có người dùng nào</td></tr>');
            userHasMore = false;
            return;
        }

        users.forEach(user => {
            $tableBody.append(`
                <tr>
                    <td>${user.fullname || 'Không có thông tin'}</td>
                    <td>${user.username}</td>
                    <td>${formatCurrency(user.balance)}</td>
                    <td>${getStatusDisplay(user.userStatus)}</td>
                    <td>${getRoleDisplay(user.role)}</td>
                    <td>
                        <a href="#" class="btn btn-view" onclick="loadUserDetail(${user.id})">Chi tiết</a>
                        <a href="#" class="btn btn-lock" onclick="deleteUser(${user.id})">Xóa</a>
                    </td>
                </tr>
            `);
        });

        userHasMore = !response.last && (response.number + 1 < response.totalPages);
        if (userHasMore) {
            currentUserPage++;
        }

    } catch (xhr) {
        handleAjaxError(xhr, MESSAGES[currentLang].FETCH_FAILED);
    } finally {
        isUserLoading = false;
    }
}

async function loadTransactions(tab, append = false) {
    if (!append) {
        transactionState[tab].page = 0;
        transactionState[tab].hasMore = true;
        transactionState[tab].isLoading = false;
    }

    if (transactionState[tab].isLoading || !transactionState[tab].hasMore) {
        return;
    }

    transactionState[tab].isLoading = true;

    try {
        const usersResponse = await ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.USERS}`,
            method: 'GET',
            ...ajaxConfig
        });
        const users = Array.isArray(usersResponse) ? usersResponse : (usersResponse && Array.isArray(usersResponse.data) ? usersResponse.data : []);


        if (!append) {
            const accountsResponse = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.TRANSACTIONS_ACCOUNTS}?page=0&size=1000`,
                method: 'GET',
                ...ajaxConfig
            });
            const vnpayResponse = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.DEPOSITS_VNPAY}?page=0&size=1000`,
                method: 'GET',
                ...ajaxConfig
            });
            const cardResponse = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.DEPOSITS_CARD}?page=0&size=1000`,
                method: 'GET',
                ...ajaxConfig
            });

            const accountsTxns = accountsResponse.content || [];
            const vnpayTxns = vnpayResponse.content || [];
            const cardTxns = cardResponse.content || [];

            const accountsTotal = accountsTxns.reduce((sum, txn) => sum + (Number(txn.price) || 0), 0);
            const vnpayTotal = vnpayTxns.reduce((sum, txn) => sum + (Number(txn.amount) || 0), 0);
            const cardTxnsSuccess = cardTxns.filter(txn => txn && txn.cardDepositStatus === 'SUCCESS');
            const cardTotal = cardTxnsSuccess.reduce((sum, txn) => sum + (Number(txn.value) || 0), 0);

            $('#accounts-total').text(formatCurrency(accountsTotal));
            $('#vnpay-total').text(formatCurrency(vnpayTotal));
            $('#card-total').text(formatCurrency(cardTotal));

            $('#accounts-count').text(accountsTxns.length);
            $('#vnpay-count').text(vnpayTxns.length);
            $('#card-count').text(cardTxnsSuccess.length);
        }

        let apiEndpoint, tableId, filterCondition, rowTemplate;
        switch (tab) {
            case 'accounts':
                apiEndpoint = CONFIG.API_ENDPOINTS.TRANSACTIONS_ACCOUNTS;
                tableId = 'accounts-table';
                filterCondition = (txn) => true;
                rowTemplate = (txn) => {
                    return `
                        <tr>
                            <td>${getGameTypeDisplay(txn.gameAccountType) || 'Không có thông tin'}</td>
                            <td>${txn.gameAccountType || 'Không có thông tin'} #${txn.accountId || 'Không có thông tin'}</td>
                            <td>${txn.username || 'Không có thông tin'}</td>
                            <td>${txn.transactorUsername || 'Không có thông tin'}</td>
                            <td>${formatCurrency(txn.price) || 'Không có thông tin'}</td>
                            <td>${formatDateTime(txn.transactionDate) || 'Không có thông tin'}</td>
                        </tr>
                    `;
                };
                break;
            case 'vnpay':
                apiEndpoint = CONFIG.API_ENDPOINTS.DEPOSITS_VNPAY;
                tableId = 'vnpay-table';
                filterCondition = (txn) => true;
                rowTemplate = (txn) => {
                    return `
                        <tr>
                            <td>${txn.depositorUsername || 'Không có thông tin'}</td>
                            <td>${txn.transactionId || 'Không có thông tin'}</td>
                            <td>${formatCurrency(txn.amount) || 'Không có thông tin'}</td>
                            <td>${formatDateTime(txn.timeOfDepositing) || 'Không có thông tin'}</td>
                            <td>${txn.paymentMethod || 'VNPAY'}</td>
                        </tr>
                    `;
                };
                break;
            case 'card':
                apiEndpoint = CONFIG.API_ENDPOINTS.DEPOSITS_CARD_SUCCESS;
                tableId = 'card-transaction-table';
                filterCondition = (txn) => true;
                rowTemplate = (txn) => {
                    return `
                        <tr>
                            <td>${txn.depositorUsername || 'Không có thông tin'}</td>
                            <td>${getNetworkProviderDisplay(txn.depositCardNetworkProvider) || 'Không có thông tin'}</td>
                            <td>${txn.serial || 'Không có thông tin'}</td>
                            <td>${formatCurrency(txn.value) || 'Không có thông tin'}</td>
                            <td>${formatCurrency(txn.actuallyReceive) || 'Không có thông tin'}</td>
                            <td>${formatDateTime(txn.timeOfDepositing) || 'Không có thông tin'}</td>
                            <td>${getCardDepositStatusDisplay(txn.cardDepositStatus) || 'Không có thông tin'}</td>
                        </tr>
                    `;
                };
                break;
            default:
                return;
        }

        const response = await ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${apiEndpoint}?page=${transactionState[tab].page}&size=10`,
            method: 'GET',
            ...ajaxConfig
        });

        const transactions = response.content || [];

        const filteredTransactions = transactions.filter(filterCondition);

        const $table = $(`#${tableId}`);
        if (!append) {
            $table.empty();
        }

        if (filteredTransactions.length === 0 && !append) {
            $table.append(`<tr><td colspan="${tab === 'accounts' ? 6 : tab === 'vnpay' ? 5 : 7}">Không có giao dịch nào.</td></tr>`);
            transactionState[tab].hasMore = false;
            return;
        }

        if (filteredTransactions.length === 0 && append) {
            transactionState[tab].hasMore = false;
            if (!$table.siblings('.no-more-data').length) {
                $table.after('<div class="no-more-data text-center">Đã tải hết dữ liệu</div>');
            }
            return;
        }

        filteredTransactions.forEach(txn => {
            $table.append(rowTemplate(txn));
        });

        transactionState[tab].hasMore = !response.last && filteredTransactions.length > 0;

        if (transactionState[tab].hasMore) {
            transactionState[tab].page++;
            setupTransactionInfiniteScroll(tab);
        } else {
            if (!$table.siblings('.no-more-data').length) {
                $table.after('<div class="no-more-data text-center">Đã tải hết dữ liệu</div>');
            }
            if (transactionObserver) {
                transactionObserver.disconnect();
            }
        }

    } catch (xhr) {
        handleAjaxError(xhr, MESSAGES[currentLang].FETCH_FAILED);
    } finally {
        transactionState[tab].isLoading = false;
    }
}

async function loadMoreTransactions(tab) {
    if (transactionState[tab].isLoading || !transactionState[tab].hasMore) {
        return;
    }

    await loadTransactions(tab, true);
}

function setupTransactionInfiniteScroll(tab) {
    const $trigger = $(`#${tab}-load-more-trigger`);
    if (!$trigger.length) {
        return;
    }

    if (transactionObserver) {
        transactionObserver.disconnect();
    }

    const debouncedLoadMore = debounceObserver((entries) => {
        if (entries[0].isIntersecting && transactionState[tab].hasMore && !transactionState[tab].isLoading) {
            loadMoreTransactions(tab);
        }
    }, 300);

    transactionObserver = new IntersectionObserver(debouncedLoadMore, { threshold: 0.5 });
    transactionObserver.observe($trigger[0]);
}

async function loadGameAccountsStats(filters = { gameTypes: ['fifa', 'lol', 'lq'], statuses: ['SELLING', 'SOLD'], priceMin: null, priceMax: null, dateFrom: null, dateTo: null }) {
    if (isLoading) return;

    isLoading = true;
    isFullyLoaded = false;

    const $loading = $('#loading-indicator');
    const $tableBody = $('#game-accounts-table');
    $('#no-more-data').remove();

    let page = 0;
    const size = 10;
    let hasMore = true;

    loadedAccounts = 0;
    accountQueue = [];

    if (observer) observer.disconnect();
    $loading.show();

    try {
        const requests = [];
        const { gameTypes, statuses } = filters;

        gameTypes.forEach(gameType => {
            statuses.forEach(status => {
                requests.push(ajaxWithRetry({
                    url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GAME_ACCOUNTS}/${gameType}?status=${status}&page=${page}&size=${size}`,
                    method: 'GET',
                    ...ajaxConfig
                }));
            });
        });

        const responses = await Promise.all(requests);

        const stats = {
            'fifa-selling': 0, 'lol-selling': 0, 'lq-selling': 0,
            'fifa-sold': 0, 'lol-sold': 0, 'lq-sold': 0
        };

        let responseIndex = 0;
        gameTypes.forEach(gameType => {
            statuses.forEach(status => {
                const response = responses[responseIndex++] || {};
                const accounts = Array.isArray(response.content) ? response.content : [];
                const key = `${gameType}-${status.toLowerCase()}`;
                stats[key] = accounts.length;
            });
        });

        $('#fifa-selling').text(stats['fifa-selling']);
        $('#lol-selling').text(stats['lol-selling']);
        $('#lq-selling').text(stats['lq-selling']);
        $('#fifa-sold').text(stats['fifa-sold']);
        $('#lol-sold').text(stats['lol-sold']);
        $('#lq-sold').text(stats['lq-sold']);

        let allAccounts = responses.reduce((acc, response) => {
            return [...acc, ...(Array.isArray(response.content) ? response.content : [])];
        }, []);

        allAccounts = allAccounts.filter(account => {
            const price = Number(account.price) || 0;
            const priceMin = filters.priceMin ? Number(filters.priceMin) : null;
            const priceMax = filters.priceMax ? Number(filters.priceMax) : null;
            if (priceMin && price < priceMin) return false;
            if (priceMax && price > priceMax) return false;

            const timeOfListing = account.timeOfListing ? new Date(account.timeOfListing) : null;
            const dateFrom = filters.dateFrom ? new Date(filters.dateFrom) : null;
            const dateTo = filters.dateTo ? new Date(filters.dateTo) : null;
            if (dateFrom && timeOfListing && timeOfListing < dateFrom) return false;
            if (dateTo && timeOfListing && timeOfListing > dateTo) return false;

            return true;
        }).sort((a, b) => {
            const timeA = a.timeOfListing ? new Date(a.timeOfListing).getTime() : 0;
            const timeB = b.timeOfListing ? new Date(b.timeOfListing).getTime() : 0;
            return timeB - timeA;
        });

        hasMore = responses.some(response => {
            const { page: pageInfo } = response;
            return pageInfo && pageInfo.number < pageInfo.totalPages - 1;
        });

        accountQueue.push(...allAccounts);
        const accountsToRender = accountQueue.splice(0, 10);
        renderGameAccounts(accountsToRender);

        if (accountQueue.length > 0 || hasMore) {
            setupInfiniteScroll(page + 1, size, filters, hasMore);
        } else {
            isFullyLoaded = true;
            if (loadedAccounts === 0) {
                $tableBody.append('<tr><td colspan="6" class="text-center">Không có tài khoản nào</td></tr>');
            } else {
                $tableBody.after('<div id="no-more-data" class="text-center">Đã tải hết dữ liệu</div>');
            }
            if (observer) observer.disconnect();
        }

        return { page: page + 1, hasMore };
    } catch (error) {
        handleAjaxError(error, MESSAGES[currentLang].FETCH_FAILED);
    } finally {
        $loading.hide();
        isLoading = false;
    }
}

async function loadMoreGameAccounts(page, size, filters, hasMore) {
    if (isLoading || isFullyLoaded) {
        return;
    }

    if (accountQueue.length > 0) {
        const accountsToRender = accountQueue.splice(0, 10);
        renderGameAccounts(accountsToRender, true);

        if (accountQueue.length === 0 && !hasMore) {
            isFullyLoaded = true;
            $('#game-accounts-table').after('<div id="no-more-data" class="text-center">Đã tải hết dữ liệu</div>');
            if (observer) observer.disconnect();
        }
        return;
    }

    isLoading = true;
    const $loading = $('#loading-indicator');
    $loading.show();

    try {
        const requests = [];
        const { gameTypes, statuses } = filters;

        gameTypes.forEach(gameType => {
            statuses.forEach(status => {
                requests.push(ajaxWithRetry({
                    url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GAME_ACCOUNTS}/${gameType}?status=${status}&page=${page}&size=${size}`,
                    method: 'GET',
                    ...ajaxConfig
                }));
            });
        });

        const responses = await Promise.all(requests);
        let newAccounts = responses.reduce((acc, response) => {
            return [...acc, ...(Array.isArray(response.content) ? response.content : [])];
        }, []);

        newAccounts = newAccounts.filter(account => {
            const price = Number(account.price) || 0;
            const priceMin = filters.priceMin ? Number(filters.priceMin) : null;
            const priceMax = filters.priceMax ? Number(filters.priceMax) : null;
            if (priceMin && price < priceMin) return false;
            if (priceMax && price > priceMax) return false;

            const timeOfListing = account.timeOfListing ? new Date(account.timeOfListing) : null;
            const dateFrom = filters.dateFrom ? new Date(filters.dateFrom) : null;
            const dateTo = filters.dateTo ? new Date(filters.dateTo) : null;
            if (dateFrom && timeOfListing && timeOfListing < dateFrom) return false;
            if (dateTo && timeOfListing && timeOfListing > dateTo) return false;

            return true;
        }).sort((a, b) => {
            const timeA = a.timeOfListing ? new Date(a.timeOfListing).getTime() : 0;
            const timeB = b.timeOfListing ? new Date(b.timeOfListing).getTime() : 0;
            return timeB - timeA;
        });

        hasMore = responses.some(response => {
            const { page: pageInfo } = response;
            return pageInfo && pageInfo.number < pageInfo.totalPages - 1;
        });

        accountQueue.push(...newAccounts);
        const accountsToRender = accountQueue.splice(0, 10);
        renderGameAccounts(accountsToRender, true);

        if (accountQueue.length > 0 || hasMore) {
            setupInfiniteScroll(page + 1, size, filters, hasMore);
        } else {
            isFullyLoaded = true;
            $('#game-accounts-table').after('<div id="no-more-data" class="text-center">Đã tải hết dữ liệu</div>');
            if (observer) observer.disconnect();
        }

        return { page: page + 1, hasMore };
    } catch (error) {
        handleAjaxError(error, MESSAGES[currentLang].FETCH_FAILED);
    } finally {
        $loading.hide();
        isLoading = false;
    }
}

async function loadDepositSettings() {
    try {
        const settings = await ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.DEPOSIT_PAYMENT_INFO}`,
            method: 'GET',
            ...ajaxConfig
        });
        const $form = $('#update-settings-form');
        $form.find('#viettelTradeCost').val(settings.viettelTradeCost);
        $form.find('#mobifoneTradeCost').val(settings.mobifoneTradeCost);
        $form.find('#vinaphoneTradeCost').val(settings.vinaphoneTradeCost);
        $form.find('#qrCodeMomo').val(settings.qrCodeMomo);
        $form.find('#qrCodeViettelPay').val(settings.qrCodeViettelPay);
    } catch (xhr) {
        handleAjaxError(xhr, MESSAGES[currentLang].FETCH_FAILED);
    }
}

function renderGameAccounts(accounts, append = false) {
    const $tableBody = $('#game-accounts-table');
    if (!append) {
        $tableBody.empty();
        $('#loaded-accounts-count').text(0);
    }

    if (!accounts || accounts.length === 0) {
        if (!append && loadedAccounts === 0) {
            $tableBody.append('<tr><td colspan="6" class="text-center">Không có tài khoản nào</td></tr>');
        }
        $('#loaded-accounts-count').text(loadedAccounts);
        return;
    }

    const rows = accounts.map(account => `
        <tr class="new-row">
            <td>${account.gameAccountType || 'N/A'}</td>
            <td>${account.gameAccountType} #${account.id || 'N/A'}</td>
            <td>${account.username || 'N/A'}</td>
            <td>${account.price ? Number(account.price).toLocaleString('vi-VN') + ' VND' : 'Không có thông tin'}</td>
            <td>${account.timeOfListing ? new Date(account.timeOfListing).toLocaleString('vi-VN') : 'N/A'}</td>
            <td>
                <a href="#" class="btn btn-view" onclick="loadGameAccountDetail('${(account.gameAccountType || '').toLowerCase()}', ${account.id || 0})">Chi tiết</a>
                <a href="#" class="btn btn-lock" onclick="deleteGameAccountFromTable('${(account.gameAccountType || '').toLowerCase()}', ${account.id || 0})">Xóa</a>
            </td>
        </tr>
    `).join('');

    $tableBody.append(rows);

    loadedAccounts += accounts.length;
    $('#loaded-accounts-count').text(loadedAccounts);
}

function setupInfiniteScroll(page, size, filters, hasMore) {
    if (isFullyLoaded) {
        if (observer) observer.disconnect();
        return;
    }

    const $trigger = $('#load-more-trigger');
    if (observer) observer.disconnect();

    observer = new IntersectionObserver(entries => {
        if (entries[0].isIntersecting && !isLoading && !isFullyLoaded) {
            loadMoreGameAccounts(page, size, filters, hasMore);
        }
    }, { threshold: 0.1 });

    observer.observe($trigger[0]);
}

function handleUserScroll() {
    const tableContainer = document.querySelector('.table-container');
    if (!tableContainer) return;

    const scrollPosition = tableContainer.scrollTop + tableContainer.clientHeight;
    const totalHeight = tableContainer.scrollHeight;

    if (scrollPosition >= totalHeight - 100 && !isUserLoading && userHasMore) {
        loadUsers(true);
    }
}

window.showSection = function (sectionId) {
    $('.main-content').removeClass('active');
    const $section = $(`#${sectionId}`);
    if ($section.length === 0) {
        return;
    }
    $section.addClass('active');

    switch (sectionId) {
        case 'stats-game-accounts':
            loadGameAccountsStats();
            break;
        case 'stats-vouchers':
            loadVouchers();
            break;
        case 'stats-giftcode':
            loadGiftcodes();
            break;
        case 'stats-users':
            loadUsers();
            break;
        case 'stats-transactions':
            loadTransactions('accounts');
            break;
        case 'manage-cards':
            loadCardDeposits();
            break;
        case 'update-settings':
            loadDepositSettings();
            break;
        case 'detail-game-account':
            break;
        case 'create-voucher':
            break;
        case 'create-giftcode':
            break;
        case 'post-game-account':
            break;
        default:
            console.warn(`No handler for section: ${sectionId}`);
    }
};

window.generateRandomCode = function () {
    const length = Math.floor(Math.random() * 3) + 10;
    const code = Math.random().toString(36).substring(2, 2 + length).toUpperCase();
    $('#voucher-code, #giftcode-code').val(code);
};
window.updatePostGameFields = function () {
    const gameType = $('#post-gameType').val().toUpperCase();
    
    $('#fifa-fields, #lol-fields, #lienquan-fields').hide();
    
    if (gameType === 'FIFA') {
        $('#fifa-fields').show();
    } else if (gameType === 'LOL') {
        $('#lol-fields').show();
    } else if (gameType === 'LQ') {
        $('#lienquan-fields').show();
    }
};
window.updateGameFields = async function (id, gameType, account) {
    try {
        const normalizedGameType = gameType.toUpperCase();
        const $form = $('#update-game-account-form');
        if (!$form.length) {
            window.showNotification('Không tìm thấy form cập nhật!', 'error');
            return;
        }

        $('#update-fifa-fields, #update-lol-fields, #update-lq-fields').hide();

        $form.find('[name="id"]').val(account.id || id);
        $form.find('#update-gameType').val(normalizedGameType).prop('disabled', false);
        $form.find('#update-username').val(account.username || '');
        $form.find('#update-password').val(account.password || '');
        $form.find('#update-phonenumber').val(account.phonenumber || '');
        $form.find('#update-email').val(account.email || '');
        $form.find('#update-description').val(account.description || '');
        $form.find('#update-price').val(account.price || 0);
        $form.find('#update-discount').val(account.discount || 0);
        $form.find('#update-imageUrls').val(account.imagesAsList ? account.imagesAsList.join(', ') : '');

        if (normalizedGameType === 'LOL') {
            $('#update-lol-fields').show();
            $form.find('#update-champ').val(account.champ || 0);
            $form.find('#update-lol-rank').val(account.rank || 'UNRANKED');
            $form.find('#update-skin').val(account.skin || 0);
            $form.find('#update-tinhhoalam').val(account.tinhhoalam || 0);
            $form.find('#update-rp').val(account.rp || 0);
        } else if (normalizedGameType === 'FIFA') {
            $('#update-fifa-fields').show();
            $form.find('#update-bp').val(account.bp || 0);
            $form.find('#update-fc').val(account.fc || 0);
            $form.find('#update-valueteam').val(account.valueteam || 0);
        } else if (normalizedGameType === 'LQ') {
            $('#update-lq-fields').show();
            $form.find('#update-champ').val(account.champ || 0);
            $form.find('#update-skin').val(account.skin || 0);
            $form.find('#update-rank').val(account.rank || 'UNRANKED');
        } else {
            window.showNotification('Loại game không được hỗ trợ!', 'error');
            return;
        }

        $form.find('button[type="submit"]').prop('disabled', false);
    } catch (error) {
        window.showNotification('Không thể cập nhật dữ liệu vào form!', 'error');
    }
};

window.loadGameAccountDetail = async function (gameType, id) {
    try {
        const response = await ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GAME_ACCOUNTS}/${gameType}/${id}`,
            method: 'GET',
            ...ajaxConfig
        });

        const account = response.responseJSON || response;

        if (!account.id || !account.gameAccountType) {
            window.showNotification('Không thể tải chi tiết tài khoản: Thiếu thông tin!', 'error');
            showSection('stats-game-accounts');
            return;
        }

        let normalizedGameType = gameType.toUpperCase();
        if (account.gameAccountType) {
            const gameTypeFromApi = account.gameAccountType.toUpperCase();
            if (gameTypeFromApi === 'FIFA' || gameTypeFromApi === 'FIFA ONLINE 4') {
                normalizedGameType = 'FIFA';
            } else if (gameTypeFromApi === 'LOL' || gameTypeFromApi === 'LIÊN MINH HUYỀN THOẠI') {
                normalizedGameType = 'LOL';
            } else if (gameTypeFromApi === 'LQ' || gameTypeFromApi === 'LIÊN QUÂN MOBILE') {
                normalizedGameType = 'LQ';
            } else {
                window.showNotification('Loại game không được hỗ trợ!', 'error');
                showSection('stats-game-accounts');
                return;
            }
        }

        await updateGameFields(id, normalizedGameType, account);
        showSection('detail-game-account');
    } catch (xhr) {
        handleAjaxError(xhr, MESSAGES[currentLang].FETCH_FAILED);
        showSection('stats-game-accounts');
    }
};

window.loadUserDetail = async function (id) {
    try {
        const user = await ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.USERS}/${id}`,
            method: 'GET',
            ...ajaxConfig
        });
        const $form = $('#update-user-form');
        $form.find('#user-id').val(user.id);
        $form.find('[name="fullname"]').val(user.fullname || '');
        $form.find('[name="email"]').val(user.email);
        $form.find('[name="balance"]').val(user.balance);
        $form.find('[name="userStatus"]').val(user.userStatus);
        $form.find('[name="role"]').val(user.role);
        $('#username-detail').text(user.username);
        showSection('detail-user');
    } catch (xhr) {
        handleAjaxError(xhr, MESSAGES[currentLang].FETCH_FAILED);
    }
};

window.deleteGameAccount = function () {
    const gameType = $('#update-gameType').val().toLowerCase();
    const id = $('#game-account-id').val();

    if (!gameType || !id) {
        window.showNotification('Không thể xóa: Thiếu thông tin tài khoản!', 'error');
        return;
    }

    showDeleteConfirmationModal(gameType, id, () => {
        ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GAME_ACCOUNTS}/${gameType}/${id}`,
            method: 'DELETE',
            ...ajaxConfig
        })
            .then(() => {
                window.showNotification(MESSAGES[currentLang].SUCCESS_DELETE, 'success');
                showSection('stats-game-accounts');
            })
            .catch(xhr => handleAjaxError(xhr, MESSAGES[currentLang].DELETE_FAILED));
    });
};

window.deleteGameAccountFromTable = function (gameType, id) {
    showDeleteConfirmationModal(gameType, id, () => {
        const $deleteButton = $(`#game-accounts-table a[onclick="deleteGameAccountFromTable('${gameType}', ${id})"]`);
        $deleteButton.text('Đang xóa...').prop('disabled', true);

        ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GAME_ACCOUNTS}/${gameType}/${id}`,
            method: 'DELETE',
            ...ajaxConfig
        })
            .then(() => {
                window.showNotification(MESSAGES[currentLang].SUCCESS_DELETE, 'success');
                loadGameAccountsStats();

                const currentAccountId = $('#game-account-id').val();
                if (currentAccountId == id) {
                    showSection('stats-game-accounts');
                }
            })
            .catch(xhr => {
                handleAjaxError(xhr, MESSAGES[currentLang].DELETE_FAILED);
                $deleteButton.text('Xóa').prop('disabled', false);
            });
    });
};

window.deleteUser = function (id) {
    showDeleteConfirmationModal('USER', id, async () => {
        try {
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.USERS}/${id}`,
                method: 'DELETE',
                ...ajaxConfig
            });
            window.showNotification(MESSAGES[currentLang].SUCCESS_DELETE, 'success');
            loadUsers();
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].DELETE_FAILED);
        }
    });
};

window.deleteUserFromDetail = function () {
    const id = $('#user-id').val();
    if (!id) {
        window.showNotification('Không tìm thấy ID người dùng!', 'error');
        return;
    }

    showDeleteConfirmationModal('USER', id, async () => {
        try {
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.USERS}/${id}`,
                method: 'DELETE',
                ...ajaxConfig
            });
            window.showNotification(MESSAGES[currentLang].SUCCESS_DELETE, 'success');
            showSection('stats-users');
            loadUsers();
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].DELETE_FAILED);
        }
    });
};

window.showDeleteConfirmationModal = function (gameType, id, onConfirm) {
    const modal = document.getElementById('deleteAccountModal');
    const modalMessage = document.getElementById('modalMessage');
    const confirmButton = document.getElementById('confirmDelete');
    const cancelButton = document.getElementById('cancelDeleteModal');
    const closeButton = document.querySelector('#deleteAccountModal .close');

    if (!modal || !modalMessage || !confirmButton || !cancelButton || !closeButton) {
        window.showNotification('Lỗi: Không thể hiển thị modal xác nhận!', 'error');
        return;
    }

    modalMessage.textContent = `Bạn có chắc chắn muốn xóa ${gameType.toLowerCase() === 'user' ? 'người dùng' : 'tài khoản'} #${id}?`;

    $(modal).addClass('show').show();

    const closeModal = () => {
        $(modal).removeClass('show').hide();
        confirmButton.removeEventListener('click', confirmHandler);
        cancelButton.removeEventListener('click', closeModal);
        closeButton.removeEventListener('click', closeModal);
        window.removeEventListener('click', clickOutsideHandler);
        window.removeEventListener('keydown', keydownHandler);
    };

    const confirmHandler = () => {
        closeModal();
        onConfirm();
    };

    const clickOutsideHandler = (event) => {
        if (event.target === modal) {
            closeModal();
        }
    };

    const keydownHandler = (event) => {
        if (event.key === 'Escape' && $(modal).is(':visible')) {
            closeModal();
        }
    };

    confirmButton.addEventListener('click', confirmHandler);
    cancelButton.addEventListener('click', closeModal);
    closeButton.addEventListener('click', closeModal);
    window.addEventListener('click', clickOutsideHandler);
    window.addEventListener('keydown', keydownHandler);
};

$(document).ready(function () {
    
    async function checkLoginStatus() {
        try {
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.USERS}`,
                method: 'GET',
                ...ajaxConfig
            });
            loadInitialData();
            return true;
        } catch (xhr) {
            showLoginModal();
            window.showNotification(MESSAGES[currentLang].NOT_LOGGED_IN, 'error', 3000);
            return false;
        }
    }


    async function loadInitialData() {
        await Promise.all([
            loadGameAccountsStats(),
            loadVouchers(),
            loadGiftcodes(),
            loadUsers(),
            loadTransactions('accounts'),
            loadCardDeposits(),
            loadDepositSettings()
        ]);
    }

    checkLoginStatus();
    updatePostGameFields();

    $('#login-form').on('submit', async function (event) {
        event.preventDefault();
        const $form = $(this);
        const data = {
            username: $form.find('#login-username').val(),
            password: $form.find('#login-password').val()
        };
    
    
        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.LOGIN}`,
                method: 'POST',
                data: JSON.stringify(data),
                ...ajaxConfig
            });
            if (response.csrfToken) {
                localStorage.setItem('isLoggedIn', 'true');
                localStorage.setItem('csrfToken', response.csrfToken);
            }
            $('#login-modal').hide();
            window.showNotification('Đăng nhập thành công!', 'success', 3000);
            loadInitialData();
        } catch (xhr) {
            let errorMessage = 'Đăng nhập thất bại!';
            try {
                const response = JSON.parse(xhr.responseText);
                errorMessage = response.message || errorMessage;
            } catch {
                errorMessage = xhr.responseText || errorMessage;
            }
    
            if (xhr.status === 401) {
                window.showNotification(MESSAGES[currentLang].LOGIN_FAILED, 'error', 3000);
            } else if (xhr.status === 403) {
                window.showNotification(MESSAGES[currentLang].ACCOUNT_LOCKED, 'error', 3000);
            } else {
                handleAjaxError(xhr, errorMessage);
            }
        }
    });

    $('#post-game-account-form').on('submit', async function (event) {
        event.preventDefault();
        const $form = $(this);
        const $submitButton = $form.find('button[type="submit"]');
        const originalButtonText = $submitButton.text();
        $submitButton.text('Đang đăng...').prop('disabled', true);
    
        try {
            const data = formToJson($form);
            const gameType = data.gameAccountType.toLowerCase();
    
            const postData = {
                gameAccountType: data.gameAccountType,
                username: data.username,
                password: data.password,
                phonenumber: data.phonenumber || "",
                email: data.email || "",
                description: data.description || "",
                price: Number(data.price) || 0,
                discount: Number(data.discount) || 0,
                imagesAsList: data.imageUrls ? data.imageUrls.split(',').map(url => url.trim()).filter(url => url.length > 0) : []
            };
    
            if (gameType === 'fifa') {
                postData.bp = Number(data.bp) || 0;
                postData.fc = Number(data.fc) || 0;
                postData.valueteam = Number(data.valueteam) || 0;
            } else if (gameType === 'lol') {
                postData.tinhhoalam = Number(data.tinhhoalam) || 0;
                postData.champ = Number(data.champ) || 0;
                postData.skin = Number(data.skin) || 0;
                postData.rp = Number(data.rp) || 0;
                postData.rank = data.rank || "Iron";
            } else if (gameType === 'lq') {
                postData.champ = Number(data.champ) || 0;
                postData.skin = Number(data.skin) || 0;
                postData.rank = data.rank || "Đồng";
            } else {
                window.showNotification('Loại game không được hỗ trợ!', 'error');
                $submitButton.text(originalButtonText).prop('disabled', false);
                return;
            }
    
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GAME_ACCOUNTS}`,
                method: 'POST',
                data: JSON.stringify(postData),
                ...ajaxConfig
            });
    
            window.showNotification(MESSAGES[currentLang].SUCCESS_POST, 'success');
            $form[0].reset();
            $('#fifa-fields, #lol-fields, #lienquan-fields').hide();
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].POST_FAILED);
        } finally {
            $submitButton.text(originalButtonText).prop('disabled', false);
        }
    });

    $('#update-game-account-form').off('submit').on('submit', async function (event) {
        event.preventDefault();
        const $form = $(this);
        const $submitButton = $form.find('button[type="submit"]');
        
        const originalButtonText = $submitButton.text();
        $submitButton.text('Đang cập nhật...').prop('disabled', true);

        try {
            const data = window.formToJson($form);

            if (!data.id || !data.gameAccountType) {
                window.showNotification('Không thể cập nhật: Thiếu thông tin tài khoản! Vui lòng kiểm tra lại dữ liệu.', 'error');
                $submitButton.text(originalButtonText).prop('disabled', false);
                return;
            }

            const gameType = data.gameAccountType.toLowerCase();

            const updatedData = {
                id: data.id,
                gameAccountType: data.gameAccountType,
                username: data.username,
                password: data.password,
                phonenumber: data.phonenumber || null,
                email: data.email || null,
                description: data.description || null,
                price: Number(data.price) || 0,
                discount: Number(data.discount) || 0,
                imageUrls: data.imageUrls ? data.imageUrls.split(',').map(url => url.trim()).filter(url => url.length > 0) : []
            };

            if (gameType === 'fifa') {
                updatedData.bp = Number(data.bp) || 0;
                updatedData.fc = Number(data.fc) || 0;
                updatedData.valueteam = Number(data.valueteam) || 0;
            } else if (gameType === 'lol') {
                updatedData.tinhhoalam = Number(data.tinhhoalam) || 0;
                updatedData.champ = Number(data.champ) || 0;
                updatedData.skin = Number(data.skin) || 0;
                updatedData.rp = Number(data.rp) || 0;
                updatedData.rank = data.rank || 'UNRANKED';
            } else if (gameType === 'lq') {
                updatedData.champ = Number(data.champ) || 0;
                updatedData.skin = Number(data.skin) || 0;
                updatedData.rank = data.rank || 'UNRANKED';
            } else {
                window.showNotification('Loại game không được hỗ trợ!', 'error');
                $submitButton.text(originalButtonText).prop('disabled', false);
                return;
            }

            await window.ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GAME_ACCOUNTS}/${gameType}/${data.id}`,
                method: 'PUT',
                data: JSON.stringify(updatedData),
                contentType: 'application/json',
                ...ajaxConfig
            });

            window.showNotification(MESSAGES[currentLang].SUCCESS_UPDATE, 'success');
            showSection('stats-game-accounts');
            if (typeof window.loadGameAccountsStats === 'function') {
                window.loadGameAccountsStats();
            }
        } catch (xhr) {
            let errorMessage = MESSAGES[currentLang].UPDATE_FAILED;
            if (xhr.responseText) {
                try {
                    const response = JSON.parse(xhr.responseText);
                    errorMessage = response.errorMessage || response.message || errorMessage;
                } catch {
                    errorMessage = xhr.responseText;
                }
            } else if (xhr.readyState === 0) {
                errorMessage = 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng hoặc server!';
            }
            window.showNotification(errorMessage, 'error');
        } finally {
            $submitButton.text(originalButtonText).prop('disabled', false);
        }
    });

    $('#update-user-form').on('submit', async function (event) {
        event.preventDefault();
        const $form = $(this);
        const data = formToJson($form);
        const id = data.id;

        try {
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.USERS}/${id}`,
                method: 'PUT',
                data: JSON.stringify(data),
                ...ajaxConfig
            });
            window.showNotification(MESSAGES[currentLang].SUCCESS_UPDATE, 'success');
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].UPDATE_FAILED);
        }
    });

    $('#update-settings-form').on('submit', async function (event) {
        event.preventDefault();
        const $form = $(this);
        const data = formToJson($form);

        try {
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.DEPOSIT_PAYMENT_INFO}`,
                method: 'PUT',
                data: JSON.stringify(data),
                ...ajaxConfig
            });
            window.showNotification(MESSAGES[currentLang].SUCCESS_UPDATE, 'success');
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].UPDATE_FAILED);
        }
    });

    $('.tab-links a').on('click', function (e) {
        e.preventDefault();
        const tab = $(this).attr('href').replace('#tab-', '');

        $('.tab-links li').removeClass('active');
        $(this).parent().addClass('active');

        $('.tab-content .tab').removeClass('active');
        $(`#tab-${tab}`).addClass('active');

        loadTransactions(tab);
    });

    $('.refresh-btn').on('click', async function () {
        const $button = $(this);
        const tab = $('.tab-links li.active a').attr('href').replace('#tab-', '');

        $button.prop('disabled', true).text('Đang làm mới...');
        try {
            await loadTransactions(tab);
            window.showNotification(MESSAGES[currentLang].REFRESH_SUCCESS, 'success');
        } catch (error) {
            handleAjaxError(error, MESSAGES[currentLang].FETCH_FAILED);
        } finally {
            $button.prop('disabled', false).text('Làm mới');
        }
    });

    $('.nav-links a:contains("Đăng xuất")').on('click', function (e) {
        e.preventDefault();
        logout();
    });

    $('.tab-links a').on('click', function (e) {
        e.preventDefault();
        const $this = $(this);
        const targetTab = $this.attr('href');

        $('.tab-links li').removeClass('active');
        $('.tab').removeClass('active');

        $this.parent().addClass('active');
        $(targetTab).addClass('active');
    });

    $('#update-game-account-form button[type="submit"]').prop('disabled', true);

    const debouncedLoadGameAccountsStats = debounce(loadGameAccountsStats, 300);
    let page = 0;
    const size = 10;
    let currentFilters = { gameTypes: ['fifa', 'lol', 'lq'], statuses: ['SELLING', 'SOLD'], priceMin: null, priceMax: null, dateFrom: null, dateTo: null };

    $('#apply-filters').on('click', function () {
        const gameTypes = $('.game-type-filter:checked').map((_, el) => el.value).get();
        const statuses = $('.status-filter:checked').map((_, el) => el.value).get();
        const priceMin = $('#price-min').val() ? Number($('#price-min').val()) : null;
        const priceMax = $('#price-max').val() ? Number($('#price-max').val()) : null;

        if (gameTypes.length === 0 || statuses.length === 0) {
            window.showNotification('Vui lòng chọn ít nhất một loại game và một trạng thái!', 'error');
            return;
        }

        if (priceMin && priceMax && priceMin > priceMax) {
            window.showNotification('Giá tối thiểu không thể lớn hơn giá tối đa!', 'error');
            return;
        }


        currentFilters = {
            gameTypes,
            statuses,
            priceMin,
            priceMax
        };

        page = 0;
        debouncedLoadGameAccountsStats(currentFilters);
    });

    $('#apply-user-filters').on('click', function () {
        const userStatus = $('#status-filter').val();
        const role = $('#role-filter').val();


        userFilters = {
            userStatus,
            role
        };

        loadUsers(false);
    });

    loadUsers(false);
    $('.table-container').on('scroll', handleUserScroll);

    loadGameAccountsStats(currentFilters).then(result => {
        if (result && result.hasMore) {
            page = result.page;
            setupInfiniteScroll(page, size, currentFilters);
        }
    });

    loadTransactions('accounts');
});

window.loadTransactions = loadTransactions;
window.loadUsers = loadUsers;
window.loadGameAccountDetail = loadGameAccountDetail;
window.loadUserDetail = loadUserDetail;
window.deleteGameAccount = deleteGameAccount;
window.deleteGameAccountFromTable = deleteGameAccountFromTable;
window.deleteUser = deleteUser;
window.deleteUserFromDetail = deleteUserFromDetail;
window.showDeleteConfirmationModal = showDeleteConfirmationModal;
window.showSection = showSection;
window.generateRandomCode = generateRandomCode;
window.updateGameFields = updateGameFields;