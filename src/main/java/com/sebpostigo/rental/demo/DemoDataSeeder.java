package com.sebpostigo.rental.demo;

import com.sebpostigo.rental.auth.User;
import com.sebpostigo.rental.auth.UserRepository;
import com.sebpostigo.rental.ledger.Expense;
import com.sebpostigo.rental.ledger.ExpenseCategory;
import com.sebpostigo.rental.ledger.ExpenseRepository;
import com.sebpostigo.rental.ledger.Income;
import com.sebpostigo.rental.ledger.IncomeRepository;
import com.sebpostigo.rental.property.Property;
import com.sebpostigo.rental.property.PropertyRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resets the one-click demo account on every startup (the frontend hardcodes its credentials).
 * Render's free tier restarts the app regularly, so visitor edits clean themselves up. Dates
 * follow the calendar so the demo never looks stale.
 */
@Component
@Profile("demo")
public class DemoDataSeeder implements ApplicationRunner {

	public static final String EMAIL = "demo@rentalledger.com";

	public static final String PASSWORD = "demopass123";

	private static final BigDecimal RENT = new BigDecimal("1200.00");

	private static final BigDecimal[] UTILITIES = { new BigDecimal("80.00"), new BigDecimal("95.00"),
			new BigDecimal("85.00") };

	private static final BigDecimal PLUMBING = new BigDecimal("220.00");

	private final UserRepository users;

	private final PropertyRepository properties;

	private final IncomeRepository incomes;

	private final ExpenseRepository expenses;

	private final PasswordEncoder passwordEncoder;

	private final Clock clock;

	public DemoDataSeeder(UserRepository users, PropertyRepository properties, IncomeRepository incomes,
			ExpenseRepository expenses, PasswordEncoder passwordEncoder, Clock clock) {
		this.users = users;
		this.properties = properties;
		this.incomes = incomes;
		this.expenses = expenses;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		seed();
	}

	@Transactional
	public void seed() {
		User user = users.findByEmail(EMAIL).orElseGet(() -> new User(EMAIL, ""));
		user.changePassword(passwordEncoder.encode(PASSWORD));
		user = users.save(user);

		properties.deleteByOwnerId(user.getId());
		Property property = properties.save(new Property(user.getId(), "Casa Miraflores 402",
				"Av. Larco 402, Miraflores, Lima", "apartment", "USD"));

		YearMonth thisMonth = YearMonth.now(clock);
		for (int i = 0; i < 3; i++) {
			YearMonth month = thisMonth.minusMonths(3 - i);
			incomes.save(new Income(property.getId(), month.atDay(1), RENT, "Rent", null));
			expenses.save(new Expense(property.getId(), month.atDay(5), UTILITIES[i], ExpenseCategory.OTHER,
					"Utilities"));
			if (i == 1) {
				expenses.save(new Expense(property.getId(), month.atDay(18), PLUMBING, ExpenseCategory.MAINTENANCE,
						"Plumbing repair"));
			}
		}
	}

}
