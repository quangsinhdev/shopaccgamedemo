let isLoggedIn = false;
let currentFullname = '';
let currentTimeout = null;
let currentPage = { all: 0, fifa: 0, lol: 0, lq: 0 };
let pageSize = { all: 9, fifa: 9, lol: 9, lq: 9 };
let totalPages = { all: 1, fifa: 1, lol: 1, lq: 1 };
let totalElements = { all: 0, fifa: 0, lol: 0, lq: 0 };
let sortDir = { all: 'desc', fifa: 'desc', lol: 'desc', lq: 'desc' };

let isFetching = false;
let loadedAccounts = { all: 0, fifa: 0, lol: 0, lq: 0 };
let currentFilter = 'all';

const CONFIG = {
    BASE_URL: 'https://localhost:8443',
    API_ENDPOINTS: {
        USER_ME: '/api/users/me',
        LOGOUT: '/api/users/logout',
        REFRESH_TOKEN: '/api/users/refresh-token',
        APPLY_VOUCHER: '/api/users/vouchers/apply',
        PURCHASE_ACCOUNT: '/api/users/transactions/gameaccounts',
        GAME_ACCOUNTS: {
            ALL: '/api/gameaccounts/all',
            FIFA: '/api/gameaccounts/fifa',
            LOL: '/api/gameaccounts/lmht',
            LQ: '/api/gameaccounts/lqm'
        }
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
        SESSION_EXPIRED: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!',
        LOGOUT_FAILED: 'Đăng xuất thất bại',
        VOUCHER_FAILED: 'Vui lòng đăng nhập để sử dụng tính năng này.',
        PURCHASE_FAILED: 'Lỗi khi mua tài khoản.',
        FETCH_ACCOUNTS_FAILED: 'Không thể tải danh sách tài khoản.',
        NOT_LOGGED_IN: 'Vui lòng đăng nhập trước khi mua tài khoản!',
        VOUCHER_CODE_EMPTY: 'Vui lòng nhập mã voucher!',
        VOUCHER_INVALID: 'Không thể áp dụng voucher do dữ liệu không hợp lệ!',
        VOUCHER_SUCCESS: (voucherCode, value) => `Áp dụng voucher "${voucherCode}" thành công! Giảm ${formatCurrency(value)} VNĐ`,
        PURCHASE_SUCCESS: (voucher) => voucher ? `Mua tài khoản thành công với voucher "${voucher}"!` : 'Mua tài khoản thành công!',
        CSRF_TOKEN_NOT_FOUND: 'CSRF token không tìm thấy. Vui lòng đăng nhập lại!'
    }
};
const currentLang = 'vi';

const ajaxConfig = {
    xhrFields: { withCredentials: true },
    contentType: 'application/json',
    timeout: 5000
};

const showNotification = (message, type, duration = 3000) => {
    if (currentTimeout) {
        clearTimeout(currentTimeout);
        $('#notification').removeClass('show').addClass('hidden');
    }

    const $notification = $('#notification');
    const icon = type === 'success' ? '<i class="fa-solid fa-check-circle icon"></i>' : '<i class="fa-solid fa-exclamation-circle icon"></i>';
    $notification.html(icon + message).removeClass('hidden success error').addClass(`show ${type}`);

    currentTimeout = setTimeout(() => {
        $notification.removeClass('show').addClass('hidden');
        currentTimeout = null;
    }, duration);
};

const resetToLoggedOutState = () => {
    isLoggedIn = false;
    currentFullname = '';
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('csrfToken');
    $('#login-item').show();
    $('#user-item').hide();
    $('#fullname-display').text('');
    $('#balance-container').hide();
};

const updateDropdown = () => {
    if (isLoggedIn) {
        $('#login-item').hide();
        $('#user-item').show();
        $('#fullname-display').text(currentFullname || 'N/A');
        $('#balance-container').show();
    } else {
        $('#login-item').show();
        $('#user-item').hide();
        $('#fullname-display').text('');
        $('#balance-container').hide();
    }
    attachLogoutEvent();
};

