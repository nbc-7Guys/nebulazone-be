package nbc.chillguys.nebulazone.application.ban.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.ban.dto.request.BanCreateRequest;
import nbc.chillguys.nebulazone.application.ban.dto.response.BanResponse;
import nbc.chillguys.nebulazone.domain.ban.dto.BanCreateCommand;
import nbc.chillguys.nebulazone.domain.ban.service.BanAdminDomainService;

@Service
@RequiredArgsConstructor
public class BanAdminService {

	private final BanAdminDomainService banAdminDomainService;

	public void createBan(BanCreateRequest request) {
		BanCreateCommand command = BanCreateCommand.from(request);
		banAdminDomainService.createBan(command);
	}

	public void unban(String ipAddress) {
		banAdminDomainService.unban(ipAddress);
	}

	public List<BanResponse> findBans() {
		return banAdminDomainService.findBanInfos().stream()
			.map(BanResponse::from)
			.toList();
	}

}
