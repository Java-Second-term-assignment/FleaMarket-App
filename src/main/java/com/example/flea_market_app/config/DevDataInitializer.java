package com.example.flea_market_app.config;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.auth.domain.AuthUserEntity;
import com.example.flea_market_app.auth.repository.AuthUserRepository;
import com.example.flea_market_app.catalog.domain.CategoryEntity;
import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.CategoryRepository;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.domain.UserRank;
import com.example.flea_market_app.user.repository.UserRankRepository;
import com.example.flea_market_app.user.repository.UserRepository;

/**
 * 開発用プロファイル（dev）で初期データを投入する。
 * user_ranks, categories, users, auth_users, items を投入する。
 */
@Component
@Profile("dev")
@Order(1)
public class DevDataInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

	private static final UUID USER_SELLER = UUID.fromString("30000000-0000-0000-0000-000000000001");
	private static final UUID USER_BUYER = UUID.fromString("30000000-0000-0000-0000-000000000002");
	private static final UUID USER_ADMIN = UUID.fromString("30000000-0000-0000-0000-000000000003");

	private static final UUID CAT_ROOT_FASHION = UUID.fromString("10000000-0000-0000-0000-000000000001");
	private static final UUID CAT_ROOT_FUN = UUID.fromString("10000000-0000-0000-0000-000000000002");
	private static final UUID CAT_LEAF_MENS = UUID.fromString("20000000-0000-0000-0000-000000000001");
	private static final UUID CAT_LEAF_LADIES = UUID.fromString("20000000-0000-0000-0000-000000000002");
	private static final UUID CAT_LEAF_SMARTPHONE = UUID.fromString("20000000-0000-0000-0000-000000000101");

	private final UserRankRepository userRankRepository;
	private final CategoryRepository categoryRepository;
	private final ItemRepository itemRepository;
	private final UserRepository userRepository;
	private final AuthUserRepository authUserRepository;
	private final PasswordEncoder passwordEncoder;

	public DevDataInitializer(
			UserRankRepository userRankRepository,
			CategoryRepository categoryRepository,
			ItemRepository itemRepository,
			UserRepository userRepository,
			AuthUserRepository authUserRepository,
			PasswordEncoder passwordEncoder) {
		this.userRankRepository = userRankRepository;
		this.categoryRepository = categoryRepository;
		this.itemRepository = itemRepository;
		this.userRepository = userRepository;
		this.authUserRepository = authUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) throws Exception {
		if (userRankRepository.count() > 0) {
			log.info("Dev data already loaded, skipping users/categories");
			if (itemRepository.count() == 0) {
				insertItems();
			}
			return;
		}
		log.info("Loading dev seed data...");

		insertUserRanks();
		insertCategories();
		insertUsers();
		insertAuthUsers();
		insertItems();

		log.info("Dev seed data loaded successfully");
	}

	private void insertUserRanks() throws Exception {
		for (UserRank rank : new UserRank[] {
				createUserRank((short) 1, "BRONZE", "ブロンズ", 1000),
				createUserRank((short) 2, "SILVER", "シルバー", 800),
				createUserRank((short) 3, "GOLD", "ゴールド", 600)
		}) {
			userRankRepository.save(rank);
		}
	}

	private UserRank createUserRank(short id, String code, String name, int commissionBps) throws Exception {
		var ctor = UserRank.class.getDeclaredConstructor();
		ctor.setAccessible(true);
		UserRank r = ctor.newInstance();
		r.setId(id);
		r.setRankCode(code);
		r.setRankName(name);
		r.setCommissionBps(commissionBps);
		return r;
	}

	private void insertCategories() {
		// ルート
		saveCategory(CAT_ROOT_FASHION, null, "ファッション");
		saveCategory(CAT_ROOT_FUN, null, "娯楽");
		// 子カテゴリ（findActiveLeafCategories 用）
		saveCategory(CAT_LEAF_MENS, CAT_ROOT_FASHION, "メンズ");
		saveCategory(CAT_LEAF_LADIES, CAT_ROOT_FASHION, "レディース");
		saveCategory(CAT_LEAF_SMARTPHONE, CAT_ROOT_FUN, "スマホ");
	}

	private void saveCategory(UUID id, UUID parentId, String name) {
		CategoryEntity c = new CategoryEntity();
		c.setId(id);
		c.setParentId(parentId);
		c.setName(name);
		c.setSortOrder(0);
		c.setActive(true);
		categoryRepository.save(c);
	}

	private void insertUsers() {
		saveUser(USER_SELLER, "出品者A", (short) 1, "VERIFIED");
		saveUser(USER_BUYER, "購入者B", (short) 1, "UNVERIFIED");
		saveUser(USER_ADMIN, "運営者C", (short) 3, "VERIFIED");
	}

	private void saveUser(UUID id, String displayName, short rankId, String identityStatus) {
		UserEntity u = new UserEntity();
		u.setId(id);
		u.setDisplayName(displayName);
		u.setUserRankId(rankId);
		u.setIdentityStatus(identityStatus);
		u.setActive(true);
		userRepository.save(u);
	}

	private void insertAuthUsers() {
		saveAuthUser(USER_SELLER, "sellerA@example.com", "password", false);
		saveAuthUser(USER_BUYER, "buyerB@example.com", "password", false);
		saveAuthUser(USER_ADMIN, "adminC@example.com", "adminpass", true);
	}

	private void saveAuthUser(UUID userId, String email, String plainPassword, boolean admin) {
		AuthUserEntity a = new AuthUserEntity();
		a.setId(UUID.randomUUID());
		a.setUserId(userId);
		a.setEmail(email);
		a.setPasswordHash(passwordEncoder.encode(plainPassword));
		a.setAdmin(admin);
		authUserRepository.save(a);
	}

	private void insertItems() {
		log.info("Inserting dev seed items...");
		saveItem("ノートPC 14インチ", "軽量で持ち運びに便利。Office搭載。", 59800L, CAT_LEAF_SMARTPHONE);
		saveItem("ワイヤレスイヤホン", "ノイズキャンセリング対応。", 12800L, CAT_LEAF_SMARTPHONE);
		saveItem("デニムジャケット", "レディースM。ほとんど未使用。", 3500L, CAT_LEAF_MENS);
		saveItem("スニーカー 白", "定番の白スニーカー。26cm。", 4200L, CAT_LEAF_MENS);
		saveItem("ワンピース", "春夏用の花柄ワンピース。", 2800L, CAT_LEAF_LADIES);
	}

	private void saveItem(String name, String description, long priceAmount, UUID categoryId) {
		ItemEntity item = new ItemEntity();
		item.setId(UUID.randomUUID());
		item.setSellerId(USER_SELLER);
		item.setCategoryId(categoryId);
		item.setName(name);
		item.setDescription(description);
		item.setPriceAmount(priceAmount);
		item.setCurrency("JPY");
		item.setStatus("PUBLISHED");
		item.setCondition("USED_GOOD");
		item.setShippingFeePayer("SELLER");
		itemRepository.save(item);
	}
}
