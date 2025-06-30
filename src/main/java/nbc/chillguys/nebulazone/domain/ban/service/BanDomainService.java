package nbc.chillguys.nebulazone.domain.ban.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.ban.dto.BanCreateCommand;
import nbc.chillguys.nebulazone.domain.ban.entity.Ban;
import nbc.chillguys.nebulazone.domain.ban.exception.BanErrorCode;
import nbc.chillguys.nebulazone.domain.ban.exception.BanException;
import nbc.chillguys.nebulazone.domain.ban.repository.BanRepository;

@Service
@RequiredArgsConstructor
public class BanDomainService {
	private final BanRepository banRepository;

	/**
	 * 밴 생성 로직
	 *
	 * @param command 밴 생성
	 * @author 정석현
	 */
	public void createBan(BanCreateCommand command) {
		validateNotAlreadyBanned(command.ipAddress());
		Ban ban = Ban.create(command);
		banRepository.save(ban);
	}

	/**
	 * 밴 유무
	 *
	 * @param ipAddress 아이피 주소
	 * @return 밴 여부
	 * @author 정석현
	 */
	public void validateBanned(String ipAddress) {
		banRepository.findActiveBanByIp(ipAddress)
			.orElseThrow(() -> new BanException(BanErrorCode.BAN_NOT_FOUND));
	}

	/**
	 * 이미 밴이 되었는지 확인
	 *
	 * @param ipAddress 아이피 주소
	 * @author 정석현
	 */
	public void validateNotAlreadyBanned(String ipAddress) {
		banRepository.findActiveBanByIp(ipAddress)
			.ifPresent(ban -> {
				throw new BanException(BanErrorCode.ALREADY_BANNED);
			});
	}
}
