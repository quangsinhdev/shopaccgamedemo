const voucherConfig = {
    currentPage: 0,
    pageSize: 10,
    isLoading: false,
    hasMore: true,
    observer: null,
    sortField: 'id',
    sortDirection: 'asc'
};

$(document).ready(function () {
    window.CONFIG = window.CONFIG || {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            VOUCHERS: '/api/admin/vouchers'
        },
        CSRF_EXCLUDED_PATHS: [
            '/api/users/login',
            '/api/users/logout',
            '/api/users/refresh-token'
        ]
    };

    window.ajaxConfig = window.ajaxConfig || {
        contentType: 'application/json',
        xhrFields: { withCredentials: true },
        dataType: 'json'
    };

    window.MESSAGES = window.MESSAGES || {
        vi: {
            FETCH_FAILED: 'Không thể tải dữ liệu!',
            SUCCESS_UPDATE: 'Cập nhật thành công!',
            UPDATE_FAILED: 'Cập nhật thất bại!',
            SUCCESS_POST: 'Tạo mới thành công!',
            POST_FAILED: 'Tạo mới thất bại!',
            CSRF_TOKEN_NOT_FOUND: 'CSRF token không tìm thấy. Vui lòng đăng nhập lại!'
        }
    };

    window.currentLang = window.currentLang || 'vi';

    window.formatCurrency = window.formatCurrency || function (value) {
        return value != null ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value) : 'Không có thông tin';
    };

    window.formatDate = window.formatDate || function (date) {
        try {
            return date ? new Date(date).toLocaleDateString('vi-VN') : 'Không có thông tin';
        } catch {
            return 'Không có thông tin';
        }
    };

    window.formatDateTime = window.formatDateTime || function (date) {
        try {
            return date ? new Date(date).toLocaleString('vi-VN') : '-';
        } catch {
            return '-';
        }
    };

    window.showNotification = window.showNotification || function (message, type) {
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
        }, 3000);
    };

    window.handleAjaxError = window.handleAjaxError || function (xhr, defaultMessage) {
        let message = defaultMessage;
        if (xhr.responseText) {
            try {
                const response = JSON.parse(xhr.responseText);
                message = response.errorMessage || response.message || message;
            } catch {
                message = xhr.responseText;
            }
        }
        window.showNotification(message, 'error');
    };

    window.formToJson = window.formToJson || function ($form) {
        return $form.serializeArray().reduce((obj, item) => {
            obj[item.name] = item.value;
            return obj;
        }, {});
    };

    window.ajaxWithRetry = window.ajaxWithRetry || async function (settings) {
        const method = settings.method || settings.type || 'GET';
        const isSafeMethod = ['GET', 'HEAD', 'OPTIONS'].includes(method.toUpperCase());
        const isExcluded = window.CONFIG.CSRF_EXCLUDED_PATHS.some(path => settings.url.includes(path));

        if (!isSafeMethod && !isExcluded) {
            if (!window.csrfToken) {
                window.showNotification(window.MESSAGES[window.currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                    window.isAuthenticated = false;
                    window.showLoginModal();
                });
                throw new Error('CSRF token not found');
            }
            settings.headers = {
                ...settings.headers,
                'X-XSRF-TOKEN': window.csrfToken
            };
        }

        settings.method = method;
        delete settings.type;

        try {
            const response = await $.ajax(settings);
            return response;
        } catch (xhr) {
            if (xhr.status === 401 && window.refreshToken) {
                const refreshed = await window.refreshToken();
                if (refreshed) {
                    if (!isSafeMethod && !isExcluded) {
                        if (!window.csrfToken) {
                            window.showNotification(window.MESSAGES[window.currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                                window.isAuthenticated = false;
                                window.showLoginModal();
                            });
                            throw new Error('CSRF token not found after refresh');
                        }
                        settings.headers = {
                            ...settings.headers,
                            'X-XSRF-TOKEN': window.csrfToken
                        };
                    }
                    return await $.ajax(settings);
                }
            }
            throw xhr;
        }
    };

    window.showConfirmationModal = window.showConfirmationModal || function (message) {
        return new Promise((resolve) => {
            const $modal = $('#confirmationDeleteVoucher');
            if (!$modal.length) return resolve(false);

            $('#deleteVoucherMessage').text(message);
            $modal.addClass('show');

            const closeModal = () => $modal.removeClass('show');
            $('#confirmDeleteVoucher').off('click').on('click', () => { closeModal(); resolve(true); });
            $('#cancelDeleteVoucher, #closeDeleteVoucherModal').off('click').on('click', () => { closeModal(); resolve(false); });
            $(document).off('keydown.confirmationModal').on('keydown.confirmationModal', (e) => {
                if (e.key === 'Escape' && $modal.hasClass('show')) {
                    closeModal();
                    resolve(false);
                }
            });
            $modal.off('click').on('click', (e) => {
                if (e.target === $modal[0]) {
                    closeModal();
                    resolve(false);
                }
            });
        });
    };

    const closeEditVoucherModal = () => $('#editVoucherModal').removeClass('show');

    const editVoucher = async (id) => {
        try {
            const voucher = await window.ajaxWithRetry({
                url: `${window.CONFIG.BASE_URL}${window.CONFIG.API_ENDPOINTS.VOUCHERS}/${id}`,
                method: 'GET',
                ...window.ajaxConfig
            });

            const $form = $('#edit-voucher-form');
            if ($form.length) {
                $form.find('#voucher-id').val(voucher.id);
                $form.find('#edit-voucher-code').val(voucher.code);
                $form.find('#edit-voucher-value').val(voucher.value);
                $form.find('#edit-voucher-expiration').val(
                    voucher.voucherExpireDate ? new Date(voucher.voucherExpireDate).toISOString().split('T')[0] : ''
                );
                $form.find('#edit-voucher-status').val(voucher.promotionStatus);
            }

            const $modal = $('#editVoucherModal');
            if ($modal.length) $modal.addClass('show');
        } catch (xhr) {
            window.handleAjaxError(xhr, window.MESSAGES[window.currentLang].FETCH_FAILED);
        }
    };

    const deleteVoucher = async (voucherId) => {
        if (!voucherId || isNaN(voucherId)) {
            window.showNotification('ID voucher không hợp lệ!', 'error');
            return;
        }

        const confirmed = await window.showConfirmationModal('Bạn có chắc chắn muốn xóa voucher này?');
        if (!confirmed) return;

        try {
            await window.ajaxWithRetry({
                url: `${window.CONFIG.BASE_URL}${window.CONFIG.API_ENDPOINTS.VOUCHERS}/${voucherId}`,
                method: 'DELETE',
                ...window.ajaxConfig
            });
            window.showNotification('Xóa voucher thành công!', 'success');
            loadVouchers();
        } catch (xhr) {
            const errorMessage = xhr.status === 404 ? 'Voucher không tồn tại!' :
                xhr.status === 403 ? 'Bạn không có quyền xóa voucher này!' : 'Không thể xóa voucher!';
            window.handleAjaxError(xhr, errorMessage);
        }
    };

    const generateVoucherCode = () => {
        const length = 11;
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
        const randomCode = Array.from({ length }, () =>
            characters.charAt(Math.floor(Math.random() * characters.length))
        ).join('');
        $('#voucher-code').val(randomCode);
    };

    const loadVouchers = async (append = false) => {
        if (voucherConfig.isLoading) return;
        if (!append && !voucherConfig.hasMore) return;

        if (!append) {
            voucherConfig.currentPage = 0;
            voucherConfig.hasMore = true;
        }

        voucherConfig.isLoading = true;
        const $tableBody = $('#voucher-table');

        try {
            const url = new URL(`${window.CONFIG.BASE_URL}${window.CONFIG.API_ENDPOINTS.VOUCHERS}`);
            url.searchParams.append('page', voucherConfig.currentPage);
            url.searchParams.append('size', voucherConfig.pageSize);
            url.searchParams.append('sort', `${voucherConfig.sortField},${voucherConfig.sortDirection}`);

            const response = await window.ajaxWithRetry({
                url: url.toString(),
                method: 'GET',
                ...window.ajaxConfig
            });

            const vouchers = response.content || [];
            const normalizedVouchers = vouchers.map(voucher => ({
                id: Number(voucher.id),
                code: voucher.code || 'Không có thông tin',
                value: voucher.value,
                timeOfListing: voucher.timeOfListing || null,
                voucherExpireDate: voucher.voucherExpireDate || null,
                timeOfUse: voucher.timeOfUse || null,
                promotionStatus: voucher.promotionStatus || 'Không có thông tin'
            }));

            if (!append) $tableBody.empty();

            if (!normalizedVouchers.length && !append) {
                $tableBody.append('<tr><td colspan="7" class="text-center">Không có voucher nào.</td></tr>');
            } else {
                normalizedVouchers.forEach(voucher => {
                    const actionButtons = voucher.id ? `
                        <a href="#" class="btn btn-update edit-voucher" data-voucher-id="${voucher.id}">Sửa</a>
                        <a href="#" class="btn btn-lock delete-voucher" data-voucher-id="${voucher.id}">Xóa</a>
                    ` : '-';

                    $tableBody.append(`
                        <tr>
                            <td>${voucher.code}</td>
                            <td>${window.formatCurrency(voucher.value)}</td>
                            <td>${window.formatDate(voucher.timeOfListing)}</td>
                            <td>${window.formatDate(voucher.voucherExpireDate)}</td>
                            <td>${window.formatDateTime(voucher.timeOfUse)}</td>
                            <td>${window.getPromotionStatusDisplay(voucher.promotionStatus)}</td>
                            <td>${actionButtons}</td>
                        </tr>
                    `);
                });
            }

            voucherConfig.hasMore = !response.last;
            if (voucherConfig.hasMore) {
                voucherConfig.currentPage++;
                setupVoucherInfiniteScroll();
            } else if (!$tableBody.siblings('.no-more-data').length) {
                $tableBody.after('<div class="no-more-data text-center">Đã tải hết dữ liệu</div>');
                if (voucherConfig.observer) voucherConfig.observer.disconnect();
            }
        } catch (xhr) {
            window.handleAjaxError(xhr, window.MESSAGES[window.currentLang].FETCH_FAILED);
        } finally {
            voucherConfig.isLoading = false;
        }
    };

    const setupVoucherInfiniteScroll = () => {
        const $trigger = $('#voucher-load-more-trigger');
        if (voucherConfig.observer) voucherConfig.observer.disconnect();

        const debouncedLoadMore = window.debounceObserver((entries) => {
            if (entries[0].isIntersecting && !voucherConfig.isLoading && voucherConfig.hasMore) {
                loadVouchers(true);
            }
        }, 300);

        voucherConfig.observer = new IntersectionObserver(debouncedLoadMore, { threshold: 0.5 });
        voucherConfig.observer.observe($trigger[0]);
    };

    window.debounceObserver = window.debounceObserver || function (func, wait) {
        let timeout;
        return (...args) => {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    };

    window.getPromotionStatusDisplay = window.getPromotionStatusDisplay || function (promotionStatus) {
        const statusMap = { 'AVAILABLE': 'Chưa sử dụng', 'USED': 'Đã sử dụng' };
        return statusMap[promotionStatus] || promotionStatus;
    };

    $('#edit-voucher-form').on('submit', async function (e) {
        e.preventDefault();
        const $form = $(this);
        const $submitButton = $form.find('button[type="submit"]');
        const originalText = $submitButton.text();

        $submitButton.text('Đang lưu...').prop('disabled', true);
        try {
            const data = window.formToJson($form);
            const id = Number(data.id);
            if (isNaN(id) || id <= 0) throw new Error('ID voucher không hợp lệ!');

            const voucherExpireDate = data.voucherExpireDate ? `${data.voucherExpireDate}T23:59:59` : null;
            const updatedData = {
                code: data.code,
                value: Number(data.value),
                voucherExpireDate,
                promotionStatus: data.promotionStatus
            };

            await window.ajaxWithRetry({
                url: `${window.CONFIG.BASE_URL}${window.CONFIG.API_ENDPOINTS.VOUCHERS}/${id}`,
                method: 'PUT',
                data: JSON.stringify(updatedData),
                ...window.ajaxConfig
            });

            window.showNotification(window.MESSAGES[window.currentLang].SUCCESS_UPDATE, 'success');
            closeEditVoucherModal();
            loadVouchers();
        } catch (xhr) {
            window.handleAjaxError(xhr, window.MESSAGES[window.currentLang].UPDATE_FAILED);
        } finally {
            $submitButton.text(originalText).prop('disabled', false);
        }
    });

    $('#create-voucher-form').on('submit', async function (e) {
        e.preventDefault();
        const $form = $(this);
        const $submitButton = $form.find('button[type="submit"]');
        const originalText = $submitButton.text();

        $submitButton.text('Đang tạo...').prop('disabled', true);
        try {
            const data = window.formToJson($form);
            const voucherExpireDate = data.voucherExpireDate ? `${data.voucherExpireDate}T23:59:59` : null;
            const newVoucherData = {
                code: data.code,
                value: Number(data.value),
                voucherExpireDate
            };

            await window.ajaxWithRetry({
                url: `${window.CONFIG.BASE_URL}${window.CONFIG.API_ENDPOINTS.VOUCHERS}`,
                method: 'POST',
                data: JSON.stringify(newVoucherData),
                ...window.ajaxConfig
            });

            window.showNotification(window.MESSAGES[window.currentLang].SUCCESS_POST, 'success');
            $form[0].reset();
            loadVouchers();
        } catch (xhr) {
            window.handleAjaxError(xhr, window.MESSAGES[window.currentLang].POST_FAILED);
        } finally {
            $submitButton.text(originalText).prop('disabled', false);
        }
    });

    $('#create-voucher-form .btn-random-voucher').on('click', function () {
        generateVoucherCode();
    });

    $('#voucher-apply-sort').on('click', () => {
        voucherConfig.sortField = $('#voucher-sort-field').val();
        voucherConfig.sortDirection = $('#voucher-sort-direction').val();
        loadVouchers();
    });

    $('#voucher-table').on('click', '.edit-voucher', function (e) {
        e.preventDefault();
        const voucherId = Number($(this).data('voucher-id'));
        if (!isNaN(voucherId)) editVoucher(voucherId);
    }).on('click', '.delete-voucher', function (e) {
        e.preventDefault();
        const voucherId = Number($(this).data('voucher-id'));
        if (!isNaN(voucherId)) deleteVoucher(voucherId);
    });

    $('#editVoucherModal .close, #cancelEditVoucher').on('click', closeEditVoucherModal);
    $(window).on('click', (e) => {
        if (e.target === document.getElementById('editVoucherModal')) closeEditVoucherModal();
    }).on('keydown', (e) => {
        if (e.key === 'Escape' && $('#editVoucherModal').hasClass('show')) closeEditVoucherModal();
    });

    window.editVoucher = editVoucher;
    window.closeEditVoucherModal = closeEditVoucherModal;
    window.deleteVoucher = deleteVoucher;
    window.loadVouchers = loadVouchers;
    window.generateVoucherCode = generateVoucherCode;

    loadVouchers();
    setupVoucherInfiniteScroll();
});