const errorHandlers = {
    401: (msg, defaultMsg) => {
        if (isLoggedIn) {
            showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error', 2500);
            resetToLoggedOutState();
            setTimeout(() => window.location.href = CONFIG.PAGES.LOGIN, 2500);
        }
    },
    403: (msg, defaultMsg) => showNotification(msg === defaultMsg ? 'Bạn không có quyền thực hiện hành động này!' : msg, 'error'),
    400: (msg, defaultMsg) => showNotification(msg === defaultMsg ? 'Yêu cầu không hợp lệ. Vui lòng kiểm tra lại!' : msg, 'error'),
    404: (msg, defaultMsg) => showNotification(msg === defaultMsg ? 'Không tìm thấy tài nguyên!' : msg, 'error'),
    409: (msg, defaultMsg) => showNotification(msg === defaultMsg ? 'Tài nguyên đang được xử lý!' : msg, 'error'),
    422: (msg, defaultMsg) => showNotification(msg === defaultMsg ? 'Dữ liệu không thể xử lý!' : msg, 'error'),
    500: (msg, defaultMsg) => showNotification(msg === defaultMsg ? 'Lỗi server. Vui lòng thử lại sau!' : msg, 'error'),
    default: (msg) => showNotification(msg, 'error')
};

function handleAjaxError(xhr, defaultMessage) {
    let errorMessage = defaultMessage;
    try {
        const errorResponse = JSON.parse(xhr.responseText);
        errorMessage = errorResponse.message || errorResponse.errorMessage || defaultMessage;
    } catch {}
    (errorHandlers[xhr.status] || errorHandlers.default)(errorMessage, defaultMessage);
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

async function refreshToken() {
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
        return true;
    } catch {
        localStorage.removeItem('isLoggedIn');
        localStorage.removeItem('csrfToken');
        window.location.href = CONFIG.PAGES.LOGIN;
        return false;
    }
}

function attachLogoutEvent() {
    $('#logout-btn').off('click').on('click', async (e) => {
        e.preventDefault();
        try {
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.LOGOUT}`,
                method: 'POST',
                data: JSON.stringify({}),
                ...ajaxConfig
            });
            resetToLoggedOutState();
            document.cookie = 'accessToken=; Max-Age=0; Path=/';
            document.cookie = 'refreshToken=; Max-Age=0; Path=/';
            window.location.href = CONFIG.PAGES.LOGIN;
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].LOGOUT_FAILED);
            resetToLoggedOutState();
            document.cookie = 'accessToken=; Max-Age=0; Path=/';
            document.cookie = 'refreshToken=; Max-Age=0; Path=/';
            setTimeout(() => window.location.href = CONFIG.PAGES.LOGIN, 1500);
        }
    });
}

const formatCurrency = (amount) => typeof amount === 'number' ? amount.toLocaleString('vi-VN') : 'N/A';

async function fetchUserDetails(callback, isRetry = false) {
    try {
        const user = await ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.USER_ME}`,
            method: 'GET',
            ...ajaxConfig
        });
        isLoggedIn = true;
        currentFullname = user.fullname || 'N/A';
        $('#login-item').hide();
        $('#user-item').show();
        $('#fullname-display').text(currentFullname);
        $('#balance-display').text(formatCurrency(user.balance) || '0');
        $('#balance-container').show();
        localStorage.setItem('isLoggedIn', 'true');
        if (callback) callback(true);
    } catch (xhr) {
        if (xhr.status === 401 && !isRetry) {
            const refreshed = await refreshToken();
            if (refreshed) {
                fetchUserDetails(callback, true);
            } else {
                resetToLoggedOutState();
                if (callback) callback(false);
            }
        } else {
            resetToLoggedOutState();
            if (callback) callback(false);
        }
    }
}

