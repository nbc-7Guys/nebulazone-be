package nbc.chillguys.nebulazone.application.ban.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.ban.dto.request.BanCreateRequest;
import nbc.chillguys.nebulazone.domain.ban.dto.BanCreateCommand;
import nbc.chillguys.nebulazone.domain.ban.service.BanDomainService;

@Service
@RequiredArgsConstructor
public class BanService {

	private final BanDomainService banDomainService;

	public void createBan(BanCreateRequest request) {
		BanCreateCommand command = BanCreateCommand.from(request);
		banDomainService.createBan(command);
	}

	public void validateBanned(String ipAddress) {
		banDomainService.validateBanned(ipAddress);
	}
}
