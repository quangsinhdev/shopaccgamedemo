package com.shopaccgame.services.user.transaction.deposit;

import com.shopaccgame.exceptions.transaction.deposit.VNPayTransactionException;
import com.shopaccgame.models.transaction.deposit.VNPayTransaction;
import com.shopaccgame.models.user.User;
import com.shopaccgame.repositories.transaction.deposit.VNPayTransactionRepository;
import com.shopaccgame.repositories.user.UserRepository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VNPayTransactionService {

	private final UserRepository userRepository;
	private final VNPayTransactionRepository vnPayTransactionRepository;

	public VNPayTransactionService(UserRepository userRepository,
			VNPayTransactionRepository vnPayTransactionRepository) {
		this.userRepository = userRepository;
		this.vnPayTransactionRepository = vnPayTransactionRepository;
	}

	@Transactional
	public void processSuccessfulPayment(User user, long amount, String transactionId, String txnRef) {
		if (vnPayTransactionRepository.existsByTxnRef(txnRef)) {
			throw new VNPayTransactionException("Giao dịch đã được xử lý trước đó: " + txnRef);
		}

		user.setBalance(user.getBalance() + amount);
		user.setTotaldeposit(user.getTotaldeposit() + amount);
		userRepository.save(user);

		VNPayTransaction transaction = new VNPayTransaction();
		transaction.setUser(user);
		transaction.setDepositorUsername(user.getUsername());
		transaction.setAmount(amount);
		transaction.setTransactionId(transactionId);
		transaction.setTxnRef(txnRef);
		transaction.setStatus("SUCCESS");
		transaction.setTimeOfDepositing(LocalDateTime.now());
		vnPayTransactionRepository.save(transaction);
	}

	public Page<VNPayTransaction> getVNPayDepositOrders(Pageable pageable, User user) {
		return vnPayTransactionRepository.findByUser(pageable, user);
	}

	public Page<VNPayTransaction> getVNPayDepositOrders(Pageable pageable) {
		return vnPayTransactionRepository.findAll(pageable);
	}
}