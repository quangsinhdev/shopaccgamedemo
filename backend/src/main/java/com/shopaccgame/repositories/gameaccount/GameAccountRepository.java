package com.shopaccgame.repositories.gameaccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopaccgame.models.gameaccount.GameAccount;

@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

}