function showPurchaseModal(accountId, gameAccountType, accountName, price, discount, fromInfoModal = false) {
    const modal = document.getElementById('purchase-modal');
    const nameElement = document.getElementById('modal-account-name');
    const priceElement = document.getElementById('modal-account-price');
    const discountedPriceElement = document.getElementById('modal-account-discounted-price');
    const discountElement = document.getElementById('modal-account-discount');
    const finalPriceElement = document.getElementById('modal-account-final-price');
    const discountedPriceRow = document.getElementById('modal-discounted-price');
    const discountRow = document.getElementById('modal-discount');
    const finalPriceRow = document.getElementById('modal-final-price');
    const voucherInput = document.getElementById('voucher-code');
    let appliedVoucher = null;
    let voucherValue = 0;
    let finalPrice = discount > 0 ? price * (1 - discount / 100) : price;

    nameElement.textContent = accountName;
    priceElement.textContent = formatCurrency(price);

    if (discount > 0) {
        const discountedPrice = price * (1 - discount / 100);
        discountedPriceElement.textContent = formatCurrency(discountedPrice);
        discountElement.textContent = discount;
        discountedPriceRow.style.display = 'block';
        discountRow.style.display = 'block';
    } else {
        discountedPriceRow.style.display = 'none';
        discountRow.style.display = 'none';
    }

    finalPriceElement.textContent = formatCurrency(finalPrice);
    finalPriceRow.style.display = 'block';
    voucherInput.value = '';

    modal.style.display = 'flex';

    $('#apply-voucher-btn').off('click').on('click', async () => {
        const voucherCode = voucherInput.value.trim();
        if (!voucherCode) {
            showNotification(MESSAGES[currentLang].VOUCHER_CODE_EMPTY, 'error');
            return;
        }

        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.APPLY_VOUCHER}`,
                method: 'POST',
                data: JSON.stringify({ code: voucherCode }),
                ...ajaxConfig
            });
            if (!response || !response.value || response.value < 0) {
                showNotification(MESSAGES[currentLang].VOUCHER_INVALID, 'error');
                return;
            }

            appliedVoucher = voucherCode;
            voucherValue = response.value;
            finalPrice = Math.max((discount > 0 ? price * (1 - discount / 100) : price) - voucherValue, 0);
            finalPriceElement.textContent = formatCurrency(finalPrice);
            showNotification(
                MESSAGES[currentLang].VOUCHER_SUCCESS(voucherCode, voucherValue),
                'success'
            );
        } catch (xhr) {
            if (xhr.status === 401) {
                showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error');
            } else {
                handleAjaxError(xhr, MESSAGES[currentLang].VOUCHER_FAILED);
            }
        }
    });

    $('#confirm-purchase-btn').off('click').on('click', (event) => {
        event.stopPropagation();
        purchaseAccount(accountId, gameAccountType, appliedVoucher);
        modal.style.display = 'none';
        if (fromInfoModal) document.getElementById('account-info-modal').style.display = 'none';
    });

    $('#cancel-purchase-btn, #purchase-modal .close-modal').off('click').on('click', () => {
        modal.style.display = 'none';
    });
}

function showAccountInfoModal(accountId, gameAccountType, accountName, price, discount, description, images, accountData = {}) {
    const modal = document.getElementById('account-info-modal');
    document.getElementById('info-account-name').textContent = accountName;
    document.getElementById('info-account-id').textContent = `${gameAccountType} - ${accountId}`;
    document.getElementById('info-account-price').textContent = formatCurrency(price);
    document.getElementById('info-account-description').textContent = description || 'Không có thông tin';

    const imagesContainer = document.getElementById('info-account-images');
    imagesContainer.innerHTML = images.length > 0
        ? images.map(img => `<img src="${img}" alt="Account Image" class="gallery-image">`).join('')
        : '<p>Không có hình ảnh</p>';

    Array.from(imagesContainer.getElementsByClassName('gallery-image')).forEach((img) => {
        img.addEventListener('click', () => showImageZoomModal(img.src));
    });

    const discountedPriceRow = document.getElementById('info-discounted-price');
    const discountRow = document.getElementById('info-discount');
    if (discount > 0) {
        const discountedPrice = price * (1 - discount / 100);
        document.getElementById('info-account-discounted-price').textContent = formatCurrency(discountedPrice);
        document.getElementById('info-account-discount').textContent = discount;
        discountedPriceRow.style.display = 'block';
        discountRow.style.display = 'block';
    } else {
        discountedPriceRow.style.display = 'none';
        discountRow.style.display = 'none';
    }

    const allExtraFields = ['fifa-gtdh', 'fifa-bp', 'fifa-fc', 'lol-rp', 'lol-champ', 'lol-skin', 'lol-rank', 'lq-champ', 'lq-skin', 'lq-rank'];
    allExtraFields.forEach(field => document.getElementById(field).style.display = 'none');

    if (gameAccountType && typeof gameAccountType === 'string') {
        switch (gameAccountType.toLowerCase()) {
            case 'fifa':
                ['fifa-gtdh', 'fifa-bp', 'fifa-fc'].forEach(field => document.getElementById(field).style.display = 'block');
                document.getElementById('info-fifa-gtdh').textContent = formatCurrency(accountData.valueteam) || 'N/A';
                document.getElementById('info-fifa-bp').textContent = formatCurrency(accountData.bp) || 'N/A';
                document.getElementById('info-fifa-fc').textContent = formatCurrency(accountData.fc) || 'N/A';
                break;
            case 'lol':
                ['lol-rp', 'lol-champ', 'lol-skin', 'lol-rank'].forEach(field => document.getElementById(field).style.display = 'block');
                document.getElementById('info-lol-rp').textContent = accountData.rp || 'N/A';
                document.getElementById('info-lol-champ').textContent = accountData.champ || 'N/A';
                document.getElementById('info-lol-skin').textContent = accountData.skin || 'N/A';
                document.getElementById('info-lol-rank').textContent = accountData.rank || 'N/A';
                break;
            case 'lq':
                ['lq-champ', 'lq-skin', 'lq-rank'].forEach(field => document.getElementById(field).style.display = 'block');
                document.getElementById('info-lq-champ').textContent = accountData.champ || 'N/A';
                document.getElementById('info-lq-skin').textContent = accountData.skin || 'N/A';
                document.getElementById('info-lq-rank').textContent = accountData.rank || 'N/A';
                break;
        }
    }

    modal.style.display = 'flex';

    $('#buy-from-info-btn').off('click').on('click', (event) => {
        event.stopPropagation();
        showPurchaseModal(accountId, gameAccountType, accountName, price, discount, true);
    });

    $('#account-info-modal .close-modal, #account-info-modal .close-modal-detail').off('click').on('click', () => {
        modal.style.display = 'none';
    });
}

function showImageZoomModal(imageSrc) {
    const modal = document.getElementById('image-zoom-modal');
    const zoomedImage = document.getElementById('zoomed-image');
    if (!modal || !zoomedImage) return;

    zoomedImage.src = imageSrc;
    modal.style.display = 'flex';

    modal.addEventListener('click', (event) => {
        if (event.target === modal) modal.style.display = 'none';
    });

    $('#image-zoom-modal .close-modal-detail').off('click').on('click', () => {
        modal.style.display = 'none';
    });
}

async function purchaseAccount(accountId, gameAccountType, voucher = null) {
    await fetchUserDetails(async (loggedIn) => {
        if (!loggedIn) {
            showNotification(MESSAGES[currentLang].NOT_LOGGED_IN, 'error');
            return;
        }

        const requestData = { accountId, gameAccountType };
        if (voucher) requestData.voucher = voucher;

        try {
            const transaction = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.PURCHASE_ACCOUNT}`,
                method: 'POST',
                data: JSON.stringify(requestData),
                ...ajaxConfig
            });
            const message = MESSAGES[currentLang].PURCHASE_SUCCESS(voucher);
            showNotification(message, 'success');
            loadedAccounts[currentFilter] = 0;
            totalElements[currentFilter] = 0;
            currentPage[currentFilter] = 0;
            fetchAccounts(currentFilter, 0, pageSize[currentFilter], sortDir[currentFilter]);
            fetchUserDetails();
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].PURCHASE_FAILED);
        }
    });
}

