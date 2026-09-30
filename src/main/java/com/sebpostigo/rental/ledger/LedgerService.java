package com.sebpostigo.rental.ledger;

import com.sebpostigo.rental.common.NotFoundException;
import com.sebpostigo.rental.property.PropertyService;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LedgerService {

	// Open-ended filters become Postgres' own date bounds, so one derived query covers every case.
	private static final LocalDate EARLIEST = LocalDate.of(1, 1, 1);

	private static final LocalDate LATEST = LocalDate.of(9999, 12, 31);

	private final PropertyService propertyService;

	private final IncomeRepository incomes;

	private final ExpenseRepository expenses;

	public LedgerService(PropertyService propertyService, IncomeRepository incomes, ExpenseRepository expenses) {
		this.propertyService = propertyService;
		this.incomes = incomes;
		this.expenses = expenses;
	}

	@Transactional
	public IncomeResponse createIncome(long ownerId, long propertyId, IncomeRequest request) {
		propertyService.requireOwned(ownerId, propertyId);
		Income income = new Income(propertyId, request.date(), request.amount(), request.source(), request.notes());
		return IncomeResponse.from(incomes.save(income));
	}

	public List<IncomeResponse> listIncomes(long ownerId, long propertyId, LocalDate start, LocalDate end) {
		propertyService.requireOwned(ownerId, propertyId);
		return incomes
			.findByPropertyIdAndDateBetweenOrderByDateAscIdAsc(propertyId, Objects.requireNonNullElse(start, EARLIEST),
					Objects.requireNonNullElse(end, LATEST))
			.stream()
			.map(IncomeResponse::from)
			.toList();
	}

	@Transactional
	public void deleteIncome(long ownerId, long propertyId, long incomeId) {
		propertyService.requireOwned(ownerId, propertyId);
		Income income = incomes.findByIdAndPropertyId(incomeId, propertyId)
			.orElseThrow(() -> new NotFoundException("Income not found"));
		incomes.delete(income);
	}

	@Transactional
	public ExpenseResponse createExpense(long ownerId, long propertyId, ExpenseRequest request) {
		propertyService.requireOwned(ownerId, propertyId);
		Expense expense = new Expense(propertyId, request.date(), request.amount(), request.category(),
				request.notes());
		return ExpenseResponse.from(expenses.save(expense));
	}

	public List<ExpenseResponse> listExpenses(long ownerId, long propertyId, LocalDate start, LocalDate end) {
		propertyService.requireOwned(ownerId, propertyId);
		return expenses
			.findByPropertyIdAndDateBetweenOrderByDateAscIdAsc(propertyId, Objects.requireNonNullElse(start, EARLIEST),
					Objects.requireNonNullElse(end, LATEST))
			.stream()
			.map(ExpenseResponse::from)
			.toList();
	}

	@Transactional
	public void deleteExpense(long ownerId, long propertyId, long expenseId) {
		propertyService.requireOwned(ownerId, propertyId);
		Expense expense = expenses.findByIdAndPropertyId(expenseId, propertyId)
			.orElseThrow(() -> new NotFoundException("Expense not found"));
		expenses.delete(expense);
	}

}
