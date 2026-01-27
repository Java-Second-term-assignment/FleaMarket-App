package com.example.flea_market_app.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.flea_market_app.user.domain.User;

/**
 * ユーザーリポジトリインターフェース。
 * 
 * <p>Userエンティティに対するデータアクセス操作を提供します。
 * JpaRepositoryを継承することで、基本的なCRUD操作が利用可能です。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

}