async function fetchAccounts(filter = 'all', page = currentPage[filter], size = pageSize[filter], sortDirField = sortDir[filter], append = false) {
    if (isFetching) {
        console.log(`Đang gọi API, bỏ qua request mới với filter=${filter}`);
        return;
    }

    if (page === 0) {
        loadedAccounts[filter] = 0;
        totalElements[filter] = 0;
        totalPages[filter] = 1;
    }

    if (totalElements[filter] === 0 && page > 0) {
        const container = $('#accounts-container');
        return;
    }

    if (loadedAccounts[filter] >= totalElements[filter] && totalElements[filter] !== 0) {
        const container = $('#accounts-container');
        return;
    }

    isFetching = true;

    const urlMap = {
        all: CONFIG.API_ENDPOINTS.GAME_ACCOUNTS.ALL,
        fifa: CONFIG.API_ENDPOINTS.GAME_ACCOUNTS.FIFA,
        lol: CONFIG.API_ENDPOINTS.GAME_ACCOUNTS.LOL,
        lq: CONFIG.API_ENDPOINTS.GAME_ACCOUNTS.LQ
    };
    const url = urlMap[filter.toLowerCase()] || urlMap.all;

    if (!['all', 'fifa', 'lol', 'lq'].includes(filter)) {
        console.error(`Filter không hợp lệ: ${filter}. Đặt về 'all'.`);
        filter = 'all';
    }

    page = parseInt(page);
    if (isNaN(page) || page < 0) {
        page = 0;
        currentPage[filter] = 0;
    }

    size = parseInt(size);
    if (isNaN(size) || size <= 0) {
        size = 9;
        pageSize[filter] = 9;
    }

    sortDirField = sortDirField && sortDirField.toLowerCase() === 'asc' ? 'asc' : 'desc';
    sortDir[filter] = sortDirField;

    console.log(`Gửi request với filter=${filter}, page=${page}, size=${size}, sort=${sortDirField}`);

    const container = $('#accounts-container');
    if (!append) {
        container.empty();
        loadedAccounts[filter] = 0;
    }
    container.append('<p class="loading-text">Đang tải danh sách tài khoản...</p>');

    try {
        const response = await ajaxWithRetry({
            url: `${CONFIG.BASE_URL}${url}?page=${page}&size=${size}&sort=${sortDirField}`,
            method: 'GET',
            ...ajaxConfig
        });

        container.find('.loading-text').remove();
        container.find('.no-more-data').remove();

        console.log('Dữ liệu trả về từ API:', response);

        let accounts = response.content || [];

        if (!Array.isArray(accounts)) {
            console.error('Danh sách tài khoản không hợp lệ:', accounts);
            container.append('<p>Lỗi dữ liệu tài khoản. Vui lòng thử lại sau.</p>');
            return;
        }

        const existingIds = new Set(
            Array.from(container.find('.account-card')).map(card => $(card).data('account-id') + '-' + $(card).find('.account-id').text())
        );
        const newAccounts = accounts.filter(account => !existingIds.has(account.id + '-' + `${account.gameAccountType} - ${account.id}`));

        if (!newAccounts.length && loadedAccounts[filter] === 0) {
            container.append('<p>Không có tài khoản nào trong danh mục này.</p>');
        } else if (!newAccounts.length) {
            console.log(`Không còn dữ liệu mới để tải cho filter=${filter}`);
        } else {
            newAccounts.forEach(account => {
                const images = account.imagesAsList || [];
                const firstImage = images.length > 0 ? images[0] : '/assets/images/defaultaccountbanner.jpg';

                const displayName = {
                    fifa: 'FIFA ONLINE 4',
                    lol: 'Liên Minh Huyền Thoại',
                    lq: 'Liên Quân Mobile'
                }[account.gameAccountType.toLowerCase()] || account.gameAccountType;

                const discountedPrice = account.discount > 0 ? account.price * (1 - account.discount / 100) : account.price;

                let accountHtml = `
                    <div class="account-card" data-account-id="${account.id}">
                        <div class="account-avatar" style="background-image: url('${firstImage}');"></div>
                        <div class="account-info">
                            <div class="account-name">Tài khoản: ${displayName}</div>
                            <div class="account-id">Mã số tài khoản: ${account.gameAccountType} - ${account.id}</div>
                            <div class="price-row">
                                <div class="account-price ${account.discount > 0 ? 'original-price' : ''}">
                                    <i class="fa-solid fa-money-bill-1"></i> Giá: ${formatCurrency(account.price)} VNĐ
                                </div>
                `;

                if (account.discount > 0) {
                    accountHtml += `
                        <div class="account-discount"><i class="fa-solid fa-tags"></i> Giảm giá: ${account.discount}%</div>
                        <div class="discounted-price">Giá sau khuyến mãi: ${formatCurrency(discountedPrice)} VNĐ</div>
                    `;
                }

                accountHtml += `
                            </div>
                            <div class="account-description">Thông tin thêm: ${account.description || 'Không có thông tin'}</div>
                            <button class="details-btn" 
                                data-account-id="${account.id}" 
                                data-account-type="${account.gameAccountType}" 
                                data-account-name="${displayName}" 
                                data-account-price="${account.price}" 
                                data-account-discount="${account.discount || 0}" 
                                data-account-description="${account.description}" 
                                data-images='${JSON.stringify(images)}'
                                data-account='${JSON.stringify(account)}'>Xem chi tiết</button>
                            <button class="buy-btn" 
                                data-account-id="${account.id}" 
                                data-account-type="${account.gameAccountType}" 
                                data-account-name="${displayName}" 
                                data-account-price="${account.price}" 
                                data-account-discount="${account.discount || 0}"><i class="fa-solid fa-cart-shopping icon-action"></i> Mua tài khoản</button>
                        </div>
                    </div>
                `;

                container.append(accountHtml);
            });

            $('.details-btn').off('click').on('click', function(event) {
                event.stopPropagation();
                const accountId = $(this).data('account-id');
                const gameAccountType = $(this).data('account-type');
                const accountName = $(this).data('account-name');
                const price = $(this).data('account-price');
                const discount = $(this).data('account-discount');
                const description = $(this).data('account-description');
                const images = JSON.parse($(this).attr('data-images'));
                const accountData = JSON.parse($(this).attr('data-account'));
                showAccountInfoModal(accountId, gameAccountType, accountName, price, discount, description, images, accountData);
            });

            $('.buy-btn').off('click').on('click', function(event) {
                event.stopPropagation();
                const accountId = $(this).data('account-id');
                const gameAccountType = $(this).data('account-type');
                const accountName = $(this).data('account-name');
                const price = $(this).data('account-price');
                const discount = $(this).data('account-discount');
                showPurchaseModal(accountId, gameAccountType, accountName, price, discount, false);
            });

            loadedAccounts[filter] += newAccounts.length;
        }

        let paginationData = response.page || {};
        currentPage[filter] = paginationData.number !== undefined ? paginationData.number : 0;
        pageSize[filter] = paginationData.size !== undefined ? paginationData.size : 9;
        totalPages[filter] = paginationData.totalPages !== undefined ? paginationData.totalPages : 1;
        totalElements[filter] = paginationData.totalElements !== undefined ? paginationData.totalElements : 0;

        if (totalElements[filter] === 0 && page === 0) {
            console.log(`Không có dữ liệu từ API, dừng gọi tiếp cho filter=${filter}`);
        }
    } catch (xhr) {
        container.find('.loading-text').remove();
        currentPage[filter] = 0;
        pageSize[filter] = 9;
        totalPages[filter] = 1;
        totalElements[filter] = 0;
        loadedAccounts[filter] = 0;

        container.empty();
        container.append('<p>Không thể tải danh sách tài khoản. Vui lòng thử lại sau.</p>');

        handleAjaxError(xhr, MESSAGES[currentLang].FETCH_ACCOUNTS_FAILED);
        console.error(`Lỗi khi gọi API với filter=${filter}:`, xhr);
    } finally {
        isFetching = false;
    }
}

