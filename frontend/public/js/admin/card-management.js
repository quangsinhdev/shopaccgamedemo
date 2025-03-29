$(document).ready(function () {
    const ajaxConfig = {
        contentType: 'application/json',
        xhrFields: {
            withCredentials: true
        },
        dataType: 'json'
    };

    const CONFIG = {
        BASE_URL: 'https://localhost:8443',
        API_ENDPOINTS: {
            DEPOSITS_CARD: '/api/admin/deposits/card',
            DEPOSITS_APPROVAL: '/api/admin/deposits/card'
        },
        CSRF_EXCLUDED_PATHS: [
            '/api/users/login',
            '/api/users/logout',
            '/api/users/refresh-token'
        ]
    };

    const MESSAGES = {
        vi: {
            FETCH_FAILED: 'Không thể tải dữ liệu!',
            MODAL_NOT_FOUND: 'Không tìm thấy modal xác nhận. Vui lòng kiểm tra HTML!',
            INVALID_CARD_ID: 'ID thẻ không hợp lệ!',
            APPROVE_FAILED: 'Không thể duyệt thẻ!',
            REJECT_FAILED: 'Không thể từ chối thẻ!',
            REFRESH_SUCCESS: 'Làm mới thành công!',
            PAGE_NOT_FOUND: 'Trang yêu cầu không tồn tại!',
            CSRF_TOKEN_NOT_FOUND: 'CSRF token không tìm thấy. Vui lòng đăng nhập lại!'
        }
    };

    const currentLang = 'vi';

    const STATUS_MAPPING = {
        SUCCESS: 'Thành công',
        REJECTED: 'Đã từ chối',
        PENDING: 'Chờ duyệt'
    };

    const cardDepositState = {
        page: 0,
        hasMore: true,
        isLoading: false,
        sort: 'timeOfDepositing,desc',
        filters: {},
        totalPages: 1
    };

    function mapCardStatus(status) {
        return STATUS_MAPPING[status] || status || 'Không có thông tin';
    }

    function getNetworkProviderDisplay(depositCardNetworkProvider) {
        const NetworkProviderMap = { 'VIETTEL': 'Viettel', 'MOBIFONE': 'Mobifone', 'VINAPHONE': 'Vinaphone' };
        return NetworkProviderMap[depositCardNetworkProvider] || 'Không có thông tin';
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
                    window.showLoginModal();
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
                const refreshed = await window.refreshToken();
                if (refreshed) {
                    if (!isSafeMethod && !isExcluded) {
                        const newCsrfToken = localStorage.getItem('csrfToken');
                        if (!newCsrfToken) {
                            window.showNotification(MESSAGES[currentLang].CSRF_TOKEN_NOT_FOUND, 'error', 2000, () => {
                                localStorage.removeItem('csrfToken');
                                window.showLoginModal();
                            });
                            throw new Error('CSRF token not found after refresh');
                        }
                        settings.headers = {
                            ...settings.headers,
                            'X-XSRF-TOKEN': newCsrfToken
                        };
                    }
                    return await $.ajax(settings);
                }
                throw xhr;
            }
            throw xhr;
        }
    }

    const showNotification = function (message, type) {
        $('.notification').remove();
        const $notification = $('<div>', {
            class: `notification ${type}`,
            html: `
                <span class="icon">${type === 'success' ? '✔' : '✖'}</span>
                <span>${message}</span>
            `
        });
        $('body').append($notification);
        setTimeout(() => {
            $notification.addClass('show');
        }, 10);
        setTimeout(() => {
            $notification.removeClass('show').addClass('hidden');
            setTimeout(() => {
                $notification.remove();
            }, 500);
        }, 3000);
    };

    const handleAjaxError = function (xhr, message) {
        let errorMessage = message;
        if (xhr.responseText) {
            try {
                const response = JSON.parse(xhr.responseText);
                errorMessage = response.errorMessage || response.message || message;
            } catch {
                errorMessage = xhr.responseText || message;
            }
        }
        showNotification(errorMessage, 'error');
    };

    function showConfirmationModal(message) {
        return new Promise((resolve) => {
            const $modalElement = $('#ApprovalConfirmationModal');
            const $modalBody = $('#approvalConfirmationModalBody');
            const $confirmBtn = $('#confirmActionBtn');
            const $cancelBtn = $('#cancelActionBtn');
            const $closeBtn = $('#closeApprovalConfirmationModal');

            if (!$modalElement.length || !$modalBody.length || !$confirmBtn.length || !$cancelBtn.length || !$closeBtn.length) {
                showNotification(MESSAGES[currentLang].MODAL_NOT_FOUND, 'error');
                resolve(false);
                return;
            }

            $modalBody.text(message);

            $modalElement.addClass('show');

            $confirmBtn.off('click').on('click', () => {
                $modalElement.removeClass('show');
                resolve(true);
            });

            $cancelBtn.off('click').on('click', () => {
                $modalElement.removeClass('show');
                resolve(false);
            });

            $closeBtn.off('click').on('click', () => {
                $modalElement.removeClass('show');
                resolve(false);
            });

            $(document).off('keydown.confirmationModal').on('keydown.confirmationModal', function (event) {
                if (event.key === 'Escape' && $modalElement.hasClass('show')) {
                    $modalElement.removeClass('show');
                    resolve(false);
                }
            });

            $modalElement.off('click').on('click', function (event) {
                if (event.target === $modalElement[0]) {
                    $modalElement.removeClass('show');
                    resolve(false);
                }
            });
        });
    }

    function formatCurrency(amount) {
        if (amount == null || isNaN(amount)) {
            return '0';
        }
        return amount.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
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

    window.loadCardDeposits = async function (page = 0) {
        cardDepositState.page = page;
        cardDepositState.isLoading = true;
    
        try {
            let url = `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.DEPOSITS_CARD}?page=${cardDepositState.page}&size=10&sort=${cardDepositState.sort}`;
            
            if (cardDepositState.filters.cardDepositStatus) {
                url += `&cardDepositStatus=${cardDepositState.filters.cardDepositStatus}`;
            }
            if (cardDepositState.filters.startDate) {
                url += `&startDate=${cardDepositState.filters.startDate}`;
            }
            if (cardDepositState.filters.endDate) {
                url += `&endDate=${cardDepositState.filters.endDate}`;
            }
            if (cardDepositState.filters.minValue) {
                url += `&minValue=${cardDepositState.filters.minValue}`;
            }
            if (cardDepositState.filters.maxValue) {
                url += `&maxValue=${cardDepositState.filters.maxValue}`;
            }
    
            const cardsResponse = await ajaxWithRetry({
                url: url,
                method: 'GET',
                ...ajaxConfig
            });

            const cards = cardsResponse.content || [];

            const totalElements = cardsResponse.page?.totalElements ?? 0;
            const totalPages = cardsResponse.page?.totalPages ?? 1;
            const currentPage = cardsResponse.page?.number ?? 0;
            const isLast = cardsResponse.page?.number === (cardsResponse.page?.totalPages - 1) ?? false;

            const $tableBody = $('#card-table');
            $tableBody.empty();

            if (cardDepositState.page >= totalPages) {
                showNotification(MESSAGES[currentLang].PAGE_NOT_FOUND, 'error');
                cardDepositState.page = 0;
                await window.loadCardDeposits(0);
                return;
            }

            if (cards.length === 0) {
                $tableBody.append('<tr><td colspan="8">Không có giao dịch nào.</td></tr>');
                cardDepositState.hasMore = false;
            } else {
                cards.forEach((card) => {
                    $tableBody.append(`
                        <tr>
                            <td>${getNetworkProviderDisplay(card.depositCardNetworkProvider) || 'Không có thông tin'}</td>
                            <td>${card.serial || 'Không có thông tin'}</td>
                            <td>${card.code || 'Không có thông tin'}</td>
                            <td>${formatCurrency(card.value) || 'Không có thông tin'}</td>
                            <td>${card.actuallyReceive ? formatCurrency(card.actuallyReceive) : 'Không có thông tin'}</td>
                            <td>${formatDateTime(card.timeOfDepositing) || 'Không có thông tin'}</td>
                            <td class="status-${card.cardDepositStatus?.toLowerCase() || 'unknown'}">${mapCardStatus(card.cardDepositStatus)}</td>
                            <td>
                                ${card.cardDepositStatus === 'PENDING' ? `
                                    <a href="#" class="btn btn-unlock approve-card" data-card-id="${card.id || ''}">Duyệt</a>
                                    <a href="#" class="btn btn-lock reject-card" data-card-id="${card.id || ''}">Từ chối</a>
                                ` : '-'}
                            </td>
                        </tr>
                    `);
                });

                $tableBody.off('click', '.approve-card').on('click', '.approve-card', function (e) {
                    e.preventDefault();
                    const cardId = $(this).data('card-id');
                    if (cardId) {
                        window.approveCard(cardId);
                    }
                });

                $tableBody.off('click', '.reject-card').on('click', '.reject-card', function (e) {
                    e.preventDefault();
                    const cardId = $(this).data('card-id');
                    if (cardId) {
                        window.rejectCard(cardId);
                    }
                });
            }

            cardDepositState.totalPages = totalPages;
            cardDepositState.hasMore = currentPage < totalPages - 1;

            renderPagination();

        } catch (xhr) {
            handleAjaxError(xhr, MESSAGES[currentLang].FETCH_FAILED);
        } finally {
            cardDepositState.isLoading = false;
        }
    };

    function renderPagination() {
        const $pagination = $('#card-deposits-pagination');
        if (!$pagination.length) {
            $('#card-table').parent().after('<div id="card-deposits-pagination" class="pagination"></div>');
        }

        const currentPage = cardDepositState.page;
        const totalPages = cardDepositState.totalPages;

        let startPage, endPage;
        if (totalPages <= 3) {
            startPage = 0;
            endPage = totalPages - 1;
        } else {
            if (currentPage === 0) {
                startPage = 0;
                endPage = 2;
            } else if (currentPage === totalPages - 1) {
                startPage = totalPages - 3;
                endPage = totalPages - 1;
            } else {
                startPage = currentPage - 1;
                endPage = currentPage + 1;
            }
        }

        let paginationHtml = '';
        for (let i = startPage; i <= endPage; i++) {
            if (i === currentPage) {
                paginationHtml += `<span class="page-item active">${i}</span>`;
            } else {
                paginationHtml += `<a href="#" class="page-item" data-page="${i}">${i}</a>`;
            }
        }

        $pagination.html(paginationHtml);

        $pagination.find('.page-item').not('.active').on('click', function (e) {
            e.preventDefault();
            const page = $(this).data('page');
            window.loadCardDeposits(page);
        });
    }

    window.approveCard = async function (cardId) {
        if (!cardId) {
            showNotification(MESSAGES[currentLang].INVALID_CARD_ID, 'error');
            return;
        }

        const confirmed = await showConfirmationModal(`Bạn có chắc chắn muốn duyệt thẻ #${cardId}?`);
        if (confirmed) {
            try {
                const url = `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.DEPOSITS_APPROVAL}/${cardId}/approve`;

                try {
                    new URL(url);
                } catch (e) {
                    throw new Error('URL không hợp lệ. Vui lòng kiểm tra cấu hình BASE_URL.');
                }

                await ajaxWithRetry({
                    url: url,
                    method: 'PATCH',
                    ...ajaxConfig
                });
                showNotification('Duyệt thẻ thành công!', 'success');
                cardDepositState.page = 0;
                cardDepositState.hasMore = true;
                await window.loadCardDeposits();
            } catch (xhr) {
                handleAjaxError(xhr, MESSAGES[currentLang].APPROVE_FAILED);
            }
        }
    };

    window.rejectCard = async function (cardId) {
        if (!cardId) {
            showNotification(MESSAGES[currentLang].INVALID_CARD_ID, 'error');
            return;
        }

        const confirmed = await showConfirmationModal(`Bạn có chắc chắn muốn từ chối thẻ #${cardId}?`);
        if (confirmed) {
            try {
                const url = `${CONFIG.BASE_URL}${CONFIG.API_ENDPOINTS.DEPOSITS_APPROVAL}/${cardId}/reject`;

                try {
                    new URL(url);
                } catch (e) {
                    throw new Error('URL không hợp lệ. Vui lòng kiểm tra cấu hình BASE_URL.');
                }

                await ajaxWithRetry({
                    url: url,
                    method: 'PATCH',
                    ...ajaxConfig
                });
                showNotification('Từ chối thẻ thành công!', 'success');
                cardDepositState.page = 0;
                cardDepositState.hasMore = true;
                await window.loadCardDeposits();
            } catch (xhr) {
                handleAjaxError(xhr, MESSAGES[currentLang].REJECT_FAILED);
            }
        }
    };

    $('#card-deposits-sort').on('change', function () {
        cardDepositState.sort = $(this).val();
        cardDepositState.page = 0;
        cardDepositState.hasMore = true;
        window.loadCardDeposits();
    });

    $('#apply-card-filters').on('click', function () {
        cardDepositState.filters = getFilters();
        cardDepositState.page = 0;
        cardDepositState.hasMore = true;
        window.loadCardDeposits();
    });

    $('.refresh-btn').on('click', async function () {
        const $button = $(this);
        $button.prop('disabled', true).text('Đang làm mới...');
        try {
            cardDepositState.page = 0;
            cardDepositState.hasMore = true;
            await window.loadCardDeposits();
            showNotification(MESSAGES[currentLang].REFRESH_SUCCESS, 'success');
        } catch (error) {
            handleAjaxError(error, MESSAGES[currentLang].FETCH_FAILED);
        } finally {
            $button.prop('disabled', false).text('Làm mới');
        }
    });

    function getFilters() {
        const filters = {};
        const cardDepositStatus = $('#card-status-filter').val();
        const startDate = $('#start-date').val();
        const endDate = $('#end-date').val();
        const minValue = $('#min-value').val();
        const maxValue = $('#max-value').val();
    
        if (cardDepositStatus) filters.cardDepositStatus = cardDepositStatus;
        if (startDate) filters.startDate = new Date(startDate).toISOString().split('T')[0];
        if (endDate) filters.endDate = new Date(endDate).toISOString().split('T')[0];
        if (minValue) filters.minValue = parseInt(minValue, 10);
        if (maxValue) filters.maxValue = parseInt(maxValue, 10);
    
        return filters;
    }

    cardDepositState.filters = getFilters();
    cardDepositState.sort = 'timeOfDepositing,desc';
    window.loadCardDeposits();

    if (typeof window.showSection === 'function') {
        window.showSection('manage-cards');
    }
});