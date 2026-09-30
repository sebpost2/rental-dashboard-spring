package com.sebpostigo.rental.ledger;

import com.sebpostigo.rental.security.CurrentUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/properties/{propertyId}/incomes")
public class IncomeController {

	private final LedgerService ledgerService;

	public IncomeController(LedgerService ledgerService) {
		this.ledgerService = ledgerService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public IncomeResponse create(@AuthenticationPrincipal Jwt jwt, @PathVariable long propertyId,
			@Valid @RequestBody IncomeRequest request) {
		return ledgerService.createIncome(CurrentUser.id(jwt), propertyId, request);
	}

	@GetMapping
	public List<IncomeResponse> list(@AuthenticationPrincipal Jwt jwt, @PathVariable long propertyId,
			@RequestParam(name = "start_date", required = false) LocalDate startDate,
			@RequestParam(name = "end_date", required = false) LocalDate endDate) {
		return ledgerService.listIncomes(CurrentUser.id(jwt), propertyId, startDate, endDate);
	}

	@DeleteMapping("/{incomeId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long propertyId, @PathVariable long incomeId) {
		ledgerService.deleteIncome(CurrentUser.id(jwt), propertyId, incomeId);
	}

}
