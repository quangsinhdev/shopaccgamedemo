const giftcodeConfig = {
    currentPage: 0,
    pageSize: 10,
    isLoading: false,
    hasMore: true,
    observer: null,
    sortField: 'id',
    sortDirection: 'asc'
};

$(document).ready(function () {
    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            USER_ME: '/api/users/me',
            ACTIVATE_GIFTCODE: '/api/users/giftcodes/activate',
            LOGOUT: '/api/users/logout',
            REFRESH_TOKEN: '/api/users/refresh-token',
            GIFTCODES: '/api/admin/giftcodes'
        },
        PAGES: { LOGIN: '/pages/client/login.html' },
        CSRF_EXCLUDED_PATHS: ['/api/users/refresh-token']
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
            FETCH_FAILED: 'Không thể tải dữ liệu!',
            SUCCESS_UPDATE: 'Cập nhật thành công!',
            UPDATE_FAILED: 'Cập nhật thất bại!',
            SUCCESS_POST: 'Tạo mới thành công!',
            POST_FAILED: 'Tạo mới thất bại!',
            CSRF_TOKEN_NOT_FOUND: 'Không tìm thấy CSRF token. Vui lòng đăng nhập lại!'
        }
    };
    const currentLang = 'vi';

    let isRefreshing = false;
    let isAuthenticated = localStorage.getItem('isLoggedIn') === 'true';
    const tokenRefreshSubscribers = [];

    const ajaxConfig = {
        xhrFields: { withCredentials: true },
        contentType: 'application/json',
        dataType: 'json',
        timeout: 5000
    };

    const showNotification = (message, type, duration = 3000, callback) => {
        $('.notification').remove();
        const $notification = $(`<div class="notification ${type}">
            <span class="icon">${type === 'success' ? '✔' : '✖'}</span>
            <span>${message}</span>
        </div>`);
        $('body').append($notification);
        setTimeout(() => $notification.addClass('show'), 10);
        setTimeout(() => {
            $notification.removeClass('show').addClass('hidden');
            setTimeout(() => $notification.remove(), 500);
        }, duration);
        if (callback) setTimeout(callback, duration);
    };

    const handleAjaxError = (xhr, defaultMsg) => {
        let msg = defaultMsg;
        try {
            const response = JSON.parse(xhr.responseText);
            msg = response.message || response.messageResponse || defaultMsg;
        } catch {
            msg = xhr.responseText || defaultMsg;
        }
        const errorHandlers = {
            400: () => showNotification(msg === 'Giftcode không tồn tại hoặc không khả dụng!' ? MESSAGES[currentLang].GIFTCODE_NOT_FOUND : (msg || MESSAGES[currentLang].BAD_REQUEST), 'error'),
            401: () => showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error', 2000, () => {
                localStorage.removeItem('isLoggedIn');
                window.location.href = CONFIG.PAGES.LOGIN;
            }),
            403: () => showNotification(msg || MESSAGES[currentLang].FORBIDDEN, 'error'),
            404: () => showNotification(msg || MESSAGES[currentLang].GIFTCODE_NOT_FOUND, 'error'),
            500: () => showNotification(MESSAGES[currentLang].SERVER_ERROR, 'error'),
            default: () => showNotification(msg, 'error')
        };
        (errorHandlers[xhr.status] || errorHandlers.default)();
    };

    const subscribeTokenRefresh = (callback) => {
        tokenRefreshSubscribers.push(callback);
    };

    const onTokenRefreshed = (success) => {
        tokenRefreshSubscribers.forEach(callback => callback(success));
        tokenRefreshSubscribers.length = 0;
    };

    const refreshToken = async () => {
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
                showNotification('Không thể làm mới CSRF token. Vui lòng đăng nhập lại!', 'error', 2000, () => {
                    isAuthenticated = false;
                    localStorage.removeItem('isLoggedIn');
                    window.location.href = CONFIG.PAGES.LOGIN;
                });
                throw new Error('No CSRF token returned');
            }

            isRefreshing = false;
            onTokenRefreshed(true);
            return true;
        } catch (xhr) {
            if (xhr.status === 401) {
                showNotification(MESSAGES[currentLang].SESSION_EXPIRED, 'error', 2000, () => {
                    isAuthenticated = false;
                    localStorage.removeItem('isLoggedIn');
                    window.location.href = CONFIG.PAGES.LOGIN;
                });
            } else if (xhr.readyState === 0) {
                showNotification('Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng!', 'error');
            } else {
                showNotification('Lỗi không xác định khi làm mới token. Vui lòng thử lại!', 'error', 2000);
            }
            isRefreshing = false;
            onTokenRefreshed(false);
            return false;
        }
    };

    const ajaxWithRetry = async (settings) => {
        const method = settings.method || settings.type || 'GET';
        const isSafeMethod = ['GET', 'HEAD', 'OPTIONS'].includes(method.toUpperCase());
        const isExcluded = CONFIG.CSRF_EXCLUDED_PATHS.some(path => settings.url.includes(path));


        if (!isSafeMethod && !isExcluded) {
            const csrfToken = localStorage.getItem('csrfToken');
            if (!csrfToken) {
                showNotification(MESSAGES[currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                    localStorage.removeItem('csrfToken');
                    localStorage.removeItem('isLoggedIn');
                    isAuthenticated = false;
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
            const response = await $.ajax(settings);
            return response;
        } catch (xhr) {
            if (xhr.status === 401) {
                const success = await refreshToken();
                if (success) {
                    if (!isSafeMethod && !isExcluded) {
                        const newCsrfToken = localStorage.getItem('csrfToken');
                        if (!newCsrfToken) {
                            showNotification(MESSAGES[currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                                localStorage.removeItem('csrfToken');
                                localStorage.removeItem('isLoggedIn');
                                isAuthenticated = false;
                                window.location.href = CONFIG.PAGES.LOGIN;
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
                            showNotification('CSRF token không hợp lệ sau khi làm mới. Vui lòng đăng nhập lại!', 'error', 2000, () => {
                                localStorage.removeItem('csrfToken');
                                localStorage.removeItem('isLoggedIn');
                                isAuthenticated = false;
                                window.location.href = CONFIG.PAGES.LOGIN;
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
                            showNotification(MESSAGES[currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                                localStorage.removeItem('csrfToken');
                                localStorage.removeItem('isLoggedIn');
                                isAuthenticated = false;
                                window.location.href = CONFIG.PAGES.LOGIN;
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
                            showNotification('CSRF token không hợp lệ sau khi làm mới. Vui lòng đăng nhập lại!', 'error', 2000, () => {
                                localStorage.removeItem('csrfToken');
                                localStorage.removeItem('isLoggedIn');
                                isAuthenticated = false;
                                window.location.href = CONFIG.PAGES.LOGIN;
                            });
                        }
                        throw retryXhr;
                    }
                } else {
                    showNotification('CSRF token không hợp lệ. Vui lòng đăng nhập lại!', 'error', 2000, () => {
                        localStorage.removeItem('csrfToken');
                        localStorage.removeItem('isLoggedIn');
                        isAuthenticated = false;
                        window.location.href = CONFIG.PAGES.LOGIN;
                    });
                    throw new Error('CSRF validation failed');
                }
            }
            throw xhr;
        }
    };

    const formatCurrency = (value) => value != null ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value) : 'Không có thông tin';
    const formatDate = (date) => date ? new Date(date).toLocaleDateString('vi-VN') : 'Không có thông tin';
    const formatDateTime = (date) => date ? new Date(date).toLocaleString('vi-VN') : '-';
    const getStatusDisplay = (promotionStatus) => ({ 'AVAILABLE': 'Chưa sử dụng', 'USED': 'Đã sử dụng' }[promotionStatus] || promotionStatus);

    const formToJson = ($form) => $form.serializeArray().reduce((obj, item) => { obj[item.name] = item.value; return obj; }, {});

    const validateGiftcode = (giftcode) => {
        if (!giftcode || /^\s*$/.test(giftcode)) return showNotification(MESSAGES[currentLang].GIFTCODE_EMPTY, 'error'), false;
        if (giftcode.length < 4 || giftcode.length > 30) return showNotification(MESSAGES[currentLang].GIFTCODE_LENGTH, 'error'), false;
        if (!/^[a-zA-Z0-9]+$/.test(giftcode)) return showNotification(MESSAGES[currentLang].GIFTCODE_PATTERN, 'error'), false;
        return true;
    };

    const loadGiftcodes = async (append = false) => {
        if (giftcodeConfig.isLoading) return;
        if (!append && !giftcodeConfig.hasMore) return;

        if (!append) {
            giftcodeConfig.currentPage = 0;
            giftcodeConfig.hasMore = true;
        }

        giftcodeConfig.isLoading = true;
        const $tableBody = $('#giftcode-table');

        try {
            const url = new URL(`${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GIFTCODES}`);
            url.searchParams.append('page', giftcodeConfig.currentPage);
            url.searchParams.append('size', giftcodeConfig.pageSize);
            url.searchParams.append('sort', `${giftcodeConfig.sortField},${giftcodeConfig.sortDirection}`);

            const response = await ajaxWithRetry({
                url: url.toString(),
                method: 'GET',
                ...ajaxConfig
            });

            const giftcodes = response.content || [];
            const normalizedGiftcodes = giftcodes.map(giftcode => ({
                id: Number(giftcode.id),
                code: giftcode.code || 'Không có thông tin',
                value: giftcode.value,
                timeOfListing: giftcode.timeOfListing || null,
                giftcodeInfo: giftcode.giftcodeInfo || 'Không có thông tin',
                promotionStatus: giftcode.promotionStatus || 'Không có thông tin',
                timeOfUse: giftcode.timeOfUse || null
            }));

            if (!append) $tableBody.empty();

            if (!normalizedGiftcodes.length && !append) {
                $tableBody.append('<tr><td colspan="7" class="text-center">Không có giftcode nào.</td></tr>');
            } else {
                normalizedGiftcodes.forEach(giftcode => {
                    const actionButtons = giftcode.id ? `
                        <a href="#" class="btn btn-update edit-giftcode" data-giftcode-id="${giftcode.id}">Sửa</a>
                        <a href="#" class="btn btn-lock delete-giftcode" data-giftcode-id="${giftcode.id}">Xóa</a>
                    ` : '-';

                    $tableBody.append(`
                        <tr>
                            <td>${giftcode.code}</td>
                            <td>${formatCurrency(giftcode.value)}</td>
                            <td>${formatDate(giftcode.timeOfListing)}</td>
                            <td>${giftcode.giftcodeInfo}</td>
                            <td>${getStatusDisplay(giftcode.promotionStatus)}</td>
                            <td>${formatDateTime(giftcode.timeOfUse)}</td>
                            <td>${actionButtons}</td>
                        </tr>
                    `);
                });
            }

            giftcodeConfig.hasMore = !response.last;
            if (giftcodeConfig.hasMore) {
                giftcodeConfig.currentPage++;
                setupGiftcodeInfiniteScroll();
            } else if (!$tableBody.siblings('.no-more-data').length) {
                $tableBody.after('<div class="no-more-data text-center">Đã tải hết dữ liệu</div>');
                if (giftcodeConfig.observer) giftcodeConfig.observer.disconnect();
            }
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].FETCH_FAILED);
        } finally {
            giftcodeConfig.isLoading = false;
        }
    };

    const setupGiftcodeInfiniteScroll = () => {
        const $trigger = $('#giftcode-load-more-trigger');
        if (giftcodeConfig.observer) giftcodeConfig.observer.disconnect();

        const debouncedLoadMore = debounceObserver((entries) => {
            if (entries[0].isIntersecting && !giftcodeConfig.isLoading && giftcodeConfig.hasMore) {
                loadGiftcodes(true);
            }
        }, 300);

        giftcodeConfig.observer = new IntersectionObserver(debouncedLoadMore, { threshold: 0.5 });
        if ($trigger.length) giftcodeConfig.observer.observe($trigger[0]);
    };

    const debounceObserver = (func, wait) => {
        let timeout;
        return (...args) => {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    };

    const generateGiftcodeCode = () => {
        const length = 11;
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
        const randomCode = Array.from({ length }, () => 
            characters.charAt(Math.floor(Math.random() * characters.length))
        ).join('');
        $('#giftcode-code').val(randomCode);
    };

    const closeEditGiftcodeModal = () => $('#editGiftcodeModal').removeClass('show');

    const editGiftcode = async (id) => {
        try {
            const giftcode = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GIFTCODES}/${id}`,
                method: 'GET',
                ...ajaxConfig
            });

            const $form = $('#edit-giftcode-form');
            if ($form.length) {
                $form.find('#giftcode-id').val(giftcode.id);
                $form.find('#edit-giftcode-code').val(giftcode.code);
                $form.find('#edit-giftcode-value').val(giftcode.value);
                $form.find('#edit-giftcode-info').val(giftcode.giftcodeInfo);
                $form.find('#edit-giftcode-status').val(giftcode.promotionStatus);
            }

            const $modal = $('#editGiftcodeModal');
            if ($modal.length) $modal.addClass('show');
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].FETCH_FAILED);
        }
    };

    const deleteGiftcode = async (giftcodeId) => {
        if (!giftcodeId || isNaN(giftcodeId)) {
            showNotification('ID giftcode không hợp lệ!', 'error');
            return;
        }

        const confirmed = await showConfirmationModal('Bạn có chắc chắn muốn xóa giftcode này?');
        if (!confirmed) return;

        try {
            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GIFTCODES}/${giftcodeId}`,
                method: 'DELETE',
                ...ajaxConfig
            });
            showNotification('Xóa giftcode thành công!', 'success');
            loadGiftcodes();
        } catch (xhr) {
            handleAjaxError(xhr, 'Không thể xóa giftcode!');
        }
    };

    const showConfirmationModal = (message) => {
        return new Promise((resolve) => {
            const $modal = $('#confirmationDeleteGiftcode');
            if (!$modal.length) return resolve(false);

            $('#deleteGiftcodeMessage').text(message);
            $modal.addClass('show');

            const closeModal = () => $modal.removeClass('show');
            $('#confirmDeleteGiftcode').off('click').on('click', () => { closeModal(); resolve(true); });
            $('#cancelDeleteGiftcode, #closeDeleteGiftcodeModal').off('click').on('click', () => { closeModal(); resolve(false); });
            $(document).off('keydown.confirmationModal').on('keydown.confirmationModal', (e) => {
                if (e.key === 'Escape' && $modal.hasClass('show')) { closeModal(); resolve(false); }
            });
            $modal.off('click').on('click', (e) => {
                if (e.target === $modal[0]) { closeModal(); resolve(false); }
            });
        });
    };

    $('#giftcode-form').on('submit', async function (e) {
        e.preventDefault();
        const giftcode = $('#giftcode').val();
        if (!validateGiftcode(giftcode)) return;

        try {
            const response = await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.ACTIVATE_GIFTCODE}`,
                method: 'POST',
                data: JSON.stringify({ code: giftcode }),
                ...ajaxConfig
            });
            showNotification(response.messageResponse || MESSAGES[currentLang].ACTIVATE_GIFTCODE_SUCCESS, 'success');
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].ACTIVATE_GIFTCODE_FAILED);
        }
    });

    $('#create-giftcode-form').on('submit', async function (e) {
        e.preventDefault();
        const $form = $(this);
        const $submitButton = $form.find('button[type="submit"]');
        const originalText = $submitButton.text();

        $submitButton.text('Đang tạo...').prop('disabled', true);
        try {
            const data = formToJson($form);
            const newGiftcodeData = {
                code: data.code,
                value: Number(data.value),
                giftcodeInfo: data.giftcodeInfo
            };

            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GIFTCODES}`,
                method: 'POST',
                data: JSON.stringify(newGiftcodeData),
                ...ajaxConfig
            });

            showNotification(MESSAGES[currentLang].SUCCESS_POST, 'success');
            $form[0].reset();
            loadGiftcodes();
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].POST_FAILED);
        } finally {
            $submitButton.text(originalText).prop('disabled', false);
        }
    });

    $('#edit-giftcode-form').on('submit', async function (e) {
        e.preventDefault();
        const $form = $(this);
        const $submitButton = $form.find('button[type="submit"]');
        const originalText = $submitButton.text();

        $submitButton.text('Đang lưu...').prop('disabled', true);
        try {
            const data = formToJson($form);
            const id = Number(data.id);
            if (isNaN(id) || id <= 0) throw new Error('ID giftcode không hợp lệ!');

            const updatedData = {
                code: data.code,
                value: Number(data.value),
                giftcodeInfo: data.giftcodeInfo,
                promotionStatus: data.promotionStatus
            };

            await ajaxWithRetry({
                url: `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.GIFTCODES}/${id}`,
                method: 'PUT',
                data: JSON.stringify(updatedData),
                ...ajaxConfig
            });

            showNotification(MESSAGES[currentLang].SUCCESS_UPDATE, 'success');
            closeEditGiftcodeModal();
            loadGiftcodes();
        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].UPDATE_FAILED);
        } finally {
            $submitButton.text(originalText).prop('disabled', false);
        }
    });

    $('#giftcode-apply-sort').on('click', () => {
        giftcodeConfig.sortField = $('#giftcode-sort-field').val();
        giftcodeConfig.sortDirection = $('#giftcode-sort-direction').val();
        loadGiftcodes();
    });

    $('#giftcode-table').on('click', '.edit-giftcode', function (e) {
        e.preventDefault();
        const giftcodeId = Number($(this).data('giftcode-id'));
        if (!isNaN(giftcodeId)) editGiftcode(giftcodeId);
    }).on('click', '.delete-giftcode', function (e) {
        e.preventDefault();
        const giftcodeId = Number($(this).data('giftcode-id'));
        if (!isNaN(giftcodeId)) deleteGiftcode(giftcodeId);
    });

    $('#editGiftcodeModal .close, #cancelEditGiftcode').on('click', closeEditGiftcodeModal);
    $(window).on('click', (e) => {
        if (e.target === document.getElementById('editGiftcodeModal')) closeEditGiftcodeModal();
    }).on('keydown', (e) => {
        if (e.key === 'Escape' && $('#editGiftcodeModal').hasClass('show')) closeEditGiftcodeModal();
    });

    $('#create-giftcode-form .btn-random-giftcode').on('click', function () {
        generateGiftcodeCode();
    });

    window.addEventListener('storage', (event) => {
        if (event.key === 'csrfToken') {
            if (!event.newValue) {
                isAuthenticated = false;
                localStorage.removeItem('isLoggedIn');
                window.location.href = CONFIG.PAGES.LOGIN;
            }
        }
    });

    window.generateGiftcodeCode = generateGiftcodeCode;
    window.loadGiftcodes = loadGiftcodes;

    loadGiftcodes();
    setupGiftcodeInfiniteScroll();
});