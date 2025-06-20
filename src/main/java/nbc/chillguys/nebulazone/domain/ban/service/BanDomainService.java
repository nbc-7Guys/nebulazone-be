package nbc.chillguys.nebulazone.domain.ban.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.ban.dto.BanCreateCommand;
import nbc.chillguys.nebulazone.domain.ban.entity.Ban;
import nbc.chillguys.nebulazone.domain.ban.repository.BanRepository;

@Service
@RequiredArgsConstructor
public class BanDomainService {
	private final BanRepository banRepository;

	public boolean isBanned(String ipAddress) {
		return banRepository.findActiveBanByIp(ipAddress).isPresent();
	}

	@Transactional
	public void createBan(BanCreateCommand command) {
		if (isBanned(command.ipAddress())) {
			return;
		}
		Ban ban = Ban.create(command);
		banRepository.save(ban);
	}
}