$(document).ready(() => {
    fetchAccounts('all');

    isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    if (isLoggedIn) {
        fetchUserDetails((success) => {
            if (!success) {
                localStorage.removeItem('isLoggedIn');
                localStorage.removeItem('csrfToken');
            }
            updateDropdown();
        });
    } else {
        updateDropdown();
    }

    $('.sort-btn-price').on('click', function () {
        const filter = $('.category-item.selected').text().trim().toLowerCase();
        const filterKey = {
            'tất cả': 'all',
            'fifa online 4': 'fifa',
            'liên minh huyền thoại': 'lol',
            'liên quân mobile': 'lq'
        }[filter] || 'all';
        sortDir[filterKey] = sortDir[filterKey] === 'asc' ? 'desc' : 'asc';
        currentPage[filterKey] = 0;
        fetchAccounts(filterKey, 0, pageSize[filterKey], sortDir[filterKey]);
        $(this).text(sortDir[filterKey] === 'asc' ? 'Sắp xếp giá ↑' : 'Sắp xếp giá ↓');
    });

    $('.category-item').on('click', function() {
        $('.category-item').removeClass('selected');
        $(this).addClass('selected');
        const filter = {
            'tất cả': 'all',
            'fifa online 4': 'fifa',
            'liên minh huyền thoại': 'lol',
            'liên quân mobile': 'lq'
        }[$(this).text().trim().toLowerCase()] || 'all';
    
        currentFilter = filter;
        currentPage[filter] = 0;
        loadedAccounts[filter] = 0;
        totalElements[filter] = 0;
        totalPages[filter] = 1;
        pageSize[filter] = 9;
        fetchAccounts(filter, 0, pageSize[filter], sortDir[filter]);
    });

    $(window).on('scroll', function() {
        if (isFetching) return;

        const scrollPosition = $(window).scrollTop() + $(window).height();
        const documentHeight = $(document).height();

        if (scrollPosition >= documentHeight - 100) {
            currentPage[currentFilter] += 1;
            fetchAccounts(currentFilter, currentPage[currentFilter], pageSize[currentFilter], sortDir[currentFilter], true);
        }
    });

    $('#user-avatar').on('click', () => {
        $('#avatar-dropdown').toggleClass('show');
    });

    $(window).on('click', (event) => {
        if (!event.target.matches('.avatar') && !event.target.closest('#avatar-dropdown') &&
            !event.target.closest('#purchase-modal') && !event.target.closest('#account-info-modal') &&
            !event.target.closest('#image-zoom-modal')) {
            $('.avatar-dropdown.show').removeClass('show');
            const purchaseModal = document.getElementById('purchase-modal');
            const infoModal = document.getElementById('account-info-modal');
            const zoomModal = document.getElementById('image-zoom-modal');

            if (purchaseModal?.style.display === 'flex') purchaseModal.style.display = 'none';
            if (infoModal?.style.display === 'flex' && purchaseModal?.style.display !== 'flex' &&
                (!zoomModal || zoomModal.style.display !== 'flex')) infoModal.style.display = 'none';
            if (zoomModal?.style.display === 'flex') zoomModal.style.display = 'none';
        }
    });
});