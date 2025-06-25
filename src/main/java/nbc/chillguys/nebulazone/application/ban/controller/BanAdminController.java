package nbc.chillguys.nebulazone.application.ban.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.ban.dto.request.BanCreateRequest;
import nbc.chillguys.nebulazone.application.ban.dto.response.BanResponse;
import nbc.chillguys.nebulazone.application.ban.service.BanAdminService;

@RestController
@RequestMapping("/admin/bans")
@RequiredArgsConstructor
public class BanAdminController {
	private final BanAdminService banAdminService;

	@PostMapping
	public ResponseEntity<Void> ban(
		@RequestBody @Valid BanCreateRequest request
	) {
		banAdminService.createBan(request);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<List<BanResponse>> findBans() {
		return ResponseEntity.ok(banAdminService.findBans());
	}

	@DeleteMapping("/{ipAddress}")
	public ResponseEntity<Void> deleteBan(@PathVariable String ipAddress) {
		banAdminService.unban(ipAddress);
		return ResponseEntity.ok().build();
	}
}
