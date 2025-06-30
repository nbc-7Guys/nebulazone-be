package nbc.chillguys.nebulazone.application.ban.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.ban.dto.request.BanCreateRequest;
import nbc.chillguys.nebulazone.application.ban.service.BanService;

@RestController
@RequestMapping("/internal/bans")
@RequiredArgsConstructor
public class BanController {
	private final BanService banService;

	@Value("${security.logstash-key}")
	private String logstashKey;

	@PostMapping
	public ResponseEntity<Void> ban(
		@RequestHeader("X-LOGSTASH-KEY") String key,
		@RequestBody @Valid BanCreateRequest request) {
		if (!logstashKey.equals(key)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		banService.createBan(request);
		return ResponseEntity.ok().build();
	}

}
