package com.sebpostigo.rental.report;

import com.sebpostigo.rental.security.CurrentUser;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportController {

	private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	@GetMapping("/summary")
	public SummaryResponse summary(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(name = "start_date", required = false) LocalDate startDate,
			@RequestParam(name = "end_date", required = false) LocalDate endDate,
			@RequestParam(name = "property_id", required = false) Long propertyId) {
		return reportService.summary(CurrentUser.id(jwt), propertyId, startDate, endDate);
	}

	@GetMapping("/timeseries")
	public List<TimeseriesPoint> timeseries(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(name = "start_date", required = false) LocalDate startDate,
			@RequestParam(name = "end_date", required = false) LocalDate endDate,
			@RequestParam(name = "property_id", required = false) Long propertyId) {
		return reportService.timeseries(CurrentUser.id(jwt), propertyId, startDate, endDate);
	}

	@GetMapping("/export")
	public ResponseEntity<String> export(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(name = "start_date", required = false) LocalDate startDate,
			@RequestParam(name = "end_date", required = false) LocalDate endDate,
			@RequestParam(name = "property_id", required = false) Long propertyId) {
		String csv = reportService.exportCsv(CurrentUser.id(jwt), propertyId, startDate, endDate);
		return ResponseEntity.ok()
			.contentType(TEXT_CSV)
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv")
			.body(csv);
	}

}
